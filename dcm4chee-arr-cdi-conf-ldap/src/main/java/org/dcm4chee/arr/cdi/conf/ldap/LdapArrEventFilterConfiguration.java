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
package org.dcm4chee.arr.cdi.conf.ldap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.ldap.LdapDicomConfigurationExtension;
import org.dcm4che3.conf.ldap.LdapUtils;
import org.dcm4che3.net.Device;
import org.dcm4chee.arr.cdi.cleanup.EventTypeFilter;
import org.dcm4chee.arr.cdi.cleanup.EventTypeFilterExtension;
import org.dcm4chee.arr.cdi.cleanup.EventTypeObject;

/**
 * The Class LdapArrEventFilterConfiguration. Event Filtering ldap config
 * extenstion
 * 
 * @author Hesham Elbadawi (bsdreko@gmail.com)
 */
public class LdapArrEventFilterConfiguration extends
	LdapDicomConfigurationExtension {

    private static final String CN_EVENT_TYPE_LOGGING_FILTER = "cn=Event Type Logging Filter,";

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dcm4che3.conf.ldap.LdapDicomConfigurationExtension#storeChilds(java
     * .lang.String, org.dcm4che3.net.Device)
     */
    @Override
    protected void storeChilds(String deviceDN, Device device)
	    throws NamingException {
	EventTypeFilterExtension ext = device
		.getDeviceExtension(EventTypeFilterExtension.class);
	if (ext != null)
	    store(deviceDN, ext.getEventTypeFilter());
    }

    /**
     * Dn of.
     * 
     * @param code
     *            the code
     * @param eventFilterDN
     *            the event filter dn
     * @return the string
     */
    private String dnOf(String code, String eventFilterDN) {
	return LdapUtils.dnOf("eventIDTypeCode", code, eventFilterDN);
    }

    /**
     * Store.
     * 
     * @param deviceDN
     *            the device dn
     * @param filter
     *            the filter
     * @throws NamingException
     *             the naming exception
     */
    private void store(String deviceDN, EventTypeFilter filter)
	    throws NamingException {
	String eventFilterDN = CN_EVENT_TYPE_LOGGING_FILTER + deviceDN;
	config.createSubcontext(eventFilterDN, LdapUtils.attrs(
		"arrEventTypeFilter", "cn", "Event Type Logging Filter"));
	for (Entry<String, EventTypeObject> entry : filter.getEntries()) {
	    String code = entry.getKey();
	    config.createSubcontext(dnOf(code, eventFilterDN),
		    storeTo(code, entry.getValue(), new BasicAttributes(true)));
	}
    }

    /**
     * Store to.
     * 
     * @param code
     *            the code
     * @param obj
     *            the obj
     * @param attrs
     *            the attrs
     * @return the attributes
     */
    private Attributes storeTo(String code, EventTypeObject obj,
	    Attributes attrs) {
	attrs.put("objectclass", "arrEventType");
	attrs.put("eventIDTypeCode", code);
	attrs.put("eventTypeRetention", obj.getRetentionTime());
	LdapUtils.storeNotNull(attrs, "eventTypeRetentionUnit",
		obj.getRetentionTimeUnit());
	return attrs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dcm4che3.conf.ldap.LdapDicomConfigurationExtension#loadChilds(org
     * .dcm4che3.net.Device, java.lang.String)
     */
    @Override
    protected void loadChilds(Device device, String deviceDN)
	    throws NamingException, ConfigurationException {
	String eventFilterDN = CN_EVENT_TYPE_LOGGING_FILTER + deviceDN;
	try {
	    config.getAttributes(eventFilterDN);
	} catch (NameNotFoundException e) {
	    return;
	}

	EventTypeFilter filter = new EventTypeFilter();

	NamingEnumeration<SearchResult> ne = config.search(eventFilterDN,
		"(objectclass=arrEventType)");
	try {
	    while (ne.hasMore()) {
		SearchResult sr = ne.next();
		Attributes attrs = sr.getAttributes();
		filter.addEventType(
			LdapUtils.stringValue(attrs.get("eventIDTypeCode"),
				null),
			new EventTypeObject(LdapUtils.stringValue(
				attrs.get("eventIDTypeCode"), null), LdapUtils
				.intValue(attrs.get("eventTypeRetention"), 1),
				LdapUtils.stringValue(
					attrs.get("eventTypeRetentionUnit"),
					null)));
	    }
	} finally {
	    LdapUtils.safeClose(ne);
	}
	device.addDeviceExtension(new EventTypeFilterExtension(filter));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dcm4che3.conf.ldap.LdapDicomConfigurationExtension#mergeChilds(org
     * .dcm4che3.net.Device, org.dcm4che3.net.Device, java.lang.String)
     */
    @Override
    protected void mergeChilds(Device prev, Device device, String deviceDN)
	    throws NamingException {
	EventTypeFilterExtension prevExt = prev
		.getDeviceExtension(EventTypeFilterExtension.class);
	EventTypeFilterExtension ext = device
		.getDeviceExtension(EventTypeFilterExtension.class);
	if (ext == null) {
	    if (prevExt != null)
		config.destroySubcontextWithChilds(CN_EVENT_TYPE_LOGGING_FILTER
			+ deviceDN);
	    return;
	}
	if (prevExt == null) {
	    store(deviceDN, ext.getEventTypeFilter());
	    return;
	}
	String eventFilterDN = CN_EVENT_TYPE_LOGGING_FILTER + deviceDN;
	EventTypeFilter filter = ext.getEventTypeFilter();
	EventTypeFilter prevFilter = prevExt.getEventTypeFilter();
	for (Entry<String, EventTypeObject> entry : prevFilter.getEntries()) {
	    String code = entry.getKey();
	    if (filter.getEventType(code) == null)
		config.destroySubcontext(dnOf(code, eventFilterDN));
	}
	for (Entry<String, EventTypeObject> entry : filter.getEntries()) {
	    String code = entry.getKey();
	    String dn = dnOf(code, eventFilterDN);
	    EventTypeObject prevObj = prevFilter.getEventType(code);
	    if (prevObj == null)
		config.createSubcontext(
			dn,
			storeTo(code, entry.getValue(), new BasicAttributes(
				true)));
	    else
		config.modifyAttributes(
			dn,
			storeDiffs(prevObj, entry.getValue(),
				new ArrayList<ModificationItem>()));
	}
    }

    /**
     * Store diffs.
     * 
     * @param prevObj
     *            the prev obj
     * @param Obj
     *            the obj
     * @param mods
     *            the mods
     * @return the list
     */
    private List<ModificationItem> storeDiffs(EventTypeObject prevObj,
	    EventTypeObject Obj, List<ModificationItem> mods) {
	LdapUtils.storeDiff(mods, "eventIDTypeCode", prevObj.getCodeID(),
		Obj.getCodeID());
	LdapUtils.storeDiff(mods, "eventTypeRetention",
		prevObj.getRetentionTime(), Obj.getRetentionTime());
	LdapUtils.storeDiff(mods, "eventTypeRetentionUnit",
		prevObj.getRetentionTimeUnit(), Obj.getRetentionTimeUnit());
	return mods;
    }

}
