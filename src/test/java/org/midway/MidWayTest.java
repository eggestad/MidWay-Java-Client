package org.midway;

import static org.junit.Assert.*;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.midway.impl.Timber;

public class MidWayTest {

	@Before
	public void setlogging() {
		Timber.uprootAll();
	}
	
	@Test
	public void testURIs() {
		
		String[] uris = new String[] {
				"srbp:/test", 
				"srbp://localhost/test", 
				"srbp://localhost:20202/test", 
				"srbp://localhost:20202", 
		};
		
		URI uri ;
		for (String u : uris) {
			try {
				uri = new URI(u);
			} catch (URISyntaxException e) {
				System.out.println("error parsing url " + u);
				e.printStackTrace();
				fail();
			}
		}		
 	}
	
	private URI getTestURI() throws URISyntaxException {
		URI uri = new URI("srbp:/test");
		return uri;
	}
	
	@Test
	public void testMidWayURI() throws Exception {
		URI uri = getTestURI();
		MidWay mw = new MidWay(uri);
		mw.detach();
	}
	@Test
	public void testMidWay() throws Exception {
		URI uri = getTestURI();
		MidWay mw = new MidWay();
		mw.attach(uri, "testclient", true);
		assertTrue(checkThread("SRB Sender"));
		assertTrue(checkThread("SRB Receiver"));
		mw.detach();
	}

	@Test
	public void testMidWayURIString() throws Exception {
		URI uri = getTestURI();
		MidWay mw = new MidWay(uri, "testcli");
		mw.detach();
	}
	
	boolean checkThread(String name ) {
		//System.out.println("checking thread "  +   name );
	    final ThreadGroup root = Thread.currentThread().getThreadGroup();
	    final ThreadMXBean thbean = ManagementFactory.getThreadMXBean( );
	    int nAlloc = thbean.getThreadCount( );
	    int n = 0;
	    Thread[] threads;
	    do {
	        nAlloc *= 2;
	        threads = new Thread[ nAlloc ];
	        n = root.enumerate( threads, true );
	    } while ( n == nAlloc );
	    for (Thread t : threads) {
	    	if (t == null) continue;
	    	String tname = t.getName();
			//System.out.println("thread " + t.getId() +  " name " + tname);

			if (tname != null && tname.equals(name)) {
	    		return  t.isAlive();
	    	}
	    }
	    return false;
	}
	
	@Test
	public void testMidWayURIStringBoolean() throws Exception {
		MidWay.useThreads = true;
		URI uri = getTestURI();
		
		MidWay mw = new MidWay(uri, "testcli");				
		assertTrue(checkThread("SRB Sender"));
		assertTrue(checkThread("SRB Receiver"));
		mw.detach();

		mw = new MidWay(uri, "testcli", false);				
		assertFalse(checkThread("SRB Sender"));
		assertFalse(checkThread("SRB Receiver"));
		mw.detach();

		MidWay.useThreads = false;
		mw = new MidWay(uri, "testcli");				
		assertFalse(checkThread("SRB Sender"));
		assertFalse(checkThread("SRB Receiver"));
		mw.detach();

		mw = new MidWay(uri, "testcli", true);				
		assertTrue(checkThread("SRB Sender"));
		assertTrue(checkThread("SRB Receiver"));
		mw.detach();
	}
}
