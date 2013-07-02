package org.eucomm.cellculturemanager.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.eucomm.cellculturemanager.controller.LabelPrinter;


public class CustomPrinterDialog implements FrameDisabler {

	private static CustomPrinterDialog	customPrinterDialog;

	private Font						labelFont;

	private static int					labelWidth	= 144;
	private static int					labelHeight	= 68;

	private JTextField					line1Field;
	private JTextField					line2Field;
	private JTextField					line3Field;

	private JLabel						labelLine1;
	private JLabel						labelLine2;
	private JLabel						labelLine3;

	private JTextField					numberField;

	private JDialog						dialog;


	private CustomPrinterDialog() {

		labelFont = CellCultureManagerGui.labelFont.deriveFont(10.0f);

	}


	public static CustomPrinterDialog getInstance() {
		if (customPrinterDialog == null) {
			customPrinterDialog = new CustomPrinterDialog();
		}
		return customPrinterDialog;
	}


	public void showDialog(Window baseFrame) {

		// Create the panel for the first line
		JLabel line1Label = new JLabel("Content of line 1:");
		line1Field = new JTextField(15);
		JPanel line1Panel = new JPanel(new BorderLayout(0, 0));
		line1Panel.add(line1Label, BorderLayout.LINE_START);
		line1Panel.add(line1Field, BorderLayout.LINE_END);

		// Create the panel for the second line
		JLabel line2Label = new JLabel("Content of line 2:");
		line2Field = new JTextField(15);
		JPanel line2Panel = new JPanel(new BorderLayout(0, 0));
		line2Panel.add(line2Label, BorderLayout.LINE_START);
		line2Panel.add(line2Field, BorderLayout.LINE_END);

		// Create the panel for the third line
		JLabel line3Label = new JLabel("Content of line 3:");
		line3Field = new JTextField(15);
		JPanel line3Panel = new JPanel(new BorderLayout(0, 0));
		line3Panel.add(line3Label, BorderLayout.LINE_START);
		line3Panel.add(line3Field, BorderLayout.LINE_END);

		// Create the vial number panel
		JLabel numberLabel = new JLabel("How many labels do you want to print:");
		numberField = new JTextField(5);
		numberField.setHorizontalAlignment(JTextField.CENTER);
		JPanel numberPanel = new JPanel(new BorderLayout(0, 0));
		numberPanel.add(numberLabel, BorderLayout.LINE_START);
		numberPanel.add(numberField, BorderLayout.LINE_END);

		// Create the button panel
		JButton printButton = new JButton("Print the labels, now!");
		printButton.addActionListener(new PrintListener());
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(printButton);

		// Create the label
		labelLine1 = new JLabel("");
		labelLine1.setFont(labelFont);
		labelLine1.setBounds(5, 7, 136, 16);

		labelLine2 = new JLabel("");
		labelLine2.setFont(labelFont);
		labelLine2.setBounds(5, 27, 136, 16);

		labelLine3 = new JLabel("");
		labelLine3.setFont(labelFont);
		labelLine3.setBounds(5, 47, 136, 16);

		JPanel label = new JPanel();
		label.setBorder(new LineBorder(Color.BLACK, 1));
		label.setBackground(Color.WHITE);
		label.setForeground(Color.BLACK);
		label.setLayout(null);

		label.setMinimumSize(new Dimension(labelWidth, labelHeight));
		label.setMaximumSize(new Dimension(labelWidth, labelHeight));
		label.setPreferredSize(new Dimension(labelWidth, labelHeight));
		label.setSize(labelWidth, labelHeight);

		label.add(labelLine1);
		label.add(labelLine2);
		label.add(labelLine3);

		// Compose the main panel
		JPanel basePanel = new JPanel();
		basePanel.setLayout(new BoxLayout(basePanel, BoxLayout.Y_AXIS));
		basePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		basePanel.add(line1Panel);
		basePanel.add(line2Panel);
		basePanel.add(line3Panel);
		basePanel.add(Box.createRigidArea(new Dimension(0, 10)));
		basePanel.add(new JSeparator(JSeparator.HORIZONTAL));
		basePanel.add(Box.createRigidArea(new Dimension(0, 10)));
		basePanel.add(label);
		basePanel.add(Box.createRigidArea(new Dimension(0, 10)));
		basePanel.add(new JSeparator(JSeparator.HORIZONTAL));
		basePanel.add(Box.createRigidArea(new Dimension(0, 10)));
		basePanel.add(numberPanel);
		basePanel.add(buttonPanel);

		// Allocate listeners
		line1Field.addKeyListener(new LineTextListener(line1Field, labelLine1));
		line2Field.addKeyListener(new LineTextListener(line2Field, labelLine2));
		line3Field.addKeyListener(new LineTextListener(line3Field, labelLine3));

		// Compose the dialog
		dialog = new JDialog(baseFrame, "Print Custom Labels");
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.addWindowListener(new FrameDisableListener(baseFrame, this));
		dialog.getContentPane().add(basePanel, BorderLayout.CENTER);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(baseFrame);
		dialog.setVisible(true);

	}

	private class LineTextListener implements KeyListener {

		private JTextField	field;
		private JLabel		label;


		public LineTextListener(JTextField field, JLabel label) {
			this.field = field;
			this.label = label;
		}


		@Override
		public void keyPressed(KeyEvent arg0) {
		}


		@Override
		public void keyReleased(KeyEvent arg0) {
			if (field.getText().length() > 22) {
				field.setText(field.getText().substring(0, 22));
			}
			label.setText(field.getText());
		}


		@Override
		public void keyTyped(KeyEvent arg0) {
		}

	}

	private class PrintListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			// Check the format of the number label
			if (!numberField.getText().matches("^\\d+$") || Integer.parseInt(numberField.getText()) < 1) {
				JOptionPane.showMessageDialog(dialog, "You must specify the number of labels as a positive integer, that means bigger than 0.", "Wrong Number Format", JOptionPane.ERROR_MESSAGE);
			}

			// Check if there is text entered
			else if (line1Field.getText().isEmpty() && line2Field.getText().isEmpty() && line2Field.getText().isEmpty()) {
				JOptionPane.showMessageDialog(dialog, "You did not write any text. Why do you want to print " + numberField.getText() + " empty labels?", "No Text", JOptionPane.ERROR_MESSAGE);
			}

			// If we have text and a number of vials, print them
			else {

				int numberOfLabels = Integer.parseInt(numberField.getText());
				if (numberOfLabels <= 15 || JOptionPane.showConfirmDialog(dialog, "Do you really want to print " + numberOfLabels + " labels?", "Please Check The Label Number", JOptionPane.WARNING_MESSAGE) == 0) {

					LabelPrinter.getInstance().printLabels(line1Field.getText(), line2Field.getText(), line3Field.getText(), numberOfLabels);

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
