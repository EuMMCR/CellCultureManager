package org.eucomm.cellculturemanager.gui;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.eucomm.cellculturemanager.controller.CCM_Utils;
import org.eucomm.cellculturemanager.controller.DatabaseProvider;
import org.eucomm.cellculturemanager.model.Barcode;
import org.eucomm.cellculturemanager.model.Clone;
import org.eucomm.cellculturemanager.model.Plate;


public class PlateVialSelection implements FrameDisabler {

	private static PlateVialSelection	plateCloneSelection;

	private JComboBox<Object>			clonePlateBox;
	private JComboBox<Object>			cloneListBox;
	private JTextField					cloneListFilter;
	private JComboBox<String>			removeReasonBox;

	private int							x;
	private int							y;
	private Plate						plate;

	private JDialog						dialog;
	private PlateViewer					plateViewer;


	private PlateVialSelection() {

	}


	public static PlateVialSelection getInstance() {
		if (plateCloneSelection == null) {
			plateCloneSelection = new PlateVialSelection();
		}
		return plateCloneSelection;
	}


	public void showCloneSelection(Window baseFrame, PlateViewer plateViewer, Plate plate, int x, int y) {

		this.plateViewer = plateViewer;
		this.plate = plate;
		this.x = x;
		this.y = y;

		List<Clone> plateClones = plate.getClones();
		Clone currentClone = plate.getCloneAt(x, y);

		JLabel selectCloneLabel = new JLabel("Select the correct clone:");
		selectCloneLabel.setFont(selectCloneLabel.getFont().deriveFont(15f));
		selectCloneLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel plateLabel = new JLabel("Select a clone from the current plate:");
		plateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		clonePlateBox = new JComboBox<Object>();
		clonePlateBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		clonePlateBox.addItem("-- not selected --");
		for (int i = 0; i < plateClones.size(); i++) {
			clonePlateBox.addItem(plateClones.get(i));
			if (currentClone != null && currentClone.getDatabaseID() == plateClones.get(i).getDatabaseID()) {
				clonePlateBox.setSelectedItem(plateClones.get(i));
			}
		}

		JLabel listLabel = new JLabel("Select a clone from the cell culture:");
		listLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel filterPanel = new JPanel();
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));
		filterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		cloneListFilter = new JTextField();
		JButton cloneListFilterButton = new JButton("filter");
		cloneListFilterButton.addActionListener(new CloneListFilterListener());
		filterPanel.add(cloneListFilter);
		filterPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		filterPanel.add(cloneListFilterButton);

		cloneListBox = new JComboBox<Object>();
		cloneListBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		updateCloneList();

		// Add the action listener to make sure that only one clone of the two lists can be selected
		clonePlateBox.addActionListener(new SelectCloneListener(clonePlateBox, cloneListBox));
		cloneListBox.addActionListener(new SelectCloneListener(cloneListBox, clonePlateBox));

		JButton saveCloneButton = new JButton("select and save");
		saveCloneButton.addActionListener(new SaveCloneListener());

		JButton cancelButton = new JButton("cancel");
		cancelButton.addActionListener(new CancelListener());

		JPanel saveClonePanel = new JPanel();
		saveClonePanel.setLayout(new BoxLayout(saveClonePanel, BoxLayout.X_AXIS));
		saveClonePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		saveClonePanel.add(saveCloneButton);
		saveClonePanel.add(Box.createHorizontalGlue());
		saveClonePanel.add(cancelButton);

		JPanel removeBarcodePanel = new JPanel();
		removeBarcodePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		removeBarcodePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JButton removeBarcodeButton = new JButton("remove vial");
		removeBarcodeButton.addActionListener(new RemoveVialListener());
		removeBarcodePanel.add(removeBarcodeButton);
		removeReasonBox = new JComboBox<String>(CCM_Utils.removeVialReasons);
		removeBarcodePanel.add(removeReasonBox);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		mainPanel.add(selectCloneLabel);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		mainPanel.add(plateLabel);
		mainPanel.add(clonePlateBox);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		mainPanel.add(listLabel);
		mainPanel.add(filterPanel);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 2)));
		mainPanel.add(cloneListBox);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		mainPanel.add(saveClonePanel);

		// Add the remove-vial function if the plate was scanned before
		if (plate.getScanDate() != null) {
			mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
			mainPanel.add(new JSeparator(JSeparator.HORIZONTAL));
			mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
			mainPanel.add(removeBarcodePanel);
		}

		dialog = new JDialog(baseFrame, "Select Your Clone");
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.addWindowListener(new FrameDisableListener(baseFrame, this));
		dialog.getContentPane().add(mainPanel, BorderLayout.CENTER);
		dialog.setResizable(false);
		dialog.pack();
		dialog.setVisible(true);
		dialog.setLocationRelativeTo(null);

	}


	public void showBarcodeSelection(Window baseFrame, PlateViewer plateViewer, Plate plate, int x, int y) {

		this.plateViewer = plateViewer;
		this.plate = plate;
		this.x = x;
		this.y = y;

		// Search for clones around the current position and add them to the combo box
		List<Clone> clones = new ArrayList<Clone>();
		boolean found = false;
		if (y > 0 && checkForUnusedBarcode(plate.getCloneAt(x, y - 1)) != null) {
			found = false;
			for (Clone clone : clones) {
				if (clone.getDatabaseID() == plate.getCloneAt(x, y - 1).getDatabaseID()) {
					found = true;
				}
			}
			if (!found) {
				clones.add(plate.getCloneAt(x, y - 1));
			}
		}
		if (x < Plate.COLUMNS - 1 && checkForUnusedBarcode(plate.getCloneAt(x + 1, y)) != null && !clones.contains(plate.getCloneAt(x + 1, y))) {
			found = false;
			for (Clone clone : clones) {
				if (clone.getDatabaseID() == plate.getCloneAt(x + 1, y).getDatabaseID()) {
					found = true;
				}
			}
			if (!found) {
				clones.add(plate.getCloneAt(x + 1, y));
			}
		}
		if (y < Plate.ROWS - 1 && checkForUnusedBarcode(plate.getCloneAt(x, y + 1)) != null && !clones.contains(plate.getCloneAt(x, y + 1))) {
			found = false;
			for (Clone clone : clones) {
				if (clone.getDatabaseID() == plate.getCloneAt(x, y + 1).getDatabaseID()) {
					found = true;
				}
			}
			if (!found) {
				clones.add(plate.getCloneAt(x, y + 1));
			}
		}
		if (x > 0 && checkForUnusedBarcode(plate.getCloneAt(x - 1, y)) != null && !clones.contains(plate.getCloneAt(x - 1, y))) {
			found = false;
			for (Clone clone : clones) {
				if (clone.getDatabaseID() == plate.getCloneAt(x - 1, y).getDatabaseID()) {
					found = true;
				}
			}
			if (!found) {
				clones.add(plate.getCloneAt(x - 1, y));
			}
		}

		cloneListBox = new JComboBox<Object>();
		cloneListBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		cloneListBox.addItem("-- not selected --");
		for (Clone clone : clones) {
			cloneListBox.addItem(clone);
		}

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		if (cloneListBox.getItemCount() > 1) {

			JLabel infoLabel = new JLabel("Only clones with exact one unused vials are listed.");
			infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

			JLabel listLabel = new JLabel("Select the clone:");
			listLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

			JButton saveCloneButton = new JButton("select and save");
			saveCloneButton.addActionListener(new SaveBarcodeListener());

			JButton cancelButton = new JButton("cancel");
			cancelButton.addActionListener(new CancelListener());

			JPanel saveClonePanel = new JPanel();
			saveClonePanel.setLayout(new BoxLayout(saveClonePanel, BoxLayout.X_AXIS));
			saveClonePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			saveClonePanel.add(saveCloneButton);
			saveClonePanel.add(Box.createHorizontalGlue());
			saveClonePanel.add(cancelButton);

			mainPanel.add(infoLabel);
			mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
			mainPanel.add(listLabel);
			mainPanel.add(Box.createRigidArea(new Dimension(0, 2)));
			mainPanel.add(cloneListBox);
			mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
			mainPanel.add(saveClonePanel);

		}

		else {

			JLabel label = new JLabel("Sorry, no clone with exact one unused vial was found.");
			label.setAlignmentX(JLabel.CENTER_ALIGNMENT);

			JButton cancelButton = new JButton("cancel");
			cancelButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
			cancelButton.addActionListener(new CancelListener());

			mainPanel.add(label);
			mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
			mainPanel.add(cancelButton);
		}

		dialog = new JDialog(baseFrame, "Select Your Clone");
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.addWindowListener(new FrameDisableListener(baseFrame, this));
		dialog.getContentPane().add(mainPanel, BorderLayout.CENTER);
		dialog.setResizable(false);
		dialog.pack();
		dialog.setVisible(true);
		dialog.setLocationRelativeTo(null);
	}


	/**
	 * Check if there is a clone at a specific position and if there is one barcode scanned that is not used on the
	 * current plate.
	 * 
	 * @param x
	 *            x-position of the clone
	 * @param y
	 *            y-position of the clone
	 * @return
	 *         <code>true</code> if a Clone wit one unused barcode was found, <code>false</code> otherwise.
	 */
	private Barcode checkForUnusedBarcode(Clone clone) {

		if (clone != null) {

			// Fetch all barcodes of the clone that were read from the plate
			List<Barcode> regionBarcodes = new ArrayList<Barcode>();
			for (PlateRegion region : plate.getRegions()) {
				if (region.getClone() != null && region.getClone().getDatabaseID() == clone.getDatabaseID()) {
					regionBarcodes.addAll(region.getBarcodes());
				}
			}

			Barcode[] databaseBarcodes = DatabaseProvider.getInstance().getBarcodes(clone.getDatabaseID());

			List<Barcode> unusedBarcodes = new ArrayList<Barcode>();

			boolean found;
			for (Barcode databaseBarcode : databaseBarcodes) {
				found = false;
				for (Barcode regionBarcode : regionBarcodes) {
					if (databaseBarcode.getCode().equals(regionBarcode.getCode())) {
						found = true;
						break;
					}
				}
				if (!found) {
					unusedBarcodes.add(databaseBarcode);
				}
			}

			if (unusedBarcodes.size() == 1) {
				return unusedBarcodes.get(0);
			}

		}

		return null;

	}


	private void updateCloneList() {

		List<Clone> clones = new ArrayList<Clone>();
		if (!cloneListFilter.getText().toLowerCase().trim().isEmpty()) {
			clones = DatabaseProvider.getInstance().getThawedClones(null, null, cloneListFilter.getText().toLowerCase().trim());
		}

		cloneListBox.removeAllItems();
		cloneListBox.addItem("-- not selected --");
		for (int i = 0; i < clones.size(); i++) {
			cloneListBox.addItem(clones.get(i));
		}

	}

	private class SaveCloneListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			// Check if one clone of both lists is selected
			if (clonePlateBox.getSelectedIndex() == 0 && cloneListBox.getSelectedIndex() == 0) {
				JOptionPane.showMessageDialog(dialog, "Please select one clone out of the two lists.", "No Clone Selected", JOptionPane.ERROR_MESSAGE);
			}
			else if (clonePlateBox.getSelectedIndex() != 0 && cloneListBox.getSelectedIndex() != 0) {
				JOptionPane.showMessageDialog(dialog, "You have selected a clone in each list.\nPlease select just one clone out of both lists.", "Two Clones Selected", JOptionPane.ERROR_MESSAGE);
			}

			// Save or update the barcode and refresh the plate viewer
			else {

				Clone clone = (Clone) (clonePlateBox.getSelectedIndex() > 0 ? clonePlateBox.getSelectedItem() : cloneListBox.getSelectedItem());

				String[] barcodes = { plate.getBarcodeAt(x, y) };
				Barcode barcode = DatabaseProvider.getInstance().getBarcode(barcodes[0]);
				if (barcode == null) {
					DatabaseProvider.getInstance().saveBarcodes(barcodes, clone.getCloneID(), clone.getDatabaseID());
				}
				else {
					DatabaseProvider.getInstance().updateBarcodes(barcodes, clone.getCloneID(), clone.getDatabaseID());
				}

				plate.setRegions(plate.getBarcodes());

				plateViewer.refreshView(plate);
				dialog.dispose();
			}
		}
	}

	private class SaveBarcodeListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			// Check if one clone of both lists is selected
			if (cloneListBox.getSelectedIndex() == 0) {
				JOptionPane.showMessageDialog(dialog, "Please select a clone.", "No Clone Selected", JOptionPane.ERROR_MESSAGE);
			}

			// Fetch the barcode and save it
			else {

				// Fetch the clone
				Clone clone = (Clone) cloneListBox.getSelectedItem();

				Barcode barcode = checkForUnusedBarcode(clone);

				// Check if we have exactly 1 barcode left
				if (barcode != null) {

					System.out.println(barcode.getCode());

					// Update the barcode in the database
					// String[] saveBarcodes = { barcode.getCode() };
					// DatabaseProvider.getInstance().updateBarcodes(saveBarcodes, clone.getCloneID(),
					// clone.getDatabaseID());

					// Set the barcode at the correct position
					String[][] barcodes = plate.getBarcodes();
					barcodes[y][x] = barcode.getCode();

					// Refresh the regions of the plate
					plate.setRegions(barcodes);

				}

				// Refresh the plate view
				plateViewer.refreshView(plate);
				dialog.dispose();

			}

		}
	}

	private class CancelListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			dialog.dispose();
		}

	}

	private class SelectCloneListener implements ActionListener {

		private JComboBox<Object>	activeBox;
		private JComboBox<Object>	dependentBox;


		public SelectCloneListener(JComboBox<Object> activeBox, JComboBox<Object> dependentBox) {
			this.activeBox = activeBox;
			this.dependentBox = dependentBox;
		}


		@Override
		public void actionPerformed(ActionEvent e) {
			if (activeBox.getSelectedIndex() != 0) {
				dependentBox.setSelectedIndex(0);
			}
		}

	}

	private class CloneListFilterListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			updateCloneList();
		}

	}

	private class RemoveVialListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			String reason = (String) removeReasonBox.getSelectedItem();

			if (reason.equals(CCM_Utils.removeVialReasons[0])) {
				JOptionPane.showMessageDialog(dialog, "Please select the reason why you want to remove the vial from the database.", "No Reason selected", JOptionPane.ERROR_MESSAGE);
			}

			else if (JOptionPane.showConfirmDialog(dialog, "Do you really want to remove the vial at " + Plate.rowNames[y] + String.format("%02d", x + 1) + "?", "Remove Vials", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0) {

				// Remove the barcode in the database
				DatabaseProvider.getInstance().removeBarcode(reason, plate.getBarcodeAt(x, y));

				// Remove the barcode from the loaded plate
				String[][] barcodes = plate.getBarcodes();
				barcodes[y][x] = Plate.EMPTY_TUBE_VALUE;
				plate.setRegions(barcodes);

				// Refresh the plate view
				plateViewer.refreshView(plate);

				dialog.dispose();

			}

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
