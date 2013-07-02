package org.eucomm.cellculturemanager.controller;


import javax.swing.table.AbstractTableModel;

import org.eucomm.cellculturemanager.gui.CellCultureManagerGui;
import org.eucomm.cellculturemanager.model.Clone;


public class CloneTableModel extends AbstractTableModel {

	private static final long	serialVersionUID	= 1L;

	private String[]			headerNames;


	public CloneTableModel(String[] headerNames) {
		this.headerNames = headerNames;
	}


	@Override
	public String getColumnName(int columnIndex) {
		return headerNames[columnIndex];
	}


	@Override
	public int getColumnCount() {
		return headerNames.length;
	}


	@Override
	public int getRowCount() {
		return CellCultureManagerGui.clones.size();
	}


	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}


	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Clone clone = CellCultureManagerGui.clones.get(rowIndex);
		String cellValue;
		switch (columnIndex) {
			case 0:
				cellValue = clone.getTaggedCloneID();
				break;
			case 1:
				cellValue = clone.getFreezingPlateID();
				break;
			case 2:
				cellValue = clone.getCellLine();
				break;
			case 3:
				cellValue = clone.getGeneName();
				break;
			case 4:
				cellValue = clone.getTechnicianThawing();
				break;
			case 5:
				cellValue = clone.getTechnicianQC();
				break;
			case 6:
				cellValue = clone.getThawDateString();
				break;
			case 7:
				cellValue = Integer.toString(clone.getVialNumber());
				break;
			case 8:
				cellValue = clone.wasFrozen() ? Integer.toString(clone.getTank()) : "";
				break;
			case 9:
				cellValue = clone.wasFrozen() ? Integer.toString(clone.getRack()) : "";
				break;
			case 10:
				cellValue = clone.wasFrozen() ? Integer.toString(clone.getShelf()) : "";
				break;
			case 11:
				cellValue = clone.wasFrozen() ? clone.getPosition() : "";
				break;
			default:
				cellValue = "";
		}
		return cellValue;
	}


	public void update() {
		this.fireTableDataChanged();
	}

}
