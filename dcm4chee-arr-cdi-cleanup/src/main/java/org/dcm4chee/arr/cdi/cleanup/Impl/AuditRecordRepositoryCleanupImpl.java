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

package org.dcm4chee.arr.cdi.cleanup.Impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dcm4che3.net.Device;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceReloaded;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceStartedCleanUp;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceStoppedCleanUp;
import org.dcm4chee.arr.cdi.cleanup.AuditRecordRepositoryCleanup;
import org.dcm4chee.arr.cdi.cleanup.AuditRecordRepositoryExport;
import org.dcm4chee.arr.cdi.conf.CleanUpConfigurationExtension;
import org.dcm4chee.arr.cdi.conf.EventTypeObject;
import org.dcm4chee.arr.cdi.Impl.ReloadEvent;
import org.dcm4chee.arr.cdi.Impl.StartCleanUpEvent;
import org.dcm4chee.arr.cdi.Impl.StopCleanUpEvent;
import org.dcm4chee.arr.cdi.cleanup.ejb.AuditRecordDeleteBean;
import org.dcm4chee.arr.entities.AuditRecord;

/**
 * The Class AuditRecordRepositoryCleanupImpl. implementation of a clean up
 * service initialization is provided by the singleton bean running the audit
 * record repository service
 * 
 * @author Hesham Elbadawi bsdreko@gmail.com
 */
@ApplicationScoped
public class AuditRecordRepositoryCleanupImpl implements
        AuditRecordRepositoryCleanup {

    @Inject
    private AuditRecordRepositoryExport exportService;

    @Inject
    private AuditRecordDeleteBean removeTool;

    private static final Logger log = LoggerFactory
            .getLogger(AuditRecordRepositoryCleanupImpl.class);

    private ScheduledFuture<?> scheduledCleanProcedure = null;

    private ScheduledFuture<?> scheduledBackUpProcedure = null;

    private boolean running;

    private boolean backup;
    private Device device = null;

    File backUpDir = null;

    private CleanUpConfigurationExtension cleanUpConfig = null;

    public CleanUpConfigurationExtension getCleanUpConfig() {
        return cleanUpConfig;
    }
    
    private Map<String, EventTypeObject> eventFilter  = null;

    private Runnable backUpProcedure = new Runnable() {
        @Override
        public void run() {
            if(isnow(cleanUpConfig.getArrBackUPStartTimeRangeInHours())) { 
            exportService.exportNow(removeTool.getRecordsDueOrderByEventType());
            }
            
        }

        private boolean isnow(String arrBackUPStartTimeRangeInHours) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            if(!arrBackUPStartTimeRangeInHours.contains("-"))
                log.error("Bad configuration for export time range in hours"
                        + " (no export possible), [correct format e.g. "
                        + "starthour-endhour]");
            if(hour >= Integer.valueOf(arrBackUPStartTimeRangeInHours.split("-")[0])
                    && hour < Integer.valueOf(arrBackUPStartTimeRangeInHours.split("-")[1]))
                return true;

            return false;
        }
    };

    private Runnable cleanProcedure = new Runnable() {

        @Override
        public void run() {
            if (cleanUpConfig.getArrDefaultCleanUpPolicy().compareToIgnoreCase(
                    "all") == 0) {
                if (cleanUpConfig.isArrCleanUpUsesRetention()) {
                    cleanWithDefaultRetentionPolicy();
                }
                if (cleanUpConfig.isArrCleanUpUsesMaxRecords()) {
                    cleanWithMaxRecordsPolicy();
                }
            } else if (cleanUpConfig.getArrDefaultCleanUpPolicy()
                    .compareToIgnoreCase("custom") == 0) {
                for (EventTypeObject obj : toCollection(eventFilter)) {
                    cleanWithCustomRetentionPolicy(obj.getCodeID(),
                            obj.getRetentionTime(), obj.getRetentionTimeUnit(),
                            cleanUpConfig.getArrCleanUpDeletePerTransaction());
                }
            }

        }
    };

    /**
     * Clean with max records policy. if max records policy is used, only this
     * number of records is left in the DB deletion is done older first
     */
    public void cleanWithMaxRecordsPolicy() {
        int maxRecordsAllowed = cleanUpConfig.getArrCleanUpMaxRecords();
        int deletePerTransaction = cleanUpConfig
                .getArrCleanUpDeletePerTransaction();
        List<Long> l = null;
        while ((l = removeTool.getPksByMaxRecords(maxRecordsAllowed,
                deletePerTransaction)) != null) {
            log.info("Deleting records with the following pks:");
            for (long pk : l) {
                removeTool.deleteRecord(cleanUpConfig, pk);
                log.info("pk = {}" , pk);
            }
          
        }

    }

    protected Collection<EventTypeObject> toCollection(Map<String, EventTypeObject> eventFilterMap) {
        Collection<EventTypeObject> result = new ArrayList<EventTypeObject>();
        for(String key : eventFilterMap.keySet())
            result.add(eventFilterMap.get(key));
        return result;
    }

    /**
     * Clean with custom retention policy. checks for each event id type code in
     * the configuration and according to retention time and unit deletes them
     * from the DB
     * 
     * @param code
     *            the code
     * @param retention
     *            the retention
     * @param retentionUnit
     *            the retention unit
     * @param deletePerTransaction
     *            the delete per transaction
     */
    public void cleanWithCustomRetentionPolicy(String code, long retention,
            String retentionUnit, int deletePerTransaction) {
        List<Long> l = null;
        while (!(l = removeTool.getPKsByEventIDTypeCode(code, (int) retention,
                retentionUnit, deletePerTransaction)).isEmpty()) {
            log.info("Deleting records with the following pks:");
            for (long pk : l) {
                removeTool.deleteRecord(cleanUpConfig, pk);
                log.info("pk = {}", pk);
            }
           
        }

    }

    /**
     * Clean with default retention policy. deletes anything older than
     * retention used if the attribute arrDefaultCleanUpPolicy
     */
    public void cleanWithDefaultRetentionPolicy() {
        int retentionTime = cleanUpConfig.getArrCleanUpRetentionTime();
        String retentionTimeUnit = cleanUpConfig
                .getArrCleanUpRetentionTimeUnit();
        int deletePerTransaction = cleanUpConfig
                .getArrCleanUpDeletePerTransaction();
        List<Long> l = null;

        while (!(l = removeTool.getPKsByRetention(retentionTime,
                retentionTimeUnit, deletePerTransaction)).isEmpty()) {
            log.info("Deleting records with the following pks:");
            for (long pk : l) {
                removeTool.deleteRecord(cleanUpConfig, pk);
                log.info("pk = {}", pk);
            }
            
        }
    }

    /**
     * Startup.
     * 
     * @param o
     *            the o
     */
    static void startup(@Observes @Initialized(ApplicationScoped.class) Object o) {
    }

    /**
     * Initialize procedure.
     */
    public void initializeProcedure() {
        if (cleanUpConfig != null && cleanUpConfig.getArrCleanUpPollInterval() > 0) {
            log.info("Initializing cleanup thread");
            scheduledCleanProcedure = device.scheduleWithFixedDelay(
                    cleanProcedure, 0,
                    (long) this.cleanUpConfig.getArrCleanUpPollInterval(),
                    TimeUnit.SECONDS);
            if (this.cleanUpConfig.isArrSafeClean()) {
                scheduledBackUpProcedure = device.scheduleWithFixedDelay(
                        backUpProcedure, 0,
                        (long) this.cleanUpConfig.getArrBackUpPollInterval(),
                        TimeUnit.SECONDS);

                log.info("Initialized BackUp thread with poll interval of "
                        + (long) this.cleanUpConfig.getArrBackUpPollInterval()
                        + " SECONDS");
            }
            log.info("Initialized cleanup thread with poll interval of {} SECONDS"
                    , (long) this.cleanUpConfig.getArrCleanUpPollInterval());
        }
    }

    /**
     * Start clean up.
     * 
     * @param start
     *            the start
     */
    public void startCleanUp(
            @Observes @AuditRecordRepositoryServiceStartedCleanUp StartCleanUpEvent start) {
        this.device = start.getDevice();
        this.cleanUpConfig = device
                .getDeviceExtension(CleanUpConfigurationExtension.class);
        if (cleanUpConfig != null) {
            this.eventFilter = cleanUpConfig.getEventTypeFilter();
            if (scheduledCleanProcedure == null
                    && (this.cleanUpConfig.isArrCleanUpUsesMaxRecords() || this.cleanUpConfig
                            .isArrCleanUpUsesRetention())) {
                initializeProcedure();
                running = true;
                log.info("Started cleanup service");
                if (this.cleanUpConfig.isArrSafeClean()) {
                    backup = true;
                    log.info("Started backup service");
                }
            }
        } else {
            log.warn("Audit Record Repository: CleanUp Configuration missing! Cleanup service not started!");
        }
    }

    /**
     * Stop clean up.
     * 
     * @param stop
     *            the stop
     */
    public void stopCleanUp(
            @Observes @AuditRecordRepositoryServiceStoppedCleanUp StopCleanUpEvent stop) {
        if (scheduledCleanProcedure != null) {
            scheduledCleanProcedure.cancel(false);
            scheduledCleanProcedure = null;
            running = false;
            log.info("Stopped cleanup service");
        }
        if (scheduledBackUpProcedure != null) {
            scheduledBackUpProcedure.cancel(false);
            scheduledBackUpProcedure = null;
            backup = false;
            log.info("Stopped backup service");
        }

    }

    /**
     * Reconfigure clean up.
     * 
     * @param reload
     *            the reload
     */
    public void reconfigureCleanUp(
            @Observes @AuditRecordRepositoryServiceReloaded ReloadEvent reload) {
        this.device = reload.getDevice();
        this.cleanUpConfig = device
                .getDeviceExtension(CleanUpConfigurationExtension.class);
        if (cleanUpConfig != null)
            this.eventFilter = cleanUpConfig.getEventTypeFilter();
        if (reload.isState()) {
            log.info("Reloaded cleanup service configuration");
            if (running)
                scheduledCleanProcedure.cancel(false);
            if (backup)
                scheduledBackUpProcedure.cancel(false);
            initializeProcedure();
        }
    }

}
