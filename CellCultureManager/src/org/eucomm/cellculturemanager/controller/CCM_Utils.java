package org.eucomm.cellculturemanager.controller;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JOptionPane;

public class CCM_Utils {

	public static final double				mmToInch			= 72 / 25.4;

	public static final String				shippingPlateID		= "shippings";
	public static final String				shippingReason		= "shipping";

	public static final String[]			removeVialReasons	= { "-- please select a reason --", "unused vial", "chromosome count", "complaint clone", "injection", "re-expansion", "just for control", "contamination", "shipping", "other..." };

	private static final SimpleDateFormat	dateFormatDatabase	= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static boolean					useWorkbenchScanner	= true;


	public static GregorianCalendar SQLDateToCalendar(java.sql.Date date) {

		GregorianCalendar calendar = null;

		if (date != null) {
			calendar = new GregorianCalendar();
			calendar.setTime(date);
		}

		return calendar;

	}


	/**
	 * Returns a String that represents the database format of a Calendar object.
	 * <p>
	 * If <b>cal</b> is null, the current date is returned.
	 * 
	 * @param cal
	 *            a Calendar object
	 * @return
	 *         a String representation of the date
	 */
	public static String getDate(Calendar cal) {

		Calendar calendar;
		if (cal == null) {
			calendar = new GregorianCalendar();
		}
		else {
			calendar = cal;
		}

		return dateFormatDatabase.format(calendar.getTime());

	}


	public static Font getFont(Window baseFrame) {
		
		// Load the font for the labels
		Font font = null;
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, new File(CCM_Utils.class.getResource("/org/eucomm/cellculturemanager/resources/DejaVuSansMono.ttf").getFile()));
		}
		catch (FontFormatException e) {
			LogWriter.writeLog(e);
		}
		catch (IOException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (font == null) {
				JOptionPane.showMessageDialog(baseFrame, "Error while loading the correct font file.\nYou must restart the application");
				System.exit(0);
			}
		}
		return font;
	}


	public static CryoScanner getScannerInstance() {
		if (useWorkbenchScanner) {
			return CryoScannerWorkbench.getInstance();
		}
		else {
			return CryoScannerFluidX.getInstance();
		}
	}

}
