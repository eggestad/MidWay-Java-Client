package org.midway;



public interface  MidWayServiceReplyListener {

	/**
	 * Called when a reply to a acall is received. if the acall had the MULIPLE flag set, many
	 * 
	 * 
	 * @param handle the handle refernce returned by acall. In the case the same 
	 * listener is used for multiple calls, this allow you to distinguish from what call 
	 * this reply is coming from
	 * @param data
	 * @param applicationReturncode 
	 */
	public void receive (MidWayReply reply);
	

}
