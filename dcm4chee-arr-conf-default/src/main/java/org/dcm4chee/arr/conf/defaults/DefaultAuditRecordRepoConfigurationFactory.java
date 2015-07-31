//
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

package org.dcm4chee.arr.conf.defaults;

import java.util.HashMap;
import java.util.Map;

import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Connection.Protocol;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceType;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4chee.arr.cdi.conf.CleanUpConfigurationExtension;
import org.dcm4chee.arr.cdi.conf.EventTypeObject;
import org.dcm4chee.storage.conf.Container;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.conf.StorageSystemGroup;

/**
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
public class DefaultAuditRecordRepoConfigurationFactory {
    
    public Device createArrDevice(String name) throws Exception {
        Device arrDevice = new Device(name);
       
        Connection auditUDP = new Connection("audit-udp", "localhost", 514);
        auditUDP.setProtocol(Protocol.SYSLOG_UDP);
        arrDevice.addConnection(auditUDP);
        
        Connection auditTls = new Connection("dicom-tls", "localhost", 6514);
        auditTls.setProtocol(Protocol.SYSLOG_TLS);
        auditTls.setTlsCipherSuites(Connection.TLS_RSA_WITH_AES_128_CBC_SHA, Connection.TLS_RSA_WITH_3DES_EDE_CBC_SHA);
        arrDevice.addConnection(auditTls);
        
        addAuditLogger(arrDevice, auditUDP);
        
        AuditRecordRepository arr = new AuditRecordRepository();
        arr.addConnection(auditUDP);
        arrDevice.addDeviceExtension(arr);
        
        CleanUpConfigurationExtension cleanUpCfg = new CleanUpConfigurationExtension();
        cleanUpCfg.setArrCleanUpMaxRecords(19);
        cleanUpCfg.setArrCleanUpUsesRetention(true);
        
        Map<String,EventTypeObject> eventTypeFilterMap = new HashMap<String,EventTypeObject>();
        addEventTypeFiler("110100^DCM", 1, "DAYS", eventTypeFilterMap);
        addEventTypeFiler("110101^DCM", 1, "DAYS", eventTypeFilterMap);
        addEventTypeFiler("110102^DCM", 1, "DAYS", eventTypeFilterMap);
        addEventTypeFiler("110103^DCM", 1, "DAYS", eventTypeFilterMap);
        addEventTypeFiler("110104^DCM", 1, "DAYS", eventTypeFilterMap);
        addEventTypeFiler("110105^DCM", 1, "DAYS", eventTypeFilterMap);
        addEventTypeFiler("110106^DCM", 1, "DAYS", eventTypeFilterMap);
        addEventTypeFiler("110107^DCM", 1, "DAYS", eventTypeFilterMap);
        addEventTypeFiler("110108^DCM", 1, "DAYS", eventTypeFilterMap);
        addEventTypeFiler("110109^DCM", 1, "DAYS", eventTypeFilterMap);
        addEventTypeFiler("110110^DCM", 1, "DAYS", eventTypeFilterMap);
        addEventTypeFiler("110111^DCM", 1, "DAYS", eventTypeFilterMap);
        addEventTypeFiler("110112^DCM", 1, "DAYS", eventTypeFilterMap);
        addEventTypeFiler("110113^DCM", 1, "DAYS", eventTypeFilterMap);
        addEventTypeFiler("110114^DCM", 1, "DAYS", eventTypeFilterMap);
        addEventTypeFiler("IHE0001^IHE", 1, "DAYS", eventTypeFilterMap);
        addEventTypeFiler("IHE0002^IHE", 1, "DAYS", eventTypeFilterMap);
        addEventTypeFiler("IHE0003^IHE", 1, "DAYS", eventTypeFilterMap);
        addEventTypeFiler("IHE0004^IHE", 1, "DAYS", eventTypeFilterMap);
        addEventTypeFiler("IHE0005^IHE", 1, "DAYS", eventTypeFilterMap);
        cleanUpCfg.setEventTypeFilter(eventTypeFilterMap);
        
        arrDevice.addDeviceExtension(cleanUpCfg);
        
        StorageSystem onlineStorageSystem = new StorageSystem();
        onlineStorageSystem.setStorageSystemID("online");
        Map<String,String> statusFileExtensionMap = new HashMap<String,String>();
        statusFileExtensionMap.put(".archived", "ARCHIVED");
        onlineStorageSystem.setStatusFileExtensions(statusFileExtensionMap);
        onlineStorageSystem.setProviderName("org.dcm4chee.storage.filesystem");
        onlineStorageSystem.setStorageSystemPath("/var/local/dcm4chee-arr/backup");
        
        StorageSystemGroup defaultStorageGroup = new StorageSystemGroup();
        defaultStorageGroup.setGroupID("DEFAULT");
        defaultStorageGroup.setBaseStorageAccessTime(2000);
        
        Container zipContainer = new Container();
        zipContainer.setProviderName("org.dcm4chee.storage.zip");
        defaultStorageGroup.setContainer(zipContainer);
        defaultStorageGroup.addStorageSystem(onlineStorageSystem);
        
        StorageDeviceExtension storageExtension = new StorageDeviceExtension();
        storageExtension.addStorageSystemGroup(defaultStorageGroup);
        arrDevice.addDeviceExtension(storageExtension);
        
        return arrDevice;
    }
    
    private static void addEventTypeFiler(String codeId, int retentionTime, String retentionUnit, Map<String,EventTypeObject> eventTypeFilterMap) {
        EventTypeObject eventType = new EventTypeObject();
        eventType.setCodeID(codeId);
        eventType.setRetentionTime(retentionTime);
        eventType.setRetentionTimeUnit(retentionUnit);
        eventTypeFilterMap.put(codeId, eventType);
    }
    
    private static void addAuditLogger(Device arrDevice, Connection auditConn) {
        AuditLogger auditLogger = new AuditLogger();
        arrDevice.addDeviceExtension(auditLogger);
        auditLogger.addConnection(auditConn);
        auditLogger.setAuditSourceTypeCodes("4");
        auditLogger.setAuditRecordRepositoryDevice(arrDevice);
    }
        
}
