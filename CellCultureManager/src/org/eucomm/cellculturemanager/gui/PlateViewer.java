package org.eucomm.cellculturemanager.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import org.eucomm.cellculturemanager.controller.CCM_Utils;
import org.eucomm.cellculturemanager.controller.DatabaseProvider;
import org.eucomm.cellculturemanager.controller.PlatePrinter;
import org.eucomm.cellculturemanager.model.Barcode;
import org.eucomm.cellculturemanager.model.Clone;
import org.eucomm.cellculturemanager.model.Plate;


public class PlateViewer implements FrameDisabler {

	private static PlateViewer		plateScanResult	= null;

	private JLabel					labelPositionRight;
	private JLabel					labelBarcodeRight;
	private JLabel					labelCloneIDRight;
	private JLabel					labelGeneNameRight;
	private JLabel					labelThawedRight;
	private JLabel					labelTaCellRight;
	private JLabel					labelTaMolRight;
	private JLabel					errorLabel;

	private JDialog					dialog;
	private JLabel					highlightPanel;

	private List<String>			warningMessages;
	private List<String>			errorMessages;

	private Plate					plate;

	private Window					baseFrame;
	private CellCultureManagerGui	ccManager;


	/**
	 * Empty Constructor
	 */
	private PlateViewer() {

	}


	/**
	 * Return an instance of PlateScanResult.
	 * 
	 * @return
	 */
	public static PlateViewer getInstance() {
		if (plateScanResult == null) {
			plateScanResult = new PlateViewer();
		}
		return plateScanResult;
	}


	/**
	 * Create a new Dialog that displays the regions of the plate and references them to the barcodes, clones etc.
	 * 
	 * @param plate
	 * @param baseFrame
	 * @param ccManager
	 */
	public void showPlate(Plate plate, Window baseFrame, CellCultureManagerGui ccManager) {

		this.plate = plate;
		this.baseFrame = baseFrame;
		this.ccManager = ccManager;

		dialog = new JDialog(baseFrame, "Plate Viewer");
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.addWindowListener(new FrameDisableListener(baseFrame, this));
		dialog.setResizable(false);

		refreshView();

		dialog.setVisible(true);

		if (errorMessages.size() > 0) {
			JOptionPane.showMessageDialog(dialog, "Some errors occured.\nYou must correct all the errors before you can save the plate.", "Errors On The Plate", JOptionPane.ERROR_MESSAGE);
		}

	}


	public void refreshView(Plate plate) {
		this.plate = plate;
		refreshView();
	}


	public void refreshView() {

		// Remove all components from the content pane
		dialog.getContentPane().removeAll();

		getErrorMessages();

		// Create the components
		if (errorMessages.size() > 0 || warningMessages.size() > 0) {
			dialog.getContentPane().add(getErrorPanel(), BorderLayout.PAGE_START);
		}
		dialog.getContentPane().add(getPlatePanel(), BorderLayout.CENTER);
		dialog.getContentPane().add(getInfoPanel(), BorderLayout.LINE_END);

		// Repaint the dialog to show new regions
		dialog.repaint();

		// Set the size and the position of the dialog
		dialog.pack();
		dialog.setLocationRelativeTo(baseFrame);
		dialog.setVisible(true);

	}


	public void getErrorMessages() {

		errorMessages = new ArrayList<String>();
		warningMessages = new ArrayList<String>();

		if (plate.getScanDate() == null) {

			int x, y;

			// Check for errors in the regions
			for (x = 0; x < plate.getRegions().size(); x++) {

				// check if a region is not correct
				if (plate.getRegions().get(x).isUnknownBarcode()) {
					errorMessages.add("There is an unknown barcode at " + getPosition(plate.getRegions().get(x).getBarcodes().get(0).getPlateX() + 1, plate.getRegions().get(x).getBarcodes().get(0).getPlateY()) + ".");
				}
				if (plate.getRegions().get(x).isUnknownClone()) {
					errorMessages.add("There is an unknown clone at " + getPosition(plate.getRegions().get(x).getBarcodes().get(0).getPlateX() + 1, plate.getRegions().get(x).getBarcodes().get(0).getPlateY()) + ".");
				}

				else if (plate.getRegions().get(x).getClone() != null) {

					boolean regionDuplicateError = false;

					// check if another region has the same clone as the current one
					for (y = x + 1; y < plate.getRegions().size(); y++) {
						if (plate.getRegions().get(y).getClone() != null && plate.getRegions().get(y).getClone().getDatabaseID() == plate.getRegions().get(x).getClone().getDatabaseID()) {
							errorMessages.add("There are two recognized regions of the clone ID \"" + plate.getRegions().get(x).getClone().getCloneID() + "\" at " + getPosition(plate.getRegions().get(x).getBarcodes().get(0).getPlateX() + 1, plate.getRegions().get(x).getBarcodes().get(0).getPlateY()) + "-" + getPosition(plate.getRegions().get(x).getBarcodes().get(plate.getRegions().get(x).getBarcodes().size() - 1).getPlateX() + 1, plate.getRegions().get(x).getBarcodes().get(plate.getRegions().get(x).getBarcodes().size() - 1).getPlateY()) + " and " + getPosition(plate.getRegions().get(y).getBarcodes().get(0).getPlateX() + 1, plate.getRegions().get(y).getBarcodes().get(0).getPlateY()) + "-" + getPosition(plate.getRegions().get(y).getBarcodes().get(plate.getRegions().get(y).getBarcodes().size() - 1).getPlateX() + 1, plate.getRegions().get(y).getBarcodes().get(plate.getRegions().get(y).getBarcodes().size() - 1).getPlateY()) + ".");
							regionDuplicateError = true;
						}
					}

					// Check the number of barcodes for each clone / region
					if (regionDuplicateError == false && plate.getRegions().get(x).getBarcodeNumber() != plate.getRegions().get(x).getClone().getVialNumber()) {
						warningMessages.add(plate.getRegions().get(x).getClone().getVialNumber() + " vials have been scanned for the clone ID \"" + plate.getRegions().get(x).getClone().getCloneID() + "\", but the region at " + getPosition(plate.getRegions().get(x).getBarcodes().get(0).getPlateX() + 1, plate.getRegions().get(x).getBarcodes().get(0).getPlateY()) + "-" + getPosition(plate.getRegions().get(x).getBarcodes().get(plate.getRegions().get(x).getBarcodes().size() - 1).getPlateX() + 1, plate.getRegions().get(x).getBarcodes().get(plate.getRegions().get(x).getBarcodes().size() - 1).getPlateY()) + " contains only " + plate.getRegions().get(x).getBarcodeNumber() + " barcodes.");
					}

				}

			}

		}
	}


	/**
	 * Create the panel that contains the information about the clones on the plate.
	 * 
	 * @return
	 */
	public JLayeredPane getPlatePanel() {

		int panelWidth = 13 * PlateRegion.gridWidth;
		int panelHeight = 9 * PlateRegion.gridHeight;

		int x, y;

		// Create the layered pane
		JLayeredPane platePanel = new JLayeredPane();
		platePanel.setLayout(null);
		platePanel.setMinimumSize(new Dimension(panelWidth, panelHeight));
		platePanel.setPreferredSize(new Dimension(panelWidth, panelHeight));
		platePanel.setSize(panelWidth, panelHeight);
		platePanel.setBackground(PlateRegion.backgroundSurrounding);
		platePanel.setOpaque(true);

		JLabel label;

		// Create the horizontal plate indices (1 - 12)
		for (x = 0; x < 12; x++) {
			label = new JLabel(Integer.toString(x + 1));
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setVerticalAlignment(SwingConstants.CENTER);
			label.setOpaque(true);
			label.setBackground(PlateRegion.backgroundSurrounding);
			label.setBounds((x + 1) * PlateRegion.gridWidth, 0, PlateRegion.gridWidth, PlateRegion.gridWidth);
			label.setBorder(new LineBorder(PlateRegion.brightBorderSurrounding));
			platePanel.add(label);
		}

		// Create the vertical plate indices (A - H)
		for (y = 0; y < 8; y++) {
			label = new JLabel(Plate.rowNames[y] + "");
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setVerticalAlignment(SwingConstants.CENTER);
			label.setOpaque(true);
			label.setBackground(PlateRegion.backgroundSurrounding);
			label.setBounds(0, (y + 1) * PlateRegion.gridWidth, PlateRegion.gridWidth, PlateRegion.gridWidth);
			label.setBorder(new LineBorder(PlateRegion.brightBorderSurrounding));
			platePanel.add(label);
		}

		// Draw the regions on the panel
		for (int i = 0; i < plate.getRegions().size(); i++) {
			plate.getRegions().get(i).drawUI(platePanel);
		}

		// Add the highlight Label
		highlightPanel = new JLabel("");
		highlightPanel.setOpaque(false);
		highlightPanel.setBorder(new LineBorder(Color.YELLOW, 2));
		highlightPanel.setVisible(false);
		platePanel.add(highlightPanel, new Integer(PlateRegion.LAYER_CLICKABLE));
		platePanel.addMouseListener(new PlateClick());

		return platePanel;

	}


	/**
	 * Create the info panel that contains the plate position and information about the vials.
	 * 
	 * @return
	 */
	public Box getInfoPanel() {

		Box infoPanel = Box.createVerticalBox();
		infoPanel.setBorder(new CompoundBorder(new MatteBorder(0, 1, 0, 0, Color.BLACK), new EmptyBorder(10, 10, 10, 10)));
		infoPanel.setPreferredSize(new Dimension(160, 10));

		JLabel plateLabel = new JLabel(plate.getPlateID().equals(CCM_Utils.shippingPlateID) ? " " : "Plate: " + plate.getPlateID());
		plateLabel.setFont(CCM_Utils.getFont(dialog).deriveFont(12f));
		infoPanel.add(plateLabel);

		JLabel tankLabel = new JLabel(plate.getPlateID().equals(CCM_Utils.shippingPlateID) ? " " : "Tank:  " + plate.getTank());
		tankLabel.setFont(CCM_Utils.getFont(dialog).deriveFont(12f));
		infoPanel.add(tankLabel);

		JLabel rackLabel = new JLabel(plate.getPlateID().equals(CCM_Utils.shippingPlateID) ? " " : "Rack:  " + plate.getRack());
		rackLabel.setFont(CCM_Utils.getFont(dialog).deriveFont(12f));
		infoPanel.add(rackLabel);

		JLabel shelfLabel = new JLabel(plate.getPlateID().equals(CCM_Utils.shippingPlateID) ? " " : "Shelf: " + plate.getShelf());
		shelfLabel.setFont(CCM_Utils.getFont(dialog).deriveFont(12f));
		infoPanel.add(shelfLabel);

		infoPanel.add(Box.createVerticalStrut(20));

		JLabel infoLabel = new JLabel("Position Details:");
		infoLabel.setFont(infoLabel.getFont().deriveFont(15f).deriveFont(Font.BOLD));
		infoPanel.add(infoLabel);
		infoLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		infoLabel.setAlignmentY(JComponent.LEFT_ALIGNMENT);

		infoPanel.add(Box.createVerticalStrut(5));

		JLabel labelPositionLeft = new JLabel("Position:");
		labelPositionRight = new JLabel(" ");
		labelPositionRight.setFont(labelPositionRight.getFont().deriveFont(Font.PLAIN));
		infoPanel.add(labelPositionLeft);
		infoPanel.add(labelPositionRight);

		infoPanel.add(Box.createVerticalStrut(5));

		JLabel labelBarcodeLeft = new JLabel("Barcode:");
		labelBarcodeRight = new JLabel(" ");
		labelBarcodeRight.setFont(labelBarcodeRight.getFont().deriveFont(Font.PLAIN));
		infoPanel.add(labelBarcodeLeft);
		infoPanel.add(labelBarcodeRight);

		infoPanel.add(Box.createVerticalStrut(5));

		JLabel labelCloneIDLeft = new JLabel("Clone ID:");
		labelCloneIDRight = new JLabel(" ");
		labelCloneIDRight.setFont(labelCloneIDRight.getFont().deriveFont(Font.PLAIN));
		infoPanel.add(labelCloneIDLeft);
		infoPanel.add(labelCloneIDRight);

		infoPanel.add(Box.createVerticalStrut(5));

		JLabel labelGeneNameLeft = new JLabel("Gene Name:");
		labelGeneNameRight = new JLabel(" ");
		labelGeneNameRight.setFont(labelGeneNameRight.getFont().deriveFont(Font.PLAIN));
		infoPanel.add(labelGeneNameLeft);
		infoPanel.add(labelGeneNameRight);

		infoPanel.add(Box.createVerticalStrut(5));

		JLabel labelThawedLeft = new JLabel("Thaw Date:");
		labelThawedRight = new JLabel(" ");
		labelThawedRight.setFont(labelThawedRight.getFont().deriveFont(Font.PLAIN));
		infoPanel.add(labelThawedLeft);
		infoPanel.add(labelThawedRight);

		infoPanel.add(Box.createVerticalStrut(5));

		JLabel labelTaCellLeft = new JLabel("TA Cell:");
		labelTaCellRight = new JLabel(" ");
		labelTaCellRight.setFont(labelTaCellRight.getFont().deriveFont(Font.PLAIN));
		infoPanel.add(labelTaCellLeft);
		infoPanel.add(labelTaCellRight);

		infoPanel.add(Box.createVerticalStrut(5));

		JLabel labelTaMolLeft = new JLabel("TA Mol:");
		labelTaMolRight = new JLabel(" ");
		labelTaMolRight.setFont(labelTaMolRight.getFont().deriveFont(Font.PLAIN));
		infoPanel.add(labelTaMolLeft);
		infoPanel.add(labelTaMolRight);

		infoPanel.add(Box.createVerticalStrut(20));

		errorLabel = new JLabel(" ");
		errorLabel.setForeground(new Color(192, 0, 0));
		infoPanel.add(errorLabel);

		infoPanel.add(Box.createVerticalStrut(25));

		// Add the rescan button
		if (plate.getScanDate() == null || plate.getPlateID().equals(CCM_Utils.shippingPlateID) || plate.isWorkbenchClones()) {
			JButton rescanButton = new JButton("rescan");
			rescanButton.addActionListener(new RescanListener());
			rescanButton.setMinimumSize(new Dimension(120, 30));
			rescanButton.setMaximumSize(new Dimension(120, 30));
			infoPanel.add(rescanButton);
			infoPanel.add(Box.createVerticalStrut(10));
		}

		// Insert the save / print button only when no errors occurred
		if (errorMessages.size() == 0) {
			JButton savePrintButton = new JButton();
			savePrintButton.setMinimumSize(new Dimension(120, 30));
			savePrintButton.setMaximumSize(new Dimension(120, 30));
			if (plate.getScanDate() == null) {
				savePrintButton.setText("save");
				savePrintButton.addActionListener(new SaveListener());
			}
			else if (plate.getPlateID().equals(CCM_Utils.shippingPlateID)) {
				savePrintButton.setText("save shippings");
				savePrintButton.addActionListener(new SaveShippingListener());
			}
			else if (plate.isWorkbenchClones()) {
				savePrintButton.setText("save clones");
				savePrintButton.addActionListener(new SaveWorkbenchClones());
			}
			else {
				savePrintButton.setText("print");
				savePrintButton.addActionListener(new PrintListener());
			}
			infoPanel.add(savePrintButton);
		}

		return infoPanel;

	}


	/**
	 * Create the error panel that contains errors and warnings.
	 * 
	 * @return
	 */
	private Box getErrorPanel() {

		Box messageBox = Box.createVerticalBox();
		messageBox.setBorder(new CompoundBorder(new MatteBorder(0, 0, 2, 0, Color.BLACK), new EmptyBorder(10, 10, 10, 10)));

		if (errorMessages.size() > 0) {
			JButton errorButton = new JButton("show all errors");
			errorButton.addActionListener(new ShowMessageListener("Error Messages", errorMessages));
			Box errorBox = Box.createHorizontalBox();
			errorBox.setBorder(new CompoundBorder(new LineBorder(new Color(192, 0, 0), 3), new EmptyBorder(10, 10, 10, 10)));
			errorBox.add(new JLabel("Some error occured during the analysis. You must correct all errors before you can save the plate."));
			errorBox.add(Box.createHorizontalGlue());
			errorBox.add(errorButton);
			messageBox.add(errorBox);
		}
		if (errorMessages.size() > 0 && warningMessages.size() > 0) {
			messageBox.add(Box.createVerticalStrut(10));
		}
		if (warningMessages.size() > 0) {
			JButton warningButton = new JButton("show all warnings");
			warningButton.addActionListener(new ShowMessageListener("Warning Messages", warningMessages));
			Box warningBox = Box.createHorizontalBox();
			warningBox.setBorder(new CompoundBorder(new LineBorder(new Color(255, 153, 0), 3), new EmptyBorder(10, 10, 10, 10)));
			warningBox.add(new JLabel("The analysis of the plate produced some warnings. Please check if something went wrong."));
			warningBox.add(Box.createHorizontalGlue());
			warningBox.add(warningButton);
			messageBox.add(warningBox);
		}
		return messageBox;

	}


	/**
	 * Return the human readable version of a rack position.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private String getPosition(int x, int y) {
		return String.format("%1s%02d", Plate.rowNames[y], x);
	}

	/**
	 * MouseListener to handle the events of somebody clicks on a rack position.
	 * 
	 * @author Joachim.Beig
	 * 
	 */
	private class PlateClick implements MouseListener {

		private Date	pressed		= null;
		private Date	released	= null;

		private Date	click		= null;


		@Override
		public void mouseClicked(MouseEvent e) {
			// This function is not used because the touch screen does not fire a click event
		}


		@Override
		public void mouseEntered(MouseEvent e) {
			// This function is not necessary
		}


		@Override
		public void mouseExited(MouseEvent e) {
			// This function is not necessary
		}


		@Override
		public void mousePressed(MouseEvent e) {
			pressed = new Date();
		}


		@Override
		public void mouseReleased(MouseEvent e) {
			released = new Date();
			if (pressed != null && released != null && released.getTime() - pressed.getTime() < 250) {
				clickEvent(e);
			}
			pressed = null;
			released = null;
		}


		private void clickEvent(MouseEvent e) {

			int x = (int) Math.floor((double) e.getX() / PlateRegion.gridWidth);
			int y = (int) Math.floor((double) e.getY() / PlateRegion.gridHeight);

			boolean doubleClick = (click != null && new Date().getTime() - click.getTime() < 250);
			click = new Date();

			if (e.getButton() == 1) {

				// If we click just once, set highlight a vial
				if (!doubleClick) {

					// Un-highlight a vial
					if (x == 0 || y == 0 || (highlightPanel.isVisible() && highlightPanel.getBounds().x == x * PlateRegion.gridWidth && highlightPanel.getBounds().y == y * PlateRegion.gridHeight)) {

						highlightPanel.setVisible(false);

						labelPositionRight.setText(" ");
						labelBarcodeRight.setText(" ");
						labelCloneIDRight.setText(" ");
						labelGeneNameRight.setText(" ");
						labelThawedRight.setText(" ");
						labelTaCellRight.setText(" ");
						labelTaMolRight.setText(" ");

						errorLabel.setText(" ");

					}

					// Highlight a vial
					else {

						highlightPanel.setBounds(x * PlateRegion.gridWidth, y * PlateRegion.gridHeight, PlateRegion.gridWidth, PlateRegion.gridHeight);
						highlightPanel.setVisible(true);

						labelPositionRight.setText(getPosition(x, y - 1));
						labelBarcodeRight.setText(plate.getBarcodeAt(x - 1, y - 1) != Plate.EMPTY_TUBE_VALUE ? plate.getBarcodeAt(x - 1, y - 1) : "---");
						labelCloneIDRight.setText(plate.getCloneAt(x - 1, y - 1) != null ? plate.getCloneAt(x - 1, y - 1).getCloneID() : "---");
						labelGeneNameRight.setText(plate.getCloneAt(x - 1, y - 1) != null ? plate.getCloneAt(x - 1, y - 1).getGeneName() : "---");
						labelThawedRight.setText(plate.getCloneAt(x - 1, y - 1) != null ? plate.getCloneAt(x - 1, y - 1).getThawDateString() : "---");
						labelTaCellRight.setText(plate.getCloneAt(x - 1, y - 1) != null ? plate.getCloneAt(x - 1, y - 1).getTechnicianThawing() : "---");
						labelTaMolRight.setText(plate.getCloneAt(x - 1, y - 1) != null ? plate.getCloneAt(x - 1, y - 1).getTechnicianQC() : "---");

						if (plate.getRegionAt(x - 1, y - 1).isUnknownBarcode()) {
							errorLabel.setText("Barcode Is Unknown");
						}
						else if (plate.getRegionAt(x - 1, y - 1).isUnknownClone()) {
							errorLabel.setText("No clone found for the barcode");
						}
						else if (plate.getRegionAt(x - 1, y - 1).isTooSmall()) {
							errorLabel.setText("Too few barcodes");
						}
						else {
							errorLabel.setText(" ");
						}

					}

				}

				// On double-click, show a dialog
				else {

					// If there is no clone but a barcode, show the clone selection dialog
					if (!plate.getBarcodeAt(x - 1, y - 1).equals(Plate.EMPTY_TUBE_VALUE)) {
						PlateVialSelection.getInstance().showCloneSelection(dialog, PlateViewer.this, plate, x - 1, y - 1);
					}

					// If there is an empty barcode, show the barcode selection dialog
					else if (plate.getScanDate() == null) {
						PlateVialSelection.getInstance().showBarcodeSelection(dialog, PlateViewer.this, plate, x - 1, y - 1);
					}

				}

			}

		}

	}

	/**
	 * Listener to show a message
	 * 
	 * @author Joachim.Beig
	 * 
	 */
	private class ShowMessageListener implements ActionListener {

		private String			windowTitle;
		private List<String>	messages;


		public ShowMessageListener(String windowTitle, List<String> messages) {
			this.windowTitle = windowTitle;
			this.messages = messages;
		}


		@Override
		public void actionPerformed(ActionEvent e) {

			JPanel messagePanel = new JPanel();
			messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.PAGE_AXIS));
			messagePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

			for (int i = 0; i < messages.size(); i++) {
				messagePanel.add(new JLabel(messages.get(i)));
			}

			JScrollPane scrollPane = new JScrollPane(messagePanel);
			scrollPane.setPreferredSize(new Dimension(500, 200));
			scrollPane.setSize(new Dimension(500, 200));

			JDialog messageDialog = new JDialog(dialog, windowTitle);
			messageDialog.add(scrollPane, BorderLayout.CENTER);
			messageDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			messageDialog.setResizable(false);
			messageDialog.pack();
			messageDialog.setVisible(true);

		}

	}

	/**
	 * ActionListener to handle the save event.
	 * 
	 * @author Joachim.Beig
	 * 
	 */
	private class SaveListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if (DatabaseProvider.getInstance().savePlate(plate)) {

				// Save the frozen state and cleanup the barcodes
				for (int i = 0; i < plate.getRegions().size(); i++) {
					if (plate.getRegions().get(i).getClone() != null) {
						Barcode[] barcodes = DatabaseProvider.getInstance().getBarcodes(plate.getRegions().get(i).getClone().getDatabaseID());
						for (int j = 0; j < barcodes.length; j++) {
							if (barcodes[j].getPlateID().isEmpty()) {
								DatabaseProvider.getInstance().removeBarcode("unused vial", barcodes[j].getCode());
							}
						}
						plate.getRegions().get(i).getClone().addStatus(Clone.FROZEN);
					}
				}

				plate.setScanDate(new GregorianCalendar());

				ccManager.updateCloneTable();
				JOptionPane.showMessageDialog(dialog, "The plate was saved successful.\n\nPlease store it into the following position:\nTank: " + plate.getTank() + "\nRack: " + plate.getRack() + "\nShelf: " + plate.getShelf(), "Plate Saved", JOptionPane.INFORMATION_MESSAGE);
				PlatePrinter.getInstance().printPlate(plate);
				dialog.dispose();
			}

			else {
				JOptionPane.showMessageDialog(dialog, "There was a problem saving your plate at the given position.\nMaybe somebody took this position before you.", "Saving Failed", JOptionPane.ERROR_MESSAGE);
			}

		}

	}

	/**
	 * ActionListener to handle the print event.
	 * 
	 * @author Joachim.Beig
	 * 
	 */
	private class PrintListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			PlatePrinter.getInstance().printPlate(plate);
		}

	}

	/**
	 * ActionListener to handle the rescan event.
	 * 
	 * @author Joachim.Beig
	 * 
	 */
	private class RescanListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				refreshView(CCM_Utils.getScannerInstance().scan(plate));
			}
			catch (Exception exception) {
				JOptionPane.showMessageDialog(null, exception.getMessage(), exception.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	private class SaveShippingListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			String[][] barcodes = plate.getBarcodes();
			int x, y;

			for (y = 0; y < Plate.ROWS; y++) {
				for (x = 0; x < Plate.COLUMNS; x++) {
					if (!barcodes[y][x].equals(Plate.EMPTY_TUBE_VALUE)) {
						DatabaseProvider.getInstance().removeBarcode(CCM_Utils.shippingReason, barcodes[y][x]);
					}
				}
			}

			JOptionPane.showMessageDialog(dialog, "Thank you for shipping the clones :-)", "Finished", JOptionPane.INFORMATION_MESSAGE);
			dialog.dispose();

		}

	}

	private class SaveWorkbenchClones implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			for (Clone clone : plate.getClones()) {
				clone.addStatus(Clone.CRYO_BENCH);
				DatabaseProvider.getInstance().updateCloneData("status", Integer.toString(clone.getStatus()), clone.getDatabaseID());
			}
			JOptionPane.showMessageDialog(dialog, "Thank you!\nThe clones are now marked as workbench clones.", "Clones Saved", JOptionPane.INFORMATION_MESSAGE);
			dialog.dispose();
		}

	}


	@Override
	public boolean disableBaseFrame() {
		return true;
	}


	@Override
	public boolean enableBaseFrame() {
		return true;
	}

}
