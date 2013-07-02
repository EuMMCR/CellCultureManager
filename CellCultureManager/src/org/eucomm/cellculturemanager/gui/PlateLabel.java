package org.eucomm.cellculturemanager.gui;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;


public class PlateLabel extends JLabel {

	private static final long	serialVersionUID	= 1L;

	private String				cloneID;
	private int					width;
	private int					height;


	public PlateLabel(String cloneID, int x, int y, int width, int height) {
		this.cloneID = cloneID;
		this.width = width;
		this.height = height;
		this.setBounds(x, y, width, height);
		this.setOpaque(false);
	}


	@Override
	public void paintComponent(Graphics g) {

		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		if (height > width) {
			g2d.rotate(Math.toRadians(270), width / 2, height / 2);
		}

		g2d.setFont(this.getFont().deriveFont(Font.BOLD));
		FontMetrics metrics = getFontMetrics(g2d.getFont());

		g2d.setColor(Color.BLACK);
		g2d.drawString(cloneID, (width - metrics.stringWidth(cloneID)) / 2, (height / 2) + metrics.getDescent() + metrics.getLeading());

	}

}
