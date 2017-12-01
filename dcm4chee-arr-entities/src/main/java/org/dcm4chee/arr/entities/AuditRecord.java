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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4chee.arr.entities;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AuditRecord.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
@Entity
@Table(name = "arr_audit_record")
@Access(AccessType.FIELD)
public class AuditRecord implements Serializable {

    private static final long serialVersionUID = -4138396955398529612L;

    private static final Logger log = LoggerFactory.getLogger(AuditRecord.class);
    
    public enum AuditFormat {
    	UNKNOWN, IHEYR4, SUP95, DICOM
    }
    
    @Id
    @GeneratedValue
    @Column(name = "pk")
    private long pk;
    
    @ManyToOne( fetch=FetchType.LAZY )
    @BatchSize( size=200 )
    @JoinColumn(name = "event_id_fk")
    @Index(name="ar_eventid_fk")
    private Code eventID;
    
    @Column(name = "event_action")
    private String eventAction;
    
    @Column(name = "event_outcome")
    private int eventOutcome;
    
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "event_date_time")
    @Index(name = "ar_event_date_time")
    private Date eventDateTime;
    
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "receive_date_time")
    @Index(name = "ar_receive_date_ti")
    private Date receiveDateTime;
    
    @ManyToOne( fetch=FetchType.LAZY )
    @BatchSize( size=200 )
    @JoinColumn(name = "event_type_fk")
    @Index(name="ar_eventtype_fk")
    private Code eventType;
    
    @Column(name = "site_id")
    private String enterpriseSiteID;
    
    @Column(name = "source_id")
    private String sourceID;
    
    @Column(name = "source_type")
    private int sourceType;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "auditRecord",targetEntity = ActiveParticipant.class)
    @Fetch( FetchMode.SELECT )
    @BatchSize( size=200 )
    private Set<ActiveParticipant> activeParticipants;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "auditRecord")
    @Fetch( FetchMode.SELECT )
    @BatchSize( size=200 )
    private Set<ParticipantObject> participantObjects;

    @Column(name = "audit_format")
    private AuditFormat auditFormat = AuditFormat.UNKNOWN;

    @Column(name = "is_due_delete", nullable = false, updatable = true)
    private boolean isDueDelete=false;

    @Lob
    @Column(name = "xmldata", length = 262144)
    private byte[] xmldata;

    
    public AuditRecord() {
        // empty
    }

    /**
     * Gets the pk.
     * 
     * @return the pk
     */
    public long getPk() {
        return pk;
    }

    /**
     * Sets the pk.
     * 
     * @param pk
     *            the new pk
     */
    public void setPk(long pk) {
        this.pk = pk;
    }


    /**
     * Gets the event id.
     * 
     * @return the event id
     */
    public Code getEventID() {
        return eventID;
    }

    /**
     * Sets the event id.
     * 
     * @param eventID
     *            the new event id
     */
    public void setEventID(Code eventID) {
        this.eventID = eventID;
    }


    /**
     * Gets the event type.
     * 
     * @return the event type
     */
    public Code getEventType() {
        return eventType;
    }

    /**
     * Sets the event type.
     * 
     * @param eventTypeCode
     *            the new event type
     */
    public void setEventType(Code eventTypeCode) {
        this.eventType = eventTypeCode;
    }


    /**
     * Gets the enterprise site id.
     * 
     * @return the enterprise site id
     */
    public String getEnterpriseSiteID() {
        return enterpriseSiteID;
    }

    /**
     * Sets the enterprise site id.
     * 
     * @param auditEnterpriseSiteID
     *            the new enterprise site id
     */
    public void setEnterpriseSiteID(String auditEnterpriseSiteID) {
        this.enterpriseSiteID = auditEnterpriseSiteID;
    }


    /**
     * Gets the source id.
     * 
     * @return the source id
     */
    public String getSourceID() {
        return sourceID;
    }

    /**
     * Sets the source id.
     * 
     * @param auditSourceID
     *            the new source id
     */
    public void setSourceID(String auditSourceID) {
        this.sourceID = auditSourceID;
    }


    /**
     * Gets the source type.
     * 
     * @return the source type
     */
    public int getSourceType() {
        return sourceType;
    }

    /**
     * Sets the source type.
     * 
     * @param typeCode
     *            the new source type
     */
    public void setSourceType(int typeCode) {
        this.sourceType = typeCode;
    }


    /**
     * Gets the active participants.
     * 
     * @return the active participants
     */
    public Collection<ActiveParticipant> getActiveParticipants() {
        return activeParticipants;
    }

    /**
     * Sets the active participants.
     * 
     * @param c
     *            the new active participants
     */
    public void setActiveParticipants(Set<ActiveParticipant> c) {
        this.activeParticipants = c;
    }

    /**
     * Adds the active participant.
     * 
     * @param ap
     *            the ap
     */
    public void addActiveParticipant(ActiveParticipant ap) {
        if (activeParticipants == null) {
            activeParticipants = new LinkedHashSet<ActiveParticipant>(3);
        }
        activeParticipants.add(ap);
        ap.setAuditRecord(this);
    }


    /**
     * Gets the participant objects.
     * 
     * @return the participant objects
     */
    public Collection<ParticipantObject> getParticipantObjects() {
        return participantObjects;
    }

    /**
     * Sets the participant objects.
     * 
     * @param c
     *            the new participant objects
     */
    public void setParticipantObjects(Set<ParticipantObject> c) {
        this.participantObjects = c;
    }

    /**
     * Adds the participant object.
     * 
     * @param po
     *            the po
     */
    public void addParticipantObject(ParticipantObject po) {
        if (participantObjects == null) {
            participantObjects = new LinkedHashSet<ParticipantObject>(3);
        }
        participantObjects.add(po);
        po.setAuditRecord(this);
    }

    
    /**
     * Gets the event action.
     * 
     * @return the event action
     */
    public String getEventAction() {
        return eventAction;
    }

    /**
     * Sets the event action.
     * 
     * @param eventActionCode
     *            the new event action
     */
    public void setEventAction(String eventActionCode) {
        this.eventAction = eventActionCode;
    }


    /**
     * Gets the event outcome.
     * 
     * @return the event outcome
     */
    public int getEventOutcome() {
        return eventOutcome;
    }

    /**
     * Sets the event outcome.
     * 
     * @param eventOutcomeIndicator
     *            the new event outcome
     */
    public void setEventOutcome(int eventOutcomeIndicator) {
        this.eventOutcome = eventOutcomeIndicator;
    }


    /**
     * Gets the event date time.
     * 
     * @return the event date time
     */
    public Date getEventDateTime() {
        return eventDateTime;
    }

    /**
     * Sets the event date time.
     * 
     * @param dt
     *            the new event date time
     */
    public void setEventDateTime(Date dt) {
        this.eventDateTime = dt;
    }


    /**
     * Gets the receive date time.
     * 
     * @return the receive date time
     */
    public Date getReceiveDateTime() {
        return receiveDateTime;
    }

    /**
     * Sets the receive date time.
     * 
     * @param dt
     *            the new receive date time
     */
    public void setReceiveDateTime(Date dt) {
        this.receiveDateTime = dt;
    }


    /**
     * Checks if is IHE yr4.
     * 
     * @return true, if is IHE yr4
     */
    public AuditFormat getAuditFormat() {
        return auditFormat;
    }

    /**
     * Sets the IHE yr4.
     * 
     * @param iheYr4
     *            the new IHE yr4
     */
    public void setAuditFormat(AuditFormat format) {
        this.auditFormat = format;
    }


    /**
     * Gets the xmldata.
     * 
     * @return the xmldata
     */
    public byte[] getXmldata() {
        return xmldata;
    }

    /**
     * Sets the xmldata.
     * 
     * @param xmldata
     *            the new xmldata
     */
    public void setXmldata(byte[] xmldata) {
        this.xmldata = xmldata;
    }

    public boolean isDueDelete() {
        return isDueDelete;
    }

    public void setDueDelete(boolean isDueDelete) {
        this.isDueDelete = isDueDelete;
    }

    /**
     * Post persit.
     */
    @PostPersist
    public void postPersit() {
        if (log.isDebugEnabled())
            log.debug("Created " + this.toString());
    }

    /**
     * Post remove.
     */
    @PostRemove
    public void postRemove() {
        if (log.isDebugEnabled())
            log.debug("Removed " + this.toString());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "AuditRecord[pk=" + pk + ", eventDateTime=" + eventDateTime
                + ", receiveDateTime=" + receiveDateTime + ", sourceID="
                + sourceID + "]";
    }
}
