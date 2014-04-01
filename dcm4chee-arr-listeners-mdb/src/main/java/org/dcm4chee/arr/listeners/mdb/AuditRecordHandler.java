/*
 *  ***** BEGIN LICENSE BLOCK ***** Version: MPL 1.1/GPL 2.0/LGPL 2.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
 * ANY KIND, either express or implied. See the License for the specific language governing rights
 * and limitations under the License.
 * 
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in Java(TM), hosted at
 * http://sourceforge.net/projects/dcm4che.
 * 
 * The Initial Developer of the Original Code is Gunter Zeilinger, Huetteldorferstr. 24/10, 1150
 * Vienna/Austria/Europe. Portions created by the Initial Developer are Copyright (C) 2002-2005 the
 * Initial Developer. All Rights Reserved.
 * 
 * Contributor(s): Gunter Zeilinger <gunterze@gmail.com>
 * 
 * Alternatively, the contents of this file may be used under the terms of either the GNU General
 * Public License Version 2 or later (the "GPL"), or the GNU Lesser General Public License Version
 * 2.1 or later (the "LGPL"), in which case the provisions of the GPL or the LGPL are applicable
 * instead of those above. If you wish to allow use of your version of this file only under the
 * terms of either the GPL or the LGPL, and not to allow others to use your version of this file
 * under the terms of the MPL, indicate your decision by deleting the provisions above and replace
 * them with the notice and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under the terms of any one of
 * the MPL, the GPL or the LGPL.
 * 
 * ***** END LICENSE BLOCK *****
 */

package org.dcm4chee.arr.listeners.mdb;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.dcm4chee.arr.entities.ActiveParticipant;
import org.dcm4chee.arr.entities.AuditRecord;
import org.dcm4chee.arr.entities.Code;
import org.dcm4chee.arr.entities.ParticipantObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The Class AuditRecordHandler. The audit record handler is used by the xml
 * reader in {@link ReceiverMDB} it is used to generate entities from the
 * received xml according to stylesheet
 * 
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jun 17, 2006
 */
public class AuditRecordHandler extends DefaultHandler {

    private static final Logger log = Logger
	    .getLogger(AuditRecordHandler.class);

    private final EntityManager em;

    private final AuditRecord rec;

    private ActiveParticipant ap;

    private ParticipantObject po;

    private StringBuffer sb = new StringBuffer(32);

    private boolean expectAuditSourceTypeCode;

    private boolean append;

    /**
     * Instantiates a new audit record handler.
     * 
     * @param em
     *            the entity manager to use for querying
     * @param rec
     *            the audit record
     */
    public AuditRecordHandler(EntityManager em, AuditRecord rec) {
	this.em = em;
	this.rec = rec;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length)
	    throws SAXException {
	if (append) {
	    sb.append(ch, start, length);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
     * java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName,
	    Attributes attrs) throws SAXException {
	if ("EventIdentification".equals(qName)) {
	    rec.setEventAction(attrs.getValue("EventActionCode"));
	    rec.setEventOutcome(getInt(attrs, "EventOutcomeIndicator",
		    "EventIdentification"));
	    rec.setEventDateTime(parseISO8601DateTime(attrs
		    .getValue("EventDateTime")));
	} else if ("EventID".equals(qName)) {
	    rec.setEventID(toCode(attrs));
	} else if ("EventTypeCode".equals(qName)) {
	    if (rec.getEventType() == null) {
		rec.setEventType(toCode(attrs));
	    } else {
		log.info("Received Audit Record with multiple Event Type Codes."
			+ " Only matching against first value supported!");
	    }
	} else if ("ActiveParticipant".equals(qName)) {
	    ap = new ActiveParticipant();
	    ap.setAuditRecord(rec);
	    ap.setUserID(toUpper(attrs.getValue("UserID")));
	    ap.setAlternativeUserID(toUpper(attrs.getValue("AlternativeUserID")));
	    ap.setUserName(toUpper(attrs.getValue("UserName")));
	    ap.setUserIsRequestor(!"false".equalsIgnoreCase(attrs
		    .getValue("UserIsRequestor")));
	    ap.setNetworkAccessPointID(toUpper(attrs
		    .getValue("NetworkAccessPointID")));
	    ap.setNetworkAccessPointType(getInt(attrs,
		    "NetworkAccessPointTypeCode", "ActiveParticipant"));
	    rec.addActiveParticipant(ap);
	} else if ("RoleIDCode".equals(qName)) {
	    if (ap.getRoleID() == null) {
		ap.setRoleID(toCode(attrs));
	    } else {
		log.info("Received Audit Record with multiple Role ID Codes "
			+ "for one Active Participant. Only matching against "
			+ "first value supported!");
	    }
	} else if ("AuditSourceIdentification".equals(qName)) {
	    if (rec.getSourceID() == null) {
		rec.setSourceID(toUpper(attrs.getValue("AuditSourceID")));
		rec.setEnterpriseSiteID(toUpper(attrs
			.getValue("AuditEnterpriseSiteID")));
		expectAuditSourceTypeCode = true;
	    } else {
		log.info("Received Audit Record with multiple Audit Source "
			+ "Identifications. Only matching against first value "
			+ "supported!");
	    }
	} else if ("AuditSourceTypeCode".equals(qName)) {
	    if (expectAuditSourceTypeCode) {
		if (rec.getSourceType() == 0) {
		    rec.setSourceType(getInt(attrs, "code",
			    "AuditSourceTypeCode"));
		} else {
		    log.info("Received Audit Record with multiple Audit Source "
			    + "Type Codes. Only matching against first value "
			    + "supported!");
		}
	    }
	} else if ("ParticipantObjectIdentification".equals(qName)) {
	    po = new ParticipantObject();
	    po.setAuditRecord(rec);
	    po.setObjectID(toUpper(attrs.getValue("ParticipantObjectID")));
	    po.setObjectType(getInt(attrs, "ParticipantObjectTypeCode",
		    "ParticipantObjectIdentification"));
	    po.setObjectRole(getInt(attrs, "ParticipantObjectTypeCodeRole",
		    "ParticipantObjectIdentification"));
	    po.setDataLifeCycle(getInt(attrs, "ParticipantObjectDataLifeCycle",
		    "ParticipantObjectIdentification"));
	    po.setObjectSensitivity(toUpper(attrs
		    .getValue("ParticipantObjectSensitivity")));
	    po.setObjectName(toUpper(attrs.getValue("ParticipantObjectName")));
	    rec.addParticipantObject(po);
	} else if ("ParticipantObjectIDTypeCode".equals(qName)) {
	    Code code = toCode(attrs);
	    if (code != null) {
		po.setObjectIDType(code);
	    } else {
		po.setObjectIDTypeRFC(getInt(attrs, "code",
			"ParticipantObjectIDTypeCode"));
	    }
	} else if ("ParticipantObjectName".equals(qName)) {
	    append = true;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName)
	    throws SAXException {
	if ("ActiveParticipant".equals(qName)) {
	    ap = null;
	} else if ("AuditSourceIdentification".equals(qName)) {
	    expectAuditSourceTypeCode = false;
	} else if ("ParticipantObjectIdentification".equals(qName)) {
	    po = null;
	} else if ("ParticipantObjectName".equals(qName)) {
	    po.setObjectName(toUpper(sb.toString()));
	}
	sb.setLength(0);
	append = false;

    }

    /**
     * Gets the attribute key.
     * 
     * @param attrs
     *            the attributes to check for key
     * @param attrName
     *            the attribute name
     * @param elName
     *            the element name
     * @return the int key to return
     */
    private int getInt(Attributes attrs, String attrName, String elName) {
	String val = attrs.getValue(attrName);
	if (val != null && val.trim().length() > 0)
	    try {
		return Integer.parseInt(val);
	    } catch (NumberFormatException e) {
		log.info("Expected int value but received <" + elName + " "
			+ attrName + "=\"" + val + "\"");
	    }
	return 0;
    }

    /**
     * Transforms a set of attributes into a code object.
     * 
     * @param attrs
     *            the attributes of a code
     * @return the code object created
     */
    private Code toCode(Attributes attrs) {
	String value = attrs.getValue("code");
	String designator = attrs.getValue("codeSystemName");
	if (value == null || designator == null) {
	    return null;
	}
	String meaning = attrs.getValue("displayName");
	Code code = findCode(value, designator);
	if (code != null)
	    return code;

	code = new Code();
	code.setValue(value);
	code.setDesignator(designator);
	code.setMeaning(meaning);
	try {
	    em.persist(code);
	    return code;
	} catch (EJBException ex) {
	    if (ex.getCause() instanceof EntityExistsException) {
		// A Code with the same values must be inserted by a concurrent
		// operation
		// so we just retrieve it
		return findCode(value, designator);
	    }
	    throw ex;
	}
    }

    /**
     * Queries a code object from the DB using JPQL with a certain value and
     * designator.
     * 
     * @param value
     *            the code value
     * @param designator
     *            the designator is the system name here (ie. DCM)
     * @return the code object retreived by the query
     */
    @SuppressWarnings("unchecked")
    private Code findCode(String value, String designator) {
	List<Code> queryResult = em
		.createQuery(
			"FROM org.dcm4chee.arr.entities.Code c WHERE "
				+ "c.value = :value AND c.designator = :designator")
		.setParameter("value", value)
		.setParameter("designator", designator)
		.setHint("org.hibernate.readOnly", Boolean.TRUE)
		.getResultList();
	if (!queryResult.isEmpty()) {
	    return queryResult.get(0);
	}
	return null;
    }

    /**
     * Returns a string to upper case.
     * 
     * @param s
     *            the string to use
     * @return the string in upper case
     */
    private static String toUpper(String s) {
	return s != null ? s.toUpperCase() : null;
    }

    /**
     * Parses the ISOdatetime to a timezone specific Date.
     * 
     * @param s
     *            the string timestamp
     * @return the date to return
     */
    private static Date parseISO8601DateTime(String s) {
	int tzindex = indexOfTimeZone(s);
	Calendar cal;
	if (tzindex == -1) {
	    cal = Calendar.getInstance();
	} else {
	    cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	    cal.set(Calendar.ZONE_OFFSET, timeZoneOffset(s, tzindex));
	    s = s.substring(0, tzindex);
	}
	int pos = 0;
	int len = 4;
	cal.set(Calendar.YEAR, Integer.parseInt(s.substring(pos, len)));
	if (!isDigit(s.charAt(pos += len))) {
	    ++pos;
	}
	len = !isDigit(s.charAt(pos + 1)) ? 1 : 2;
	cal.set(Calendar.MONTH,
		Integer.parseInt(s.substring(pos, pos + len)) - 1);
	if (!isDigit(s.charAt(pos += len))) {
	    ++pos;
	}
	len = !isDigit(s.charAt(pos + 1)) ? 1 : 2;
	cal.set(Calendar.DAY_OF_MONTH,
		Integer.parseInt(s.substring(pos, pos + len)));
	if (!isDigit(s.charAt(pos += len))) {
	    ++pos;
	}
	len = !isDigit(s.charAt(pos + 1)) ? 1 : 2;
	cal.set(Calendar.HOUR_OF_DAY,
		Integer.parseInt(s.substring(pos, pos + len)));
	if (!isDigit(s.charAt(pos += len))) {
	    ++pos;
	}
	len = !isDigit(s.charAt(pos + 1)) ? 1 : 2;
	cal.set(Calendar.MINUTE, Integer.parseInt(s.substring(pos, pos + len)));
	int sec = 0;
	int ms = 0;
	if ((pos += 2) < s.length()) {
	    if (!isDigit(s.charAt(pos))) {
		++pos;
	    }
	    float f = Float.parseFloat(s.substring(pos));
	    sec = (int) f;
	    ms = (int) ((f - sec) * 1000);
	}
	cal.set(Calendar.SECOND, sec);
	cal.set(Calendar.MILLISECOND, ms);
	return cal.getTime();
    }

    /**
     * Retreives the int index for a time zone.
     * 
     * @param s
     *            the string timestamp
     * @return the int index in timezones and -1 if cannot find the timezone
     *         offset in the timestamp
     */
    private static int indexOfTimeZone(String s) {
	int len = s.length();
	int index = len - 1;
	char c = s.charAt(index);
	if (c == 'Z') {
	    return index;
	}
	index = len - 6;
	c = s.charAt(index);
	if (c == '-' || c == '+') {
	    return index;
	}
	index = len - 3;
	c = s.charAt(index);
	if (c == '-' || c == '+') {
	    return index;
	}
	return -1;
    }

    /**
     * Time zone offset in seconds.
     * 
     * @param s
     *            the string timestamp
     * @param tzindex
     *            the time zone index retrieved earlier in indexOfTimeZone
     * @return the offset value in seconds
     */
    private static int timeZoneOffset(String s, int tzindex) {
	char c = s.charAt(tzindex);
	if (c == 'Z') {
	    return 0;
	}
	int off = Integer.parseInt(s.substring(tzindex + 1, tzindex + 3)) * 3600000;
	if (tzindex + 6 == s.length()) {
	    off += Integer.parseInt(s.substring(tzindex + 4)) * 60000;
	}
	return c == '-' ? -off : off;
    }

    /**
     * Checks if is digit.
     * 
     * @param c
     *            the char to check
     * @return true, if is digit
     */
    private static boolean isDigit(char c) {
	return c >= '0' && c <= '9';
    }
}
