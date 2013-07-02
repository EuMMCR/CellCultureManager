package org.eucomm.cellculturemanager.gui;


import java.awt.Component;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


public class FrameDisableListener implements WindowListener {

	private Component		baseFrame;
	private FrameDisabler	source;
	private boolean			closed	= false;


	public FrameDisableListener(Component baseFrame, FrameDisabler source) {
		this.baseFrame = baseFrame;
		this.source = source;

		if (source.disableBaseFrame()) {
			baseFrame.setEnabled(false);
		}
	}


	@Override
	public void windowActivated(WindowEvent e) {

	}


	@Override
	public void windowClosed(WindowEvent e) {
		if (!closed && source.enableBaseFrame()) {
			baseFrame.setEnabled(true);
			baseFrame.setVisible(true);
			closed = true;
		}
	}


	@Override
	public void windowClosing(WindowEvent e) {

	}


	@Override
	public void windowDeactivated(WindowEvent e) {

	}


	@Override
	public void windowDeiconified(WindowEvent e) {

	}


	@Override
	public void windowIconified(WindowEvent e) {

	}


	@Override
	public void windowOpened(WindowEvent e) {

	}

}
