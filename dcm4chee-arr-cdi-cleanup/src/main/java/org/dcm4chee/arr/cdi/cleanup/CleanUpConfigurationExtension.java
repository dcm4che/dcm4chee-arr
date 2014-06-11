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
package org.dcm4chee.arr.cdi.cleanup;

import org.dcm4che3.conf.api.generic.ConfigClass;
import org.dcm4che3.conf.api.generic.ConfigField;
import org.dcm4che3.conf.api.generic.ReflectiveConfig;
import org.dcm4che3.net.DeviceExtension;

/**
 * The Class CleanUpConfigurationExtension.
 * a generic device extension for cleanup configuration
 * @author Hesham Elbadawi bsdreko@gmail.com
 */
@ConfigClass(commonName = "CleanUpConfigurationExtension", objectClass = "arrCleanUp", nodeName = "arrCleanUp")
public class CleanUpConfigurationExtension extends DeviceExtension {

	private static final long serialVersionUID = 7013531480908387184L;

	//checks if retention time is to be used for deletion
	@ConfigField(name="arrCleanUpUsesRetention" , def="false" )
	private boolean arrCleanUpUsesRetention;

	//checks if max record count is to be used for deletion
	@ConfigField(name="arrCleanUpUsesMaxRecords" ,def="false")
	private boolean arrCleanUpUsesMaxRecords;

	//specify max number of records to keep
	@ConfigField(name="arrCleanUpMaxRecords", def="19")
	private int arrCleanUpMaxRecords;
	
	//specify poll interval (service starts to delete each poll interval)
	@ConfigField(name="arrCleanUpPollInterval" ,def="3600")
	private int arrCleanUpPollInterval;
	
	//specify retention time (together with the retention unit are used to compare with every object creation date to delte or not) ie 1MONTH
	@ConfigField(name="arrCleanUpRetentionTime" , def="1")
	private int arrCleanUpRetentionTime;
	
	//specify the unit for retention time will use Java TimeUnit enumeration
	@ConfigField(name="arrCleanUpRetentionTimeUnit" ,def="DAYS")
	private String arrCleanUpRetentionTimeUnit;
	
	@ConfigField(name="arrCleanUpDeletePerTransaction", def="2")
	private int arrCleanUpDeletePerTransaction;

	@ConfigField(name="arrDefaultCleanUpPolicy", def="all")
	private String arrDefaultCleanUpPolicy;
	
	@ConfigField(name="arrsafeClean", def="true")
	private boolean arrSafeClean;
	
	@ConfigField(name="arrBackUpPollInterval", def="3600")
        private int arrBackUpPollInterval;
	
	@ConfigField(name="arrBackUpDir", def="Audit-Record-Repository-BackUps")
        private String arrBackUpDir;
	
	public boolean isArrSafeClean() {
        return arrSafeClean;
    }

    public void setArrSafeClean(boolean arrSafeClean) {
        this.arrSafeClean = arrSafeClean;
    }

    public int getArrBackUpPollInterval() {
        return arrBackUpPollInterval;
    }

    public void setArrBackUpPollInterval(int arrBackUpPollInterval) {
        this.arrBackUpPollInterval = arrBackUpPollInterval;
    }

    public String getArrBackUpDir() {
        return arrBackUpDir;
    }

    public void setArrBackUpDir(String arrBackUpDir) {
        this.arrBackUpDir = arrBackUpDir;
    }

    /**
     * Gets the arr default clean up policy.
     * 
     * @return the arr default clean up policy
     */
	public String getArrDefaultCleanUpPolicy() {
		return arrDefaultCleanUpPolicy;
	}

	/**
     * Sets the arr default clean up policy.
     * 
     * @param arrDefaultCleanUpPolicy
     *            the new arr default clean up policy
     */
	public void setArrDefaultCleanUpPolicy(String arrDefaultCleanUpPolicy) {
		this.arrDefaultCleanUpPolicy = arrDefaultCleanUpPolicy;
	}

	/**
     * Gets the arr clean up delete per transaction.
     * 
     * @return the arr clean up delete per transaction
     */
	public int getArrCleanUpDeletePerTransaction() {
		return arrCleanUpDeletePerTransaction;
	}

	/**
     * Sets the arr clean up delete per transaction.
     * 
     * @param arrCleanUpDeletePerTransaction
     *            the new arr clean up delete per transaction
     */
	public void setArrCleanUpDeletePerTransaction(
			int arrCleanUpDeletePerTransaction) {
		this.arrCleanUpDeletePerTransaction = arrCleanUpDeletePerTransaction;
	}

	/**
     * Checks if is arr clean up uses retention.
     * 
     * @return true, if is arr clean up uses retention
     */
	public boolean isArrCleanUpUsesRetention() {
		return arrCleanUpUsesRetention;
	}

	/**
     * Sets the arr clean up uses retention.
     * 
     * @param arrCleanUpUsesRetention
     *            the new arr clean up uses retention
     */
	public void setArrCleanUpUsesRetention(boolean arrCleanUpUsesRetention) {
		this.arrCleanUpUsesRetention = arrCleanUpUsesRetention;
	}

	/**
     * Checks if is arr clean up uses max records.
     * 
     * @return true, if is arr clean up uses max records
     */
	public boolean isArrCleanUpUsesMaxRecords() {
		return arrCleanUpUsesMaxRecords;
	}

	/**
     * Sets the arr clean up uses max records.
     * 
     * @param arrCleanUpUsesMaxRecords
     *            the new arr clean up uses max records
     */
	public void setArrCleanUpUsesMaxRecords(boolean arrCleanUpUsesMaxRecords) {
		this.arrCleanUpUsesMaxRecords = arrCleanUpUsesMaxRecords;
	}

	/**
     * Gets the arr clean up max records.
     * 
     * @return the arr clean up max records
     */
	public int getArrCleanUpMaxRecords() {
		return arrCleanUpMaxRecords;
	}

	/**
     * Sets the arr clean up max records.
     * 
     * @param arrCleanUpMaxRecords
     *            the new arr clean up max records
     */
	public void setArrCleanUpMaxRecords(int arrCleanUpMaxRecords) {
		this.arrCleanUpMaxRecords = arrCleanUpMaxRecords;
	}

	/**
     * Gets the arr clean up poll interval.
     * 
     * @return the arr clean up poll interval
     */
	public int getArrCleanUpPollInterval() {
		return arrCleanUpPollInterval;
	}

	/**
     * Sets the arr clean up poll interval.
     * 
     * @param arrCleanUpPollInterval
     *            the new arr clean up poll interval
     */
	public void setArrCleanUpPollInterval(int arrCleanUpPollInterval) {
		this.arrCleanUpPollInterval = arrCleanUpPollInterval;
	}

	/**
     * Gets the arr clean up retention time.
     * 
     * @return the arr clean up retention time
     */
	public int getArrCleanUpRetentionTime() {
		return arrCleanUpRetentionTime;
	}

	/**
     * Sets the arr clean up retention time.
     * 
     * @param arrCleanUpRetentionTime
     *            the new arr clean up retention time
     */
	public void setArrCleanUpRetentionTime(int arrCleanUpRetentionTime) {
		this.arrCleanUpRetentionTime = arrCleanUpRetentionTime;
	}

	/**
     * Gets the arr clean up retention time unit.
     * 
     * @return the arr clean up retention time unit
     */
	public String getArrCleanUpRetentionTimeUnit() {
		return arrCleanUpRetentionTimeUnit;
	}

	/**
     * Sets the arr clean up retention time unit.
     * 
     * @param arrCleanUpRetentionTimeUnit
     *            the new arr clean up retention time unit
     */
	public void setArrCleanUpRetentionTimeUnit(String arrCleanUpRetentionTimeUnit) {
		this.arrCleanUpRetentionTimeUnit = arrCleanUpRetentionTimeUnit;
	}
		
	 /* (non-Javadoc)
 	 * @see org.dcm4che3.net.DeviceExtension#reconfigure(org.dcm4che3.net.DeviceExtension)
 	 */
 	@Override
	    public void reconfigure(DeviceExtension from){
	      CleanUpConfigurationExtension clUpExt=(CleanUpConfigurationExtension)from;
	      ReflectiveConfig.reconfigure(clUpExt, this);
	    }
}
