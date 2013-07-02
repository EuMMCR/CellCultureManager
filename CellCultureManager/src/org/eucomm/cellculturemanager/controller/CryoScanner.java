package org.eucomm.cellculturemanager.controller;


import org.eucomm.cellculturemanager.model.Plate;


public interface CryoScanner {

	public Plate scan(Plate plate) throws Exception;

}
