package org.midway.impl;

/* Copyright (C) Adadz AS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * 
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */

public class SRBMessageREADY extends SRBMessage {

	public SRBMessageREADY(String msg) {
		command = SRB_READY;
		
	}
}
