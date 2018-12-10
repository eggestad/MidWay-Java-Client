package org.midway.impl;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
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
	private static final int MAXDATAPERCHUNK = 3000;
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

		Timber.d("start attach {} {} {}", uri, name, useThreads);
		Timber.d("start attach %s %s %s", uri, name, useThreads);

		// decode IP and port;
		String scheme = uri.getScheme();
		if ( ! scheme.equalsIgnoreCase("srbp")) 
			throw new ProtocolException("protocol " + scheme + " not supported");

		int port= uri.getPort();
		if (port == -1) port = MidWay.BROKERPORT;

		String host = uri.getHost();

		InetAddress addr = InetAddress.getByName(host);
		Timber.d("connecting to %s:%d", addr, port);
		connection = new Socket(addr, port);
		connection.setTcpNoDelay(true);
		connection.setKeepAlive(true);

		connbufout = new BufferedOutputStream(connection.getOutputStream(), 10000);
		
		// read SRB READY
		SRBMessage msg = getNextSRBMessage();
		
		// send SRB INIT
 
		msg = SRBMessage.makeInitReq("java pure client", "test", null);
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


	// TODO: !!!!! This break if MULTIPLE and a second reply comes and the first is not fetched!
	private class MWPendingReply {
		long handle;
		ArrayList<byte[]> data = new ArrayList<byte[]>();
		int pendingDataChunks;
		Integer appRC = null;
		Boolean success = null;
		MidWayServiceReplyListener listener;
		boolean more;

		ArrayList<MidWayReply> readyReplies = new ArrayList<MidWayReply>();

		public boolean isReady() {
			return success != null && pendingDataChunks == 0;
		}

		private void clear() {
			data = new ArrayList<byte[]>();
			appRC = null;
			pendingDataChunks = 0;
			success = null;
			more = false;
		}
		
		public MidWayReply getReply() {
			MidWayReply rpl = new MidWayReply();
			rpl.more = more;
			rpl.success = success;
			rpl.appreturncode = appRC;
			int datalen = 0;
			for (byte[] d : data ) {
				datalen += d.length;
			}
			rpl.data = new byte[datalen];
			int offset = 0;
			for (byte[] d : data ) {
				for (int i = 0; i < d.length ; i++) {
					rpl.data[offset++] = d[i];
				}
			}
			return null;
		}
	}

	HashMap<Long, MWPendingReply> pendingServiceCalls = new HashMap<Long, MidWayImpl.MWPendingReply>();

	public synchronized long acall(String servicename, byte[] data,
			MidWayServiceReplyListener listener, int flags) throws Exception {
		
		long handle = 0;
		if (data == null) data = new byte[0];

		boolean noreply = (flags & MidWay.NOREPLY) != 0;

		if ( !noreply || data.length > MAXDATAPERCHUNK) {
			handle = lasthandle ++;
			if (handle > 0xFFFFFF) handle = 1000;
			lasthandle = handle;
		}
		if ( !noreply ) { 
			MWPendingReply pending = new MWPendingReply();
			pending.listener = listener;
			pending.handle = handle;
			pendingServiceCalls.put(handle, pending);
		}


		int chunks = data.length / MAXDATAPERCHUNK;
		Timber.v("extra chunks to send %d", chunks);
		boolean multiple = (flags & MidWay.MULTIPLE) > 0;
		byte[] chunk = data;
		if (chunks > 0) {
			chunk =  Arrays.copyOfRange(data, 0, MAXDATAPERCHUNK);
		}
		SRBMessage msg = SRBMessage.makeCallReq(servicename, data, chunks, handle,multiple, 0);

		if (useThreads) sendqueue.put(msg);
		else msg.send(connbufout);

		int offset = MAXDATAPERCHUNK;
		while (chunks  > 0) {
			chunk =  Arrays.copyOfRange(data, offset, Math.min(data.length-offset, MAXDATAPERCHUNK));
			msg = SRBMessage.makeData(servicename, data, chunks, handle);
			if (useThreads) sendqueue.put(msg);
			else msg.send(connbufout);
			offset += MAXDATAPERCHUNK;
			chunks--;
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

	private  SRBMessage getNextSRBMessage() throws IOException, ParseException {
		InputStream is = connection.getInputStream();
		
        BufferedReader receiveRead = new BufferedReader(new InputStreamReader(is));
        
        Timber.d("starting read message with %d available ready = %b", is.available(), receiveRead.ready());
        String line = receiveRead.readLine();
        
        if (line == null)  throw new IOException("server connection closed");
       
        Timber.d("got message %s", line);

        SRBMessage msg = new SRBMessage();
		msg.parse(line);
		Timber.d("got message %s", msg);
		return msg;
		// drain socket, or block if there was nothing
//		do {
//			byte[] buf = new byte[128*1024];
//			int read = is.read(buf);
//			if (read == -1) throw new IOException("Connection broken");
//			String part = new String(buf, 0, read);
//
//			messagebuffer.append(part);
//		} while (is.available() > 0);
//		
//		int eomidx;
//		while ((eomidx = messagebuffer.indexOf(SRBMessage.SRB_MESSAGEBOUNDRY)) != -1) {
//
//			SRBMessage msg = new SRBMessage();
//			char[] dst = new char[eomidx+2];
//			messagebuffer.getChars(0, eomidx+2, dst, 0);
//			msg.parse(dst); 
//			Timber.d("got message %s", msg);
//			messagebuffer.delete(0,  eomidx+1);
//			return msg;
//			java.nio.
//			
// 		}

	} 
	
	void doSRBMessage(SRBMessage msg) {
		if (msg.command.equals("EVENT"))
			doEvent(msg);
		if (msg.command.equals("SVCCALL"))
			doSvcCallReply(msg);
		if (msg.command.equals("SVCDATA"))
			doSvcCallReply(msg);
		
		if (msg.command.equals("TERM"))
			doShutdown(msg);
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

	private void doSvcCallReply(SRBMessage msg) {
		long hdl = msg.getHandle();

		MWPendingReply pending = pendingServiceCalls.get(hdl);

		synchronized (pending) {
			byte[] data = msg.getData();
			int chunks = msg.getChunks();

			if (data != null) pending.data.add(data);
			pending.pendingDataChunks = chunks;

			if (msg.command.equals(SRBMessage.SRB_SVCCALL)) {
				pending.appRC = msg.getAppRC();
				int rc = msg.getReturnCode();

				pending.more = rc == MidWay.MORE;
				pending.success = ! (rc == MidWay.FAIL);
			}

			if (pending.isReady() ) {
				if (pending.listener != null) {

					if (!pending.more)
						pendingServiceCalls.remove(hdl);

					MidWayReply rpl = pending.getReply();
					pending.listener.receive(rpl);
				} else {
					if (useThreads) {

						pending.notify();

					}
				}
			}
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

		MWPendingReply pending = pendingServiceCalls.get(handle);
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
