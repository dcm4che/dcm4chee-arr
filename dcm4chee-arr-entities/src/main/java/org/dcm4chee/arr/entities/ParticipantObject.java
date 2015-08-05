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

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ParticipantObject.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
@Entity
@Table(name = "arr_part_obj")
@Access(AccessType.FIELD)
public class ParticipantObject implements Serializable {

    private static final long serialVersionUID = -6663278166268884512L;

    private static final Logger log = LoggerFactory.getLogger(ParticipantObject.class);
        
    @Id
    @GeneratedValue
    @Column(name = "pk")
    private long pk;

    @ManyToOne
    @JoinColumn(name = "audit_record_fk")
    private AuditRecord auditRecord;
    
    @Column(name = "obj_id")
    private String objectID;

    
    @Column(name = "obj_type")
    private int objectType;

    @Column(name = "obj_role")
    private int objectRole;
    
    @Column(name = "data_life_cycle")
    private int dataLifeCycle;

    @Column(name = "obj_sensitivity")
    private String objectSensitivity;

    @Column(name = "obj_id_type_rfc")
    private int objectIDTypeRFC;
    
    @ManyToOne
    @JoinColumn(name = "obj_id_type_fk")
    private Code objectIDType;

    @Column(name = "name")
    private String objectName;


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
     * Gets the audit record.
     * 
     * @return the audit record
     */
    public AuditRecord getAuditRecord() {
        return auditRecord;
    }

    /**
     * Sets the audit record.
     * 
     * @param auditRecord
     *            the new audit record
     */
    public void setAuditRecord(AuditRecord auditRecord) {
        this.auditRecord = auditRecord;
    }


    /**
     * Gets the object id type.
     * 
     * @return the object id type
     */
    public Code getObjectIDType() {
        return objectIDType;
    }

    /**
     * Sets the object id type.
     * 
     * @param code
     *            the new object id type
     */
    public void setObjectIDType(Code code) {
        this.objectIDType = code;
    }

    
    /**
     * Gets the object id type rfc.
     * 
     * @return the object id type rfc
     */
    public int getObjectIDTypeRFC() {
        return objectIDTypeRFC;
    }

    /**
     * Sets the object id type rfc.
     * 
     * @param code
     *            the new object id type rfc
     */
    public void setObjectIDTypeRFC(int code) {
        this.objectIDTypeRFC = code;
    }

    
    /**
     * Gets the object name.
     * 
     * @return the object name
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * Sets the object name.
     * 
     * @param name
     *            the new object name
     */
    public void setObjectName(String name) {
        this.objectName = name;
    }


    /**
     * Gets the object id.
     * 
     * @return the object id
     */
    public String getObjectID() {
        return objectID;
    }

    /**
     * Sets the object id.
     * 
     * @param id
     *            the new object id
     */
    public void setObjectID(String id) {
        this.objectID = id;
    }

    
    /**
     * Gets the object type.
     * 
     * @return the object type
     */
    public int getObjectType() {
        return objectType;
    }

    /**
     * Sets the object type.
     * 
     * @param code
     *            the new object type
     */
    public void setObjectType(int code) {
        this.objectType = code;
    }


    /**
     * Gets the object role.
     * 
     * @return the object role
     */
    public int getObjectRole() {
        return objectRole;
    }

    /**
     * Sets the object role.
     * 
     * @param code
     *            the new object role
     */
    public void setObjectRole(int code) {
        this.objectRole = code;
    }

    
    /**
     * Gets the data life cycle.
     * 
     * @return the data life cycle
     */
    public int getDataLifeCycle() {
        return dataLifeCycle;
    }

    /**
     * Sets the data life cycle.
     * 
     * @param code
     *            the new data life cycle
     */
    public void setDataLifeCycle(int code) {
        this.dataLifeCycle = code;
    }

    
    /**
     * Gets the object sensitivity.
     * 
     * @return the object sensitivity
     */
    public String getObjectSensitivity() {
        return objectSensitivity;
    }

    /**
     * Sets the object sensitivity.
     * 
     * @param sensitivity
     *            the new object sensitivity
     */
    public void setObjectSensitivity(String sensitivity) {
        this.objectSensitivity = sensitivity;
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
        return "ParticipantObject[pk=" + pk + ", id=" + objectID + "]";
    }
}
