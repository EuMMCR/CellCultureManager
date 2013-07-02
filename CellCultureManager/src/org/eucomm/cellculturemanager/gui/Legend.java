package org.eucomm.cellculturemanager.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;


public class Legend implements FrameDisabler {

	private static Legend	legend;


	private Legend() {

	}


	public static Legend getInstance() {
		if (legend == null) {
			legend = new Legend();
		}
		return legend;
	}


	public void show(Window baseFrame) {

		JDialog dialog = new JDialog(baseFrame, "Legend: Color Scheme");
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.addWindowListener(new FrameDisableListener(baseFrame, this));

		JLabel standardLabel = new JLabel("standard Color (clone is in progress)");
		standardLabel.setHorizontalAlignment(JLabel.CENTER);
		standardLabel.setForeground(CloneTableCellRenderer.FOREGROUND);
		JPanel standardPanel = new JPanel();
		standardPanel.setLayout(new BorderLayout(0, 0));
		standardPanel.setBackground(CloneTableCellRenderer.BACKCROUND);
		standardPanel.setAlignmentY(JPanel.CENTER_ALIGNMENT);
		standardPanel.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 1), new EmptyBorder(10, 10, 10, 10)));
		standardPanel.add(standardLabel);

		JLabel printedLabel = new JLabel("labels are printed");
		printedLabel.setHorizontalAlignment(JLabel.CENTER);
		printedLabel.setForeground(CloneTableCellRenderer.FOREGROUND_PRINTED);
		JPanel printedPanel = new JPanel();
		printedPanel.setLayout(new BorderLayout(0, 0));
		printedPanel.setBackground(CloneTableCellRenderer.BACKGROUND_PRINTED);
		printedPanel.setAlignmentY(JPanel.CENTER_ALIGNMENT);
		printedPanel.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 1), new EmptyBorder(10, 10, 10, 10)));
		printedPanel.add(printedLabel);

		JLabel scannedLabel = new JLabel("vials are scanned");
		scannedLabel.setHorizontalAlignment(JLabel.CENTER);
		scannedLabel.setForeground(CloneTableCellRenderer.FOREGROUND_SCANNED);
		JPanel scannedPanel = new JPanel();
		scannedPanel.setLayout(new BorderLayout(0, 0));
		scannedPanel.setBackground(CloneTableCellRenderer.BACKGROUND_SCANNED);
		scannedPanel.setAlignmentY(JPanel.CENTER_ALIGNMENT);
		scannedPanel.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 1), new EmptyBorder(10, 10, 10, 10)));
		scannedPanel.add(scannedLabel);

		JLabel frozenLabel = new JLabel("the clone is in the cryo-storage");
		frozenLabel.setHorizontalAlignment(JLabel.CENTER);
		frozenLabel.setForeground(CloneTableCellRenderer.FOREGROUND_FROZEN);
		JPanel frozenPanel = new JPanel();
		frozenPanel.setLayout(new BorderLayout(0, 0));
		frozenPanel.setBackground(CloneTableCellRenderer.BACKGROUND_FROZEN);
		frozenPanel.setAlignmentY(JPanel.CENTER_ALIGNMENT);
		frozenPanel.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 1), new EmptyBorder(10, 10, 10, 10)));
		frozenPanel.add(frozenLabel);

		JLabel deadLabel = new JLabel("the cells died during cell culture");
		deadLabel.setHorizontalAlignment(JLabel.CENTER);
		deadLabel.setForeground(CloneTableCellRenderer.FOREGROUND_DEAD);
		JPanel deadPanel = new JPanel();
		deadPanel.setLayout(new BorderLayout(0, 0));
		deadPanel.setBackground(CloneTableCellRenderer.BACKGROUND_DEAD);
		deadPanel.setAlignmentY(JPanel.CENTER_ALIGNMENT);
		deadPanel.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 1), new EmptyBorder(10, 10, 10, 10)));
		deadPanel.add(deadLabel);

		JLabel tagLabelTopic = new JLabel("Clone ID Tags:");
		tagLabelTopic.setFont(tagLabelTopic.getFont().deriveFont(15f).deriveFont(Font.BOLD));
		tagLabelTopic.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		JLabel tagLabelDCopy = new JLabel("(D)");
		JLabel tagLabekDCopyDescription = new JLabel("D-Copy");
		JLabel tagLabelReexpansion = new JLabel("-E-");
		JLabel tagLabelReexpansionDescription = new JLabel("Re-Expansion");
		JLabel tagLabelComplaint = new JLabel("-C-");
		JLabel tagLabelComplaintDescription = new JLabel("Complaint Clone");
		JPanel tagPanel = new JPanel();
		tagPanel.setLayout(new GridLayout(0, 2, 5, 5));
		tagPanel.add(tagLabelTopic);
		tagPanel.add(new JLabel(""));
		tagPanel.add(tagLabelDCopy);
		tagPanel.add(tagLabekDCopyDescription);
		tagPanel.add(tagLabelReexpansion);
		tagPanel.add(tagLabelReexpansionDescription);
		tagPanel.add(tagLabelComplaint);
		tagPanel.add(tagLabelComplaintDescription);

		Box mainBox = Box.createVerticalBox();
		mainBox.setBorder(new EmptyBorder(10, 10, 10, 10));
		mainBox.add(standardPanel);
		mainBox.add(Box.createRigidArea(new Dimension(0, 10)));
		mainBox.add(printedPanel);
		mainBox.add(Box.createRigidArea(new Dimension(0, 10)));
		mainBox.add(scannedPanel);
		mainBox.add(Box.createRigidArea(new Dimension(0, 10)));
		mainBox.add(frozenPanel);
		mainBox.add(Box.createRigidArea(new Dimension(0, 10)));
		mainBox.add(deadPanel);
		mainBox.add(Box.createRigidArea(new Dimension(0, 10)));
		mainBox.add(new JSeparator(JSeparator.HORIZONTAL));
		mainBox.add(Box.createRigidArea(new Dimension(0, 10)));
		mainBox.add(tagPanel);

		dialog.getContentPane().add(mainBox);

		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(baseFrame);
		dialog.setVisible(true);

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
