package org.eucomm.cellculturemanager.controller;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.eucomm.cellculturemanager.model.Plate;


public class CryoScannerWorkbench implements CryoScanner {

	private static CryoScannerWorkbench	scanner				= null;

	private Socket						socketWb;
	private OutputStream				streamToWb;
	private InputStream					streamFromWb;

	public static final int				SCANNER_INITIALIZED	= 1;
	public static final int				SCANNER_SCANNING	= 2;
	public static final int				SCANNER_FINISHED	= 4;
	public static final int				SCANNER_DATA_READY	= 8;
	public static final int				SCANNER_DATA_SENT	= 16;
	public static final int				SCANNER_RACK96		= 32;
	public static final int				SCANNER_EMPTY		= 64;
	public static final int				SCANNER_ERROR		= 128;


	private CryoScannerWorkbench() {

	}


	public static CryoScannerWorkbench getInstance() {
		if (scanner == null) {
			scanner = new CryoScannerWorkbench();
		}
		return scanner;
	}


	@Override
	public Plate scan(Plate plate) throws Exception {

		Plate newPlate = new Plate(plate.getTank(), plate.getRack(), plate.getShelf(), plate.getPlateID(), plate.getScanDate());
		newPlate.setWorkbenchClones(plate.isWorkbenchClones());

		String plateId = null;
		String[][] scannedBarcodes = null;

		int row = 0;
		int col = 0;

		// Create the socket to communicate with the workbench scanner
		socketWb = new Socket("146.107.38.44", 8000);
		streamToWb = socketWb.getOutputStream();
		streamFromWb = socketWb.getInputStream();

		// Start scanning
		streamToWb.write(new String("S" + System.getProperty("line.separator")).getBytes());

		if (getResponseMessage().equals("OKS")) {

			// Wait while the scanner is scanning
			boolean scanningFinished = false;
			do {
				scanningFinished = false;
				streamToWb.write(new String("L" + System.getProperty("line.separator")).getBytes());
				String response = getResponseMessage();
				if (!response.isEmpty()) {
					Integer value = Integer.parseInt(response.replace("OKL", ""));
					if ((value & SCANNER_FINISHED) == SCANNER_FINISHED) {
						scanningFinished = true;
					}
					if ((value & SCANNER_ERROR) == SCANNER_ERROR) {
						throw new Exception("There was an error with the scanner. Please inform your administrator.");
					}
				}
			}
			while (!scanningFinished);

			// Fetch the rack id
			streamToWb.write(new String("B" + System.getProperty("line.separator")).getBytes());
			plateId = getResponseMessage().replace("OKB", "");
			if (plate.getPlateID() != null && plate.getPlateID().equals(CCM_Utils.shippingPlateID)) {
				plateId = CCM_Utils.shippingPlateID;
			}

			// Fetch the barcodes
			streamToWb.write(new String("D" + System.getProperty("line.separator")).getBytes());
			String[] responseData = getResponseMessage().replace("OKD", "").split(",");
			scannedBarcodes = new String[Plate.ROWS][Plate.COLUMNS];
			for (int i = 0; i < responseData.length; i++) {
				col = i % Plate.COLUMNS;
				if (i > 0 && col == 0) {
					row++;
				}
				scannedBarcodes[row][col] = responseData[i];
			}

		}
		else {
			throw new Exception("Sorry, there was a problem with the scanner. Please inform your administrator.");
		}

		// Close socket and depending strams
		streamFromWb.close();
		streamToWb.close();
		socketWb.close();

		if (plateId == null || plateId.isEmpty() || plateId.equals("No Read")) {
			throw new Exception("Sorry, I could not read the plate id. Please check the camera and try again.");
		}

		else if (plate.getPlateID() != null && !plate.getPlateID().equals(plateId)) {
			throw new Exception("Sorry, the scanned plate (" + plateId + ") does not match the requested plate (" + plate.getPlateID() + "). Please check if you have the correct plate.");
		}

		else if (DatabaseProvider.getInstance().checkPlate(plateId) != null) {
			int[] position = DatabaseProvider.getInstance().checkPlate(plateId);
			throw new Exception("Sorry, the scanned plate (" + plateId + ") was already scanned and is stored at:\nTank: " + position[0] + "\nRack: " + position[1] + "\n" + position[2]);
		}

		String[][] plateBarcodes = plate.getBarcodes();

		if (plateBarcodes == null) {
			plateBarcodes = scannedBarcodes;
		}
		else {
			for (row = 0; row < Plate.ROWS; row++) {
				for (col = 0; col < Plate.COLUMNS; col++) {
					if (plateBarcodes[row][col] == null || plateBarcodes[row][col].equals(Plate.EMPTY_TUBE_VALUE)) {
						plateBarcodes[row][col] = scannedBarcodes[row][col];
					}
				}
			}
		}

		newPlate.setPlateID(plateId);
		newPlate.setRegions(plateBarcodes);

		return newPlate;

	}


	private String getResponseMessage() throws InterruptedException, IOException {
		String response = "";
		int availableTry = 0;
		do {
			Thread.sleep(500);
			availableTry++;
			int available = streamFromWb.available();
			if (available > 0) {
				availableTry = 4;
				byte[] streamRead = new byte[available];
				streamFromWb.read(streamRead, 0, available);
				response += new String(streamRead);
			}
		}
		while (availableTry < 5);
		return response;
	}

}
