package org.eucomm.cellculturemanager.model;


import java.util.Date;
import java.util.GregorianCalendar;


public class Barcode {

	private int					databaseID;
	private String				code;
	private String				cloneID;
	private int					thawedCloneID;
	private GregorianCalendar	scanDate;
	private String				plateID;
	private int					plateX;
	private int					plateY;


	/**
	 * Create a new Barcode Object
	 * @param databaseID
	 * @param code
	 * @param cloneID
	 * @param thawedCloneID
	 * @param scanDate
	 * @param plateID
	 * @param plateX
	 * @param plateY
	 */
	public Barcode(int databaseID, String code, String cloneID, int thawedCloneID, Date scanDate, String plateID, int plateX, int plateY) {
		this.databaseID = databaseID;
		this.code = code;
		this.cloneID = cloneID;
		this.thawedCloneID = thawedCloneID;
		if (scanDate != null) {
			this.scanDate = new GregorianCalendar();
			this.scanDate.setTime(scanDate);
		}
		else {
			this.scanDate = null;
		}
		this.plateID = plateID;
		this.plateX = plateX;
		this.plateY = plateY;
	}


	/**
	 * @return the databaseID
	 */
	public int getDatabaseID() {
		return databaseID;
	}


	/**
	 * @param databaseID
	 *            the databaseID to set
	 */
	public void setDatabaseID(int databaseID) {
		this.databaseID = databaseID;
	}


	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}


	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}


	/**
	 * @return the cloneID
	 */
	public String getCloneID() {
		return cloneID;
	}


	/**
	 * @param cloneID
	 *            the cloneID to set
	 */
	public void setCloneID(String cloneID) {
		this.cloneID = cloneID;
	}


	/**
	 * @return the thawedCloneID
	 */
	public int getThawedCloneID() {
		return thawedCloneID;
	}


	/**
	 * @param thawedCloneID
	 *            the thawedCloneID to set
	 */
	public void setThawedCloneID(int thawedCloneID) {
		this.thawedCloneID = thawedCloneID;
	}


	/**
	 * @return the scanDate
	 */
	public GregorianCalendar getScanDate() {
		return scanDate;
	}


	/**
	 * @param scanDate
	 *            the scanDate to set
	 */
	public void setScanDate(GregorianCalendar scanDate) {
		this.scanDate = scanDate;
	}


	/**
	 * @return the plateID
	 */
	public String getPlateID() {
		return plateID;
	}


	/**
	 * @param plateID
	 *            the plateID to set
	 */
	public void setPlateID(String plateID) {
		this.plateID = plateID;
	}


	/**
	 * @return the plateX
	 */
	public int getPlateX() {
		return plateX;
	}


	/**
	 * @param plateX
	 *            the plateX to set
	 */
	public void setPlateX(int plateX) {
		this.plateX = plateX;
	}


	/**
	 * @return the plateY
	 */
	public int getPlateY() {
		return plateY;
	}


	/**
	 * @param plateY
	 *            the plateY to set
	 */
	public void setPlateY(int plateY) {
		this.plateY = plateY;
	}

}
