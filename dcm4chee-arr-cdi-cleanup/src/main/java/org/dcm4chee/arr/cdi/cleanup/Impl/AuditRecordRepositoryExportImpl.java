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

import org.dcm4che3.net.Device;
import org.dcm4chee.arr.cdi.cleanup.AuditRecordRepositoryExport;
import org.dcm4chee.arr.cdi.cleanup.ejb.AuditRecordDeleteBean;
import org.dcm4chee.arr.cdi.conf.ArrDevice;
import org.dcm4chee.arr.cdi.conf.CleanUpConfigurationExtension;
import org.dcm4chee.arr.entities.AuditRecord;
import org.dcm4chee.storage.ContainerEntry;
import org.dcm4chee.storage.StorageContext;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * The Class AuditRecordRepositoryExportImpl. implementation of
 * the export service utilized by the arr to export containers
 *
 * @author Hesham Elbadawi bsdreko@gmail.com
 */
@ApplicationScoped
public class AuditRecordRepositoryExportImpl implements
        AuditRecordRepositoryExport {

    private final static Logger log = LoggerFactory.getLogger(AuditRecordRepositoryExportImpl.class);

    @Inject
    private StorageService storageService;

    @Inject
    private AuditRecordDeleteBean removeTool;

    @Inject
    @ArrDevice
    private Device device;

    @Override
    public void exportNow(List<AuditRecord> toExport) {
        CleanUpConfigurationExtension cleanUpConfig = device
                .getDeviceExtension(CleanUpConfigurationExtension.class);
        log.info("Started back up procedure ...");
        StorageSystem storageSystem = null;
        try {
            storageSystem = storageService.selectStorageSystem(
                    cleanUpConfig.getArrBackUPStorageGroupID(), 0);
        } catch (Exception e) {
            log.error("Error selecting storage system from group {}", cleanUpConfig.getArrBackUPStorageGroupID());
        }
        StorageContext ctx = storageService.createStorageContext(storageSystem);
        HashMap<String, ArrayList<AuditRecord>> recordsMap = new HashMap<String, ArrayList<AuditRecord>>();
        for (AuditRecord rec : toExport) {
            String dateTime = getYearMonthDayStructure(cleanUpConfig.isArrBackUPUseDailyFolder(), rec.getEventDateTime());
            ArrayList<AuditRecord> records = recordsMap.get(dateTime);
            if (records == null) {
                records = new ArrayList<AuditRecord>();
                recordsMap.put(dateTime, records);
            }
            records.add(rec);
        }

        HashMap<ContainerEntry, AuditRecord> entriesToRecordsPerFolder = null;

        for (String dateTime : recordsMap.keySet()) {
            String folderRelativePath = dateTime.replace("-", "/");
            Path folderPath = storageService.getBaseDirectory(storageSystem).resolve(folderRelativePath);
            if (!folderPath.toFile().exists())
                folderPath.toFile().mkdirs();
            try {
                entriesToRecordsPerFolder = createEntries(recordsMap.get(dateTime));
                HashMap<String, ArrayList<ContainerEntry>> containersMap = toContainersMap(entriesToRecordsPerFolder);
                for (String containerName : containersMap.keySet()) {
                    String containerKey = containerName;
                    containerName += "-export at[" + new Date(System.currentTimeMillis()).toString().replace(":", "-") + "]";
                    storageService.storeContainerEntries(ctx, containersMap.get(containerKey), folderRelativePath + "/" + containerName);

                }
                for (ContainerEntry entry : entriesToRecordsPerFolder.keySet()) {
                    removeTool.deleteRecord(cleanUpConfig, entriesToRecordsPerFolder.get(entry).getPk());
                    Files.delete(entry.getSourcePath());
                }
            } catch (Exception e) {
                log.error("Error performing backup on records for "
                        + "{} - Exception {}", dateTime, e.getMessage());
            }
        }

    }

    private HashMap<String, ArrayList<ContainerEntry>> toContainersMap(
            HashMap<ContainerEntry, AuditRecord> entriesToRecordsPerFolder) {
        HashMap<String, ArrayList<ContainerEntry>> result = new HashMap<String, ArrayList<ContainerEntry>>();
        for (ContainerEntry entry : entriesToRecordsPerFolder.keySet()) {
            String containerName = entriesToRecordsPerFolder.get(entry).getEventType().getMeaning();
            if (result.containsKey(containerName))
                result.get(containerName).add(entry);
            else {
                ArrayList<ContainerEntry> tmpEntries = new ArrayList<ContainerEntry>();
                tmpEntries.add(entry);
                result.put(containerName, tmpEntries);
            }
        }
        return result;
    }


    private HashMap<ContainerEntry, AuditRecord> createEntries(ArrayList<AuditRecord> records) throws IOException, NoSuchAlgorithmException {

        DigestOutputStream dout = null;
        HashMap<ContainerEntry, AuditRecord> entriestoRecordsMap = new HashMap<ContainerEntry, AuditRecord>();
        for (int i = 0; i < records.size(); i++) {
            String entryName = i + " - " + records.get(i).getSourceID() + "-" + toTimeOnly(records.get(i).getEventDateTime());
            File entryFile = File.createTempFile("tmp-" + entryName, "");
            dout = new DigestOutputStream(new FileOutputStream(entryFile),
                    MessageDigest.getInstance("MD5"));

            dout.write(records.get(i).getXmldata());
            dout.flush();
            dout.close();
            ContainerEntry entry = new ContainerEntry.Builder(entryName, dout
                    .getMessageDigest().toString()).setSourcePath(
                    entryFile.toPath()).build();
            entriestoRecordsMap.put(entry, records.get(i));
        }
        return entriestoRecordsMap;
    }

    private String toTimeOnly(Date eventDateTime) {
        String str = "";
        Calendar cal = Calendar.getInstance();
        cal.setTime(eventDateTime);
        str += cal.get(Calendar.HOUR_OF_DAY) + "-" + cal.get(Calendar.MINUTE) + "-" + cal.get(Calendar.SECOND);
        return str;
    }


    private String getYearMonthDayStructure(boolean daily, Date eventDateTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(eventDateTime);

        return cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1)
                + (daily ? "-" + cal.get(Calendar.DATE) : "");
    }

}
