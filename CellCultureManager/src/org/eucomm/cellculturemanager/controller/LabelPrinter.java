package org.eucomm.cellculturemanager.controller;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;


public class LabelPrinter implements Printable {

	// Define the printer instance
	private static LabelPrinter	printer		= null;

	// Define the name of the label printer
	private static final String	printerName	= "Brady BP-THT IP 600";

	// Define printer settings
	private PageFormat			pageFormat;
	private PrintService		service;

	// Define the label values
	private String				line1;
	private String				line2;
	private String				line3;

	// Define the page number
	private int					pageNumber;

	// Define the label font
	private Font				labelFont;


	/**
	 * Create an instance of the Printer class
	 * 
	 * @return an instance of the Printer class
	 */
	public static LabelPrinter getInstance() {
		if (printer == null) {
			printer = new LabelPrinter();
		}
		return printer;
	}


	/**
	 * Create a new Printer Object. Defines the paper size and sets the Printer Service.
	 */
	private LabelPrinter() {

		// Set the page format
		Paper paper = new Paper();
		paper.setSize(45.72 * CCM_Utils.mmToInch, 12.5 * CCM_Utils.mmToInch);
		paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
		pageFormat = new PageFormat();
		pageFormat.setPaper(paper);

		// Set the printer service
		service = null;
		PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
		for (int i = 0; i < services.length; i++) {
			if (services[i].getName().equals(printerName)) {
				service = services[i];
			}
		}

		// Set the label font
		labelFont = CCM_Utils.getFont(null).deriveFont(4.0f);

	}


	/**
	 * Print a number of labels.
	 */
	public void printLabels(String line1, String line2, String line3, int pageNumber) {

		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		this.pageNumber = pageNumber;

		// Check if the printer was found and print the page
		if (service != null) {

			// Create the print job
			PrinterJob job = PrinterJob.getPrinterJob();
			try {

				// Print the page
				job.setPrintService(service);
				job.setPrintable(this, pageFormat);
				job.print();

			}
			catch (PrinterException e) {
				LogWriter.writeLog(e);
			}

		}

		else {
			LogWriter.writeLog("Printer not found", true);
		}

	}


	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

		if (pageIndex > pageNumber - 1) {
			return NO_SUCH_PAGE;
		}

		else {
			Graphics2D g2d = (Graphics2D) graphics;
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			g2d.setPaint(Color.BLACK);

			g2d.setFont(labelFont);

			int indentionLeft = 2;
			int lineHeight = 8;
			int indentionTop = 7;

			g2d.drawString(line1, indentionLeft, lineHeight * 0 + indentionTop);
			g2d.drawString(line2, indentionLeft, lineHeight * 1 + indentionTop);
			g2d.drawString(line3, indentionLeft, lineHeight * 2 + indentionTop);

			return PAGE_EXISTS;
		}

	}

}
