package org.eucomm.cellculturemanager.gui;


import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;

import org.eucomm.cellculturemanager.controller.CCM_Utils;
import org.eucomm.cellculturemanager.model.Barcode;
import org.eucomm.cellculturemanager.model.Clone;


public class PlateRegion {

	private Clone				clone;
	private List<Barcode>		barcodes				= new ArrayList<Barcode>();

	private int					maxX					= -1;
	private int					minX					= -1;
	private int					maxY					= -1;
	private int					minY					= -1;

	private int[][]				matrix					= null;

	public static final int		gridWidth				= 57;
	public static final int		gridHeight				= 57;

	public static final int		printGridWidth			= (int) (20 * CCM_Utils.mmToInch);
	public static final int		printGridHeight			= (int) (16 * CCM_Utils.mmToInch);

	public static final Color	darkBorder				= new Color(0, 0, 0);
	public static final Color	brightBorderSurrounding	= new Color(238, 238, 238);
	public static final Color	brightBorderRegularTube	= new Color(0, 240, 0);
	public static final Color	brightBorderWrongTube	= new Color(240, 0, 0);
	public static final Color	brightBorderSmallRegion	= new Color(240, 140, 0);
	public static final Color	backgroundSurrounding	= new Color(255, 255, 255);
	public static final Color	backgroundEmptyTube		= new Color(128, 128, 128);
	public static final Color	backgroundRegularTube	= new Color(0, 255, 0);
	public static final Color	backgroundWrongTube		= new Color(255, 0, 0);
	public static final Color	backgroundSmallRegion	= new Color(255, 153, 0);

	public static final String	LABEL_EMPTY_TUBE		= "X";
	public static final String	LABEL_NO_CLONE			= "???";

	public static final int		LAYER_PANEL				= 0;
	public static final int		LAYER_LABEL				= 1;
	public static final int		LAYER_CLICKABLE			= 2;


	public PlateRegion(Clone clone, Barcode barcode) {
		this.clone = clone;
		barcodes.add(barcode);
	}


	/**
	 * Get the number of barcodes in this region.
	 * 
	 * @return
	 */
	public int getBarcodeNumber() {
		return barcodes.size();
	}


	/**
	 * Get the barcodes of this region.
	 * 
	 * @return
	 */
	public List<Barcode> getBarcodes() {
		return barcodes;
	}


	/**
	 * Add a barcode to the region.
	 * 
	 * @param barcode
	 */
	public void addBarcode(Barcode barcode) {
		barcodes.add(barcode);
	}


	/**
	 * Return the clone of the region.
	 * 
	 * @return
	 */
	public Clone getClone() {
		return clone;
	}


	public void drawUI(JLayeredPane panel) {

		// Create a minimum sized matrix from the positions that contains the region
		calculateMatrix();

		// Draw the background panels of the region
		drawPanels(panel);

		// Draw the label of the region at the longest connected area in the region
		drawLabel(panel);

	}


	public void drawPrinting(Graphics2D g2d, int xStart, int yStart) {

		// Create a minimum sized matrix from the positions that contains the region
		calculateMatrix();

		// Draw the boundary of the region
		drawBoundary(g2d, xStart, yStart);

		// Draw the label of the region
		drawLabel(g2d, xStart, yStart);

	}


	private void drawPanels(JLayeredPane panel) {

		// Set the background and bright border color for the panels
		Color background;
		Color brightBorder;

		if (this.isEmpty()) {
			background = backgroundEmptyTube;
			brightBorder = darkBorder;
		}
		else if (this.isUnknownBarcode() || this.isUnknownClone()) {
			background = backgroundWrongTube;
			brightBorder = brightBorderWrongTube;
		}
		else if (this.isTooSmall()) {
			background = backgroundSmallRegion;
			brightBorder = brightBorderSmallRegion;
		}
		else {
			background = backgroundRegularTube;
			brightBorder = brightBorderRegularTube;
		}

		// Create the region panels
		JPanel clonePanel;
		int x, y;
		for (y = 0; y < matrix.length; y++) {
			for (x = 0; x < matrix[y].length; x++) {

				if (matrix[y][x] == 1) {

					clonePanel = new JPanel();
					clonePanel.setOpaque(true);
					clonePanel.setBackground(background);

					clonePanel.setBounds((x + getMinX() + 1) * gridWidth, (y + getMinY() + 1) * gridHeight, gridWidth, gridHeight);

					boolean top = (y == 0 || matrix[y - 1][x] == 0);
					boolean right = (x == matrix[y].length - 1 || matrix[y][x + 1] == 0);
					boolean bottom = (y == matrix.length - 1 || matrix[y + 1][x] == 0);
					boolean left = (x == 0 || matrix[y][x - 1] == 0);

					clonePanel.setBorder(new CompoundBorder(new MatteBorder(top ? 1 : 0, left ? 1 : 0, bottom ? 1 : 0, right ? 1 : 0, darkBorder), new MatteBorder(top ? 0 : 1, left ? 0 : 1, bottom ? 0 : 1, right ? 0 : 1, brightBorder)));
					panel.add(clonePanel, new Integer(LAYER_PANEL));

				}

			}
		}
	}


	private void drawBoundary(Graphics2D g2d, int xStart, int yStart) {

		int x, y;
		for (y = 0; y < matrix.length; y++) {
			for (x = 0; x < matrix[y].length; x++) {

				if (matrix[y][x] == 1) {

					// Print top border
					if (y == 0 || matrix[y - 1][x] == 0) {
						g2d.drawLine((x + getMinX()) * printGridWidth + xStart, (y + getMinY()) * printGridHeight + yStart, (x + 1 + getMinX()) * printGridWidth + xStart, (y + getMinY()) * printGridHeight + yStart);
					}

					// Print right border
					if (x == matrix[y].length - 1 || matrix[y][x + 1] == 0) {
						g2d.drawLine((x + 1 + getMinX()) * printGridWidth + xStart, (y + getMinY()) * printGridHeight + yStart, (x + 1 + getMinX()) * printGridWidth + xStart, (y + 1 + getMinY()) * printGridHeight + yStart);
					}

					// Print bottom border
					if (y == matrix.length - 1 || matrix[y + 1][x] == 0) {
						g2d.drawLine((x + getMinX()) * printGridWidth + xStart, (y + 1 + getMinY()) * printGridHeight + yStart, (x + 1 + getMinX()) * printGridWidth + xStart, (y + 1 + getMinY()) * printGridHeight + yStart);
					}

					// Print left border
					if (x == 0 || matrix[y][x - 1] == 0) {
						g2d.drawLine((x + getMinX()) * printGridWidth + xStart, (y + getMinY()) * printGridHeight + yStart, (x + getMinX()) * printGridWidth + xStart, (y + 1 + getMinY()) * printGridHeight + yStart);
					}

				}

			}
		}

	}


	private void drawLabel(JLayeredPane panel) {

		Rectangle labelBounds = getLabelBounds();

		// Create the label
		String labelString = "";
		if (clone != null) {
			labelString = clone.getCloneID();
		}
		else if (barcodes.get(0).getCode() != null) {
			labelString = LABEL_NO_CLONE;
		}
		else {
			labelString = LABEL_EMPTY_TUBE;
		}

		panel.add(new PlateLabel(labelString, (labelBounds.x + 1) * gridWidth, (labelBounds.y + 1) * gridHeight, labelBounds.width * gridWidth, labelBounds.height * gridHeight), new Integer(LAYER_LABEL));

	}


	private void drawLabel(Graphics2D g2d, int xStart, int yStart) {

		Rectangle labelBounds = getLabelBounds();

		// Create the label
		String labelString = "";
		if (clone != null) {
			labelString = null;
		}
		else if (barcodes.get(0).getCode() != null) {
			labelString = LABEL_NO_CLONE;
		}
		else {
			labelString = LABEL_EMPTY_TUBE;
		}

		FontMetrics fontMetrics = g2d.getFontMetrics();

		if (labelBounds.height > labelBounds.width) {
			g2d.rotate(Math.toRadians(270), labelBounds.width / 2, labelBounds.height / 2);
		}

		g2d.setColor(Color.BLACK);

		int x = (labelBounds.x * printGridWidth) + xStart;
		int y = labelBounds.y * printGridHeight + (printGridHeight / 2) + fontMetrics.getDescent() + fontMetrics.getLeading() + yStart;

		if (labelString == null) {
			g2d.drawString(clone.getCloneID() + (clone.isComplaint() ? " (C)" : "") + (clone.isExpandedAgain() ? " (E)" : ""), x + fontMetrics.stringWidth(" "), y - fontMetrics.getHeight());
			g2d.drawString(clone.getGeneName(), x + fontMetrics.stringWidth(" "), y);
			g2d.drawString(clone.getCellLine(), x + fontMetrics.stringWidth(" "), y + fontMetrics.getHeight());
		}

		else {
			g2d.drawString(labelString, x + (printGridWidth * labelBounds.width / 2) - (fontMetrics.stringWidth(labelString) / 2), y);
		}

	}


	private Rectangle getLabelBounds() {

		int x, y;

		// Calculate the position of the label
		int longestX = 0;
		int tmpLongestX = 0;
		Point longestXStart = null;
		Point tmpLongestXStart = null;
		for (y = 0; y < matrix.length; y++) {
			for (x = 0; x < matrix[y].length; x++) {
				if (matrix[y][x] == 1) {
					tmpLongestX++;
					if (tmpLongestXStart == null) {
						tmpLongestXStart = new Point(x, y);
					}
				}
				if (matrix[y][x] == 0 || x == matrix[y].length - 1) {
					if (tmpLongestX > longestX) {
						longestX = tmpLongestX;
						longestXStart = tmpLongestXStart;
					}
					tmpLongestX = 0;
					tmpLongestXStart = null;
				}
			}
		}

		// Calculate the longest vertical line
		int longestY = 0;
		int tmpLongestY = 0;
		Point longestYStart = null;
		Point tmpLongestYStart = null;
		for (x = 0; x < matrix[0].length; x++) {
			for (y = 0; y < matrix.length; y++) {
				if (matrix[y][x] == 1) {
					tmpLongestY++;
					if (tmpLongestYStart == null) {
						tmpLongestYStart = new Point(x, y);
					}
				}
				if (matrix[y][x] == 0 || y == matrix.length - 1) {
					if (tmpLongestY > longestY) {
						longestY = tmpLongestY;
						longestYStart = tmpLongestYStart;
					}
					tmpLongestY = 0;
					tmpLongestYStart = null;
				}
			}
		}

		int labelX;
		int labelY;
		int labelWidth;
		int labelHeight;

		if (longestX >= longestY) {
			labelX = longestXStart.x + getMinX();
			labelY = longestXStart.y + getMinY();
			labelWidth = longestX;
			labelHeight = 1;
		}
		else {
			labelX = longestYStart.x + getMinX();
			labelY = longestYStart.y + getMinX();
			labelWidth = 1;
			labelHeight = longestY;
		}

		return new Rectangle(labelX, labelY, labelWidth, labelHeight);

	}


	private int getMaxX() {
		if (maxX == -1) {
			for (int i = 0; i < barcodes.size(); i++) {
				if (barcodes.get(i).getPlateX() > maxX) {
					maxX = barcodes.get(i).getPlateX();
				}
			}
		}
		return maxX;
	}


	private int getMinX() {
		if (minX == -1) {
			for (int i = 0; i < barcodes.size(); i++) {
				if (minX == -1 || barcodes.get(i).getPlateX() < minX) {
					minX = barcodes.get(i).getPlateX();
				}
			}
		}
		return minX;
	}


	private int getMaxY() {
		if (maxY == -1) {
			for (int i = 0; i < barcodes.size(); i++) {
				if (barcodes.get(i).getPlateY() > maxY) {
					maxY = barcodes.get(i).getPlateY();
				}
			}
		}
		return maxY;
	}


	private int getMinY() {
		if (minY == -1) {
			for (int i = 0; i < barcodes.size(); i++) {
				if (minY == -1 || barcodes.get(i).getPlateY() < minY) {
					minY = barcodes.get(i).getPlateY();
				}
			}
		}
		return minY;
	}


	/**
	 * Calculate a matrix that has the width and height of the region and contains 1 where a position is from the
	 * region.
	 * 
	 * @return
	 */
	public int[][] calculateMatrix() {
		if (matrix == null) {
			matrix = new int[getMaxY() - getMinY() + 1][getMaxX() - getMinX() + 1];
			for (int i = 0; i < barcodes.size(); i++) {
				matrix[barcodes.get(i).getPlateY() - getMinY()][barcodes.get(i).getPlateX() - getMinX()] = 1;
			}
		}
		return matrix;
	}


	/**
	 * Return the bounds where the Region is located
	 * 
	 * @return
	 */
	public Rectangle getBounds() {
		return new Rectangle(getMinX(), getMinY(), getMaxX() - getMinX(), getMaxY() - getMinY());
	}


	/**
	 * Return if the region is a empty position on the plate.
	 * A region is empty, if no clone and no barcode is set.
	 * 
	 * @return boolean
	 */
	public boolean isEmpty() {
		return clone == null && barcodes.get(0).getCode() == null;
	}


	/**
	 * Return if the region is an unknown barcode.
	 * This is the case, if a barcode was not yet scanned with the single scanner and is not in the databse.
	 * 
	 * @return boolean
	 */
	public boolean isUnknownBarcode() {
		return clone == null && barcodes.get(0).getCode() != null && barcodes.get(0).getDatabaseID() == 0;
	}


	/**
	 * Return if the region is an unknown clone.
	 * This is the case, if the barcode was recognized but could not be connected to a clone.
	 * 
	 * @return boolean
	 */
	public boolean isUnknownClone() {
		return clone == null && barcodes.size() > 0 && barcodes.get(0).getDatabaseID() > 0;
	}


	/**
	 * Return if the region is too small.
	 * If the clone is set, but the region contains just one or two barcodes, the region is too small.
	 * 
	 * @return boolean
	 */
	public boolean isTooSmall() {
		return clone != null && barcodes.size() < 3;
	}


	/**
	 * Return if the region is a regular region.
	 * A regular region contains at least 3 barcodes and the corresponding clone is not empty.
	 * 
	 * @return
	 */
	public boolean isRegular() {
		return clone != null && barcodes.size() > 2;
	}

}
