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
import java.net.URLEncoder;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Pattern;

import org.midway.IMIdWayServiceCallback;
import org.midway.MidWay;
import org.midway.MidWayEventListener;
import org.midway.MidWayReply;
import org.midway.MidWayServiceReplyListener;


public class MidWayImpl {
	private static final int MAXDATAPERCHUNK = 1000;
	private boolean useThreads = false;
	private Socket connection;
	private BufferedOutputStream connbufout;
	private boolean shutdown = false;

	private int queuesize = 10000;

	private long lasthandle = 1000;

	ArrayBlockingQueue<SRBMessage> sendqueue;
	SendThread senderthread ;
	RecvThread receiverThread;


	public MidWayImpl() {
		Timber.plant(new Timber.DebugTree());		
		Timber.d("start");
	}
	public final void attach(URI uri, String name, boolean useThreads) throws Exception{

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
		
		
		for (InetAddress addr : addrs) {
			SocketAddress socketAddress = new InetSocketAddress(addr, port);
			connection = new Socket();
			Timber.d("connecting to %s", socketAddress);
			
			try {
				connection.connect(socketAddress, 1000); 
			} catch (Exception e) {
				Timber.w("failed on connect");
				continue;
			}
			 
			connection.setTcpNoDelay(true);
			connection.setKeepAlive(true);
		}
		if (connection == null || !connection.isConnected()) 
			throw new IOException("unable to connect to server");
		
		connbufout = new BufferedOutputStream(connection.getOutputStream(), 10000);
		
		// read SRB READY
		SRBMessage msg = getNextSRBMessage();
		
		// send SRB INIT
 
		msg = SRBMessage.makeInitReq("java pure client", domain, null);
 		msg.send(connbufout);
		
		// read SRC INIT OK
		msg = getNextSRBMessage();

		this.useThreads = useThreads;
		if (useThreads) {
			sendqueue =  new ArrayBlockingQueue<SRBMessage>(queuesize);
			senderthread = new SendThread();
			receiverThread = new RecvThread();
			senderthread.start();
			receiverThread.start();
		} else {
			
		}
		Timber.d("end attach");

	}


	public final void detach() throws Exception{

		SRBMessage msg = SRBMessage.makeTerm(0);
		msg.send(connbufout);
		connection.close();
		connection = null;
		connbufout = null;
	}

	public MidWayReply call(String servicename, byte[] data, int flags) throws Exception {
		// clear MORE flag is set
		long hdl = acall( servicename, data, null,  flags);
		return fetchx(hdl);
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

		if (useThreads) sendqueue.put(msg);
		else msg.send(connbufout);

		int offset = MAXDATAPERCHUNK;
		while (datachunks.size() > 0) {			
			msg = SRBMessage.makeData(servicename, datachunks.remove(0), handle);
			if (useThreads) sendqueue.put(msg);
			else msg.send(connbufout);
		}
		return handle;
	}


	private class SendThread extends Thread {
		public void run() {

			try {
				while (!shutdown) {                
					SRBMessage msg = sendqueue.take();
					msg.send(connbufout);                        
				}
			} catch (InterruptedException e) {
				// TODO shutdown
				e.printStackTrace();
			} catch (IOException e) {
				// TODO reconnect
				e.printStackTrace();
			}
		}

	}

	private class RecvThread extends Thread {
		public void run() {
			try {
				while(!shutdown)
					getNextSRBMessage();
			} catch (IOException | ParseException e) {
				// TODO reconnect??
				e.printStackTrace();
			}
		}
	}

	StringBuffer messagebuffer  = new StringBuffer();

	private  SRBMessage xgetNextSRBMessage() throws IOException, ParseException {
		InputStream is = connection.getInputStream();
		
        BufferedReader receiveRead = new BufferedReader(new InputStreamReader(is));
        
        Timber.d("starting read message with %d available ready = %b", is.available(), receiveRead.ready());
        String line = receiveRead.readLine();
        
        if (line == null)  throw new IOException("server connection closed");
       
        Timber.d("got message %d %s", line.length(), line);

        SRBMessage msg = new SRBMessage();
		msg.parse(line);
		Timber.d("got message %s", msg);
		return msg;


	} 
	private  SRBMessage getNextSRBMessage() throws IOException, ParseException {
		InputStream is = connection.getInputStream();
		//SocketChannel schannel = connection.getChannel();
          
		int eomidx;
		while ((eomidx = messagebuffer.indexOf(SRBMessage.SRB_MESSAGEBOUNDRY)) == -1) {
			Timber.d("starting read message with %d available  ", messagebuffer.length());

			// get more data			
			byte[] buf = new byte[128*1024];
			int read = is.read(buf);
			Timber.d("read %d bytes from connection", read);
			if (read == -1) throw new IOException("Connection broken");
			String part = new String(buf, 0, read);
			
			messagebuffer.append(part);			
		}
		

		SRBMessage msg = new SRBMessage();
		char[] dst = new char[eomidx];
		String s = messagebuffer.substring(0, eomidx);
        Timber.d("got message %d %s", s.length(), s);

		//messagebuffer.getChars(0, eomidx+2, dst, 0);
		msg.parse(s); 
		Timber.d("got message %s", msg);
		messagebuffer.delete(0,  eomidx+2);
		return msg;
	} 
	
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
			connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		connection = null;
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
				if (!pending.more)
					pendingServiceCalls.remove(hdl);
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
	

	public int drain() {
		try {
			if ( connection.getInputStream().available() > 0)
				getNextSRBMessage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}
	public boolean fetch(long handle) throws Exception {

		if (useThreads) throw  new Exception("Using threads, no need to call fetch");
		//MWPendingReply pending = pendingServiceCalls.get(handle);
		int pendingsize = pendingServiceCalls.size();
		
		if (pendingsize == 0) throw new Exception("No pending replies");
		SRBMessage msg;
		
		msg = getNextSRBMessage();
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

		if (! useThreads  && connection.getInputStream().available() > 0)
			getNextSRBMessage();

		synchronized(pending) {

			while (!(pending.isReady())) {
				if (useThreads) {                
					pending.wait();
				} else {
					getNextSRBMessage();
				}

			}

			if (!pending.more)
				pendingServiceCalls.remove(handle);
			rpl = pending.getReply();

		}
		return rpl;
	}

	HashMap<MidWayEventListener,Pattern> eventlisteners = new HashMap<MidWayEventListener, Pattern >();

	public final void subscribe(Pattern regex, MidWayEventListener listener) {

		eventlisteners.put(listener, regex);
		// write SUBSCRIBE message
		SRBMessage.makeSubscribeReq(regex.toString(), false);
	}


	public final void unsubscribe(MidWayEventListener listener) {
		// find listener
		Pattern regex = eventlisteners.remove(listener);
		// write UNSUBSCRIBE message
		SRBMessage.makeSubscribeReq(regex.toString(), true);
	}


}
