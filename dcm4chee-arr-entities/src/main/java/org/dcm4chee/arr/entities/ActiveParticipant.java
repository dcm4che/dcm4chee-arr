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

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ActiveParticipant.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */

@Entity
@Table(name = "arr_active_part")
@Access(AccessType.FIELD)
public class ActiveParticipant implements Serializable {

    private static final long serialVersionUID = 513457139488147710L;

    private static final Logger log = LoggerFactory.getLogger(ActiveParticipant.class);
    
    @Id
    @GeneratedValue
    @Column(name = "pk")
    private long pk;
    
    @ManyToOne
    @JoinColumn(name = "audit_record_fk")
    @Index(name = "ap_ar_fk")
    private AuditRecord auditRecord;
    
    @Column(name = "user_id")
    private String userID;
    
    @Column(name = "alt_user_id")
    private String alternativeUserID;
    
    @Column(name = "user_name")
    private String userName;

    @Column(name = "requestor")
    private boolean userIsRequestor;

    @Column(name = "net_access_pt_id")
    private String networkAccessPointID;

    @Column(name = "net_access_pt_type")
    private int networkAccessPointType;
    
    @ManyToOne
    @JoinColumn(name = "role_id_fk")
    @Index(name = "ap_role_fk")
    private Code roleID;

 
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
     * Gets the role id.
     * 
     * @return the role id
     */
    public Code getRoleID() {
        return roleID;
    }

    /**
     * Sets the role id.
     * 
     * @param roleID
     *            the new role id
     */
    public void setRoleID(Code roleID) {
        this.roleID = roleID;
    }

   
    /**
     * Gets the user id.
     * 
     * @return the user id
     */
    public String getUserID() {
        return userID;
    }

    /**
     * Sets the user id.
     * 
     * @param userID
     *            the new user id
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    
    /**
     * Gets the alternative user id.
     * 
     * @return the alternative user id
     */
    public String getAlternativeUserID() {
        return alternativeUserID;
    }

    /**
     * Sets the alternative user id.
     * 
     * @param alternativeUserID
     *            the new alternative user id
     */
    public void setAlternativeUserID(String alternativeUserID) {
        this.alternativeUserID = alternativeUserID;
    }

    
    /**
     * Gets the user name.
     * 
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the user name.
     * 
     * @param userName
     *            the new user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    
    /**
     * Gets the user is requestor.
     * 
     * @return the user is requestor
     */
    public boolean getUserIsRequestor() {
        return userIsRequestor;
    }

    /**
     * Sets the user is requestor.
     * 
     * @param userIsRequestor
     *            the new user is requestor
     */
    public void setUserIsRequestor(boolean userIsRequestor) {
        this.userIsRequestor = userIsRequestor;
    }

    
    /**
     * Gets the network access point id.
     * 
     * @return the network access point id
     */
    public String getNetworkAccessPointID() {
        return networkAccessPointID;
    }

    /**
     * Sets the network access point id.
     * 
     * @param id
     *            the new network access point id
     */
    public void setNetworkAccessPointID(String id) {
        this.networkAccessPointID = id;
    }

    
    /**
     * Gets the network access point type.
     * 
     * @return the network access point type
     */
    public int getNetworkAccessPointType() {
        return networkAccessPointType;
    }

    /**
     * Sets the network access point type.
     * 
     * @param code
     *            the new network access point type
     */
    public void setNetworkAccessPointType(int code) {
        this.networkAccessPointType = code;
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
        return "ActiveParticipant[pk=" + pk + ", id=" + userID + "]";
    }
    
}
