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

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

/**
 * The Class EventTypeFilter.
 * Contains the map of the children eventType
 * used by the filter extension as a data structure
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
public class EventTypeFilter {
	private HashMap<String, EventTypeObject> map = new HashMap<String, EventTypeObject>();

	/**
     * Gets the map.
     * Retrieves the map of all event types loaded from ldap or prefs
     * @return the map
     */
	public HashMap<String, EventTypeObject> getMap() {
		return map;
	}

	/**
     * Gets the event type.
     * retrieves one child 
     * @param code
     *            the code
     * @return the event type
     */
	public EventTypeObject getEventType(String code) {
		
		return map.get(code);
	}
	
	/**
     * Adds the event type.
     * adds a child to the map
     * @param code
     *            the code
     * @param obj
     *            the obj
     */
	public void addEventType(String code,EventTypeObject obj)
	{
		map.put(code, obj);
	}
	
	/**
     * Removes the event type.
     * 
     * @param code
     *            the code
     */
	public void removeEventType(String code)
	{
		map.remove(code);
	}
	   
   	/**
     * Gets the entries.
     * 
     * @return the entries
     */
   	public Set<Entry<String, EventTypeObject>> getEntries() {
	        return Collections.unmodifiableMap(map).entrySet();
	    }
	   
   	/**
     * Sets the map.
     * 
     * @param m
     *            the m
     */
   	public void setMap(HashMap<String, EventTypeObject> m)
	   {
	       this.map=m;
	   }

}
