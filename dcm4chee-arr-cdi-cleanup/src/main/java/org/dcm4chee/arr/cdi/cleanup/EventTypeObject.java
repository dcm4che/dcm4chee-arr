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

/**
 * The Class EventTypeObject.
 * Object for ldap or prefs configuration bound to a code 
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
public class EventTypeObject {
	
	String codeID;
	String RetentionTimeUnit;
	long RetentionTime;
	
	/**
     * Instantiates a new event type object.
     * 
     * @param code
     *            the code
     * @param retention
     *            the retention
     * @param unit
     *            the unit
     */
	public EventTypeObject(String code, long retention, String unit)
	{
		this.codeID=code;
		this.RetentionTime=retention;
		this.RetentionTimeUnit=unit;
	}
	
	/**
     * Gets the code id.
     * 
     * @return the code id
     */
	public String getCodeID() {
		return codeID;
	}
	
	/**
     * Sets the code id.
     * 
     * @param codeID
     *            the new code id
     */
	public void setCodeID(String codeID) {
		this.codeID = codeID;
	}
	
	/**
     * Gets the retention time unit.
     * 
     * @return the retention time unit
     */
	public String getRetentionTimeUnit() {
		return RetentionTimeUnit;
	}
	
	/**
     * Sets the retention time unit.
     * 
     * @param retentionTimeUnit
     *            the new retention time unit
     */
	public void setRetentionTimeUnit(String retentionTimeUnit) {
		RetentionTimeUnit = retentionTimeUnit;
	}
	
	/**
     * Gets the retention time.
     * 
     * @return the retention time
     */
	public long getRetentionTime() {
		return RetentionTime;
	}
	
	/**
     * Sets the retention time.
     * 
     * @param retentionTime
     *            the new retention time
     */
	public void setRetentionTime(long retentionTime) {
		RetentionTime = retentionTime;
	}
	

}
