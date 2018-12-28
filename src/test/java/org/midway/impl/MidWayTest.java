package org.midway.impl;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

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

	public static String bigdata = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghij" + 
			"\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijk" + 
			"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijkl" + 
			"$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklm" + 
			"%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmn" + 
			"&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmno" + 
			"'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnop" + 
			"()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopq" + 
			")*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqr" + 
			"*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrs" + 
			"+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrst" + 
			",-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstu" + 
			"-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuv" + 
			"./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvw" + 
			"/0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwx" + 
			"0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxy" + 
			"123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz" + 
			"23456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{" + 
			"3456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|" + 
			"456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}" + 
			"56789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} " + 
			"6789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !" + 
			"789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"" + 
			"89:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#" + 
			"9:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$" + 
			":;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%" + 
			";<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&" + 
			"<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'" + 
			"=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'(" + 
			">?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()" + 
			"?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*" + 
			"@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+" + 
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+," + 
			"BCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-" + 
			"CDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-." + 
			"DEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./" + 
			"EFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0" + 
			"FGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./01" + 
			"GHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./012" + 
			"HIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123" + 
			"IJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./01234" + 
			"JKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./012345" + 
			"KLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456" + 
			"LMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./01234567" + 
			"MNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./012345678" + 
			"NOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789" + 
			"OPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:" + 
			"PQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;" + 
			"QRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<" + 
			"RSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=" + 
			"STUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>" + 
			"TUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?" + 
			"UVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@" + 
			"VWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@A" + 
			"WXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@AB" + 
			"XYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABC" + 
			"YZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCD" + 
			"Z[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDE" + 
			"[\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEF" + 
			"\\]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFG" + 
			"]^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGH" + 
			"^_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHI" + 
			"_`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJ" + 
			"`abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJK" + 
			"abcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKL" + 
			"bcdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLM" + 
			"cdefghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMN" + 
			"defghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNO" + 
			"efghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOP" + 
			"fghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQ" + 
			"ghijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQR" + 
			"hijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRS" + 
			"ijklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRST" + 
			"jklmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTU" + 
			"klmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUV" + 
			"lmnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVW" + 
			"mnopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWX" + 
			"nopqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXY" + 
			"opqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ" + 
			"pqrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[" + 
			"qrstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\" + 
			"rstuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]" + 
			"stuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^" + 
			"tuvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_" + 
			"uvwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`" + 
			"vwxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`a" + 
			"wxyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`ab" + 
			"xyz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abc" + 
			"yz{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcd" + 
			"z{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcde" + 
			"{|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdef" + 
			"|} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefg" + 
			"} !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefgh" + 
			" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghi";
	
	public static String bigdata2 = "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789----+---+-----+---+---!!!----------!\"#$%&'()*+------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" + 
			 "0123456789------------------!!!----+------------------0123456789\n" + 
			 "0123456789------------------!!!------ !\"#$%&'()*+----------------0123456789\n" + 
			 "0123456789--- --- ---- ---- ----!!!------- !\"#$%&'()*+---------------0123456789\n" + 
			 "0123456789------------------!!!----------------------0123456789\n" ; 

	public URI getTestURI() throws URISyntaxException {
		URI uri = new URI("srbp:/test");
		return uri;
	}

	@Test
	public void testMidWayURI() throws Exception {
		URI uri = getTestURI();
		MidWay mw = new MidWay(uri);

		//mw.acall("sleep1", "data", (reply)->  System.out.println(reply) );
		mw.acall("testchargen", "100", (reply)->  System.out.println("reply " + reply) );
		while(!mw.fetch());

		mw.acall("testtime", bigdata, (reply)->  System.out.println("reply " + reply) );
		while(!mw.fetch());

		mw.detach();
	}

	
	@Test
	public void testMidWay() throws Exception {
		URI uri = getTestURI();
		MidWay mw = new MidWay();
		mw.attach(uri, "testclient", true);
		mw.detach();
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
