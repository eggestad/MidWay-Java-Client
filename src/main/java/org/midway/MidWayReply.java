package org.midway;

/* Copyright (C) Adadz AS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * 
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */

public class MidWayReply {
	
	public MidWayReply() {
		appreturncode = null;
		data = null;
		more = false;
		success = false;
	}
    public byte[] data;
    public Integer appreturncode;
    public boolean more;
    public boolean success;
}
