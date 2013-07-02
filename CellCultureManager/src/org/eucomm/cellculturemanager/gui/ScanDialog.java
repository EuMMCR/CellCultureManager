package org.eucomm.cellculturemanager.gui;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.GregorianCalendar;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.eucomm.cellculturemanager.controller.CCM_Utils;
import org.eucomm.cellculturemanager.controller.DatabaseProvider;
import org.eucomm.cellculturemanager.controller.LogWriter;
import org.eucomm.cellculturemanager.model.Plate;


public class ScanDialog implements FrameDisabler {

	private JFrame					dialog;
	private Window					baseFrame;
	private CellCultureManagerGui	ccManager;
	private boolean					enableBaseFrame;

	private JPanel					plateIdPanel;
	private JTextField				plateIdField;

	private static ScanDialog		scanDialog;


	private ScanDialog() {

	}


	public static ScanDialog getInstance() {
		if (scanDialog == null) {
			scanDialog = new ScanDialog();
		}
		return scanDialog;
	}


	public void showDialog(Window baseFrame, CellCultureManagerGui ccManager) {

		enableBaseFrame = true;

		this.baseFrame = baseFrame;
		this.ccManager = ccManager;

		int buttonWidth = 200;
		int buttonHeight = 50;

		JPanel buttonPanel = new JPanel(new BorderLayout(10, 10));
		buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JButton scanStorageButton = new JButton("Scan Storage Plate");
		scanStorageButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
		scanStorageButton.addActionListener(new ScanStorageListener());
		buttonPanel.add(scanStorageButton, BorderLayout.PAGE_START);

		JButton scanWorkbenchButton = new JButton("Scan Workbench Clones");
		scanWorkbenchButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
		scanWorkbenchButton.addActionListener(new ScanWorkbenchListener());
		buttonPanel.add(scanWorkbenchButton, BorderLayout.CENTER);

		JButton scanShippingButton = new JButton("Scan Shipping Clones");
		scanShippingButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
		scanShippingButton.addActionListener(new ScanShippingListener());
		buttonPanel.add(scanShippingButton, BorderLayout.PAGE_END);

		JPanel scannerSelectPanel = new JPanel(new BorderLayout(10, 10));
		scannerSelectPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JCheckBox useWbScannerBox = new JCheckBox("Use the workbench scanner", CCM_Utils.useWorkbenchScanner);
		useWbScannerBox.addActionListener(new UseScannerListener());
		scannerSelectPanel.add(useWbScannerBox, BorderLayout.CENTER);

		plateIdPanel = new JPanel(new BorderLayout(10, 10));
		plateIdPanel.setVisible(!CCM_Utils.useWorkbenchScanner);

		JLabel plateIdLabel = new JLabel("Plate ID:");
		plateIdLabel.setPreferredSize(new Dimension(50, 25));
		plateIdPanel.add(plateIdLabel, BorderLayout.LINE_START);

		plateIdField = new JTextField();
		plateIdField.setPreferredSize(new Dimension(125, 25));
		plateIdPanel.add(plateIdField, BorderLayout.LINE_END);
		scannerSelectPanel.add(plateIdPanel, BorderLayout.PAGE_END);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(buttonPanel);
		mainPanel.add(new JSeparator(JSeparator.HORIZONTAL));
		mainPanel.add(scannerSelectPanel);

		dialog = new JFrame("Scan Vials");
		dialog.addWindowListener(new FrameDisableListener(baseFrame, this));
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		dialog.setResizable(false);
		dialog.getContentPane().add(mainPanel);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);

	}

	private class ScanStorageListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {

			enableBaseFrame = false;

			String plateId = null;
			if (!CCM_Utils.useWorkbenchScanner) {
				plateId = plateIdField.getText();
				if (plateId == null || plateId.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Please enter the plate id of the plate you want to scan.", "No Plate Id", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}

			int tank = DatabaseProvider.getInstance().getTanks(false)[0];
			int rack = DatabaseProvider.getInstance().getRacks(tank, false)[0];
			int shelf = DatabaseProvider.getInstance().getShelfs(tank, rack, false)[0];

			JOptionPane.showMessageDialog(dialog, "Please hold the plate on the scanner.\n\nThe scanning will start after you click the OK button.", "Scan Storage Plate", JOptionPane.INFORMATION_MESSAGE);
			Plate plate = new Plate(tank, rack, shelf, plateId);
			try {
				PlateViewer.getInstance().showPlate(CCM_Utils.getScannerInstance().scan(plate), baseFrame, ccManager);
			}
			catch (Exception exception) {
				LogWriter.writeLog(exception);
				JOptionPane.showMessageDialog(null, exception.getMessage(), exception.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				enableBaseFrame = true;
			}

			dialog.dispose();

		}

	}

	private class ScanWorkbenchListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {

			enableBaseFrame = false;

			JOptionPane.showMessageDialog(dialog, "Please hold the plate on the scanner.\n\nThe scanning will start after you click the OK button.", "Scan Workbench Clones", JOptionPane.INFORMATION_MESSAGE);
			Plate plate = new Plate(0, 0, 0, null, new GregorianCalendar());
			plate.setWorkbenchClones(true);
			try {
				PlateViewer.getInstance().showPlate(CCM_Utils.getScannerInstance().scan(plate), baseFrame, ccManager);
			}
			catch (Exception exception) {
				JOptionPane.showMessageDialog(null, exception.getMessage(), exception.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				enableBaseFrame = true;
			}

			dialog.dispose();

		}

	}

	private class ScanShippingListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {

			enableBaseFrame = false;

			JOptionPane.showMessageDialog(dialog, "Please hold the plate on the scanner.\n\nThe scanning will start after you click the OK button.", "Scan Shipping Clones", JOptionPane.INFORMATION_MESSAGE);
			Plate plate = new Plate(0, 0, 0, CCM_Utils.shippingPlateID, new GregorianCalendar());
			try {
				PlateViewer.getInstance().showPlate(CCM_Utils.getScannerInstance().scan(plate), baseFrame, ccManager);
			}
			catch (Exception exception) {
				JOptionPane.showMessageDialog(null, exception.getMessage(), exception.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				enableBaseFrame = true;
			}

			dialog.dispose();

		}
	}

	private class UseScannerListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			CCM_Utils.useWorkbenchScanner = !CCM_Utils.useWorkbenchScanner;
			plateIdPanel.setVisible(!CCM_Utils.useWorkbenchScanner);
			dialog.pack();
		}

	}


	@Override
	public boolean disableBaseFrame() {
		return true;
	}


	@Override
	public boolean enableBaseFrame() {
		return enableBaseFrame;
	}

}
