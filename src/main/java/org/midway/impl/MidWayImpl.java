package org.midway.impl;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Pattern;

import org.midway.MidWay;
import org.midway.MidWayEventListener;
import org.midway.MidWayReply;
import org.midway.MidWayServiceReplyListener;


public class MidWayImpl {
	private static final int MAXDATAPERCHUNK = 1000;
	private boolean useThreads = false;
 	private boolean shutdown = false;

	private long lasthandle = 1000;
	RecvThread receiverThread;
	private SRBConnectionEndPoint srbEndPoint;


	public MidWayImpl() {
		Timber.d("start");
	}
	public final synchronized void attach(URI uri, String name, boolean useThreads) throws Exception{

		Timber.d("start attach uri %s %s %s %s", 
				uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath());
		Timber.d("start attach %s %s %s", uri, name, useThreads);
 
		// decode IP and port;
		String scheme = uri.getScheme();
		if ( ! scheme.equalsIgnoreCase("srbp")) 
			throw new ProtocolException("protocol " + scheme + " not supported");

		int port= uri.getPort();
		if (port == -1) port = MidWay.BROKERPORT;
		String host = uri.getHost();
		if (host == null) host = "localhost";
		String domain = uri.getPath();
		if (domain != null) {
			if (domain.startsWith("/")) 
				domain = domain.substring(1);
		}
		InetAddress addrs[] = InetAddress.getAllByName(host);
		
		
		Socket connection = null;
		for (InetAddress addr : addrs) {
			SocketAddress socketAddress = new InetSocketAddress(addr, port);
			connection = new Socket();
			Timber.d("connecting to %s", socketAddress);
			
			try {
				connection.connect(socketAddress, 1000); 
				Timber.d("connected");

			} catch (Exception e) {
				Timber.w("failed on connect");
				continue;
			}
			 
			connection.setTcpNoDelay(true);
			connection.setKeepAlive(true);
		}
		if (connection == null || !connection.isConnected()) 
			throw new IOException("unable to connect to server");
		Timber.d("getting READY  msg");

		srbEndPoint = new SRBConnectionEndPoint(connection, useThreads);		
		Timber.d("getting READY  msg");

		// read SRB READY
		SRBMessage msg = srbEndPoint.getNextSRBMessage();
		Timber.d("got READY  msg");

		// send SRB INIT
 
		msg = SRBMessage.makeInitReq("java pure client", domain, null);
		srbEndPoint.send(msg);
 		
		Timber.d("getting INIT reaply  msg");

		// read SRC INIT OK
		msg = srbEndPoint.getNextSRBMessage();

		this.useThreads = useThreads;
		if (useThreads) {
  			receiverThread = new RecvThread();
  			receiverThread.setName("SRB Receiver");
 			receiverThread.start();
		} else {
			
		}
		Timber.d("end attach");

	}


	public final synchronized void detach() throws Exception{

		SRBMessage msg = SRBMessage.makeTerm(0);
		srbEndPoint.send(msg);
		srbEndPoint.close();
		if (useThreads) receiverThread.join();
	}


	HashMap<Long, MidWayPendingReply> pendingServiceCalls = new HashMap<Long, MidWayPendingReply>();

	public synchronized long acall(String servicename, byte[] data,
			MidWayServiceReplyListener listener, int flags) throws Exception {
		
		long handle = 0;
		boolean multiple = (flags & MidWay.MULTIPLE) > 0;
		if (data == null) data = new byte[0];
		int totallength = data.length;
		int chunks = totallength / MAXDATAPERCHUNK + 1;
		ArrayList<byte[]> datachunks = new ArrayList<byte[]>(chunks);
		
		
		for (int i = 0; i < chunks; i++) {			
			byte[] chunk = data;	
			int start = i*MAXDATAPERCHUNK;
			int end = Math.min(data.length, (i+1)*MAXDATAPERCHUNK);
			Timber.d("chunk %d  havd start=%d and end=%d", i, start, end);
			chunk =  Arrays.copyOfRange(data, start, end);
			datachunks.add(chunk);
		}
		Timber.v("extra chunks to send %d", datachunks.size()-1);		
		boolean noreply = (flags & MidWay.NOREPLY) != 0;
		if ( !noreply || datachunks.size() > 1) {
			handle = lasthandle ++;
			if (handle > 0xFFFFFF) handle = 1000;
			lasthandle = handle;
		}
		if ( !noreply ) { 
			MidWayPendingReply pending = new MidWayPendingReply();
			pending.listener = listener;
			pending.handle = handle;
			pendingServiceCalls.put(handle, pending);
		}


		
		SRBMessage msg = SRBMessage.makeCallReq(servicename, datachunks.remove(0), totallength, handle,multiple, 0);

		srbEndPoint.send(msg);
		
		int offset = MAXDATAPERCHUNK;
		while (datachunks.size() > 0) {			
			msg = SRBMessage.makeData(servicename, datachunks.remove(0), handle);
			srbEndPoint.send(msg);
		}
		return handle;
	}


	

	private class RecvThread extends Thread {
		public void run() {
			SRBMessage msg;
		
			while(!shutdown) {
				try {
					msg = srbEndPoint.getNextSRBMessage();
					Timber.d("done one SRB message");
					doSRBMessage(msg);
					
				} catch (IOException e) {
					try {
						srbEndPoint.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					return;					
				} catch ( ParseException e) {
					// TODO reconnect??
					e.printStackTrace();
				}
			}			
		}
	}

	StringBuffer messagebuffer  = new StringBuffer();


	
	void doSRBMessage(SRBMessage msg) {
		
		switch (msg.command) {
		case SRBMessage.SRB_EVENT:
			doEvent(msg);
			break;
		
		case SRBMessage.SRB_SVCCALL:
			doSvcCallReply(msg);
			break;
			
		case SRBMessage.SRB_SVCDATA:
			doSvcCallData(msg);
			break;
			
		case   SRBMessage.SRB_TERM:
			doShutdown(msg);
			break;
	
		default:
			Timber.e("got SRB message with unknown command %s", msg.command);
		}
		return;
	}
		
	
	private void doShutdown(SRBMessage msg) {
		shutdown = true;
		try {
			srbEndPoint.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	private void doEvent(SRBMessage msg) {
		//lookuplistener;
		//call_listener;
	}
	private void doSvcCallData(SRBMessage msg) {
		
		long hdl = msg.getHandle();
		Timber.d("srv data with handle %x", hdl);
		MidWayPendingReply pending = pendingServiceCalls.get(hdl);
		if (pending == null) {
			Timber.e("got unexpected svc data");
			return;
		} 
		synchronized (pending) {
			byte[] data = msg.getData();
			Timber.d("datalen %d", data.length);
			pending.data.add(data);
			if (pending.isComplete() ) {
				Timber.d("is ready");
				if (!pending.more) {
					pendingServiceCalls.remove(hdl);
					pending.notifyAll();
				}
				pending.deliver(); 

			} 
		}

	}

	private void doSvcCallReply(SRBMessage msg) {
		long hdl = msg.getHandle();
		Timber.d("srv reply with handle %x", hdl);
		MidWayPendingReply pending = pendingServiceCalls.get(hdl);
		
		if (pending == null) {
			Timber.e("got unexpected svc call reply");
			return;
		} 
		pending.doServiceCallReply(msg);
		
		if (pending.isComplete() ) {
			Timber.d("is ready");
			if (!pending.more)
				pendingServiceCalls.remove(hdl);
			pending.deliver(); 
			
		}
		
	}
	

//	public int drain() {
//		try {
//			if ( connection.getInputStream().available() > 0)
//				getNextSRBMessage();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return 0;
//	}
	public boolean fetch(long handle) throws Exception {

		if (useThreads) throw  new Exception("Using threads, no need to call fetch");
		//MWPendingReply pending = pendingServiceCalls.get(handle);
		int pendingsize = pendingServiceCalls.size();
		
		if (pendingsize == 0) throw new Exception("No pending replies");
		SRBMessage msg;
		
		msg = srbEndPoint.getNextSRBMessage();
		doSRBMessage(msg);
		Timber.d("done one SRB message");
		if (pendingsize >  pendingServiceCalls.size()) return true;
		
//		synchronized(pending) {
//
//			while (!(pending.isReady())) {
//				if (useThreads) {                
//					pending.wait();
//				} else {
//					processInMessage();
//				}
//
//			}
//
//			if (!pending.more)
//				pendingServiceCalls.remove(handle);
//			rpl = pending.getReply();
//
//		}
//		return rpl;
		return false;
	}
	public MidWayReply fetchx(long handle) throws Exception {

		MidWayPendingReply pending = pendingServiceCalls.get(handle);
		MidWayReply rpl;

		srbEndPoint.getNextSRBMessage();

		synchronized(pending) {

			while (!(pending.isReady())) {
				if (useThreads) {                
					pending.wait();
				} else {
					srbEndPoint.getNextSRBMessage();
				}

			}

			if (!pending.more)
				pendingServiceCalls.remove(handle);
			rpl = pending.getReply();

		}
		return rpl;
	}

	HashMap<MidWayEventListener,Pattern> eventlisteners = new HashMap<MidWayEventListener, Pattern >();

	public final synchronized void subscribe(Pattern regex, MidWayEventListener listener) {

		eventlisteners.put(listener, regex);
		// write SUBSCRIBE message
		SRBMessage.makeSubscribeReq(regex.toString(), false);
	}


	public final synchronized void unsubscribe(MidWayEventListener listener) {
		// find listener
		Pattern regex = eventlisteners.remove(listener);
		// write UNSUBSCRIBE message
		SRBMessage.makeSubscribeReq(regex.toString(), true);
	}


}
