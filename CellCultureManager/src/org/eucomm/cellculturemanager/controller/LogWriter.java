package org.eucomm.cellculturemanager.controller;


import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eucomm.cellculturemanager.gui.CellCultureManagerGui;


public class LogWriter {

	static Writer	logWriter	= null;
	static String	hostName	= "";
	static String	hostAddress	= "";


	/**
	 * Write a string to the log file.
	 * 
	 * @param log
	 *            the string that will be written
	 * @param addDate
	 *            true to take the current date as prefix, false to take whitespace
	 */
	public static void writeLog(String log, boolean addDate) {

		if (logWriter == null) {
			try {
				logWriter = new FileWriter(CellCultureManagerGui.class.getName() + ".log", true);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		String prefix = null;

		if (addDate) {
			Calendar calendar = new GregorianCalendar();

			if (hostName.isEmpty() || hostAddress.isEmpty()) {
				try {
					hostName = InetAddress.getLocalHost().getHostName();
					hostAddress = InetAddress.getLocalHost().getHostAddress();
				}
				catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}

			prefix = String.format("%04d-%02d-%02d %02d:%02d:%02d.%03d - %-10s / %-15s - ", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), calendar.get(Calendar.MILLISECOND), hostName, hostAddress);
		}

		else {
			prefix = String.format("%57s", "");
		}

		if (logWriter != null) {
			try {
				logWriter.write(prefix + log + "\n");
				logWriter.flush();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

	}


	/**
	 * Write an Exception to the log file. The output will be like the printStackTrace() method of exceptions, but
	 * formatted to fit the log file.
	 * 
	 * @param e
	 *            the exception
	 */
	public static void writeLog(Exception e) {
		writeLog(e.toString(), true);
		StackTraceElement[] elements = e.getStackTrace();
		for (int i = 0; i < elements.length; i++) {
			writeLog("\t" + elements[i].toString(), false);
		}
	}

}
