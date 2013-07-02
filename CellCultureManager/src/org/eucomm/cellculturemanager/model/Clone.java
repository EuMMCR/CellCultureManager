package org.eucomm.cellculturemanager.model;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eucomm.cellculturemanager.controller.DatabaseProvider;


public class Clone {

	private int					databaseID;

	private String				cloneID;
	private String				freezingPlateID;
	private String				cellLine;
	private String				geneName;
	private String				technicianThawing;
	private String				technicianQC;
	private GregorianCalendar	thawDate;
	private int					status;
	private int					vialNumber;
	private Barcode[]			barcodes;
	private int					tank;
	private int					rack;
	private int					shelf;
	private String				position;

	public static final String	TAG_D_COPY		= "(D)";
	public static final String	TAG_COMPLAINT	= "-C-";
	public static final String	TAG_REEXPAND	= "-E-";
	public static final String	TAG_DUPLICATE	= "-2-";

	/**
	 * Indicates that a clone has been thawed.
	 */
	public static final int		THAWED			= 1;

	/**
	 * Indicates that a clone has been printed.
	 */
	public static final int		PRINTED			= 2;

	/**
	 * Indicates that a clone has been scanned.
	 */
	public static final int		SCANNED			= 4;

	/**
	 * Indicates that a clone has been frozen.
	 */
	public static final int		FROZEN			= 8;

	/**
	 * Indicates that a clone is a complaint clone.
	 */
	public static final int		COMPLAINT		= 16;

	/**
	 * Indicates that a clone is a D-Copy
	 */
	public static final int		D_COPY			= 32;

	/**
	 * Indicates that a clone died during cell culture
	 */
	public static final int		DEAD			= 64;

	/**
	 * Indicates that a clone was expanded again because too few vials are left.
	 */
	public static final int		EXPANDED_AGAIN	= 128;

	/**
	 * Indicates that a clone was duplicated
	 */
	public static final int		DUPLICATE		= 256;

	/**
	 * Indicates that a clone was frozen with the Askion cryo bench
	 */
	public static final int		CRYO_BENCH		= 512;


	/**
	 * Create a new Clone Object.
	 * 
	 * @param cloneID
	 *            the clone ID
	 * @param freezingPlateID
	 *            the freezing plate ID
	 * @param cellLine
	 *            the cell line
	 * @param geneName
	 *            the gene name
	 * @param technicianThawing
	 *            the name of the technician that is responsible for the cell culture
	 * @param technicianQC
	 *            the name of the technician who is responsible for the QC
	 * @param thawDate
	 *            the thawing date
	 * @param status
	 *            the status
	 */
	public Clone(int databaseID, String cloneID, String freezingPlateID, String cellLine, String geneName, String technicianThawing, String technicianQC, Date thawDate, int status, int vialNumber) {
		this.databaseID = databaseID;
		this.cloneID = cloneID;
		this.freezingPlateID = freezingPlateID;
		this.cellLine = cellLine;
		this.geneName = geneName;
		this.technicianThawing = technicianThawing;
		this.technicianQC = technicianQC;
		if (thawDate != null) {
			this.thawDate = new GregorianCalendar();
			this.thawDate.setTime(thawDate);
		}
		else {
			thawDate = null;
		}
		this.status = status;
		this.vialNumber = vialNumber;

	}


	/**
	 * @return the databaseID
	 */
	public int getDatabaseID() {
		return databaseID;
	}


	/**
	 * Return the ID of the clone.
	 * 
	 * @return the cloneID
	 */
	public String getCloneID() {
		return cloneID;
	}


	/**
	 * Return the ID of the clone tagged with additional information
	 * 
	 * @return the tagged clone ID
	 */
	public String getTaggedCloneID() {

		String cloneID = this.cloneID;

		if (this.isDCopy()) {
			cloneID += " " + TAG_D_COPY;
		}

		if (this.isComplaint()) {
			cloneID += " " + TAG_COMPLAINT;
		}

		if (this.isExpandedAgain()) {
			cloneID += " " + TAG_REEXPAND;
		}

		if (this.isDuplicate()) {
			cloneID += " " + TAG_DUPLICATE;
		}

		return cloneID;

	}


	/**
	 * Return the ID if the freezing plate.
	 * 
	 * @return the freezingPlateID
	 */
	public String getFreezingPlateID() {
		return freezingPlateID;
	}


	/**
	 * Return the name of the cell line.
	 * 
	 * @return the cellLine
	 */
	public String getCellLine() {
		return cellLine;
	}


	/**
	 * Return the name of the gene.
	 * 
	 * @return the geneName
	 */
	public String getGeneName() {
		return geneName;
	}


	/**
	 * Return the name of the technician who is responsible for the cell culture of this clone.
	 * 
	 * @return the technicianThawing
	 */
	public String getTechnicianThawing() {
		return technicianThawing;
	}


	/**
	 * Return the name of the technician who is responsible for the QC in the molecular biology lab.
	 * 
	 * @return the technicianQC
	 */
	public String getTechnicianQC() {
		return technicianQC;
	}


	/**
	 * Return the thawing date.
	 * 
	 * @return the thawDate
	 */
	public GregorianCalendar getThawDate() {
		return thawDate;
	}


	/**
	 * Return the thawing date as a string formatted as YYYY-MM-DD.
	 * 
	 * @return a string representation of the thawing date
	 */
	public String getThawDateString() {
		if (thawDate == null) {
			return "???";
		}
		else {
			return String.format("%04d-%02d-%02d", thawDate.get(Calendar.YEAR), thawDate.get(Calendar.MONTH) + 1, thawDate.get(Calendar.DAY_OF_MONTH));
		}
	}


	/**
	 * Return the status of the clone. The status is an integer that contains the addition of multiple statuses that are
	 * powers of two.
	 * 
	 * @return the addition of all statuses
	 */
	public int getStatus() {
		return status;
	}


	/**
	 * @return the vialNumber
	 */
	public int getVialNumber() {
		return vialNumber;
	}


	/**
	 * @param vialNumber
	 *            the vialNumber to set
	 */
	public void setVialNumber(int vialNumber) {
		this.vialNumber = vialNumber;
	}


	public Barcode[] getBarcodes() {
		return barcodes;
	}


	public void setBarcodes(Barcode[] barcodes) {
		this.barcodes = barcodes;
	}


	public int getTank() {
		return tank;
	}


	public void setTank(int tank) {
		this.tank = tank;
	}


	public int getRack() {
		return rack;
	}


	public void setRack(int rack) {
		this.rack = rack;
	}


	public int getShelf() {
		return shelf;
	}


	public void setShelf(int shelf) {
		this.shelf = shelf;
	}


	public String getPosition() {
		return position;
	}


	public void setPosition(String position) {
		this.position = position;
	}


	/**
	 * Return if the clone was thawed.
	 * 
	 * @return boolean
	 */
	public boolean wasThawed() {
		return (this.status & THAWED) == THAWED;
	}


	/**
	 * Return if the clone was printed.
	 * 
	 * @return boolean
	 */
	public boolean wasPrinted() {
		return (this.status & PRINTED) == PRINTED;
	}


	/**
	 * Return if the clone was scanned.
	 * 
	 * @return boolean
	 */
	public boolean wasScanned() {
		return (this.status & SCANNED) == SCANNED;
	}


	/**
	 * Return if the clone was frozen.
	 * 
	 * @return boolean
	 */
	public boolean wasFrozen() {
		return (this.status & FROZEN) == FROZEN;
	}


	/**
	 * Return if the clone is a complaint clone.
	 * 
	 * @return boolean
	 */
	public boolean isComplaint() {
		return (this.status & COMPLAINT) == COMPLAINT;
	}


	public boolean isExpandedAgain() {
		return (this.status & EXPANDED_AGAIN) == EXPANDED_AGAIN;
	}


	/**
	 * Return if the clone is a D-Copy
	 * 
	 * @return boolean
	 */
	public boolean isDCopy() {
		return (this.status & D_COPY) == D_COPY;
	}


	/**
	 * Return if a clone died during cell culture
	 * 
	 * @return boolean
	 */
	public boolean isDead() {
		return (this.status & DEAD) == DEAD;
	}


	/**
	 * Return true if a clone is a duplicate (e.g. from a Split to 1x6 to 1x24)
	 * 
	 * @return boolean
	 */
	public boolean isDuplicate() {
		return (this.status & DUPLICATE) == DUPLICATE;
	}


	/**
	 * Return if a clone was frozen with the cryo bench
	 * 
	 * @return
	 */
	public boolean isCryoBenchClone() {
		return (this.status & CRYO_BENCH) == CRYO_BENCH;
	}


	/**
	 * Add a status to the statuses
	 * 
	 * @param status
	 */
	public void addStatus(int status) {
		this.status = this.status | status;
		DatabaseProvider.getInstance().updateCloneData("status", Integer.toString(this.status), this.databaseID);
	}


	/**
	 * Remove a status from the statuses
	 * 
	 * @param status
	 */
	public void removeStatus(int status) {
		if ((this.status & status) > 0) {
			this.status = this.status ^ status;
		}
		DatabaseProvider.getInstance().updateCloneData("status", Integer.toString(this.status), this.databaseID);
	}


	/**
	 * Return a String representation of the clone including its clone ID, cell culture technician and the thawing date.
	 */
	public String toString() {
		return this.cloneID + " - " + getTechnicianThawing() + " - " + getThawDateString();
	}

}
