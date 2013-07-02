package org.eucomm.cellculturemanager.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.eucomm.cellculturemanager.controller.DatabaseProvider;
import org.eucomm.cellculturemanager.controller.LabelPrinter;
import org.eucomm.cellculturemanager.model.Clone;


public class PrintDialog implements FrameDisabler {

	private JDialog					dialog;
	private Window					baseFrame;

	private static int				labelWidth					= 144;
	private static int				labelHeight					= 68;

	private static int				defaultFreezingLabelNumber	= 11;
	private static int				defaultDnaLabelNumber		= 1;

	private static final String		DISTRIBUTOR					= "EUMMCR";
	private String					moleTechnician;

	private GregorianCalendar		freezingDate;
	private JLabel					freezingDateLabel;
	private JLabel					freezingLabelFreezingDate;
	private JLabel					dnaLabelFreezingDate;

	private JTextField				freezePageNumber;
	private JTextField				dnaPageNumber;

	private CellCultureManagerGui	ccManager;
	private Clone					clone;
	private int						cloneIndex;

	private boolean					enableBaseFrame				= true;


	public void open(Window baseFrame, CellCultureManagerGui ccManager, Clone clone, int cloneIndex) {

		this.baseFrame = baseFrame;
		this.ccManager = ccManager;
		this.clone = clone;
		this.cloneIndex = cloneIndex;

		if (clone.isDead()) {
			if (JOptionPane.showConfirmDialog(baseFrame, "The clone \"" + clone.getCloneID() + "\" is marked as DEAD.\nDo you want to remove this state?", "Clone is DEAD", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == 0) {
				clone.removeStatus(Clone.DEAD);
				ccManager.updateClone(cloneIndex, clone);
			}
			else {
				return;
			}

		}

		this.freezingDate = new GregorianCalendar();

		this.moleTechnician = "(" + (clone.getTechnicianQC().isEmpty() ? "-" : clone.getTechnicianQC().charAt(0)) + ")" + (clone.isComplaint() ? Clone.TAG_COMPLAINT : "") + (clone.isExpandedAgain() ? Clone.TAG_REEXPAND : "") + (clone.isDuplicate() ? Clone.TAG_DUPLICATE : "");

		Box baseBox = Box.createVerticalBox();

		JPanel freezeLabel = createLabel(clone.getCloneID(), clone.getGeneName(), DISTRIBUTOR, clone.getCellLine());
		JPanel dnaLabel = createLabel(clone.getCloneID(), clone.getGeneName(), "(" + (clone.getTechnicianQC().isEmpty() ? "-" : clone.getTechnicianQC().charAt(0)) + ")" + (clone.isComplaint() ? Clone.TAG_COMPLAINT : "") + (clone.isExpandedAgain() ? Clone.TAG_REEXPAND : "") + (clone.isDuplicate() ? Clone.TAG_DUPLICATE : ""), clone.getCellLine());
		freezePageNumber = new JTextField(Integer.toString(defaultFreezingLabelNumber), 3);
		freezePageNumber.setHorizontalAlignment(JTextField.CENTER);
		dnaPageNumber = new JTextField(Integer.toString(defaultDnaLabelNumber), 3);
		dnaPageNumber.setHorizontalAlignment(JTextField.CENTER);
		JLabel freezeText = new JLabel("Number of labels:");
		JLabel dnaText = new JLabel("Number of labels:");

		// Get the date labels
		freezingLabelFreezingDate = (JLabel) freezeLabel.getComponent(4);
		dnaLabelFreezingDate = (JLabel) dnaLabel.getComponent(4);

		// Add freezing Date panel
		freezingDateLabel = new JLabel(getFreezingDateString(false));
		freezingDateLabel.setFont(freezingDateLabel.getFont().deriveFont(20f));
		JPanel freezingDatePanel = new JPanel();
		freezingDatePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		freezingDatePanel.setLayout(new FlowLayout());
		freezingDatePanel.add(new JLabel("Freezing date:"));
		freezingDatePanel.add(freezingDateLabel);
		JButton plusDayButton = new JButton("+");
		plusDayButton.addActionListener(new FreezingDateActionListener(true));
		JButton minusDayButton = new JButton("-");
		minusDayButton.addActionListener(new FreezingDateActionListener(false));
		freezingDatePanel.add(plusDayButton);
		freezingDatePanel.add(minusDayButton);

		JPanel freezePanel = new JPanel();
		freezePanel.setBorder(new EmptyBorder(10, 10, 5, 10));
		freezePanel.add(freezeLabel, BorderLayout.LINE_START);
		freezePanel.add(freezeText, BorderLayout.CENTER);
		freezePanel.add(freezePageNumber, BorderLayout.LINE_END);

		JPanel dnaPanel = new JPanel();
		dnaPanel.setBorder(new EmptyBorder(5, 10, 10, 10));
		dnaPanel.add(dnaLabel, BorderLayout.LINE_START);
		dnaPanel.add(dnaText, BorderLayout.CENTER);
		dnaPanel.add(dnaPageNumber, BorderLayout.LINE_END);

		JButton printButton = new JButton("print labels");
		printButton.addActionListener(new PinterActionListener());

		JButton scanButton = new JButton("scan vials");
		scanButton.addActionListener(new ScanActionListener());

		Box actionBox = Box.createHorizontalBox();
		actionBox.setBorder(new EmptyBorder(10, 10, 10, 10));
		actionBox.add(printButton);
		actionBox.add(Box.createHorizontalGlue());
		actionBox.add(scanButton);

		JButton deadButton = new JButton("clones are dead");
		deadButton.addActionListener(new DeadClonesActionListener());
		JPanel deadButtonPanel = new JPanel();
		deadButtonPanel.setLayout(new FlowLayout());
		deadButtonPanel.add(deadButton);

		JButton duplicateButton = new JButton("Duplicate Clone (split 1x6 and 1x24)");
		duplicateButton.addActionListener(new DuplicateCloneActionListener());
		JPanel duplicatePanel = new JPanel();
		duplicatePanel.setLayout(new FlowLayout());
		duplicatePanel.add(duplicateButton);

		baseBox.add(freezingDatePanel);
		baseBox.add(new JSeparator(SwingConstants.HORIZONTAL));
		baseBox.add(freezePanel);
		baseBox.add(dnaPanel);
		baseBox.add(new JSeparator(SwingConstants.HORIZONTAL));
		baseBox.add(actionBox);
		baseBox.add(new JSeparator());
		baseBox.add(deadButtonPanel);
		baseBox.add(new JSeparator());
		baseBox.add(duplicatePanel);

		dialog = new JDialog(baseFrame, "Clone: " + clone.getCloneID());
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.addWindowListener(new FrameDisableListener(baseFrame, this));
		dialog.getContentPane().add(baseBox, BorderLayout.CENTER);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(baseFrame);
		dialog.setVisible(true);

	}


	private JPanel createLabel(String cloneID, String geneName, String distributor, String cellLine) {

		Font labelFont = CellCultureManagerGui.labelFont.deriveFont(10.0f);

		JPanel label = new JPanel();
		label.setBorder(new LineBorder(Color.BLACK, 1));
		label.setBackground(Color.WHITE);
		label.setForeground(Color.BLACK);
		label.setLayout(null);

		JLabel freezePanelCloneID = new JLabel(cloneID);
		freezePanelCloneID.setFont(labelFont);
		freezePanelCloneID.setBounds(5, 7, 136, 16);

		JLabel freezePanelGeneName = new JLabel(geneName);
		freezePanelGeneName.setFont(labelFont);
		freezePanelGeneName.setBounds(5, 27, 78, 16);

		JLabel freezePanelDistributor = new JLabel(distributor);
		freezePanelDistributor.setFont(labelFont);
		freezePanelDistributor.setBounds(102, 27, 36, 16);

		JLabel freezePanelCellLine = new JLabel(cellLine);
		freezePanelCellLine.setFont(labelFont);
		freezePanelCellLine.setBounds(5, 47, 72, 16);

		JLabel freezePanelFreezeDate = new JLabel(getFreezingDateString(true));
		freezePanelFreezeDate.setFont(labelFont);
		freezePanelFreezeDate.setBounds(90, 47, 48, 16);

		label.add(freezePanelCloneID);
		label.add(freezePanelGeneName);
		label.add(freezePanelDistributor);
		label.add(freezePanelCellLine);
		label.add(freezePanelFreezeDate);

		label.setMinimumSize(new Dimension(labelWidth, labelHeight));
		label.setMaximumSize(new Dimension(labelWidth, labelHeight));
		label.setPreferredSize(new Dimension(labelWidth, labelHeight));
		label.setSize(labelWidth, labelHeight);

		return label;

	}


	private String getFreezingDateString(boolean label) {
		if (label) {
			return String.format("%02d.%02d.%02d", freezingDate.get(Calendar.DAY_OF_MONTH), freezingDate.get(Calendar.MONTH) + 1, freezingDate.get(Calendar.YEAR) % 100);
		}
		else {
			return String.format("%04d-%02d-%02d", freezingDate.get(Calendar.YEAR), freezingDate.get(Calendar.MONTH) + 1, freezingDate.get(Calendar.DAY_OF_MONTH));
		}
	}

	private class FreezingDateActionListener implements ActionListener {

		private int	multiplier	= 0;


		public FreezingDateActionListener(boolean add) {
			multiplier = add ? 1 : -1;
		}


		@Override
		public void actionPerformed(ActionEvent e) {
			freezingDate.add(Calendar.DAY_OF_MONTH, multiplier);
			freezingDateLabel.setText(getFreezingDateString(false));
			freezingLabelFreezingDate.setText(getFreezingDateString(true));
			dnaLabelFreezingDate.setText(getFreezingDateString(true));
		}

	}

	private class PinterActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if (!freezePageNumber.getText().matches("^\\d+$")) {
				JOptionPane.showMessageDialog(dialog, "Please specify the number of labels for the freezing vials by a positive integer or 0.", "Wrong Format", JOptionPane.ERROR_MESSAGE);
			}

			else if (!dnaPageNumber.getText().matches("^\\d+$")) {
				JOptionPane.showMessageDialog(dialog, "Please specify the number of labels for the DNA vials by a positive integer or 0.", "Wrong Format", JOptionPane.ERROR_MESSAGE);
			}

			else {

				// Check if the clone was printed before and if so, re-check the printing
				if (!clone.wasPrinted() || JOptionPane.showConfirmDialog(dialog, "There have been printed already some labels.\nDo you really want to print more?", "Labels Already Printed", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == 0) {

					int labelNumberFreeze = Integer.parseInt(freezePageNumber.getText());
					int labelNumberDNA = Integer.parseInt(dnaPageNumber.getText());

					if ((labelNumberFreeze < 12 || JOptionPane.showConfirmDialog(dialog, "Are you sure you want to print " + labelNumberFreeze + " labels of this clone?", "So Many Labels...", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0) && (labelNumberDNA < 3 || JOptionPane.showConfirmDialog(dialog, "Are you sure you want to print " + labelNumberDNA + " DNA labels of this clone?", "So Many Labels...", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0)) {
						clone.addStatus(Clone.PRINTED);

						String line1 = String.format("%-22s", clone.getCloneID());
						String line2Freeze = String.format("%-15s", clone.getGeneName()) + String.format("%7s", DISTRIBUTOR);
						String line2Dna = String.format("%-15s", clone.getGeneName()) + String.format("%7s", moleTechnician);
						String line3 = String.format("%-13s", clone.getCellLine()) + String.format("%9s", getFreezingDateString(true));

						LabelPrinter.getInstance().printLabels(line1, line2Freeze, line3, labelNumberFreeze);
						LabelPrinter.getInstance().printLabels(line1, line2Dna, line3, labelNumberDNA);
						dialog.dispose();
						ccManager.updateClone(cloneIndex, clone);
					}

				}

			}

		}
	}

	private class ScanActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			enableBaseFrame = false;
			dialog.dispose();
			new VialScanner(ccManager).openScanWindow(baseFrame, clone, cloneIndex);
		}

	}

	private class DeadClonesActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (JOptionPane.showConfirmDialog(dialog, "Do you really want to mark this clones as DEAD?\nThis will remove all scanned vials!", "Are the clones really dead?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0) {

				// Remove all scanned vials from the database
				String[] codes = DatabaseProvider.getInstance().getCloneVials(clone.getDatabaseID());
				for (int i = 0; i < codes.length; i++) {
					DatabaseProvider.getInstance().removeBarcode("dead clone", codes[i]);
				}

				// Set the clone's vial number and status
				clone.setVialNumber(0);
				clone.addStatus(Clone.DEAD);

				// Update the clone
				ccManager.updateClone(cloneIndex, clone);

				// Close the dialog
				dialog.dispose();

			}
		}

	}

	private class DuplicateCloneActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (DatabaseProvider.getInstance().duplicateClone(clone.getDatabaseID())) {
				JOptionPane.showMessageDialog(dialog, "Your clone was duplicated.", "Duplication Succeed", JOptionPane.INFORMATION_MESSAGE);
				dialog.dispose();
				ccManager.updateCloneTable();
			}
			else {
				JOptionPane.showMessageDialog(dialog, "Sorry, I could not duplicate your clone.", "Duplication Failed", JOptionPane.ERROR_MESSAGE);
			}
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
