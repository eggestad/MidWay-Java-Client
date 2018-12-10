package org.midway;

import java.net.URI;

import org.midway.impl.MidWayImpl;




/**
 * 
 * Main class for users of this library. Implementation is in MidWayIpml.
 * 
 * This library implement the client API of MidWay and is a pure java implementation. 
 * For anyone wanting to write servers you need to use the java wrapper library that uses the MidWay
 * C library.
 * The Server methods is defined here but throws  UnsupportedOperationException. 
 * All the public classes in org.midway.* are identical to the Java wrapper library so that 
 * each library is selectable by classpath. 
 * 
 * As of now the only protocol supported in this library is srbp 
 * 
 * @author terje
 *
 */
public class MidWay{
	
    public static final int NOBLOCK = 0x00000002;
    public static final int NOREPLY = 0x00000001;
    public static final int MULTIPLE = 0x00000040;

    public static final int FAIL = 0;
    public static final int SUCCESS = 1;
    public static final int MORE = 2;
    
    public static final int BROKERPORT = 1102;

    private  MidWayImpl driver;
    
    public MidWay() {
    	System.out.println("test");
    	driver = new MidWayImpl(); 
    }

    public MidWay(URI uri) throws Exception {
    	this();
    	attach(uri, "", false);
    }
    
    public MidWay(URI uri, String name) throws Exception {
    	this();
        attach(uri, name, false);
    }

    public MidWay(URI uri, String name, boolean useThreads) throws Exception {
    	this();
        attach(uri, name, useThreads);
    }

    private void attach(URI uri) throws Exception {
        attach(uri, "", false);
    }
    
    private void attach(URI uri, String name) throws Exception {
    	attach(uri, name, false);
    }
    
	private void attach(URI uri, String name, boolean useThreads) throws Exception {
		driver.attach( uri,  name,  useThreads);		
	}

	public void detach() throws Exception {
		driver.detach();
	}
	
	public MidWayReply  call(String service, byte[] data) throws Exception {
		return driver.call(service, data, 0);
	}
	

	public void acall(String service, byte[] data, MidWayServiceReplyListener listener, int flags) throws Exception {		
		driver.acall(service, data, listener, flags);
		return ;
	}
	
	public void acall(String service, byte[] data, MidWayServiceReplyListener listener) throws Exception {
		acall(service, data, listener, 0);
		return; 
	}
	
	public void acall(String service, String data, MidWayServiceReplyListener listener, int flags) throws Exception {		
		driver.acall(service, data.getBytes(), listener, flags);
		return ;
	}
	
	public void acall(String service, String data, MidWayServiceReplyListener listener) throws Exception {
		acall(service, data.getBytes(), listener, 0);
		return; 
	}
}
