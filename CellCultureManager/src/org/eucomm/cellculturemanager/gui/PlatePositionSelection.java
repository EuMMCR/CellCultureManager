package org.eucomm.cellculturemanager.gui;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.eucomm.cellculturemanager.controller.CCM_Utils;
import org.eucomm.cellculturemanager.controller.DatabaseProvider;
import org.eucomm.cellculturemanager.model.Plate;


public class PlatePositionSelection implements FrameDisabler {

	private static PlatePositionSelection	platePositionSelection	= null;

	private Window							baseFrame;
	private CellCultureManagerGui			ccManager;
	private JDialog							dialog;

	private boolean							loadExistingPlate;

	private JComboBox<String>				tankField;
	private JComboBox<String>				rackField;
	private JComboBox<String>				shelfField;
	private JTextField						plateNameField;
	private JButton							saveButton;
	private JButton							loadButton;

	private static final String				selectDefault			= "Please Select:";
	private static final int				labelWidth				= 75;
	private static final int				fieldWidth				= 100;
	private static final int				lineHeight				= 22;

	private boolean							enableBaseFrame			= true;


	private PlatePositionSelection() {

	}


	public static PlatePositionSelection getInstance() {
		if (platePositionSelection == null) {
			platePositionSelection = new PlatePositionSelection();
		}
		return platePositionSelection;
	}


	public void showLoader(Window baseFrame, CellCultureManagerGui ccManager) {
		loadExistingPlate = true;
		showGui(baseFrame, ccManager);
	}


	public void showScanner(Window baseFrame, CellCultureManagerGui ccManager) {
		loadExistingPlate = false;
		showGui(baseFrame, ccManager);
	}


	private void showGui(Window baseFrame, CellCultureManagerGui ccManager) {

		int i;

		this.baseFrame = baseFrame;
		this.ccManager = ccManager;

		// Create fields
		tankField = new JComboBox<String>();
		tankField.setPreferredSize(new Dimension(fieldWidth, lineHeight));
		int[] tanks = DatabaseProvider.getInstance().getTanks(loadExistingPlate);
		if (tanks != null) {
			tankField.addItem(selectDefault);
			for (i = 0; i < tanks.length; i++) {
				tankField.addItem(Integer.toString(tanks[i]));
			}
		}
		tankField.addActionListener(new TankListener());

		rackField = new JComboBox<String>();
		rackField.setPreferredSize(new Dimension(fieldWidth, lineHeight));
		rackField.setEnabled(false);
		rackField.addActionListener(new RackListener());

		shelfField = new JComboBox<String>();
		shelfField.setPreferredSize(new Dimension(fieldWidth, lineHeight));
		shelfField.setEnabled(false);
		shelfField.addActionListener(new ShelfListener());

		plateNameField = new JTextField();
		plateNameField.setPreferredSize(new Dimension(fieldWidth, lineHeight));

		saveButton = new JButton("save position and scan rack");
		saveButton.setEnabled(false);
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.addActionListener(new ScanListener());

		loadButton = new JButton("load plate");
		loadButton.setEnabled(true);
		loadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		loadButton.addActionListener(new LoadListener());

		JLabel tankLabel = new JLabel("Tank:");
		tankLabel.setPreferredSize(new Dimension(labelWidth, lineHeight));
		JLabel rackLabel = new JLabel("Rack:");
		rackLabel.setPreferredSize(new Dimension(labelWidth, lineHeight));
		JLabel shelfLabel = new JLabel("Shelf:");
		shelfLabel.setPreferredSize(new Dimension(labelWidth, lineHeight));
		JLabel plateLabel = new JLabel("Plate ID:");
		plateLabel.setPreferredSize(new Dimension(labelWidth, lineHeight));

		Box tankBox = Box.createHorizontalBox();
		tankBox.add(tankLabel);
		tankBox.add(tankField);

		Box rackBox = Box.createHorizontalBox();
		rackBox.add(rackLabel);
		rackBox.add(rackField);

		Box shelfBox = Box.createHorizontalBox();
		shelfBox.add(shelfLabel);
		shelfBox.add(shelfField);

		Box plateBox = Box.createHorizontalBox();
		plateBox.add(plateLabel);
		plateBox.add(plateNameField);

		JLabel topic = new JLabel(loadExistingPlate ? "<html>Please select the position or<br>type in the plate ID</html>" : "<html>Please select the position of the<br>plate in the cryo stock:</html>");
		topic.setAlignmentX(Component.CENTER_ALIGNMENT);
		topic.setHorizontalAlignment(SwingConstants.CENTER);

		// Box mainBox = Box.createVerticalBox();
		JPanel mainBox = new JPanel();
		mainBox.setLayout(new BoxLayout(mainBox, BoxLayout.Y_AXIS));
		mainBox.add(topic);
		mainBox.add(Box.createRigidArea(new Dimension(0, 20)));
		mainBox.add(tankBox);
		mainBox.add(Box.createRigidArea(new Dimension(0, 5)));
		mainBox.add(rackBox);
		mainBox.add(Box.createRigidArea(new Dimension(0, 5)));
		mainBox.add(shelfBox);
		mainBox.add(Box.createRigidArea(new Dimension(0, 10)));
		mainBox.add(plateBox);
		mainBox.add(Box.createRigidArea(new Dimension(0, 20)));
		mainBox.add(loadExistingPlate ? loadButton : saveButton);

		mainBox.setBorder(new EmptyBorder(10, 10, 10, 10));

		dialog = new JDialog(baseFrame, "Select Plate Position");
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.addWindowListener(new FrameDisableListener(baseFrame, this));
		dialog.setResizable(false);
		dialog.getContentPane().add(mainBox, BorderLayout.CENTER);
		dialog.pack();
		dialog.setLocationRelativeTo(baseFrame);
		dialog.setVisible(true);

	}


	@Override
	public boolean disableBaseFrame() {
		return true;
	}


	@Override
	public boolean enableBaseFrame() {
		return enableBaseFrame;
	}

	private class TankListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			rackField.removeAllItems();
			rackField.setEnabled(false);
			shelfField.removeAllItems();
			shelfField.setEnabled(false);
			saveButton.setEnabled(false);

			if (tankField.getSelectedIndex() > 0) {
				int tank = Integer.parseInt((String) tankField.getSelectedItem());
				int[] racks = DatabaseProvider.getInstance().getRacks(tank, loadExistingPlate);
				if (racks != null) {
					rackField.addItem(selectDefault);
					for (int i = 0; i < racks.length; i++) {
						rackField.addItem(Integer.toString(racks[i]));
					}
				}
				rackField.setEnabled(true);
			}

		}
	}

	private class RackListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			shelfField.removeAllItems();
			shelfField.setEnabled(false);
			saveButton.setEnabled(false);

			if (rackField.getSelectedIndex() > 0) {
				int tank = Integer.parseInt((String) tankField.getSelectedItem());
				int rack = Integer.parseInt((String) rackField.getSelectedItem());
				int[] shelfs = DatabaseProvider.getInstance().getShelfs(tank, rack, loadExistingPlate);
				if (shelfs != null) {
					shelfField.addItem(selectDefault);
					for (int i = 0; i < shelfs.length; i++) {
						shelfField.addItem(Integer.toString(shelfs[i]));
					}
				}
				shelfField.setEnabled(true);
			}

		}
	}

	private class ShelfListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (shelfField.getSelectedIndex() > 0) {
				saveButton.setEnabled(true);
			}
			else {
				saveButton.setEnabled(false);
			}
		}
	}

	private class ScanListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			String plateID = plateNameField.getText().trim().toUpperCase();

			if (tankField.getSelectedIndex() > 0 && rackField.getSelectedIndex() > 0 && shelfField.getSelectedIndex() > 0 && !plateID.isEmpty()) {

				int tank = Integer.parseInt((String) tankField.getSelectedItem());
				int rack = Integer.parseInt((String) rackField.getSelectedItem());
				int shelf = Integer.parseInt((String) shelfField.getSelectedItem());

				if (plateID.matches(".*[^A-Z0-9]")) {
					JOptionPane.showMessageDialog(dialog, "The plate ID must not contain special characters. Only letters and numbers are allowed.", "Wrong Characters", JOptionPane.ERROR_MESSAGE);
				}
				else {
					int[] platePosition = DatabaseProvider.getInstance().checkPlate(plateID);
					if (platePosition != null) {
						JOptionPane.showMessageDialog(dialog, "The plate ID is already in the cryo stock at the following position:\nTank: " + platePosition[0] + "\nRack: " + platePosition[1] + "\nShelf: " + platePosition[2] + "\nPlease check id the plate ID is correct.", "Plate ID Already Stored", JOptionPane.ERROR_MESSAGE);
					}
					else {

						try {

							Plate plate = CCM_Utils.getScannerInstance().scan(new Plate(tank, rack, shelf, plateID));

							if (plate.getRegions() != null) {
								enableBaseFrame = false;
								PlateViewer.getInstance().showPlate(plate, baseFrame, ccManager);
								dialog.dispose();
							}

							else {
								JOptionPane.showMessageDialog(dialog, "There was a problem scanning the plate.\nPlease try again and/or inform your administrator.", "Scan Error", JOptionPane.ERROR_MESSAGE);
							}

						}
						catch (Exception exception) {
							JOptionPane.showMessageDialog(null, exception.getMessage(), exception.getClass().getName(), JOptionPane.ERROR_MESSAGE);
						}

					}
				}
			}

			else {
				JOptionPane.showMessageDialog(dialog, "Please fill out all fields.", "Fields Are Empty", JOptionPane.ERROR_MESSAGE);
			}

		}
	}

	private class LoadListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			Plate plate = null;

			// Try to fetch a plate by the ID
			String plateID = plateNameField.getText().trim().toUpperCase();
			if (!plateID.isEmpty()) {
				plate = DatabaseProvider.getInstance().loadPlate(plateID);
			}

			if (plate == null) {

				// If no plate fetched, try to fetch the plate by the position
				if (tankField.getSelectedIndex() > 0 && rackField.getSelectedIndex() > 0 && shelfField.getSelectedIndex() > 0) {

					int tank = Integer.parseInt((String) tankField.getSelectedItem());
					int rack = Integer.parseInt((String) rackField.getSelectedItem());
					int shelf = Integer.parseInt((String) shelfField.getSelectedItem());

					plate = DatabaseProvider.getInstance().loadPlate(tank, rack, shelf);

				}

			}

			if (plate == null) {
				JOptionPane.showMessageDialog(dialog, "Sorry, I could not fetch any plate.\nYou must either select a position or insert the plate ID.", "Nothing Found", JOptionPane.ERROR_MESSAGE);
			}

			else {
				enableBaseFrame = false;
				PlateViewer.getInstance().showPlate(plate, baseFrame, ccManager);
				dialog.dispose();
			}

		}
	}

}
