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
 * https://github.com/gunterze/dcm4che.
 * 
 * The Initial Developer of the Original Code is Agfa Healthcare. Portions created by the Initial
 * Developer are Copyright (C) 2011 the Initial Developer. All Rights Reserved.
 * 
 * Contributor(s): See @authors listed below
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

package org.dcm4chee.arr.cdi.cleanup;

import javax.enterprise.event.Observes;

import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceReloaded;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceStartedCleanUp;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceStoppedCleanUp;
import org.dcm4chee.arr.cdi.Impl.ReloadEvent;
import org.dcm4chee.arr.cdi.Impl.StartCleanUpEvent;
import org.dcm4chee.arr.cdi.Impl.StopCleanUpEvent;
import org.dcm4chee.arr.cdi.conf.CleanUpConfigurationExtension;

/**
 * The Interface AuditRecordRepositoryCleanupImpl.
 * @author Hesham Elbadawi bsdreko@gmail.com
 */
public interface AuditRecordRepositoryCleanup {

    /**
     * Clean with max records policy.
     * if max records policy is used, only this number of records is left in the DB
     * deletion is done older first
     */
     void cleanWithMaxRecordsPolicy() ;

    /**
     * Clean with custom retention policy.
     * checks for each event id type code in the configuration and according to retention time and unit deletes them from the DB
     * @param code
     *            the code
     * @param retention
     *            the retention
     * @param retentionUnit
     *            the retention unit
     * @param deletePerTransaction
     *            the delete per transaction
     */
     void cleanWithCustomRetentionPolicy(String code, long retention,
	    String retentionUnit, int deletePerTransaction) ;
    /**
     * Clean with default retention policy.
     * deletes anything older than retention
     * used if the attribute arrDefaultCleanUpPolicy
     */
    void cleanWithDefaultRetentionPolicy() ;


    /**
     * Initialize procedure.
     */
    void initializeProcedure() ;

    /**
     * Start clean up.
     * 
     * @param start
     *            the start
     */
    public void startCleanUp(
	    @Observes @AuditRecordRepositoryServiceStartedCleanUp StartCleanUpEvent start) ;
    /**
     * Stop clean up.
     * 
     * @param stop
     *            the stop
     */
    public void stopCleanUp(
	    @Observes @AuditRecordRepositoryServiceStoppedCleanUp StopCleanUpEvent stop);

    /**
     * Reconfigure clean up.
     * 
     * @param reload
     *            the reload
     */
    public void reconfigureCleanUp(
	    @Observes @AuditRecordRepositoryServiceReloaded ReloadEvent reload);

    CleanUpConfigurationExtension getCleanUpConfig();

}
