package org.eucomm.cellculturemanager.gui;


public interface FrameDisabler {

	/**
	 * This function determines whether the baseFrame should be disabled on loading the FrameDisabler.
	 * 
	 * @return boolean
	 */
	public boolean disableBaseFrame();


	/**
	 * This function determines whether if the baseFrame should be enabled on disposing the FrameDisabler.
	 * 
	 * @return boolean
	 */
	public boolean enableBaseFrame();

}
