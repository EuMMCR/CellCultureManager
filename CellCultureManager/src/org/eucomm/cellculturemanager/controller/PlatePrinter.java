package org.eucomm.cellculturemanager.controller;


import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Calendar;

import org.eucomm.cellculturemanager.gui.PlateRegion;
import org.eucomm.cellculturemanager.model.Plate;


public class PlatePrinter implements Printable {

	// Define the printer instance
	private static PlatePrinter	printer	= null;

	// Define the label font
	private Font				labelFont;

	// Define the plate to print
	private Plate				plate;


	/**
	 * Create an instance of the Printer class
	 * 
	 * @return an instance of the Printer class
	 */
	public static PlatePrinter getInstance() {
		if (printer == null) {
			printer = new PlatePrinter();
		}
		return printer;
	}


	/**
	 * Create a new Printer Object. Defines the paper size and sets the Printer Service.
	 */
	private PlatePrinter() {

		// Set the label font
		labelFont = CCM_Utils.getFont(null).deriveFont(10.0f);

	}


	public void printPlate(Plate plate) {

		this.plate = plate;

		PrinterJob job = PrinterJob.getPrinterJob();
		PageFormat pageFormat = job.defaultPage();
		pageFormat.setOrientation(PageFormat.LANDSCAPE);
		job.setPrintable(this, pageFormat);
		try {
			if (job.printDialog()) {
				job.print();
			}
		}
		catch (PrinterException e) {
			LogWriter.writeLog(e);
		}
	}


	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

		if (pageIndex > 0) {
			return NO_SUCH_PAGE;
		}

		else {

			Graphics2D g2d = (Graphics2D) graphics;

			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			g2d.setPaint(Color.BLACK);
			g2d.setFont(labelFont);

			FontMetrics fontMetrics = g2d.getFontMetrics();

			g2d.drawString("Tank: ", 0, 15);
			g2d.drawString(Integer.toString(plate.getTank()), 100, 15);

			g2d.drawString("Rack: ", 0, 30);
			g2d.drawString(Integer.toString(plate.getRack()), 100, 30);

			g2d.drawString("Shelf: ", 0, 45);
			g2d.drawString(Integer.toString(plate.getShelf()), 100, 45);

			g2d.drawString("Plate ID: ", 250, 15);
			g2d.drawString(plate.getPlateID(), 350, 15);

			g2d.drawString("Scan Date:", 250, 45);
			g2d.drawString(String.format("%04d-%02d-%02d", plate.getScanDate().get(Calendar.YEAR), plate.getScanDate().get(Calendar.MONTH) + 1, plate.getScanDate().get(Calendar.DAY_OF_MONTH)), 350, 45);

			g2d.drawLine(0, 60, (int) pageFormat.getImageableWidth(), 60);

			int xStart = 20;
			int yStart = 90;

			int x = 0, y = 0, i = 0;
			int stringWidth = 0;

			for (x = 0; x < 12; x++) {
				stringWidth = fontMetrics.stringWidth(x + "");
				g2d.drawString((x + 1) + "", (x * PlateRegion.printGridWidth) + xStart + (PlateRegion.printGridWidth / 2 - stringWidth / 2), yStart - fontMetrics.getDescent() - fontMetrics.getLeading());
			}

			for (y = 0; y < 8; y++) {
				stringWidth = fontMetrics.stringWidth(Plate.rowNames[y] + " ");
				g2d.drawString(Plate.rowNames[y] + " ", xStart - stringWidth, (y * PlateRegion.printGridHeight) + yStart + (PlateRegion.printGridHeight / 2) + fontMetrics.getDescent() + fontMetrics.getLeading());
			}

			for (i = 0; i < plate.getRegions().size(); i++) {
				plate.getRegions().get(i).drawPrinting(g2d, xStart, yStart);
			}

			return PAGE_EXISTS;
		}

	}
}
