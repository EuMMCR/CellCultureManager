package org.eucomm.cellculturemanager.controller;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eucomm.cellculturemanager.model.Barcode;
import org.eucomm.cellculturemanager.model.Clone;
import org.eucomm.cellculturemanager.model.Plate;


public class DatabaseProvider {

	private static DatabaseProvider	provider;

	private Connection				connection;

	private Date					lastOperation;
	private long					timeout	= 60 * 1000;


	/**
	 * Construct a new {@link DatabaseProvider} object.
	 */
	private DatabaseProvider() {
		refreshConnection();
	}


	/**
	 * Get the instance of the {@link DatabaseProvider}
	 * 
	 * @return
	 */
	public static DatabaseProvider getInstance() {
		if (provider == null) {
			provider = new DatabaseProvider();
		}
		return provider;
	}


	/**
	 * Fetch a list of clones for the cell culture. Usually, the clones with a thawing date in the past four weeks are
	 * fetched.
	 * <p>
	 * If <i>cellTA</i> is given, only clones are fetched that are assigned to the cell culture technician with this
	 * name.
	 * <p>
	 * If <i>moleTA</i> is given, only clones are fetched that are assigned to the molecular biology technician with
	 * this name.
	 * <p>
	 * If <i>searchTerm</i> is given, all clones are fetched that matches the search term in the clone ID or the gene
	 * name, no matter when they were thawed.
	 * <p>
	 * If both <i>taName</i> and <i>searchTerm</i> are given, both filters are applied on the list.
	 * 
	 * @param cellTA
	 *            the name of the cell culture technician
	 * @param moleTA
	 *            the name of the molecular biology technician who gets the DNA of the clone
	 * @param searchTerm
	 *            the search term to filter clones
	 * @return a list of clones
	 */
	public List<Clone> getThawedClones(String cellTA, String moleTA, String searchTerm) {

		refreshConnection();

		List<Clone> clones = new ArrayList<Clone>();

		PreparedStatement statement = null;
		ResultSet result = null;

		try {
			String query = "select `id`, `cloneID`, `freezingPlateID`, `cellLine`, `geneName`, `technicianThawing`, `technicianQC`, `thawingDate`, `status`, (select count(*) from `barcodes` where `thawedCloneRow` = `thawedClones`.`id`) as `vials` from `thawedClones` where 1 ";
			if (cellTA != null) {
				query += " and `technicianThawing` = ? ";
			}
			if (moleTA != null) {
				query += " and `technicianQC` = ? ";
			}
			if (!searchTerm.isEmpty()) {
				query += " and (`cloneID` like ? or `geneName` like ?) ";
			}
			else {
				query += " and `thawingDate` > date_sub(now(), interval 4 week) and `thawingDate` <= now() ";
			}
			query += " order by `thawingDate` desc, `geneName` asc";
			statement = connection.prepareStatement(query);
			int paramCount = 0;
			if (cellTA != null) {
				statement.setString(++paramCount, cellTA);
			}
			if (moleTA != null) {
				statement.setString(++paramCount, moleTA);
			}
			if (!searchTerm.isEmpty()) {
				statement.setString(++paramCount, "%" + searchTerm + "%");
				statement.setString(++paramCount, "%" + searchTerm + "%");
			}
			result = statement.executeQuery();
			result.beforeFirst();
			while (result.next()) {

				Clone clone = new Clone(result.getInt("id"), result.getString("cloneID"), result.getString("freezingPlateID"), result.getString("cellLine"), result.getString("geneName"), result.getString("technicianThawing"), result.getString("technicianQC"), result.getDate("thawingDate"), result.getInt("status"), result.getInt("vials"));

				Barcode[] barcodes = getBarcodes(clone.getDatabaseID());
				clone.setBarcodes(barcodes);

				if (barcodes != null && barcodes.length > 0) {
					int[] platePosition = checkPlate(barcodes[0].getPlateID());
					if (platePosition != null) {
						clone.setTank(platePosition[0]);
						clone.setRack(platePosition[1]);
						clone.setShelf(platePosition[2]);
					}
					clone.setPosition(String.format("%s%02d-%s%02d", Plate.rowNames[barcodes[0].getPlateY()], barcodes[0].getPlateX() + 1, Plate.rowNames[barcodes[barcodes.length - 1].getPlateY()], barcodes[barcodes.length - 1].getPlateX() + 1));
				}

				clones.add(clone);

			}
			statement.close();
		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (result != null) {
				try {
					result.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return clones;

	}


	public boolean duplicateClone(int cloneID) {

		refreshConnection();

		int rowCount = 0;

		Statement statement = null;

		try {
			statement = connection.createStatement();
			rowCount = statement.executeUpdate("insert into `thawedClones` select null, " + cloneID + ", `technicianThawing`, `technicianQC`, `cloneID`, `freezingPlateID`, `geneName`, `cellLine`, `thawingDate`, `status` + " + Clone.DUPLICATE + " from `thawedClones` where `id` = " + cloneID);
			statement.close();
		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return rowCount == 1;

	}


	/**
	 * Retrieve all technicians that ever were in the cell culture
	 * 
	 * @return a list of names
	 */
	public String[] getTechniciansCell() {

		refreshConnection();

		String[] techniciansCell = null;

		Statement statement = null;
		ResultSet result = null;

		try {
			statement = connection.createStatement();
			result = statement.executeQuery("select distinct(`technicianThawing`) from `thawedClones` where `technicianThawing` != '' order by `technicianThawing` asc");
			if (result.last()) {
				techniciansCell = new String[result.getRow()];
				result.beforeFirst();
				while (result.next()) {
					techniciansCell[result.getRow() - 1] = result.getString(1);
				}
			}
		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (result != null) {
				try {
					result.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return techniciansCell;

	}


	/**
	 * Retrieve all technicians that ever were in the molecular biology
	 * 
	 * @return
	 */
	public String[] getTechniciansMole() {

		refreshConnection();

		String[] techniciansMole = null;

		Statement statement = null;
		ResultSet result = null;

		try {
			statement = connection.createStatement();
			result = statement.executeQuery("select distinct(`technicianQC`) from `thawedClones` where `technicianQC` != '' order by `technicianQC` asc");
			if (result.last()) {
				techniciansMole = new String[result.getRow()];
				result.beforeFirst();
				while (result.next()) {
					techniciansMole[result.getRow() - 1] = result.getString(1);
				}
			}
		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (result != null) {
				try {
					result.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return techniciansMole;

	}


	public String[] getThawedYears() {

		refreshConnection();

		String[] thawedYears = null;

		Statement statement = null;
		ResultSet result = null;

		try {
			statement = connection.createStatement();
			result = statement.executeQuery("select distinct(date_format(`thawingDate`, '%Y')) from `thawedClones` order by `thawingDate` desc");
			if (result.last()) {
				thawedYears = new String[result.getRow()];
				result.beforeFirst();
				while (result.next()) {
					thawedYears[result.getRow() - 1] = result.getString(1);
				}
			}
		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (result != null) {
				try {
					result.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return thawedYears;

	}


	/**
	 * Update the data of a thawed clone in the database.
	 * 
	 * @param field
	 *            the field name
	 * @param value
	 *            the new value
	 * @param databaseID
	 *            the ID of the dataset
	 */
	public void updateCloneData(String field, String value, int databaseID) {

		refreshConnection();

		PreparedStatement statement = null;

		try {
			statement = connection.prepareStatement("update `thawedClones` set `" + field + "` = ? where `id` = " + databaseID);
			statement.setString(1, value);
			statement.execute();
		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

	}


	/**
	 * Check if the barcode exists already in the database.
	 * 
	 * @param code
	 * @return the <b>Clone ID</b> if the barcode exists, <b>null</b> otherwise
	 */
	public String checkBarCode(String code) {

		refreshConnection();

		String cloneID = null;

		PreparedStatement statement = null;
		ResultSet result = null;

		try {
			statement = connection.prepareStatement("select `cloneID` from `barcodes` where `code` like ?");
			statement.setString(1, code);
			result = statement.executeQuery();
			if (result.first()) {
				cloneID = result.getString(1);
			}
		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (result != null) {
				try {
					result.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return cloneID;

	}


	/**
	 * Count the number of vials that are already scanned for this clone.
	 * 
	 * @return the number of existing barcodes
	 */
	public String[] getCloneVials(int databaseID) {

		refreshConnection();

		String[] codes = new String[0];

		PreparedStatement statement = null;
		ResultSet result = null;

		try {
			statement = connection.prepareStatement("select `code` from `barcodes` where `thawedCloneRow` = ?");
			statement.setInt(1, databaseID);
			result = statement.executeQuery();
			if (result.last()) {
				codes = new String[result.getRow()];
				result.beforeFirst();
				while (result.next()) {
					codes[result.getRow() - 1] = result.getString(1);
				}
			}
		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (result != null) {
				try {
					result.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return codes;

	}


	/**
	 * Save a list of scanned barcodes into the database.
	 * 
	 * @param cloneID
	 *            the ID of the clones in the vials
	 * @param codes
	 *            the list of barcodes
	 * @return <b>true</b> if the barcodes were saved, <b>false</b> if not
	 */
	public boolean saveBarcodes(String[] codes, String cloneID, int databaseID) {

		refreshConnection();

		boolean saved = false;

		PreparedStatement statement = null;
		ResultSet result = null;

		try {

			// Disable auto commit (start transaction)
			connection.setAutoCommit(false);

			// Prepare insertion statement
			statement = connection.prepareStatement("insert into `barcodes` (`code`, `cloneID`, `thawedCloneRow`, `scanDate`, `fileName`, `plateID`, `plateX`, `plateY`) values (?, ?, ?, now(), '', '', 0, 0)", Statement.RETURN_GENERATED_KEYS);

			// Insert all codes and count the number of successful inserts
			int insertCount = 0;
			for (int i = 0; i < codes.length; i++) {
				statement.setString(1, codes[i]);
				statement.setString(2, cloneID);
				statement.setInt(3, databaseID);
				statement.execute();
				result = statement.getGeneratedKeys();
				if (result.first()) {
					insertCount++;
				}
				else {
					break;
				}
			}

			// Check the number of successful inserts and commit or rollback the transaction
			if (insertCount == codes.length) {
				connection.commit();
				saved = true;
			}
			else {
				connection.rollback();
			}

		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			try {
				connection.setAutoCommit(true);
			}
			catch (SQLException e) {
				LogWriter.writeLog(e);
			}
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (result != null) {
				try {
					result.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return saved;

	}


	public boolean updateBarcodes(String[] barcodes, String cloneID, int databaseID) {

		refreshConnection();

		int i;
		int rowCount;
		int updateCount = 0;

		PreparedStatement statement = null;

		try {

			// Disable auto commit (start transaction)
			connection.setAutoCommit(false);

			// Prepare update statement
			statement = connection.prepareStatement("update `barcodes` set `cloneID` = ?, `thawedCloneRow` = ? where `code` = ?");

			// Update all barcodes
			for (i = 0; i < barcodes.length; i++) {

				statement.setString(1, cloneID);
				statement.setInt(2, databaseID);
				statement.setString(3, barcodes[i]);
				rowCount = statement.executeUpdate();

				if (rowCount > 0) {
					updateCount++;
				}
				else {
					break;
				}

			}

			// Check the number of successful inserts and commit or rollback the transaction
			if (updateCount == barcodes.length) {
				connection.commit();
			}
			else {
				connection.rollback();
			}

		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			try {
				connection.setAutoCommit(true);
			}
			catch (SQLException e) {
				LogWriter.writeLog(e);
			}
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return updateCount == barcodes.length;

	}


	/**
	 * Get the date of the last import of the thawed clones list and check if the list have to be imported. The list
	 * will be imported once a day.
	 * 
	 * @return
	 *         boolean
	 */
	public boolean haveToCheck() {

		refreshConnection();

		boolean haveToCheck = false;

		Statement statement = null;
		ResultSet result = null;

		try {
			statement = connection.createStatement();
			result = statement.executeQuery("select count(*) from `thawedCheck` where datediff(now(), `checkDate`) = 0");
			if (result.first()) {
				if (result.getInt(1) == 0) {
					haveToCheck = true;
					connection.createStatement().execute("insert into `thawedCheck` (`checkDate`) values (now())");
				}
			}
		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (result != null) {
				try {
					result.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return haveToCheck;

	}


	/**
	 * Fetch a Barcode from the database with all its information.
	 * 
	 * @param code
	 * @return the Barcode
	 */
	public Barcode getBarcode(String code) {

		refreshConnection();

		Barcode barcode = null;

		PreparedStatement statement = null;
		ResultSet result = null;

		try {
			statement = connection.prepareStatement("select `id`, `code`, `cloneID`, `thawedCloneRow`, `scanDate`, `plateID`, `plateX`, `plateY` from `barcodes` where `code` = ?");
			statement.setString(1, code);
			result = statement.executeQuery();
			if (result.first()) {
				barcode = new Barcode(result.getInt("id"), result.getString("code"), result.getString("cloneID"), result.getInt("thawedCloneRow"), result.getDate("scanDate"), result.getString("plateID"), result.getInt("plateX"), result.getInt("plateY"));
			}
		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (result != null) {
				try {
					result.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return barcode;

	}


	/**
	 * Fetch all Barcodes of a distinct clone's database ID
	 * 
	 * @param cloneID
	 * @return
	 */
	public Barcode[] getBarcodes(int cloneID) {

		refreshConnection();

		Barcode[] barcodes = null;

		PreparedStatement statement = null;
		ResultSet result = null;

		try {
			statement = connection.prepareStatement("select `id`, `code`, `cloneID`, `thawedCloneRow`, `scanDate`, `plateID`, `plateX`, `plateY` from `barcodes` where `thawedCloneRow` = ? order by `plateY` asc, `plateX` asc");
			statement.setInt(1, cloneID);
			result = statement.executeQuery();
			if (result.last()) {
				barcodes = new Barcode[result.getRow()];
				result.beforeFirst();
				while (result.next()) {
					barcodes[result.getRow() - 1] = new Barcode(result.getInt("id"), result.getString("code"), result.getString("cloneID"), result.getInt("thawedCloneRow"), result.getDate("scanDate"), result.getString("plateID"), result.getInt("plateX"), result.getInt("plateY"));
				}
			}
		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (result != null) {
				try {
					result.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return barcodes;

	}


	/**
	 * Fetch a Clone from the database with all its information.
	 * 
	 * @param databaseID
	 * @return the Clone
	 */
	public Clone getClone(int databaseID) {

		refreshConnection();

		Clone clone = null;

		PreparedStatement statement = null;
		ResultSet result = null;

		try {
			statement = connection.prepareStatement("select `id`, `cloneID`, `freezingPlateID`, `cellLine`, `geneName`, `technicianThawing`, `technicianQC`, `thawingDate`, `status`, (select count(*) from `barcodes` where `thawedCloneRow` = `thawedClones`.`id`) as `vials` from `thawedClones` where `id` = ?");
			statement.setInt(1, databaseID);
			result = statement.executeQuery();
			if (result.first()) {
				clone = new Clone(result.getInt("id"), result.getString("cloneID"), result.getString("freezingPlateID"), result.getString("cellLine"), result.getString("geneName"), result.getString("technicianThawing"), result.getString("technicianQC"), result.getDate("thawingDate"), result.getInt("status"), result.getInt("vials"));
			}
		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (result != null) {
				try {
					result.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return clone;

	}


	/**
	 * Return tank positions in the cryo stock.
	 * <p>
	 * If <code>used</code> is <b>true</b>, only tanks are returned that are not empty.
	 * <p>
	 * If <code>used</used> is <b>false</b>, only tanks are returned where empty positions are available.
	 * <p>
	 * If <code>used</used> is <b>null</b>, all tanks are returned.
	 * 
	 * @param used
	 *            determines what kind of tanks will be returned
	 * @return a list of tanks
	 */
	public int[] getTanks(Boolean used) {

		refreshConnection();

		int[] tanks = null;

		String query = "select distinct(`tank`) from `platePositions` ";
		if (used != null) {
			query += " where `plateID` is " + (used ? "not" : "") + " null ";
		}
		query += " order by `tank` asc";

		Statement statement = null;
		ResultSet result = null;

		try {
			statement = connection.createStatement();
			result = statement.executeQuery(query);
			if (result.last()) {
				tanks = new int[result.getRow()];
				result.beforeFirst();
				while (result.next()) {
					tanks[result.getRow() - 1] = result.getInt(1);
				}
			}
		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (result != null) {
				try {
					result.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return tanks;

	}


	/**
	 * Return rack positions in the cryo stock.
	 * <p>
	 * If <code>used</code> is <b>true</b>, only racks are returned that are not empty.
	 * <p>
	 * If <code>used</used> is <b>false</b>, only racks are returned where empty positions are available.
	 * <p>
	 * If <code>used</used> is <b>null</b>, all racks are returned.
	 * 
	 * @param tank
	 *            the tank of the shelf
	 * @param used
	 *            determines what kind of racks will be returned
	 * @return a list of racks
	 */
	public int[] getRacks(int tank, Boolean used) {

		refreshConnection();

		int[] racks = null;

		String query = "select distinct(`rack`) from `platePositions` where `tank` = " + tank + " ";
		if (used != null) {
			query += " and `plateID` is " + (used ? "not" : "") + " null ";
		}
		query += " order by `rack` asc";

		Statement statement = null;
		ResultSet result = null;

		try {
			statement = connection.createStatement();
			result = statement.executeQuery(query);
			if (result.last()) {
				racks = new int[result.getRow()];
				result.beforeFirst();
				while (result.next()) {
					racks[result.getRow() - 1] = result.getInt(1);
				}
			}
		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (result != null) {
				try {
					result.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return racks;

	}


	/**
	 * Return shelf positions in the cryo stock.
	 * <p>
	 * If <code>used</code> is <b>true</b>, only shelfs are returned that are not empty.
	 * <p>
	 * If <code>used</used> is <b>false</b>, only shelfs are returned that are empty.
	 * <p>
	 * If <code>used</used> is <b>null</b>, all shelfs are returned.
	 * 
	 * @param tank
	 *            the tank of the shelf
	 * @param rack
	 *            the rack of the shelf
	 * @param used
	 *            determines what kind of shelfs will be returned
	 * @return a list of shelfs
	 */
	public int[] getShelfs(int tank, int rack, Boolean used) {

		refreshConnection();

		int[] shelfs = null;

		String query = "select distinct(`shelf`) from `platePositions` where `tank` = " + tank + " and `rack` = " + rack + " ";
		if (used != null) {
			query += " and `plateID` is " + (used ? "not" : "") + " null ";
		}
		query += " order by `shelf` asc";

		Statement statement = null;
		ResultSet result = null;

		try {
			statement = connection.createStatement();
			result = statement.executeQuery(query);
			if (result.last()) {
				shelfs = new int[result.getRow()];
				result.beforeFirst();
				while (result.next()) {
					shelfs[result.getRow() - 1] = result.getInt(1);
				}
			}
		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (result != null) {
				try {
					result.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return shelfs;

	}


	public int[] checkPlate(String plateID) {

		refreshConnection();

		int[] platePosition = null;

		PreparedStatement statement = null;
		ResultSet result = null;

		try {
			statement = connection.prepareStatement("select `tank`, `rack`, `shelf` from `platePositions` where `plateID` = ?");
			statement.setString(1, plateID);
			result = statement.executeQuery();
			if (result.first()) {
				platePosition = new int[] { result.getInt(1), result.getInt(2), result.getInt(3) };
			}
		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (result != null) {
				try {
					result.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return platePosition;

	}


	/**
	 * Save the plate and the position on the plate of a barcode.
	 * 
	 * @param barcodeMatrix
	 * @param plateID
	 */
	public boolean setBarcodePlatePosition(String barcode, String plateID, int plateX, int plateY) {

		refreshConnection();

		PreparedStatement statement = null;
		ResultSet result = null;

		try {
			statement = connection.prepareStatement("update `barcodes` set `plateID` = ?, `plateX` = ?, `plateY` = ? where `code` = ?");
			statement.setString(1, plateID);
			statement.setInt(2, plateX);
			statement.setInt(3, plateY);
			statement.setString(4, barcode);
			if (statement.executeUpdate() > 0) {
				return true;
			}

		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (result != null) {
				try {
					result.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return false;

	}


	public Plate loadPlate(String plateID) {

		refreshConnection();

		Plate plate = null;

		PreparedStatement platePositions = null;
		PreparedStatement fetchVials = null;
		ResultSet result = null;

		try {
			platePositions = connection.prepareStatement("select * from `platePositions` where `plateID` = ?");
			platePositions.setString(1, plateID);
			result = platePositions.executeQuery();
			if (result.first()) {

				// Create a new plate
				plate = new Plate(result.getInt("tank"), result.getInt("rack"), result.getInt("shelf"), result.getString("plateID"), CCM_Utils.SQLDateToCalendar(result.getDate("saveDate")));
				result.close();
				
				// Fetch all barcodes for that plate
				fetchVials = connection.prepareStatement("select `code`, `plateX`, `plateY` from `barcodes` where `plateID` = ?");
				fetchVials.setString(1, plateID);
				String[][] barcodeMatrix = new String[8][12];
				result = fetchVials.executeQuery();
				result.beforeFirst();
				while (result.next()) {
					barcodeMatrix[result.getInt("plateY")][result.getInt("plateX")] = result.getString("code");
				}

				// Set the plate regions
				plate.setRegions(barcodeMatrix);

			}

		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (platePositions != null) {
				try {
					platePositions.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (fetchVials != null) {
				try {
					fetchVials.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (result != null) {
				try {
					result.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return plate;

	}


	public Plate loadPlate(int tank, int rack, int shelf) {

		refreshConnection();

		Plate plate = null;

		PreparedStatement statement = null;
		ResultSet result = null;

		try {

			statement = connection.prepareStatement("select `plateID` from `platePositions` where `tank` = ? and `rack` = ? and `shelf` = ?");
			statement.setInt(1, tank);
			statement.setInt(2, rack);
			statement.setInt(3, shelf);
			result = statement.executeQuery();

			if (result.first()) {
				plate = loadPlate(result.getString(1));
			}

		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (result != null) {
				try {
					result.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return plate;

	}


	public Plate loadPlate(Clone clone) {

		refreshConnection();

		Plate plate = null;

		PreparedStatement statement = null;
		ResultSet result = null;

		try {

			statement = connection.prepareStatement("select distinct(`plateID`) from `barcodes` where `thawedCloneRow` = ?");
			statement.setInt(1, clone.getDatabaseID());
			result = statement.executeQuery();

			if (result.first()) {
				plate = loadPlate(result.getString(1));
			}

		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
			if (result != null) {
				try {
					result.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return plate;

	}


	public boolean savePlate(Plate plate) {

		refreshConnection();

		PreparedStatement statement = null;

		try {

			connection.setAutoCommit(false);

			statement = connection.prepareStatement("update `platePositions` set `plateID` = ?, `saveDate` = now() where `tank` = ? and `rack` = ? and `shelf` = ? and `plateID` is null");
			statement.setString(1, plate.getPlateID());
			statement.setInt(2, plate.getTank());
			statement.setInt(3, plate.getRack());
			statement.setInt(4, plate.getShelf());
			int rows = statement.executeUpdate();

			boolean rollback = false;

			if (rows > 0) {

				int i, j;
				Barcode barcode;

				if (plate.getRegions() != null) {
					for (i = 0; i < plate.getRegions().size(); i++) {
						for (j = 0; j < plate.getRegions().get(i).getBarcodes().size(); j++) {
							barcode = plate.getRegions().get(i).getBarcodes().get(j);
							if (barcode.getCode() != null) {
								if (!setBarcodePlatePosition(barcode.getCode(), barcode.getPlateID(), barcode.getPlateX(), barcode.getPlateY())) {
									rollback = true;
									break;
								}
							}
						}
						if (rollback) {
							break;
						}
					}
				}

			}

			if (rollback) {
				connection.rollback();
			}
			else {
				connection.commit();
			}

			connection.setAutoCommit(true);
			return !rollback;

		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}
		finally {
			if (statement != null) {
				try {
					statement.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}
		}

		return false;

	}


	public boolean removeBarcode(String reason, String code) {

		refreshConnection();

		// Start the transaction
		try {
			connection.setAutoCommit(false);
		}
		catch (SQLException e) {
			LogWriter.writeLog(e);
		}

		// Initialize all required parameters
		boolean removed = false;
		String removeDate = CCM_Utils.getDate(null);

		PreparedStatement removeBarcode = null;
		PreparedStatement deleteBarcode = null;
		PreparedStatement removeUnknownBarcode = null;

		try {

			// Check if the barcode exists in the database
			if (checkBarCode(code) != null) {

				// Copy an existing barcode from the barcodes table to the removed barcodes table
				removeBarcode = connection.prepareStatement("insert into `barcodesRemoved` select null, ?, ?, `code`, `cloneID`, `thawedCloneRow`, `scanDate`, `plateID`, `plateX`, `plateY` from `barcodes` where `code` = ?");
				removeBarcode.setString(1, removeDate);
				removeBarcode.setString(2, reason);
				removeBarcode.setString(3, code);
				int rowCount = removeBarcode.executeUpdate();

				// Check if the barcode was inserted successfully
				if (rowCount > 0) {
					deleteBarcode = connection.prepareStatement("delete from `barcodes` where `code` like ?");
					deleteBarcode.setString(1, code);
					deleteBarcode.executeUpdate();

				}
			}

			// If the barcode does not exist already in the database, just insert it into the removed barcodes table
			else {
				removeUnknownBarcode = connection.prepareStatement("insert into `barcodesRemoved` (`removeDate`, `reason`, `code`, `cloneID`, `thawedCloneRow`, `scanDate`, `plateID`, `plateX`, `plateY`) values (?, ?, ?, '', 0, '0000-00-00 00:00:00', '', 0, 0)");
				removeUnknownBarcode.setString(1, removeDate);
				removeUnknownBarcode.setString(2, reason);
				removeUnknownBarcode.setString(3, code);
				removeUnknownBarcode.executeUpdate();
			}

			// Check if the barcode was deleted from the barcodes table
			removed = checkBarCode(code) == null;

		}

		catch (Exception e) {
			LogWriter.writeLog(e);
		}

		finally {

			if (removeBarcode != null) {
				try {
					removeBarcode.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}

			if (deleteBarcode != null) {
				try {
					deleteBarcode.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}

			if (removeUnknownBarcode != null) {
				try {
					removeUnknownBarcode.close();
				}
				catch (SQLException e) {
					LogWriter.writeLog(e);
				}
			}

			// Commit or rollback the transaction and close the transaction
			try {

				if (removed) {
					connection.commit();
				}
				else {
					connection.rollback();
				}

				connection.setAutoCommit(true);

			}
			catch (SQLException e) {
				LogWriter.writeLog(e);
			}
		}

		return removed;

	}


	/**
	 * Check if the last operation on the database was longer ago as the timeout variable. In this case, create a new
	 * connection.
	 */
	private void refreshConnection() {

		if (lastOperation == null || lastOperation.getTime() < new Date().getTime() - timeout) {

			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				connection = DriverManager.getConnection("jdbc:mysql://mysql5.helmholtz-muenchen.de:3306/eucomm_test?zeroDateTimeBehavior=convertToNull", "eucommadm", "e34#8aBr");
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally {
				if (connection == null) {
					System.err.println("Could not establish a SQL connection.");
					System.exit(0);
				}
			}

		}

		// Reset the last operation timestamp
		lastOperation = new Date();

	}

}
