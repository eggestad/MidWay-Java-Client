package org.midway;

/**
 * 
 * Interface used for receiving events. 
 *  
 * @author terje
 *
 */
public interface  IMidWayEventListener {

	/**
	 * Called when an event is received. 
	 * 
	 * @param name the event name 
	 * @param data
	 */
	public void onEvent (String name, byte[] data);
	
	
}
