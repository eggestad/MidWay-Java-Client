package org.midway.impl;

import java.util.ArrayList;

import org.midway.MidWay;
import org.midway.MidWayReply;
import org.midway.MidWayServiceReplyListener;

/* Copyright (C) Adadz AS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * 
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */

// TODO: !!!!! This break if MULTIPLE and a second reply comes and the first is not fetched!
public class MidWayPendingReply {
	long handle;
	ArrayList<byte[]> data = new ArrayList<byte[]>();
	Integer dataTotal = null;
	Integer appRC = null;
	Boolean success = null;
	MidWayServiceReplyListener listener;
	boolean more;

	ArrayList<MidWayReply> readyReplies = new ArrayList<MidWayReply>();

	public boolean isReady() {
		return success != null && dataTotal != null;
	}

	private void clear() {
		data = new ArrayList<byte[]>();
		appRC = null;
		dataTotal =  null;
		success = null;
		more = false;
	}
	
	public MidWayReply getReply() {
		MidWayReply reply = new MidWayReply();
		reply.more = more;
		reply.success = success;
		reply.appreturncode = appRC;
		int datalen = 0;
		for (byte[] d : data ) {
			datalen += d.length;
		}
		reply.data = new byte[datalen];
		int offset = 0;
		for (byte[] d : data ) {
			for (int i = 0; i < d.length ; i++) {
				reply.data[offset++] = d[i];
			}
		}
		return reply;
	}
	public synchronized void doServiceCallReply(SRBMessage msg) {
		byte[] data = msg.getData();
		Integer datatotal = msg.getDataTotal();
		Timber.d("datalen %d", data.length);

		if (data != null) this.data.add(data);
		this.dataTotal = datatotal;

		if (msg.command.equals(SRBMessage.SRB_SVCCALL)) {
			this.appRC = msg.getAppRC();
			int rc = msg.getReturnCode();

			this.more = rc == MidWay.MORE;
			this.success = ! (rc == MidWay.FAIL);
		}

		
	}

	public void deliver() {
		if ( this.listener != null) {
			MidWayReply rpl = this.getReply();
			Timber.d("reply %s", rpl);
			this.listener.receive(rpl);
		}
	}

	public boolean isComplete() {
		if (dataTotal == null) return true;
		int datasofar = 0;
		for (byte[] datasegment: this.data) {
			datasofar += datasegment.length;
		}
		Timber.d("expecting %d datatotal received %d so far", dataTotal, datasofar);
		if (datasofar == dataTotal) {
			dataTotal = null;
			return true;
		}
		return false;		
	}
}