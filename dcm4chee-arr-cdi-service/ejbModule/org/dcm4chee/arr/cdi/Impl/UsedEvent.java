/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chee.arr.cdi.Impl;

import org.dcm4che3.net.Device;

/**
 * The Class UsedEvent.
 * An event that indicates audit record repository has been used (Queried via rest service)
 * 
 * @author Hesham Elbadawi bsdreko@gmail.com
 */
public class UsedEvent {
	
	 private Device device;
	 private RemoteSource source;
	    
    	/**
     * Sets the remote source.
     * 
     * @param source
     *            the new source
     */
    	public void setSource(RemoteSource source) {
			this.source = source;
		}
		private boolean state;
	    
	    /**
     * Checks if is state.
     * 
     * @return true, if is state
     */
    	public boolean isState() {
			return state;
		}

		/**
	 * Sets the state.
	 * 
	 * @param state
	 *            the new state
	 */
		public void setState(boolean state) {
			this.state = state;
		}

		/**
	 * Instantiates a new used event.
	 * 
	 * @param state
	 *            the state
	 * @param device
	 *            the device
	 * @param source
	 *            the source
	 */
	    public UsedEvent(boolean state,Device device, RemoteSource source) {
	        super();
	        this.device = device;
	        this.source = source;
	        this.state=state;
	    }

	    /**
     * Gets the device.
     * 
     * @return the device
     */
    	public Device getDevice() {
	        return device;
	    }
	    
    	/**
     * Gets the source.
     * 
     * @return the source
     */
    	public RemoteSource getSource() {
	        return source;
	    }

    
    

}
