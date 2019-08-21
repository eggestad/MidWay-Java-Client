package org.midway;


public class MidWayCallReply {
	
	public MidWayCallReply() {
		appreturncode = null;
		data = null;
		more = false;
		success = false;
	}
    public byte[] data;
    public Integer appreturncode;
    public boolean more;
    public boolean success;
    
    @Override
    public String toString() {

    	return String.format("success:%b, more:%b apprc=%d data=(%d)%s", 
    			success, more, appreturncode, data.length, new String(data));
    }
}
