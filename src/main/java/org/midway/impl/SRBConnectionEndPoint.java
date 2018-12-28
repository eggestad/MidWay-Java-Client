package org.midway.impl;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.ParseException;
import java.util.concurrent.ArrayBlockingQueue;

public class SRBConnectionEndPoint {

	private Socket connection;
	private BufferedOutputStream connbufout;
	private int queuesize = 10000;
	ArrayBlockingQueue<SRBMessage> sendqueue;
 	SendThread senderthread ;
 	private boolean shutdown = false;
	StringBuffer messagebuffer  = new StringBuffer();
	InputStream is;

	private class SendThread extends Thread {
		
		public SendThread() {
			setName("SRB Sender Thread");
		}
		
		public void run() {
			Timber.d("starting send thread");
			while (!shutdown) {                
				try {
					Timber.d("dedueue a message, sending");
					SRBMessage msg = sendqueue.take();
 					connbufout.write(msg.encode());
					connbufout.flush();
				} catch (InterruptedException e) {					
					Timber.w("got and interrupt, ignoring");
				} catch (IOException e) {
					if (!shutdown) {
						Timber.e("Got an IO exception in send thread");
					}
				}
			}
			Timber.d("exiting send thread");
		}	
	}
	
	public SRBConnectionEndPoint(Socket conn, boolean useSendThread) throws IOException {
		this.connection = conn;
		connbufout = new BufferedOutputStream(connection.getOutputStream(), 10000);
		is = connection.getInputStream();
		if (useSendThread) {
			sendqueue = new ArrayBlockingQueue<SRBMessage>(queuesize);
			senderthread = new SendThread();
			senderthread.start();
		}
		
	
	}
	
	public boolean send(SRBMessage msg) throws IOException {
		if (senderthread != null) {
			Timber.d("queueing msg");
			sendqueue.add(msg);
		} else {
			Timber.d("sending msg");
			connbufout.write(msg.encode());
			connbufout.flush();
		}
		return true;
	}
//	private  SRBMessage xgetNextSRBMessage() throws IOException, ParseException {
//		InputStream is = connection.getInputStream();
//		
//        BufferedReader receiveRead = new BufferedReader(new InputStreamReader(is));
//        
//        Timber.d("starting read message with %d available ready = %b", is.available(), receiveRead.ready());
//        String line = receiveRead.readLine();
//        
//        if (line == null)  throw new IOException("server connection closed");
//       
//        Timber.d("got message %d %s", line.length(), line);
//
//        SRBMessage msg = new SRBMessage();
//		msg.parse(line);
//		Timber.d("got message %s", msg);
//		return msg;
//
//
//	} 
	public  SRBMessage getNextSRBMessage() throws IOException, ParseException {
		//SocketChannel schannel = connection.getChannel();
		Timber.d("getting next message ");

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

	public void close() throws IOException {
		shutdown = true;
		Timber.d("closing endpoint");
		connection.close();
		 
	} 
	
}
