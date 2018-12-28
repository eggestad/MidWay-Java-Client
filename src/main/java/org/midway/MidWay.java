package org.midway;

import java.net.URI;

import org.midway.impl.MidWayImpl;
import org.midway.impl.Timber;




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
    
    /**
     * Normal operation create two threads on the communication endpoint, one for send and one for recv. 
     * Set this to false to not use threads in future instances. 
     * If this is set to false, fetch() must be called explicitly. 
     */
    public static boolean useThreads = true;
    
    /**
     * Enable debugging in this MidWay client library. 
     * It uses Timber
     */
    public static void enableDebugging() {
    	Timber.uprootAll();
		Timber.plant(new Timber.DebugTree());		
    };
    
    static  {
		Timber.plant(new Timber.DebugTree());		

    }
    
    /**
     * Create an instance of a MidWay client endpoint. 
     * The newly created endpoint will not connected/attached. 
     * An explicit attach() will be necessary.  
     */
    public MidWay() {
    	driver = new MidWayImpl(); 
    }
    
    /**
     * Create an instance of a MidWay client endpoint and 
     * call attach to the URI. The instance is either attached 
     * successfully or exception is thrown
     * 
     * @param uri an URI on the form srbp:[//host[:port]]/domain
     * @throws Exception if there was a problem with 
     */
    public MidWay(URI uri) throws Exception {
    	this();
    	attach(uri, "", useThreads);
    }
    
    /**
     * Create an instance of a MidWay client endpoint and 
     * call attach to the URI. The instance is either attached 
     * successfully or exception is thrown
     * 
     * @param uri an URI on the form srbp:[//host[:port]]/domain
     * @param name give a client name to the server
     * @throws Exception
     */
    public MidWay(URI uri, String name) throws Exception {
    	this();
        attach(uri, name, useThreads);
    }

    /**
     * Create an instance of a MidWay client endpoint and 
     * call attach to the URI. The instance is either attached 
     * successfully or exception is thrown
     * 
     * @param uri an URI on the form srbp:[//host[:port]]/domain
     * @param name give a client name to the server
     * @param useThreads overrides the static config MidWay.useThreads see fetch()
     * @throws Exception
     */
    public MidWay(URI uri, String name, boolean useThreads) throws Exception {
    	this();
        attach(uri, name, useThreads);
    }

    /**
     * Connects this unconnected MidWay instance to a server 
     * using the given URI
     * 
     * @param uri an URI on the form srbp:[//host[:port]]/domain
     * @throws Exception
     */
    public void attach(URI uri) throws Exception {
        attach(uri, "", useThreads);
    }
    
    /**
     * Connects this unconnected MidWay instance to a server 
     * using the given URI
     * 
     * @param uri an URI on the form srbp:[//host[:port]]/domain
     * @param name give a client name to the server
     * @throws Exception
     */
    public void attach(URI uri, String name) throws Exception {
    	attach(uri, name, useThreads);
    }
    
    /**
     * Connects this unconnected MidWay instance to a server 
     * using the given URI
     * 
     * @param uri an URI on the form srbp:[//host[:port]]/domain
     * @param name give a client name to the server
     * @param useThreads overrides the static config MidWay.useThreads see fetch()
     * @throws Exception
     */
    public void attach(URI uri, String name, boolean useThreads) throws Exception {
		driver.attach( uri,  name,  useThreads);		
	}

    /**
     * Detaching this instance, it may be reused by calling attach again.
     * @throws Exception
     */
	public void detach() throws Exception {
		driver.detach();
	}
	
	/**
	 * Submitting a service request call  to the given service. The given listener 
	 * will be called with the result.
	 * 
	 * NB: If you have set the MidWay.useThreads to false or attached with useFlags = false
	 * you must call fetch() to receive replies as well as events
	 *   
	 * @param service name, may not be null
	 * @param data to be passed on to in the service, may be null
	 * @param listener the call back for the reply. If null, the request is in the blind, not even failure will be reported
	 * @param flags TBD
	 * @throws Exception
	 */
	public void call(String service, byte[] data, MidWayServiceReplyListener listener, int flags) throws Exception {		
		driver.acall(service, data, listener, flags);
		return ;
	}
	
	/**
	 * Submitting a service request call  to the given service. The given listener 
	 * will be called with the result.
	 * 
	 * NB: If you have set the MidWay.useThreads to false or attached with useFlags = false
	 * you must call fetch() to receive replies as well as events
	 *   
	 * @param service name, may not be null
	 * @param data to be passed on to in the service, may be null
	 * @param listener the call back for the reply. If null, the request is in the blind, not even failure will be reported
	 * @throws Exception
	 */
	public void call(String service, byte[] data, MidWayServiceReplyListener listener) throws Exception {
		call(service, data, listener, 0);
		return; 
	}
	
	/**
	 * Submitting a service request call  to the given service. The given listener 
	 * will be called with the result.
	 * 
	 * NB: If you have set the MidWay.useThreads to false or attached with useFlags = false
	 * you must call fetch() to receive replies as well as events
	 *   
	 * @param service name, may not be null
	 * @param data to be passed on to in the service, may be null
	 * @param listener the call back for the reply. If null, the request is in the blind, not even failure will be reported
	 * @param flags TBD
	 * @throws Exception
	 */
	public void call(String service, String data, MidWayServiceReplyListener listener, int flags) throws Exception {		
		call(service, data.getBytes(), listener, flags);
		return ;
	}
	
	/**
	 * Submitting a service request call  to the given service. The given listener 
	 * will be called with the result.
	 * 
	 * NB: If you have set the MidWay.useThreads to false or attached with useFlags = false
	 * you must call fetch() to receive replies as well as events
	 *   
	 * @param service name, may not be null
	 * @param data to be passed on to in the service, may be null
	 * @param listener the call back for the reply. If null, the request is in the blind, not even failure will be reported
	 * @throws Exception
	 */
	public void call(String service, String data, MidWayServiceReplyListener listener) throws Exception {
		call(service, data.getBytes(), listener, 0);
		return; 
	}
	
	/**
	 * NB Only needed if MidWay.useThreads = false or attach/constructor was called with useThreads = false
	 * The method handle incoming SRB protocol messages. It's recommended that you 
	 * keep this going in a Thread or Executor. 
	 * @return true if a service reply was received, false if some kind of message was received like an event
	 * @throws Exception
	 */
	public boolean  fetch() throws Exception {
		return driver.fetch(0);
	}
	
}
