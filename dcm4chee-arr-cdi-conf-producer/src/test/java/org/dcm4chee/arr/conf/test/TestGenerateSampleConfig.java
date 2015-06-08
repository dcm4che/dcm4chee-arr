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

package org.dcm4chee.arr.conf.test;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.dicom.DicomConfigurationBuilder;
import org.dcm4che3.net.Connection.Protocol;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.SSLManagerFactory;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4che3.util.ResourceLocator;
import org.dcm4chee.arr.cdi.conf.CleanUpConfigurationExtension;
import org.dcm4chee.arr.cdi.conf.EventTypeObject;
import org.dcm4chee.storage.conf.Availability;
import org.dcm4chee.storage.conf.Container;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.conf.StorageSystemGroup;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestGenerateSampleConfig  {
    protected KeyStore keystore;

    private static final Logger LOG = LoggerFactory.getLogger(TestGenerateSampleConfig.class);

    private static String[] EVENT_ID_CODES = {
        "110100^DCM","110101^DCM","110102^DCM","110103^DCM",
        "110104^DCM","110105^DCM","110106^DCM","110107^DCM",
        "110108^DCM","110109^DCM","110110^DCM","110111^DCM",
        "110112^DCM","110113^DCM","110114^DCM","IHE0001^IHE",
        "IHE0002^IHE","IHE0003^IHE","IHE0004^IHE","IHE0005^IHE"
    };

    protected DicomConfiguration config;


    protected Device createARRDevice(String name, Connection.Protocol protocol, int port) {
        Device arrDevice = new Device(name);
        AuditRecordRepository arr = new AuditRecordRepository();
        arrDevice.addDeviceExtension(arr);
        Connection auditUDP = new Connection("audit-udp", "localhost", port);
        auditUDP.setProtocol(protocol);
        arrDevice.addConnection(auditUDP);
        arr.addConnection(auditUDP);
        Connection dicomTLS = new Connection("dicom-tls", "localhost", 6514);
        dicomTLS.setTlsCipherSuites(
                Connection.TLS_RSA_WITH_AES_128_CBC_SHA,
                Connection.TLS_RSA_WITH_3DES_EDE_CBC_SHA);
        dicomTLS.setProtocol(Protocol.SYSLOG_TLS);
        arrDevice.addConnection(dicomTLS);
        addAuditLogger(arrDevice);
        addCleanUpExtension(arrDevice);
        addStorageExtension(arrDevice);
        return arrDevice ;
    }

    private void addAuditLogger(Device arrDevice) {
        Connection auditUDP = new Connection("audit-udp", "localhost");
        auditUDP.setProtocol(Connection.Protocol.SYSLOG_UDP);
        arrDevice.addConnection(auditUDP);

        AuditLogger auditLogger = new AuditLogger();
        arrDevice.addDeviceExtension(auditLogger);
        auditLogger.addConnection(auditUDP);
        auditLogger.setAuditSourceTypeCodes("4");
        auditLogger.setAuditRecordRepositoryDevice(arrDevice);
    }

    private void addCleanUpExtension(Device arrDevice) {
        CleanUpConfigurationExtension ext = new CleanUpConfigurationExtension();
        
        ext.setEventTypeFilter(getEventTypeFilter());
        ext.setArrCleanUpUsesRetention(true);
        ext.setArrCleanUpUsesMaxRecords(false);
        ext.setArrCleanUpMaxRecords(19);
        ext.setArrCleanUpPollInterval(3600);
        ext.setArrCleanUpRetentionTime(1);
        ext.setArrCleanUpRetentionTimeUnit("DAYS");
        ext.setArrDefaultCleanUpPolicy("all");
        ext.setArrCleanUpDeletePerTransaction(2);
        ext.setArrSafeClean(true);
        ext.setArrBackUpPollInterval(3600);
        ext.setArrBackUPUseDailyFolder(true);
        ext.setArrBackUPStorageGroupID("DEFAULT");
        arrDevice.addDeviceExtension(ext);

    }

    private void addStorageExtension(Device arrDevice) {
        StorageSystem arr = new StorageSystem();
        arr.setStorageSystemID("online");
        arr.setProviderName("org.dcm4chee.storage.filesystem");
        arr.setStorageSystemPath("/var/local/dcm4chee-arr/backup");
        arr.setAvailability(Availability.ONLINE);
        Map<String,String> exts = new LinkedHashMap<String, String>();
        exts.put(".archived", "ARCHIVED");
        arr.setStatusFileExtensions(exts);


        Container container = new Container();
        container.setProviderName("org.dcm4chee.storage.zip");

        StorageSystemGroup online = new StorageSystemGroup();
        
        online.setGroupID("DEFAULT");
        online.addStorageSystem(arr);
        online.setBaseStorageAccessTime(2000);
        online.setActiveStorageSystemIDs(arr.getStorageSystemID());
        online.setContainer(container);
        StorageDeviceExtension ext = new StorageDeviceExtension();
        ext.addStorageSystemGroup(online);
        arrDevice.addDeviceExtension(ext);
    }
    private Map<String, EventTypeObject> getEventTypeFilter() {
        Map<String, EventTypeObject> eventTypeFilter = 
                new HashMap<String, EventTypeObject>();
        
        for(String code : EVENT_ID_CODES) {
            EventTypeObject obj = new EventTypeObject();
            obj.setCodeID(code);
            obj.setRetentionTime(1);
            obj.setRetentionTimeUnit("DAYS");
            eventTypeFilter.put(code , obj);
        }
        return eventTypeFilter;
    }

    @Before
    public void setUp() throws Exception {
        //keystore = SSLManagerFactory.loadKeyStore("JKS",
        //        ResourceLocator.resourceURL("cacerts.jks"), "secret");

        DicomConfigurationBuilder builder;
        if (System.getProperty("ldap") != null) {
            Properties env = new Properties();
            try  {
                InputStream inStream = Thread.currentThread()
                        .getContextClassLoader().getResourceAsStream("ldap.properties");
                env.load(inStream);
            }
            catch(Exception e) {
                LOG.error("Error loading ldap properties, {}",e);
            }
            builder = DicomConfigurationBuilder.newLdapConfigurationBuilder(env);
        } else {
            builder = DicomConfigurationBuilder.newJsonConfigurationBuilder("target/config.json");
        }
        builder.registerDeviceExtension(AuditRecordRepository.class);
        builder.registerDeviceExtension(AuditLogger.class);
        builder.registerDeviceExtension(CleanUpConfigurationExtension.class);
        builder.registerDeviceExtension(StorageDeviceExtension.class);
 
        config = builder.build();
        try {
            config.removeDevice("dcm4chee-arr");
        } catch (ConfigurationNotFoundException e) {
        }
    }
    
    @Test
    public void test() throws Exception {

        Device arrDevice = createARRDevice("dcm4chee-arr", Protocol.SYSLOG_UDP, 514);

        config.persist(arrDevice);
        config.sync();
    }


}
