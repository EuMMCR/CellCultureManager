package org.eucomm.cellculturemanager.gui;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.eucomm.cellculturemanager.controller.CCM_Utils;
import org.eucomm.cellculturemanager.controller.DatabaseProvider;
import org.eucomm.cellculturemanager.model.Clone;


public class VialScanner implements FrameDisabler {

	private JDialog						dialog;

	private CellCultureManagerGui		ccManager;

	private Clone						clone;
	private int							cloneIndex;

	private JTextField					removeVialField;
	private JComboBox<String>			removeReasonBox;
	private JTextField					barcodeField;

	private DefaultListModel<String>	model;


	public VialScanner(CellCultureManagerGui ccManager) {
		this.ccManager = ccManager;
	}


	public void openRemoveWindow(Window baseFrame) {

		dialog = new JDialog(baseFrame, "Remove Scanned Vials");
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.addWindowListener(new FrameDisableListener(baseFrame, this));

		JLabel label1 = new JLabel("1. Select a reason");
		label1.setAlignmentX(JLabel.CENTER_ALIGNMENT);

		JLabel label2 = new JLabel("2. focus the text field");
		label2.setAlignmentX(JLabel.CENTER_ALIGNMENT);

		JLabel label3 = new JLabel("3. scan a vial");
		label3.setAlignmentX(JLabel.CENTER_ALIGNMENT);

		removeReasonBox = new JComboBox<String>(CCM_Utils.removeVialReasons);
		removeReasonBox.setAlignmentX(JComboBox.CENTER_ALIGNMENT);

		removeVialField = new JTextField(10);
		removeVialField.setHorizontalAlignment(JTextField.CENTER);
		removeVialField.setAlignmentX(JTextField.CENTER_ALIGNMENT);
		removeVialField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// Do nothing here, we are not interested in this event.
			}


			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					removeCode();
				}
			}


			@Override
			public void keyPressed(KeyEvent e) {
				// Do nothing here, we are not interested in this event.
			}

		});

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(label1);
		panel.add(label2);
		panel.add(label3);
		panel.add(Box.createRigidArea(new Dimension(0, 10)));
		panel.add(removeReasonBox);
		panel.add(Box.createRigidArea(new Dimension(0, 5)));
		panel.add(removeVialField);

		dialog.getContentPane().add(panel, BorderLayout.CENTER);

		dialog.pack();
		dialog.setLocationRelativeTo(baseFrame);
		dialog.setResizable(false);
		dialog.setVisible(true);

	}


	public void openScanWindow(Window baseFrame, Clone clone, int cloneIndex) {

		this.clone = clone;
		this.cloneIndex = cloneIndex;

		if (clone.getVialNumber() == 0 || JOptionPane.showConfirmDialog(baseFrame, "There are already " + clone.getVialNumber() + " vials scanned for this clone.\nDo you want to continue, anyway?", "Barcodes exist!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == 0) {

			dialog = new JDialog(baseFrame, "Scan Vials");
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.addWindowListener(new FrameDisableListener(baseFrame, this));

			// Add the center panel to the frame. The center panel contains all elements of the application.
			Box centerPanel = Box.createVerticalBox();
			centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

			// Add the clone ID
			JLabel label1 = new JLabel("Scan vials for the clone");
			label1.setHorizontalAlignment(SwingConstants.CENTER);
			label1.setAlignmentX(Component.CENTER_ALIGNMENT);

			JLabel label2 = new JLabel(clone.getCloneID());
			label2.setHorizontalAlignment(SwingConstants.CENTER);
			label2.setAlignmentX(Component.CENTER_ALIGNMENT);
			label2.setFont(label2.getFont().deriveFont(20f).deriveFont(Font.BOLD));

			JLabel label3 = new JLabel(clone.getGeneName());
			label3.setHorizontalAlignment(SwingConstants.CENTER);
			label3.setAlignmentX(Component.CENTER_ALIGNMENT);
			label3.setFont(label3.getFont().deriveFont(20f).deriveFont(Font.BOLD));

			centerPanel.add(label1);
			centerPanel.add(label2);
			centerPanel.add(label3);

			centerPanel.add(Box.createVerticalStrut(10));
			centerPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
			centerPanel.add(Box.createVerticalStrut(10));

			// Create the panel that takes the scanned barcode.
			JPanel barcodePanel = new JPanel();
			barcodePanel.setLayout(new BoxLayout(barcodePanel, BoxLayout.X_AXIS));
			barcodePanel.add(new JLabel("2. Scan The Vials:"));
			barcodePanel.add(Box.createHorizontalStrut(10));
			barcodeField = new JTextField(10);
			barcodeField.setHorizontalAlignment(SwingConstants.CENTER);
			barcodeField.addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent e) {
					// Do nothing here, we are not interested in this event.
				}


				@Override
				public void keyReleased(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						addCode();
					}
				}


				@Override
				public void keyPressed(KeyEvent e) {
					// Do nothing here, we are not interested in this event.
				}

			});
			barcodePanel.add(barcodeField);
			centerPanel.add(barcodePanel);

			// Add a separator.
			centerPanel.add(Box.createVerticalStrut(10));
			centerPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
			centerPanel.add(Box.createVerticalStrut(10));

			// Create a left-aligned topic of the barcode list
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			panel.add(new JLabel("Barcode List:"));
			panel.add(Box.createHorizontalGlue());
			centerPanel.add(panel);

			// Create the barcode list.
			model = new DefaultListModel<String>();
			JList<String> list = new JList<String>(model);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setLayoutOrientation(JList.VERTICAL);
			list.setVisibleRowCount(12);
			list.setEnabled(false);
			JScrollPane listScroller = new JScrollPane(list);
			centerPanel.add(listScroller);

			// Add a separator.
			centerPanel.add(Box.createVerticalStrut(10));
			centerPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
			centerPanel.add(Box.createVerticalStrut(10));

			// create the save and clear button panel.
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
			JButton saveButton = new JButton("save");
			JButton clearButton = new JButton("clear");
			saveButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					saveCodes();
				}

			});
			clearButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					clearFields();
				}

			});
			buttonPanel.add(clearButton);
			buttonPanel.add(Box.createHorizontalGlue());
			buttonPanel.add(saveButton);
			centerPanel.add(buttonPanel);

			dialog.getContentPane().add(centerPanel, BorderLayout.CENTER);

			dialog.pack();
			dialog.setLocationRelativeTo(baseFrame);
			dialog.setResizable(false);
			dialog.setVisible(true);

		}

		else {
			baseFrame.setEnabled(true);
		}

	}


	/**
	 * Check if the barcode syntax is correct.
	 */
	private boolean checkCodeSyntax(String code) {
		if (code.length() != 10) {
			JOptionPane.showMessageDialog(dialog, "The code you've entered (" + code + ") is not 10 characters long.\nPlease check if the code is correct, and if so, please contact your administrator.", "Barcode Length Not Correct", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		else {
			return true;
		}
	}


	/**
	 * Add a barcode to the list.
	 */
	private void addCode() {
		String code = barcodeField.getText().toUpperCase().trim();
		if (!code.isEmpty() && checkCodeSyntax(code)) {
			if (model.indexOf(code) > -1) {

				JOptionPane.showMessageDialog(dialog, "The code already exists in the list.\nThis time I will forget you've scanned it twice, but don't do this again...");

				// TODO: add duplicate counter and output different messages...

				// Clear the barcode field
				barcodeField.setText("");
			}
			else {

				// Clear the barcode field
				barcodeField.setText("");

				// Check if the barcode exists in the database
				String cloneID = DatabaseProvider.getInstance().checkBarCode(code);

				// If the barcode exists, ask the user whether the code should be overwritten or not. If yes, delete the
				// existing code from the database.
				if (cloneID != null && JOptionPane.showConfirmDialog(dialog, "The code (" + code + ") is used for the clone " + cloneID + ".\nDo you want to overwrite the existing code?", "Barcode Exists", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == 0) {
					cloneID = null;
				}

				// If the code is OK, add it to the list.
				if (cloneID == null) {
					model.addElement(code);
				}
			}
		}
	}


	/**
	 * Clear all fields of the application.
	 */
	private void clearFields() {
		barcodeField.setText("");
		model.clear();
	}


	/**
	 * Save the codes into the database.
	 */
	private void saveCodes() {

		// Check the number of barcodes
		if (model.size() == 0) {
			JOptionPane.showMessageDialog(dialog, "There are no scanned barcodes in the list.\nI think you don't want to save this...", "No Barcodes", JOptionPane.WARNING_MESSAGE);
		}

		// If everything is OK, we can start saving the barcodes
		else {

			// Get all codes in a String array and delete the codes from the database (this was questioned if a code
			// exists in the database)
			String[] codes = new String[model.size()];
			for (int i = 0; i < codes.length; i++) {
				codes[i] = (String) model.get(i);
				if (!DatabaseProvider.getInstance().removeBarcode("unused vials", codes[i])) {
					codes[i] = null;
					JOptionPane.showMessageDialog(dialog, "Sorry, I could not remove the code (" + codes[i] + ") from the database.\nPlease contact your administrator as soon as possible!", "Deletion Failed", JOptionPane.ERROR_MESSAGE);
					break;
				}
			}

			// If the last code is not null, all codes can be saved. So save the codes and clear all fields.
			if (codes[codes.length - 1] != null && DatabaseProvider.getInstance().saveBarcodes(codes, clone.getCloneID(), clone.getDatabaseID())) {
				JOptionPane.showMessageDialog(dialog, "The barcodes were saved sucessfully!", "Barcodes Saved", JOptionPane.INFORMATION_MESSAGE);
				clearFields();
				clone.setVialNumber(DatabaseProvider.getInstance().getCloneVials(clone.getDatabaseID()).length);
				if (clone.getVialNumber() > 0) {
					clone.addStatus(Clone.SCANNED);
				}
				else {
					clone.removeStatus(Clone.SCANNED);
				}
				ccManager.updateClone(cloneIndex, clone);
				dialog.dispose();
			}

		}
	}


	/**
	 * Remove a code from the database.
	 * 
	 * @param code
	 */
	private void removeCode() {

		String code = removeVialField.getText();
		String reason = (String) removeReasonBox.getSelectedItem();

		if (!code.isEmpty()) {

			if (reason.equals(CCM_Utils.removeVialReasons[0])) {
				JOptionPane.showMessageDialog(dialog, "Please select the reason why you want to remove the vial from the database.", "No Reason selected", JOptionPane.ERROR_MESSAGE);
				removeVialField.setText("");
			}

			else if (checkCodeSyntax(code)) {

				if (DatabaseProvider.getInstance().removeBarcode(reason, code)) {
					removeVialField.setText("");
					removeReasonBox.setSelectedIndex(0);
					JOptionPane.showMessageDialog(dialog, "The vial was successfully removed from the database.", "Vial Removed", JOptionPane.INFORMATION_MESSAGE);
					ccManager.updateCloneTable();
				}
				else {
					JOptionPane.showMessageDialog(dialog, "Sorry, I could not remove the code (" + code + ") from the database.\nPlease contact your administrator as soon as possible!", "Deletion Failed", JOptionPane.ERROR_MESSAGE);
				}
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
