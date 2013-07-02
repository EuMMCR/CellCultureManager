package org.eucomm.cellculturemanager.gui;


import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.eucomm.cellculturemanager.controller.DatabaseProvider;
import org.eucomm.cellculturemanager.model.Plate;


public class CloneTableClickListener implements MouseListener {

	private Window					baseFrame;
	private JTable					table;

	private CellCultureManagerGui	ccManager;


	public CloneTableClickListener(Window baseFrame, JTable table, CellCultureManagerGui ccManager) {
		this.baseFrame = baseFrame;
		this.table = table;
		this.ccManager = ccManager;
	}


	@Override
	public void mouseClicked(MouseEvent e) {
		
		int row;
		
		// Handle double-click of the left mouse button
		if (e.getButton() == 1 && e.getClickCount() == 2) {
			row = table.rowAtPoint(e.getPoint());
			new PrintDialog().open(baseFrame, ccManager, CellCultureManagerGui.clones.get(row), row);
		}
		
		// Handle double-click of the right mouse button
		else if (e.getButton() == 3 && e.getClickCount() == 2) {
			row = table.rowAtPoint(e.getPoint());
			if (CellCultureManagerGui.clones.get(row).wasFrozen()) {
				Plate plate = DatabaseProvider.getInstance().loadPlate(CellCultureManagerGui.clones.get(row));
				if (plate != null) {
					PlateViewer.getInstance().showPlate(plate, baseFrame, ccManager);
				}
				else {
					JOptionPane.showMessageDialog(baseFrame, "Sorry, I could not find the clone on any plate in the cryo stock.", "No Plate", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
			
	}


	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}


	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}


	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}


	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}
