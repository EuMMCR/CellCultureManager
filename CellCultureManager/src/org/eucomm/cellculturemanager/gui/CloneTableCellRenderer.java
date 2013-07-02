package org.eucomm.cellculturemanager.gui;


import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;


public class CloneTableCellRenderer extends JLabel implements TableCellRenderer {

	private static final long	serialVersionUID			= 1L;

	private Font				font;

	public static final Color	BACKCROUND					= new Color(255, 255, 255);
	public static final Color	BACKGROUND_PRINTED			= new Color(255, 192, 0);
	public static final Color	BACKGROUND_SCANNED			= new Color(255, 255, 0);
	public static final Color	BACKGROUND_FROZEN			= new Color(0, 255, 0);
	public static final Color	BACKGROUND_DEAD				= new Color(128, 128, 128);

	public static final Color	BACKGROUND_SELECTED			= new Color(125, 192, 255);
	public static final Color	BACKGROUND_SELECTED_PRINTED	= new Color(190, 192, 128);
	public static final Color	BACKGROUND_SELECTED_SCANNED	= new Color(190, 224, 128);
	public static final Color	BACKGROUND_SELECTED_FROZEN	= new Color(63, 224, 128);
	public static final Color	BACKGROUND_SELECTED_DEAD	= new Color(127, 160, 192);

	public static final Color	FOREGROUND					= new Color(0, 0, 0);
	public static final Color	FOREGROUND_PRINTED			= new Color(136, 64, 0);
	public static final Color	FOREGROUND_SCANNED			= new Color(128, 128, 0);
	public static final Color	FOREGROUND_FROZEN			= new Color(0, 64, 0);
	public static final Color	FOREGROUND_DEAD				= new Color(204, 204, 204);

	private int					outerBorderWidth			= 0;
	private int					innerBorderWidth			= 5;


	public CloneTableCellRenderer() {
		this.setOpaque(true);
		this.font = this.getFont().deriveFont(Font.PLAIN);
	}


	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		this.setText((String) value);

		if (CellCultureManagerGui.clones.get(row).isDead()) {
			if (isSelected) {
				this.setBackground(BACKGROUND_SELECTED_DEAD);
			}
			else {
				this.setBackground(BACKGROUND_DEAD);
			}
			this.setForeground(FOREGROUND_DEAD);
		}
		else if (CellCultureManagerGui.clones.get(row).wasFrozen()) {
			if (isSelected) {
				this.setBackground(BACKGROUND_SELECTED_FROZEN);
			}
			else {
				this.setBackground(BACKGROUND_FROZEN);
			}
			this.setForeground(FOREGROUND_FROZEN);
		}
		else if (CellCultureManagerGui.clones.get(row).wasScanned()) {
			if (isSelected) {
				this.setBackground(BACKGROUND_SELECTED_SCANNED);
			}
			else {
				this.setBackground(BACKGROUND_SCANNED);
			}
			this.setForeground(FOREGROUND_SCANNED);
		}
		else if (CellCultureManagerGui.clones.get(row).wasPrinted()) {
			if (isSelected) {
				this.setBackground(BACKGROUND_SELECTED_PRINTED);
			}
			else {
				this.setBackground(BACKGROUND_PRINTED);
			}
			this.setForeground(FOREGROUND_PRINTED);
		}
		else {
			if (isSelected) {
				this.setBackground(BACKGROUND_SELECTED);
			}
			else {
				this.setBackground(BACKCROUND);
			}
			this.setForeground(FOREGROUND);
			this.setFont(font);
		}

		// if (CellCultureManagerGui.clones.get(row).isDead()) {
		//
		// }
		// else if (CellCultureManagerGui.clones.get(row).wasPrinted()) {
		// if (CellCultureManagerGui.clones.get(row).wasScanned()) {
		//
		// }
		// else {
		//
		// }
		// this.setFont(font.deriveFont(Font.ITALIC));
		// }
		// else {
		//
		// }

		this.setBorder(new CompoundBorder(new LineBorder(this.getBackground(), outerBorderWidth), new EmptyBorder(innerBorderWidth, innerBorderWidth, innerBorderWidth, innerBorderWidth)));

		if (column > 1) {
			this.setHorizontalAlignment(CENTER);
		}
		else {
			this.setHorizontalAlignment(LEFT);
		}

		return this;

	}

}
