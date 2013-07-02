package org.eucomm.cellculturemanager.model;


import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.eucomm.cellculturemanager.controller.DatabaseProvider;
import org.eucomm.cellculturemanager.gui.PlateRegion;


public class Plate {

	private int					tank;
	private int					rack;
	private int					shelf;
	private String				plateID;
	private GregorianCalendar	scanDate;

	private List<PlateRegion>	regions				= new ArrayList<PlateRegion>();
	private int[][]				regionMatrix;
	private Clone[][]			cloneMatrix;
	private String[][]			barcodeMatrix;

	private boolean				workbenchClones		= false;

	/**
	 * The row number of a plate.
	 */
	public static final int		ROWS				= 8;

	/**
	 * The column number of a plate.
	 */
	public static final int		COLUMNS				= 12;

	/**
	 * The default value for an empty or unreadable tube that is sended by the scanner.
	 */
	public static final String	EMPTY_TUBE_VALUE	= "null";

	/**
	 * The names of the rows of a plate in Roman letters.
	 */
	public static final char[]	rowNames			= { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };


	/**
	 * Create a new instance of Plate.
	 * 
	 * @param tank
	 * @param rack
	 * @param shelf
	 * @param plateID
	 */
	public Plate(int tank, int rack, int shelf, String plateID) {

		setTank(tank);
		setRack(rack);
		setShelf(shelf);
		setPlateID(plateID);

	}


	/**
	 * Create a new instance of Plate.
	 * 
	 * @param tank
	 * @param rack
	 * @param shelf
	 * @param plateID
	 * @param scanDate
	 */
	public Plate(int tank, int rack, int shelf, String plateID, GregorianCalendar scanDate) {

		setTank(tank);
		setRack(rack);
		setShelf(shelf);
		setPlateID(plateID);
		setScanDate(scanDate);

	}


	/**
	 * Create a new instance of Plate.
	 * 
	 * @param tank
	 * @param rack
	 * @param shelf
	 * @param plateID
	 * @param scanDate
	 * @param regions
	 */
	public Plate(int tank, int rack, int shelf, String plateID, GregorianCalendar scanDate, List<PlateRegion> regions) {

		setTank(tank);
		setRack(rack);
		setShelf(shelf);
		setPlateID(plateID);
		setScanDate(scanDate);
		setRegions(regions);

	}


	/**
	 * Return the number of the tank in the cryo storage.
	 * 
	 * @return the tank
	 */
	public int getTank() {
		return tank;
	}


	/**
	 * Set the number of the tank in the cryo storage.
	 * 
	 * @param tank
	 *            the tank to set
	 */
	public void setTank(int tank) {
		this.tank = tank;
	}


	/**
	 * Return the number of the rack in the cryo storage.
	 * 
	 * @return the rack
	 */
	public int getRack() {
		return rack;
	}


	/**
	 * Set the number of the rack in the cryo storage.
	 * 
	 * @param rack
	 *            the rack to set
	 */
	public void setRack(int rack) {
		this.rack = rack;
	}


	/**
	 * Return the number of the shelf in the cryo storage.
	 * 
	 * @return the shelf
	 */
	public int getShelf() {
		return shelf;
	}


	/**
	 * Set the number of the shelf in the cryo storage.
	 * 
	 * @param shelf
	 *            the shelf to set
	 */
	public void setShelf(int shelf) {
		this.shelf = shelf;
	}


	/**
	 * Return the ID of the plate.
	 * 
	 * @return the plateID
	 */
	public String getPlateID() {
		return plateID;
	}


	/**
	 * Set the ID of the plate.
	 * 
	 * @param plateID
	 *            the plateID to set
	 */
	public void setPlateID(String plateID) {
		this.plateID = plateID;
	}


	/**
	 * Return the scan date of the plate.
	 * 
	 * @return the scanDate
	 */
	public GregorianCalendar getScanDate() {
		return scanDate;
	}


	/**
	 * Set the scan date of the plate.
	 * 
	 * @param scanDate
	 *            the scanDate to set
	 */
	public void setScanDate(GregorianCalendar scanDate) {
		this.scanDate = scanDate;
	}


	/**
	 * Return the regions of the plate.
	 * 
	 * @return the regions
	 */
	public List<PlateRegion> getRegions() {
		return regions;
	}


	/**
	 * Set the regions of the plate.
	 * 
	 * @param regions
	 *            the regions to set
	 */
	public void setRegions(List<PlateRegion> regions) {
		this.regions = regions;
		barcodeMatrix = calculateBarcodeMatrix();
		regionMatrix = calculateRegionMatrix();
		cloneMatrix = calculateCloneMatrix();
	}


	/**
	 * Set the regions of the plate by an array of barcodes.
	 * This method fetches the clone for each given barcode and calculates regions from that information.
	 * 
	 * @param barcodeMatrix
	 */
	public void setRegions(String[][] barcodeMatrix) {

		// Clean up the existing regions (if any), so there is no overlapping
		regions.clear();

		this.barcodeMatrix = barcodeMatrix;

		// Create the region and clone matrices
		regionMatrix = new int[Plate.ROWS][Plate.COLUMNS];
		cloneMatrix = new Clone[Plate.ROWS][Plate.COLUMNS];

		if (barcodeMatrix == null) {
			return;
		}

		int x, y;
		Barcode barcode;

		// Go through each region
		for (y = 0; y < Plate.ROWS; y++) {
			for (x = 0; x < Plate.COLUMNS; x++) {

				// Initialize the regionIndex with -1
				regionMatrix[y][x] = -1;

				// Initialize the clone
				if (barcodeMatrix[y][x] != null && !barcodeMatrix[y][x].equals(EMPTY_TUBE_VALUE)) {

					barcode = DatabaseProvider.getInstance().getBarcode(barcodeMatrix[y][x]);

					if (barcode != null) {

						barcode.setPlateID(plateID);
						barcode.setPlateX(x);
						barcode.setPlateY(y);

						cloneMatrix[y][x] = DatabaseProvider.getInstance().getClone(barcode.getThawedCloneID());

						if (cloneMatrix[y][x] != null) {

							// If an adjacent clone is the same as the current clone, expand the current region
							if (y - 1 >= 0 && cloneMatrix[y - 1][x] != null && cloneMatrix[y][x].getDatabaseID() == cloneMatrix[y - 1][x].getDatabaseID()) {
								regionMatrix[y][x] = regionMatrix[y - 1][x];
								regions.get(regionMatrix[y][x] - 1).addBarcode(barcode);

							}
							else if (x - 1 >= 0 && cloneMatrix[y][x - 1] != null && cloneMatrix[y][x].getDatabaseID() == cloneMatrix[y][x - 1].getDatabaseID()) {
								regionMatrix[y][x] = regionMatrix[y][x - 1];
								regions.get(regionMatrix[y][x] - 1).addBarcode(barcode);
							}

							else {
								regions.add(new PlateRegion(cloneMatrix[y][x], barcode));
								regionMatrix[y][x] = regions.size();
							}

						}

						else {
							regions.add(new PlateRegion(null, barcode));
							regionMatrix[y][x] = regions.size();
						}

					}
					else {
						regions.add(new PlateRegion(null, new Barcode(0, barcodeMatrix[y][x], "", 0, null, "", x, y)));
						regionMatrix[y][x] = regions.size();
					}
				}
				else {
					regions.add(new PlateRegion(null, new Barcode(0, null, "", 0, null, "", x, y)));
					regionMatrix[y][x] = regions.size();
					if (barcodeMatrix[y][x] == null) {
						barcodeMatrix[y][x] = Plate.EMPTY_TUBE_VALUE;
					}
				}

			}

		}
	}


	/**
	 * Calculate the barcodes on the plate.
	 * 
	 * @return the barcodeMatrix
	 */
	private String[][] calculateBarcodeMatrix() {

		barcodeMatrix = new String[Plate.ROWS][Plate.COLUMNS];

		int x, y;

		// Initialize the barcodes with "null"
		for (y = 0; y < Plate.ROWS; y++) {
			for (x = 0; x < Plate.COLUMNS; x++) {
				barcodeMatrix[y][x] = Plate.EMPTY_TUBE_VALUE;
			}
		}

		// Set all barcodes to their positions
		if (regions != null) {
			for (x = 0; x < regions.size(); x++) {
				for (y = 0; y < regions.get(x).getBarcodes().size(); y++) {
					if (regions.get(x).getBarcodes().get(y).getCode() != null) {
						barcodeMatrix[regions.get(x).getBarcodes().get(y).getPlateY()][regions.get(x).getBarcodes().get(y).getPlateX()] = regions.get(x).getBarcodes().get(y).getCode();
					}
				}
			}
		}

		return barcodeMatrix;

	}


	/**
	 * Calculate the indices of the regions on the plate according to the barcodes.
	 * 
	 * @return the regionMatrix
	 */
	private int[][] calculateRegionMatrix() {

		regionMatrix = new int[Plate.ROWS][Plate.COLUMNS];

		int x, y;

		if (regions != null) {
			for (x = 0; x < regions.size(); x++) {
				for (y = 0; y < regions.get(x).getBarcodes().size(); y++) {
					if (regions.get(x).getBarcodes().get(y).getCode() != null) {
						regionMatrix[regions.get(x).getBarcodes().get(y).getPlateY()][regions.get(x).getBarcodes().get(y).getPlateX()] = x;
					}
				}
			}
		}

		return regionMatrix;

	}


	/**
	 * Calculate the clones on the plate according to the barcodes.
	 * 
	 * @return the cloneMatrix
	 */
	private Clone[][] calculateCloneMatrix() {

		cloneMatrix = new Clone[Plate.ROWS][Plate.COLUMNS];

		int x, y;

		if (regions != null) {
			for (x = 0; x < regions.size(); x++) {
				for (y = 0; y < regions.get(x).getBarcodes().size(); y++) {
					if (regions.get(x).getBarcodes().get(y).getCode() != null) {
						cloneMatrix[regions.get(x).getBarcodes().get(y).getPlateY()][regions.get(x).getBarcodes().get(y).getPlateX()] = regions.get(x).getClone();
					}
				}
			}
		}

		return cloneMatrix;

	}


	/**
	 * Return the barcode that is at the requested position
	 * 
	 * @param x
	 *            the column
	 * @param y
	 *            the row
	 * @return a Barcode
	 */
	public String getBarcodeAt(int x, int y) {
		return barcodeMatrix[y][x];
	}


	/**
	 * Return the clone that is at the requested position
	 * 
	 * @param x
	 *            the column
	 * @param y
	 *            the row
	 * @return a Clone
	 */
	public Clone getCloneAt(int x, int y) {
		return cloneMatrix[y][x];
	}


	/**
	 * Return the region that is at the requested position
	 * 
	 * @param x
	 *            the column
	 * @param y
	 *            the row
	 * @return a Region
	 */
	public PlateRegion getRegionAt(int x, int y) {
		if (regionMatrix[y][x] > 0) {
			return regions.get(regionMatrix[y][x] - 1);
		}
		else {
			return null;
		}
	}


	/**
	 * Return the barcodes of the plate as readed from the scanner in a two-dimensional array.
	 * 
	 * @return the barcodes
	 */
	public String[][] getBarcodes() {
		return barcodeMatrix;
	}


	/**
	 * Return a unique list of clones that are on the plate
	 * 
	 * @return clones of the plate
	 */
	public List<Clone> getClones() {
		List<Clone> clones = new ArrayList<Clone>();
		int i = 0;
		for (int y = 0; y < Plate.ROWS; y++) {
			for (int x = 0; x < Plate.COLUMNS; x++) {
				if (cloneMatrix[y][x] != null) {
					for (i = 0; i < clones.size(); i++) {
						if (clones.get(i).getDatabaseID() == cloneMatrix[y][x].getDatabaseID()) {
							break;
						}
					}
					if (i == clones.size()) {
						clones.add(cloneMatrix[y][x]);
					}
				}
			}
		}
		return clones;
	}


	public boolean isWorkbenchClones() {
		return workbenchClones;
	}


	public void setWorkbenchClones(boolean workbenchClones) {
		this.workbenchClones = workbenchClones;
	}
}
