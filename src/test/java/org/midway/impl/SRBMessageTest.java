package org.midway.impl;
import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SRBMessageTest {

	@Before 
	public void init() {
		//Timber.plant(new Timber.DebugTree());
	}
	@Test
	public void testnibbler() {
		assertEquals(9, SRBMessage.nibble((byte) '9'));
		assertEquals(0, SRBMessage.nibble((byte) '0'));
		assertEquals(0xA, SRBMessage.nibble((byte) 'A'));
		assertEquals(0xF, SRBMessage.nibble((byte) 'F'));
		
		try {
			SRBMessage.nibble((byte) 'x');
			fail("nibble from 'x' shod have failed");
		} catch (Exception e) {
			
		}
	}
	
	@Test
	public void testmessagewrite() throws IOException {

		Timber.d("hello %d", 66);
		SRBMessage msg = new SRBMessage();
		msg.command = "TEST";
		msg.marker = msg.SRB_NOTIFICATIONMARKER;
		
		msg.put("KEY1", "VAL1".getBytes());
		msg.put("KEY2", "VAL1???+\\+3242 æø æø END".getBytes());
	
		byte encmsg[] = msg.encode();		
		String encstr = new String(encmsg);
		
		System.out.println("encoded message: " + new String(encmsg));
		String expected = "TEST!KEY2=VAL1%3F%3F%3F%2B%5C%2B3242+%C3%A6%C3%B8+%C3%A6%C3%B8+END&KEY1=VAL1\r\n";
		assertEquals(expected, encstr);
	}
	
	@Test
	public void testurlcode() {
		System.out.println ("test with bye encoders code");
		PrintStream out = System.out;
		String teststrings[] = {"123\0\0x456 ", "vbnvbcn" , 
				"!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghij" 
						+ "klmnopqrstuvwxyz{|} \n", 
						MidWayTest.bigdata
		};
		
		for (String s : teststrings) {
			byte[] b = s.getBytes();
			out.println("raw len = " + b.length);
			String enc = SRBMessage.byteArrayToURLString(b);
			out.println(enc);
			out.println("enc len = " + enc.length());
			byte[] b2 = SRBMessage.byteArrayFromURLString(enc);
			out.println("raw len = " + b2.length);
			out.print(new String(b2));
			assertEquals(new String(b2),  s);
		}
		
	}
	@Test
	public void testurlcode2() {
		PrintStream out = System.out;
		String teststring;
		teststring = "1!!2";
		byte[] b = teststring.getBytes();
		out.println("raw len = " + b.length);
		String enc = SRBMessage.byteArrayToURLString(b);
		out.println(enc);
		byte[] b2 = SRBMessage.byteArrayFromURLString(enc);
		out.println("raw len = " + b2.length);
		out.print(new String(b2));
		assertEquals(new String(b2), teststring);
	}
	
	@Test
	public void test() throws UnsupportedEncodingException {
		String s = "123\0\0x456";
		
		System.out.println ("test with URL*code");

		System.out.println (s);
		System.out.println (s.length());
		String s2 = URLEncoder.encode(s, "utf-8");
		System.out.println (URLEncoder.encode(s, "utf-8"));
		s2 = URLDecoder.decode(s, "utf-8");
		System.out.println (s2);
		System.out.println (s2.length());
		assertEquals(s,  s2);
		
		
	}
}
