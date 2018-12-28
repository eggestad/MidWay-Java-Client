package org.midway.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Log {

	public static int ASSERT = 0;
	public static final int ERROR = 1;
	public static final int WARN = 2;
	public static final int INFO = 3;
	public static final int VERBOSE = 4;
	public static final int DEBUG = 5;
	
	static String[] plbl = new String[] { "ASS", "ERR", "WARN", "INFO", "VBOS", "DBG" }; 

	public static void wtf(String tag, String part) {
		println(ASSERT, tag, part);
	}

	public static void println(int priority, String tag, String message) {
		DateFormat inst;
		//inst = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, 
		//		SimpleDateFormat.FULL);
		inst = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
		Date date = new Date();
		Object msg = String.format("%s %4s %-20s %s", 
				inst.format(date),
				plbl[priority], 
				tag, message); 
		if (priority < INFO)
			System.err.println(msg);
		else 
			System.out.println(msg);
		
	}

}
