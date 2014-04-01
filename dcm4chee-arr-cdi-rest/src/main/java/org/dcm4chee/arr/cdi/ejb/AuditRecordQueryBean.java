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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2010
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below.
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

package org.dcm4chee.arr.cdi.ejb;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.Stateless;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import org.dcm4chee.arr.entities.AuditRecord;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

/**
 * The Class AuditRecordQueryBean.
 * used to query entities for records based on the given restrictions
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date:: xxxx-xx-xx $
 * @since Feb 10, 2010
 */
@Stateless
public class AuditRecordQueryBean implements AuditRecordQueryLocal {

    @PersistenceContext(unitName="dcm4chee-arr")
    private transient Session session;

    /* (non-Javadoc)
     * @see org.dcm4chee.arr.cdi.ejb.AuditRecordQueryLocal#findRecords(javax.servlet.http.HttpServletRequest)
     */
    @SuppressWarnings("unchecked")
    public List<byte[]> findRecords(HttpServletRequest rq) {

        try {
            Criteria criteria = session.createCriteria(AuditRecord.class);
            buildCriteria(criteria, rq);
            
            List<byte[]> result = criteria.list();
            return result;
        }
        catch(Exception e)
        {
        	return null;
        }
    }

    /**
     * Builds the criteria.
     * adds restrictions on the query for each element of the record
     * @param criteria
     *            the criteria
     * @param rq
     *            the rq
     */
    private static void buildCriteria(Criteria criteria,
            HttpServletRequest rq) {
        criteria.setResultTransformer(Criteria.ROOT_ENTITY);
        criteria.setProjection(Projections.property("xmldata"));
        addCriteriaForEvent(criteria, rq);
        addCriteriaForActiveParticipant(criteria, rq);
        addCriteriaForAuditSource(criteria, rq);
        addCriteriaForParticipantObject(criteria, rq);
        criteria.addOrder(Order.desc("eventDateTime"));
        setMaxResults(criteria, rq);
    }

    /**
     * Adds the criteria for event.
     * 
     * @param criteria
     *            the criteria
     * @param rq
     *            the rq
     */
    private static void addCriteriaForEvent(Criteria criteria,
            HttpServletRequest rq) {

        Date lowerDateTime = getDateTime(rq, "lowerDateTime");
        if (lowerDateTime != null)
            criteria.add(Restrictions.ge("eventDateTime", lowerDateTime));

        Date upperDateTime = getDateTime(rq, "upperDateTime");
        if (upperDateTime != null)
            criteria.add(Restrictions.le("eventDateTime", upperDateTime));

        String[][] eventIDs = getCodes(rq, "eventID");
        if (eventIDs != null)
            criteria.createAlias("eventID", "ei").add(
                    getCodeStringCriteria("ei", eventIDs));

        String[][] eventTypes = getCodes(rq, "eventTypeCode");
        if (eventTypes != null)
            criteria.createAlias("eventType", "et").add(
                    getCodeStringCriteria("et", eventTypes));

        Integer[] eventOutcomes = getIntegers(rq, "eventOutcomeIndicator");
        if (eventOutcomes != null)
            criteria.add(Restrictions.in("eventOutcome", eventOutcomes));

        String[] eventActions = rq.getParameterValues("eventActionCode");
        if (eventActions != null)
            criteria.add(Restrictions.in("eventAction", eventActions));
    }

    /**
     * Adds the criteria for active participant.
     * 
     * @param criteria
     *            the criteria
     * @param rq
     *            the rq
     */
    private static void addCriteriaForActiveParticipant(Criteria criteria,
            HttpServletRequest rq) {

        String userID = rq.getParameter("userID");
        String altUserID = rq.getParameter("alternativeUserID");
        String userName = rq.getParameter("userName");
        Boolean userIsRequestor = getBoolean(rq, "userIsRequestor");
        String[][] roleIDs = getCodes(rq, "roleIDCode");
        String napID = rq.getParameter("networkAccessPointID");
        String[] napTypes = rq.getParameterValues("networkAccessPointTypeCode");

        if (userID != null || altUserID != null || userName != null
                || userIsRequestor != null || roleIDs != null || napID != null
                || napTypes != null) {
            criteria.createAlias("activeParticipants", "ap1");
            Conjunction criterion = Restrictions.conjunction();

            if (userID != null)
                criterion.add(Restrictions.like("ap1.userID", userID.toUpperCase()));

            if (altUserID != null)
                criterion.add(Restrictions.like("ap1.alternativeUserID",
                        altUserID.toUpperCase()));

            if (userName != null)
                criterion.add(Restrictions.like("ap1.userName", userName.toUpperCase()));

            if (userIsRequestor != null)
                criterion.add(Restrictions.eq("ap1.userIsRequestor",
                        userIsRequestor));

            if (roleIDs != null) {
                criteria.createAlias("ap1.roleID", "rid1");
                criterion.add(getCodeStringCriteria("rid1", roleIDs));
            }

            if (napID != null)
                criterion.add(Restrictions.like("ap1.networkAccessPointID",
                        napID.toUpperCase()));

            if (napTypes != null)
                criterion.add(Restrictions.in("ap1.networkAccessPointType",
                        napTypes));

            criteria.add(criterion);
        }
    }

    /**
     * Adds the criteria for audit source.
     * 
     * @param criteria
     *            the criteria
     * @param rq
     *            the rq
     */
    private static void addCriteriaForAuditSource(Criteria criteria,
            HttpServletRequest rq) {
        String sourceID = rq.getParameter("auditSourceID");
        if (sourceID != null)
            criteria.add(Restrictions.like("sourceID", sourceID.toUpperCase()));
        
        String siteID = rq.getParameter("auditEnterpriseSiteID");
        if (siteID != null)
            criteria.add(Restrictions.like("enterpriseSiteID", siteID.toUpperCase()));

        Integer[] sourceTypes = getIntegers(rq, "auditSourceTypeCode");
        if (sourceTypes != null)
            criteria.add(Restrictions.in("sourceType", sourceTypes));
    }

    /**
     * Adds the criteria for participant object.
     * 
     * @param criteria
     *            the criteria
     * @param rq
     *            the rq
     */
    private static void addCriteriaForParticipantObject(Criteria criteria,
            HttpServletRequest rq) {

        String objectID = rq.getParameter("participantObjectID");
        String[] objectIDTypes =  rq.getParameterValues("participantObjectTypeCode");
        String objectName = rq.getParameter("participantObjectName");
        Integer[] objectTypes = getIntegers(rq, "participantObjectTypeCode");
        Integer[] objectRoles = getIntegers(rq, "participantObjectTypeCodeRole");
        Integer[] lifeCycles = getIntegers(rq, "participantObjectDataLifeCycle");

        if (objectID != null || objectIDTypes != null
                || objectName != null || objectTypes != null
                || objectRoles != null || lifeCycles != null) {
            criteria.createAlias("participantObjects", "po");
            if (objectID != null)
                criteria.add(Restrictions.like("po.objectID", objectID.toUpperCase()));

             if (objectIDTypes != null) {
                // Split the IDTypes into two sets, one for code, which is String;
                // one for RFC, which is integer
                List<Integer> rfcIDTypes = new ArrayList<Integer>(objectIDTypes.length);
                List<String[]> codeIDTypes = new ArrayList<String[]>(objectIDTypes.length);
                for (String idType : objectIDTypes) {
                    String[] code = idType.split("\\^");
                    if (code.length >= 2)
                        codeIDTypes.add(code);
                    else
                        try {
                            rfcIDTypes.add(Integer.valueOf(idType));
                        } catch (IllegalArgumentException e) {
                                throw new IllegalArgumentException(
                                        "Error:participantObjectTypeCode parameter is invalid! "
                                        + "Must be an integer or in format code^codeSystemName");
                        }
                }

                if (!codeIDTypes.isEmpty())
                    criteria.createAlias("po.objectIDType", "pooit");

                if (!rfcIDTypes.isEmpty()) {
                    if (!codeIDTypes.isEmpty()) {
                        criteria.add(Restrictions.or(getCodeStringCriteria("pooit",
                                codeIDTypes.toArray(new String[][] {})),
                                Restrictions.in("po.objectIDTypeRFC",
                                        rfcIDTypes.toArray(new Integer[] {}))));
                    } else
                        criteria.add(Restrictions.in("po.objectIDTypeRFC",
                                rfcIDTypes.toArray(new Integer[] {})));
                } else
                    criteria.add(getCodeStringCriteria("pooit",
                            codeIDTypes.toArray(new String[][] {})));
            }

            if (objectName != null)
                criteria.add(Restrictions.like("po.objectName", objectName.toUpperCase()));

            if (objectTypes != null)
                criteria.add(Restrictions.in("po.objectType", objectTypes));

            if (objectRoles != null)
                criteria.add(Restrictions.in("po.objectRole", objectRoles));

            if (lifeCycles != null)
                criteria.add(Restrictions.in("po.dataLifeCycle", lifeCycles));
        }
    }

    /**
     * Gets the code string criteria.
     * 
     * @param alias
     *            the alias
     * @param codeStrings
     *            the code strings
     * @return the code string criteria
     */
    private static Disjunction getCodeStringCriteria(String alias,
            String[][] codeStrings) {
        Disjunction disjuncation = Restrictions.disjunction();
        for (String codeString[] : codeStrings) {
            disjuncation.add(Restrictions.conjunction().add(
            		Restrictions.eq(alias + ".value", codeString[0])).add(
            				Restrictions.eq(alias + ".designator", codeString[1])));
        }
        return disjuncation;
    }


    /**
     * Sets the max results.
     * max results in the max number of result a criteria will retrieve from the session
     * @param criteria
     *            the criteria
     * @param rq
     *            the rq
     */
    private static void setMaxResults(Criteria criteria,
            HttpServletRequest rq) {
        int mostRecentResults = getInt(rq, "mostRecentResults");
        if (mostRecentResults > 0)
            criteria.setMaxResults(mostRecentResults);
    }

    /**
     * Gets the integer from a string request to be used in maximum results
     * 
     * @param rq
     *            the rq
     * @param name
     *            the name
     * @return the int
     */
    private static int getInt(HttpServletRequest rq, String name) {
        String s = rq.getParameter(name);
        if (s == null)
            return 0;

        try {
            return Integer.parseInt(s);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Error: " + name 
                    + " parameter is invalid! Must be an integer");
        }
    }

    /**
     * Gets the boolean from a string request to be used in criteria restriction
     * 
     * @param rq
     *            the rq
     * @param name
     *            the name
     * @return the boolean
     */
    private static Boolean getBoolean(HttpServletRequest rq, String name) {
        String s = rq.getParameter(name);
        if (s == null)
            return null;

        if (s.equalsIgnoreCase("true"))
            return Boolean.TRUE;

        if (s.equalsIgnoreCase("false"))
            return Boolean.FALSE;

        throw new IllegalArgumentException("Error: " + name 
                + " parameter is invalid! Must be \"true\" or \"false\"");
    }


    /**
     * Gets the integers from a string request to be used for criteria restrictions.
     * 
     * @param rq
     *            the rq
     * @param name
     *            the name
     * @return the integers
     */
    private static Integer[] getIntegers(HttpServletRequest rq, String name) {
        String[] ss = rq.getParameterValues(name);
        if (ss == null)
            return null;

        try {
            Integer[] is = new Integer[ss.length];
            for (int i = 0; i < ss.length; i++)
                is[i] = Integer.valueOf(ss[i]);
            return is;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Error: " + name 
                    + " parameter is invalid! Must be an integer");
        }
    }

    /**
     * Gets the date time from a string request to be used for criteria restrictions.
     * 
     * @param rq
     *            the rq
     * @param name
     *            the name
     * @return the date time
     */
    private static Date getDateTime(HttpServletRequest rq, String name) {
        String s = rq.getParameter(name);
        if (s == null)
            return null;

        try {
            int tzindex = indexOfTimeZone(s);
            Calendar cal;
            if (tzindex == -1) {
                cal = Calendar.getInstance();
            } else {
                cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                cal.set(Calendar.ZONE_OFFSET, timeZoneOffset(s, tzindex));
                s = s.substring(0, tzindex);
            }
            cal.set(Calendar.YEAR, Integer.parseInt(s.substring(0, 4)));
            cal.set(Calendar.MONTH, Integer.parseInt(s.substring(5, 7)) - 1);
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(s.substring(8, 10)));
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(s.substring(11, 13)));
            cal.set(Calendar.MINUTE, Integer.parseInt(s.substring(14, 16)));
            cal.set(Calendar.SECOND, Integer.parseInt(s.substring(17, 19)));
            cal.set(Calendar.MILLISECOND, s.length() <= 20 ? 0 :
                    (int) (Float.parseFloat(s.substring(19)) * 1000));
            return cal.getTime();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error: " + name 
                    + " parameter is invalid! Must be in format yyyy-mm-ddThh:mm:ss.");
        }
    }

    /**
     * Index of time zone.
     * 
     * @param s
     *            the s
     * @return the int
     */
    private static int indexOfTimeZone(String s) {
        int len = s.length();
        int index = len - 1;
        char c = s.charAt(index);
        if (c == 'Z')
            return index;

        index = len - 6;
        c = s.charAt(index);
        if (c == '-' || c == '+')
            return index;

        return -1;
    }

    /**
     * Time zone offset.
     * 
     * @param s
     *            the s
     * @param tzindex
     *            the tzindex
     * @return the int
     */
    private static int timeZoneOffset(String s, int tzindex) {
        char c = s.charAt(tzindex);
        if (c == 'Z')
            return 0;

        int off = Integer.parseInt(s.substring(tzindex + 1, tzindex + 3)) * 3600000
                + Integer.parseInt(s.substring(tzindex + 4)) * 60000;
        return c == '-' ? -off : off;
    }

    /**
     * Gets the codes.
     * used to get a separate code and codesystem name data structure (String[][]) from the request to be used for querying
     * can be used with any attribute of type code
     * @param rq
     *            the rq
     * @param name
     *            the name
     * @return the codes
     */
    private static String[][] getCodes(HttpServletRequest rq,
            String name) {
        String[] ss = rq.getParameterValues(name);
        if (ss == null)
            return null;

        String[][] codes = new String[ss.length][];
        for (int i = 0; i < ss.length; i++)
            if ((codes[i] = ss[i].split("_")).length < 2)
                throw new IllegalArgumentException("Error: " + name 
                        + " parameter is invalid! Must be in format code^codeSystemName");
        return codes;
    }

}
