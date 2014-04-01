package org.dcm4chee.arr.cdi.Impl;

import org.dcm4che3.net.Device;

public interface BasicEvent {
    
    /**
     * Checks if is state.
     * 
     * @return true, if is state
     */
    public boolean isState();

	/**
     * Sets the state.
     * 
     * @param state
     *            the new state
     */
	public void setState(boolean state) ;



    /**
     * Gets the device.
     * 
     * @return the device
     */
    public Device getDevice();
    
    /**
     * Gets the source.
     * 
     * @return the source
     */
    public Participant getSource();

}
