package org.eucomm.cellculturemanager.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.eucomm.cellculturemanager.controller.DatabaseProvider;

import de.codeworking.excelparser.Parser;
import de.codeworking.excelparser.ParserThawed;


public class SplashScreen {

	private static JFrame	splashScreen;


	public static void show() {

		splashScreen = new JFrame("Program is loading...");
		splashScreen.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		JLabel title = new JLabel("Cell Culture Manager");
		title.setFont(title.getFont().deriveFont(20f));
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);

		JLabel message1 = new JLabel("Please wait while the program is loading.");
		message1.setAlignmentX(JLabel.CENTER_ALIGNMENT);

		JLabel message2 = new JLabel("This may take a few seconds.");
		message2.setAlignmentX(JLabel.CENTER_ALIGNMENT);

		JPanel mainBox = new JPanel();
		mainBox.setLayout(new BoxLayout(mainBox, BoxLayout.Y_AXIS));
		mainBox.setBorder(new EmptyBorder(15, 15, 15, 15));
		mainBox.add(title);
		mainBox.add(Box.createRigidArea(new Dimension(0, 10)));
		mainBox.add(message1);
		mainBox.add(message2);

		JPanel borderPanel = new JPanel();
		borderPanel.setLayout(new BorderLayout());
		borderPanel.setBorder(new CompoundBorder(new LineBorder(Color.WHITE, 5), new LineBorder(Color.BLACK, 5)));

		borderPanel.add(mainBox, BorderLayout.CENTER);

		splashScreen.add(borderPanel, BorderLayout.CENTER);
		splashScreen.setUndecorated(true);
		splashScreen.pack();
		splashScreen.setVisible(true);
		splashScreen.setLocationRelativeTo(null);

		// Load the thawed clones from the Excel list into the database
		try {
			File file = new File("\\\\nas.scidom.de\\IDG\\Daten\\AG_Wurst\\blackboard_distribution_center\\");
			if (file.canRead() && DatabaseProvider.getInstance().haveToCheck()) {
				Parser parser = ParserThawed.getInstance();
				parser.parseAllSheets();
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(splashScreen, "I could not parse the Excel file.\nPlease inform your administrator.\nThank you!", "Excel Parser Error", JOptionPane.ERROR_MESSAGE);
		}

		new CellCultureManagerGui().createAndShowGUI();

	}


	public static void close() {
		if (splashScreen != null) {
			splashScreen.dispose();
		}
	}

}
