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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// TODO: Auto-generated Javadoc
/**
 * The Class Code.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
@Entity(name = "org.dcm4chee.arr.entities.Code")
@Table(name = "arr_code", uniqueConstraints = { 
        @UniqueConstraint(columnNames = { "code_value", "code_designator" }) })
public class Code implements Serializable {

    private static final long serialVersionUID = 4271322436818986479L;

    private static final Logger log = LoggerFactory.getLogger(Code.class);
    
    @Id
    @GeneratedValue
    @Column(name = "pk")
    private long pk;
    
    @Column(name = "code_value", nullable = false)
    private String value;

    @Column(name = "code_designator", nullable = false)
    private String designator;

    @Column(name = "code_meaning")
    private String meaning;
    
    
    public Code() {
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
     * Gets the value.
     * 
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value.
     * 
     * @param value
     *            the new value
     */
    public void setValue(String value) {
        this.value = value;
    }

    
    /**
     * Gets the designator.
     * 
     * @return the designator
     */
    public String getDesignator() {
        return designator;
    }

    /**
     * Sets the designator.
     * 
     * @param designator
     *            the new designator
     */
    public void setDesignator(String designator) {
        this.designator = designator;
    }

    
    /**
     * Gets the meaning.
     * 
     * @return the meaning
     */
    public String getMeaning() {
        return meaning;
    }

    /**
     * Sets the meaning.
     * 
     * @param meaning
     *            the new meaning
     */
    public void setMeaning(String meaning) {
        this.meaning = meaning;
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
        return "Code[pk=" + pk 
                + ", (" + value + ", " + designator + ", \"" + meaning + "\")]";
    }
}
