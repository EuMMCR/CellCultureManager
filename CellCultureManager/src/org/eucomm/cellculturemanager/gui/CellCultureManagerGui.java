package org.eucomm.cellculturemanager.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.JTableHeader;

import org.eucomm.cellculturemanager.CellCultureManager;
import org.eucomm.cellculturemanager.controller.CCM_Utils;
import org.eucomm.cellculturemanager.controller.CloneTableModel;
import org.eucomm.cellculturemanager.controller.DatabaseProvider;
import org.eucomm.cellculturemanager.model.Clone;


public class CellCultureManagerGui {

	CloneTableModel					model;
	public static List<Clone>		clones			= new ArrayList<Clone>();

	private JFrame					baseFrame;

	private CloneTableClickListener	tableListener;
	private CloneTableCellRenderer	cellRenderer;

	private JTextField				searchField;
	private JComboBox<String>		technicianCellBox;
	private JComboBox<String>		technicianMoleBox;

	public static Font				labelFont		= null;

	private String[]				techniciansCell;
	private String[]				techniciansMole;

	String[]						thawedYears;
	String[]						thawedMonths	= { "----", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
	String[]						thawedDays		= { "--", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" };

	private boolean					resetFilter		= false;

	private int						elementHeight	= 20;


	public void createAndShowGUI() {

		// Create the main application frame
		baseFrame = new JFrame("Cell Culture Manager (Version " + CellCultureManager.version + ")");
		baseFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Load the font for the labels
		labelFont = CCM_Utils.getFont(baseFrame);

		// Create the ActionListener that updates the clones
		ActionListener cloneActionListener = new CloneActionListener();

		// Fetch all recently thawed clones
		clones = getThawedClones();

		// Create the table model
		String[] tableHeader = { "Clone ID", "Freezing Plate", "Cell Line", "Gene Name", "TA Cell", "TA Mol", "Thawed", "Vials", "Tank", "Rack", "Shelf", "Position" };
		model = new CloneTableModel(tableHeader);

		// Create the table
		JTable table = new JTable(model);
		cellRenderer = new CloneTableCellRenderer();
		tableListener = new CloneTableClickListener(baseFrame, table, this);
		table.setDefaultRenderer(String.class, cellRenderer);
		table.addMouseListener(tableListener);
		table.setPreferredScrollableViewportSize(new Dimension(975, 500));
		table.setFillsViewportHeight(true);
		table.setGridColor(new Color(204, 204, 204));

		// Format the table header
		JTableHeader header = table.getTableHeader();
		Font headerFont = table.getFont().deriveFont(Font.BOLD);
		header.setFont(headerFont);
		header.setReorderingAllowed(false);
		header.setPreferredSize(new Dimension(header.getPreferredSize().getSize().width, 30));
		header.setResizingAllowed(true);

		// Format the table rows
		table.getColumnModel().getColumn(0).setPreferredWidth(140);
		table.getColumnModel().getColumn(1).setPreferredWidth(115);
		table.getColumnModel().getColumn(2).setPreferredWidth(85);
		table.getColumnModel().getColumn(3).setPreferredWidth(110);
		table.getColumnModel().getColumn(4).setPreferredWidth(85);
		table.getColumnModel().getColumn(5).setPreferredWidth(85);
		table.getColumnModel().getColumn(6).setPreferredWidth(80);
		table.getColumnModel().getColumn(7).setPreferredWidth(50);
		table.getColumnModel().getColumn(8).setPreferredWidth(50);
		table.getColumnModel().getColumn(9).setPreferredWidth(50);
		table.getColumnModel().getColumn(10).setPreferredWidth(50);
		table.getColumnModel().getColumn(11).setPreferredWidth(75);

		table.setRowHeight(30);
		table.setBorder(null);

		// Create the scroll pane that contains the table and add it to the frame
		JScrollPane tablePane = new JScrollPane(table);
		tablePane.setBorder(new MatteBorder(2, 0, 2, 0, Color.black));

		String[] tmpTechniciansCell = DatabaseProvider.getInstance().getTechniciansCell();
		techniciansCell = new String[tmpTechniciansCell.length + 1];
		System.arraycopy(tmpTechniciansCell, 0, techniciansCell, 1, tmpTechniciansCell.length);
		techniciansCell[0] = "-- select all --";

		String[] tmpTechniciansMole = DatabaseProvider.getInstance().getTechniciansMole();
		techniciansMole = new String[tmpTechniciansMole.length + 1];
		System.arraycopy(tmpTechniciansMole, 0, techniciansMole, 1, tmpTechniciansMole.length);
		techniciansMole[0] = "-- select all --";

		// Create the molecular biology technician filter panel
		technicianMoleBox = new JComboBox<String>(techniciansMole);
		technicianMoleBox.addActionListener(cloneActionListener);
		technicianMoleBox.setPreferredSize(new Dimension(100, elementHeight));
		JPanel moleFilterTopPanel = new JPanel(new FlowLayout(SwingConstants.CENTER, 0, 0));
		moleFilterTopPanel.add(new JLabel("Select TA Mole"));
		JPanel moleFilterBottomPanel = new JPanel(new FlowLayout(SwingConstants.CENTER, 0, 0));
		moleFilterBottomPanel.add(technicianMoleBox);

		JPanel moleFilterPanel = new JPanel(new BorderLayout(0, 2));
		moleFilterPanel.setBorder(new EmptyBorder(5, 20, 5, 20));
		moleFilterPanel.add(moleFilterTopPanel, BorderLayout.PAGE_START);
		moleFilterPanel.add(moleFilterBottomPanel, BorderLayout.PAGE_END);

		// Create the cell culture technician filter panel
		technicianCellBox = new JComboBox<String>(techniciansCell);
		technicianCellBox.addActionListener(cloneActionListener);
		technicianCellBox.setPreferredSize(new Dimension(100, 20));
		technicianCellBox.setPreferredSize(new Dimension(100, elementHeight));
		JPanel cellFilterTopPanel = new JPanel(new FlowLayout(SwingConstants.CENTER, 0, 0));
		cellFilterTopPanel.add(new JLabel("Select TA Cell:"));
		JPanel cellFilterBottomPanel = new JPanel(new FlowLayout(SwingConstants.CENTER, 0, 0));
		cellFilterBottomPanel.add(technicianCellBox);

		JPanel cellFilterPanel = new JPanel(new BorderLayout(0, 2));
		cellFilterPanel.setBorder(new EmptyBorder(5, 20, 5, 20));
		cellFilterPanel.add(cellFilterTopPanel, BorderLayout.PAGE_START);
		cellFilterPanel.add(cellFilterBottomPanel, BorderLayout.PAGE_END);

		// Create the search panel
		searchField = new JTextField();
		searchField.setHorizontalAlignment(SwingConstants.CENTER);
		searchField.setPreferredSize(new Dimension(120, elementHeight));
		searchField.addKeyListener(new CloneActionListener());
		JButton searchButton = new JButton("search");
		searchButton.addActionListener(cloneActionListener);
		searchButton.setPreferredSize(new Dimension(searchButton.getPreferredSize().width, elementHeight));

		JPanel searchTopPanel = new JPanel(new FlowLayout(SwingConstants.CENTER, 0, 0));
		searchTopPanel.add(new JLabel("Search for a clone ID or gene name:"));

		JPanel searchBottomPanel = new JPanel(new FlowLayout(SwingConstants.CENTER, 2, 0));
		searchBottomPanel.add(searchField);
		searchBottomPanel.add(searchButton);

		JPanel searchPanel = new JPanel(new BorderLayout(0, 2));
		searchPanel.setBorder(new EmptyBorder(5, 20, 5, 20));
		searchPanel.add(searchTopPanel, BorderLayout.PAGE_START);
		searchPanel.add(searchBottomPanel, BorderLayout.PAGE_END);

		// Create the reset panel
		JButton resetButton = new JButton("reset filters");
		resetButton.setPreferredSize(new Dimension(resetButton.getPreferredSize().width, elementHeight));
		resetButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resetFilter = true;
				searchField.setText("");
				technicianCellBox.setSelectedIndex(0);
				technicianMoleBox.setSelectedIndex(0);
				resetFilter = false;
				updateClones(getThawedClones());
			}

		});
		JPanel resetPanel = new JPanel(new FlowLayout(SwingConstants.CENTER));
		resetPanel.setBorder(new EmptyBorder(5, 20, 5, 20));
		resetPanel.add(resetButton);

		// Create the remove vials panel
		JButton removeVialsButton = new JButton("Remove Vials");
		removeVialsButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new VialScanner(CellCultureManagerGui.this).openRemoveWindow(baseFrame);
			}

		});

		JButton scanButton = new JButton("Scan Vials");
		scanButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ScanDialog.getInstance().showDialog(baseFrame, CellCultureManagerGui.this);
			}

		});

		JButton loadPlateButton = new JButton("Load Plate");
		loadPlateButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				PlatePositionSelection.getInstance().showLoader(baseFrame, CellCultureManagerGui.this);
			}

		});

		JButton customPrinterButton = new JButton("Print Labels");
		customPrinterButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				CustomPrinterDialog.getInstance().showDialog(baseFrame);
			}

		});

		JButton showHelpButton = new JButton("Help / Legend");
		showHelpButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Legend.getInstance().show(baseFrame);
			}

		});

		// Create the clone selection panel
		JPanel cloneSelectionPanel = new JPanel();
		cloneSelectionPanel.setLayout(new FlowLayout());
		cloneSelectionPanel.add(moleFilterPanel);
		cloneSelectionPanel.add(cellFilterPanel);
		cloneSelectionPanel.add(searchPanel);
		cloneSelectionPanel.add(resetPanel);

		// Create the action panel
		Box actionPanel = Box.createHorizontalBox();
		actionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		actionPanel.add(removeVialsButton);
		actionPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		actionPanel.add(Box.createHorizontalGlue());
		actionPanel.add(scanButton);
		actionPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		actionPanel.add(loadPlateButton);
		actionPanel.add(Box.createHorizontalGlue());
		actionPanel.add(customPrinterButton);
		actionPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		actionPanel.add(showHelpButton);

		baseFrame.getContentPane().add(cloneSelectionPanel, BorderLayout.PAGE_START);
		baseFrame.getContentPane().add(tablePane, BorderLayout.CENTER);
		baseFrame.getContentPane().add(actionPanel, BorderLayout.PAGE_END);

		baseFrame.pack();
		baseFrame.setLocationRelativeTo(null);
		baseFrame.setVisible(true);

		SplashScreen.close();

	}


	/**
	 * Fetch the clones that were thawed in the last four weeks for a distinct technician. To fetch all clones, taName
	 * must be <code>null</code>.
	 * 
	 * @return a list of clones
	 */
	private List<Clone> getThawedClones() {

		String cellTA = technicianCellBox != null ? (String) technicianCellBox.getSelectedItem() : "-";
		if (cellTA.startsWith("-")) {
			cellTA = null;
		}

		String moleTA = technicianMoleBox != null ? (String) technicianMoleBox.getSelectedItem() : "-";
		if (moleTA.startsWith("-")) {
			moleTA = null;
		}

		if (searchField != null) {
			searchField.setText(searchField.getText().trim());
		}

		String searchTerm = searchField != null ? searchField.getText().toUpperCase().trim() : "";

		return DatabaseProvider.getInstance().getThawedClones(cellTA, moleTA, searchTerm);

	}


	private void updateClones() {
		model.update();
	}


	private void updateClones(List<Clone> newClones) {
		if (newClones.size() == 0) {
			JOptionPane.showMessageDialog(baseFrame, "Sorry, I did not find any clone that fits your filter.\nPlease change your filters or search term.", "Nothing found", JOptionPane.ERROR_MESSAGE);
		}
		else {
			clones = newClones;
			updateClones();
		}
	}


	public void updateCloneTable() {
		updateClones(getThawedClones());
	}


	public void updateClone(int index, Clone clone) {
		clones.set(index, clone);
		updateClones();
	}

	private class CloneActionListener implements ActionListener, KeyListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!resetFilter) {
				updateClones(getThawedClones());
			}
		}


		@Override
		public void keyPressed(KeyEvent e) {

		}


		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				updateClones(getThawedClones());
			}
		}


		@Override
		public void keyTyped(KeyEvent e) {

		}

	}

}
