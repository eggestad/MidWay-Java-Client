package org.midway.impl;

import static org.junit.Assert.*;

import java.net.URI;

import org.junit.Test;
import org.midway.MidWay;

/* Copyright (C) Adadz AS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * 
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */

public class MidWayTest {


	@Test
	public void testMidWay() {
		try {
			URI uri = new URI("srbp:/test");
			MidWay mw = new MidWay(uri);
			
			//mw.acall("sleep1", "data", (reply)->  System.out.println(reply) );
			mw.acall("testchargen", "100", (reply)->  System.out.println("reply " + reply) );
			while(!mw.fetch());
			
			mw.detach();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testMidWayURI() {
		fail("Not yet implemented");
	}

	@Test
	public void testMidWayURIString() {
		fail("Not yet implemented");
	}

	@Test
	public void testMidWayURIStringBoolean() {
		fail("Not yet implemented");
	}
}
