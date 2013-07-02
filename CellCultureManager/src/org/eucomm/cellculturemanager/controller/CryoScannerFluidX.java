package org.eucomm.cellculturemanager.controller;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eucomm.cellculturemanager.model.Plate;


public class CryoScannerFluidX implements CryoScanner {

	private static CryoScannerFluidX	scanner	= null;


	private CryoScannerFluidX() {

	}


	public static CryoScannerFluidX getInstance() {

		if (scanner == null) {
			scanner = new CryoScannerFluidX();
		}

		return scanner;

	}


	@Override
	public Plate scan(Plate plate) throws Exception {

		int x, y;

		// Fetch all barcodes from the plate
		String[][] plateBarcodes = plate.getBarcodes();

		// Scan the barcodes from the current plate on the scanner
		String[][] scannedBarcodes = scan();

		// Merge the barcodes
		if (plateBarcodes == null) {
			plateBarcodes = scannedBarcodes;
		}
		else {
			for (y = 0; y < Plate.ROWS; y++) {
				for (x = 0; x < Plate.COLUMNS; x++) {
					if ((plateBarcodes[y][x] == null || plateBarcodes[y][x].equals(Plate.EMPTY_TUBE_VALUE)) && scannedBarcodes[y][x] != null && !scannedBarcodes[y][x].equals(Plate.EMPTY_TUBE_VALUE)) {
						plateBarcodes[y][x] = scannedBarcodes[y][x];
					}
				}
			}
		}

		Plate newPlate = new Plate(plate.getTank(), plate.getRack(), plate.getShelf(), plate.getPlateID(), plate.getScanDate());
		newPlate.setRegions(plateBarcodes);

		return newPlate;

	}


	private String[][] scan() throws Exception {

		String[][] rack = new String[Plate.ROWS][Plate.COLUMNS];

		Socket echoSocket = null;
		PrintWriter out = null;
		BufferedReader in = null;

		try {

			// Establish the connection to the socket and the communication channels with the socket
			echoSocket = new Socket("localhost", 201);
			out = new PrintWriter(echoSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));

			out.println("set tube = nunc");
			out.println("get");
			out.println("minimise");
			String serverOutput;
			boolean reading = false;
			String[] split;
			int i, x, y;

			// Read the output from the scanner
			while ((serverOutput = in.readLine()) != null) {

				// If the first vial is reported, set the reading process.
				if (!reading && serverOutput.startsWith("A01")) {
					reading = true;
				}

				// If we are in the reading process, there are no more blank lines until the end of the rack.
				if (reading) {

					// Break up reading the response of the scanner
					if (serverOutput.isEmpty()) {
						break;
					}

					// Read the scanner response
					else {
						y = 0;
						x = 0;
						split = serverOutput.split(",");
						for (i = 0; i < Plate.rowNames.length; i++) {
							if (split[0].charAt(0) == Plate.rowNames[i]) {
								y = i;
								break;
							}
						}
						x = Integer.parseInt(split[0].substring(1)) - 1;
						rack[y][x] = split[1];
					}

				}

				// If we reach a blank line during the reading process, we finished reading.
				else if (reading && serverOutput.isEmpty()) {
					break;
				}
			}
			out.close();
			in.close();
			echoSocket.close();

		}
		catch (ConnectException e) {
			LogWriter.writeLog(e);
			throw new Exception("The XTR-96 scanner software seems to be offline.\nPlease make sure that the software is running and try again.");
		}
		catch (UnknownHostException e) {
			LogWriter.writeLog(e);
			throw new Exception("Sorry, but I can't connect to the XTR-96 scanner.\nPlease contact your administrator.");

		}
		catch (IOException e) {
			LogWriter.writeLog(e);
			throw new Exception("Sorry, there was a problem communicating with the XTR-96 scanner.\nPlease contact your administrator.");
		}

		return rack;

	}

}
