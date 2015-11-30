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
package org.dcm4chee.arr.cdi.Impl;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.InternalConfigChangeEvent;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryService;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceReloaded;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceStarted;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceStartedCleanUp;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceStopped;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceStoppedCleanUp;
import org.dcm4chee.arr.cdi.conf.ArrDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * This is the audit record repository service responsible for configuring, starting, stopping and reloading the device
 * <p>
 * This is a singelton as a workaround for initializing the cdi instance in jboss 7
 *
 * @author Hesham Elbadawi bsdreko@gmail.com
 */
@Startup
@Singleton
public class AuditRecordRepositoryServiceImpl implements AuditRecordRepositoryService {

    private static final Logger log = LoggerFactory.getLogger(AuditRecordRepositoryServiceImpl.class);

    private static final String DEVICE_NAME_PROPERTY = "org.dcm4chee.arr.deviceName";
    private static final String DEF_DEVICE_NAME = "dcm4chee-arr";
    private static String[] JBOSS_PROPERITIES = {"jboss.home", "jboss.modules", "jboss.server.base",
            "jboss.server.config", "jboss.server.data", "jboss.server.deploy", "jboss.server.log",
            "jboss.server.temp",};

    @Inject
    private AuditRecordRepositoryHandlerImpl auditRecordRepositoryHandlerImpl;

    @Inject
    @AuditRecordRepositoryServiceStarted
    private Event<StartStopEvent> auditRecordRepositoryServiceStarted;

    @Inject
    @AuditRecordRepositoryServiceStopped
    private Event<StartStopEvent> auditRecordRepositoryServiceStopped;

    @Inject
    @AuditRecordRepositoryServiceReloaded
    private Event<ReloadEvent> auditRecordRepositoryServiceReloaded;

    @Inject
    @AuditRecordRepositoryServiceStartedCleanUp
    private Event<StartCleanUpEvent> auditRecordRepositoryServiceStartedCleanUp;

    @Inject
    @AuditRecordRepositoryServiceStoppedCleanUp
    private Event<StopCleanUpEvent> auditRecordRepositoryServiceStoppedCleanUp;

    @Inject
    private DicomConfiguration conf;

    @Inject 
    private Instance<Device> devices;
    
    private Device device;

    boolean deviceMaintainedByOtherService = false;

    private boolean running;

    /**
     * Adds the jboss system properties.
     */
    private static void addJBossDirURLSystemProperties() {
        for (String key : JBOSS_PROPERITIES) {
            String url = new File(System.getProperty(key + ".dir")).toURI().toString();
            System.setProperty(key + ".url", url.substring(0, url.length() - 1));
        }
    }

    /**
     * Find device.
     * Loads appropriate configuration for a device using it's name fom either ldap or prefs.
     *
     * @return the device
     * @throws ConfigurationException the configuration exception
     */
    public Device findDevice() throws ConfigurationException {
        conf.sync();
        return conf.findDevice(
                System.getProperty(
                        DEVICE_NAME_PROPERTY,
                        DEF_DEVICE_NAME));
    }

    /**
     * init() runs pre-start. initializies the device, set the scheduled
     * executor to be used (used by the listeners in the AuditRecordRepository
     * extension) finally sets the handler implementation to handle the incoming
     * messages on the registered connections
     */
    @Override
    @PostConstruct
    public void init() {

        addJBossDirURLSystemProperties();
        try {
            String devName = System.getProperty(DEVICE_NAME_PROPERTY, DEF_DEVICE_NAME);
            for (Device d : devices) {
                if (d.getDeviceName().equals(devName)) {
                    device = d;
                    deviceMaintainedByOtherService = true;
                    break;
                }
                
            }
            if (device == null)
                device = findDevice();
            if (device.getExecutor() == null)
                device.setExecutor(Executors.newCachedThreadPool());
            if (device.getScheduledExecutor() == null)
                device.setScheduledExecutor(Executors.newSingleThreadScheduledExecutor());
            AuditRecordRepository arrCfg = device.getDeviceExtension(AuditRecordRepository.class);
            if (arrCfg != null) {
                arrCfg.setAuditRecordHandler(
                    auditRecordRepositoryHandlerImpl);
            } else {
                log.warn("Audit Record Handler not set! Missing AuditRecordRepository device extension!");
            }
            if (deviceMaintainedByOtherService) {
                afterStart();
            } else {
                start();
            }
        } catch (RuntimeException re) {
            log.error(re.getMessage());
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Destroy takes care of closing the jms connection used by the handler.
     */
    @PreDestroy
    public void destroy() {
        auditRecordRepositoryHandlerImpl.closeJMS();
        stop();
    }

    /**
     * Start binds the connections registered in the AuditRecordRepository
     * device extension sends the start event for the cleanup and if not using
     * singleton will need the startstop event with an observer the observer
     * specified will have to listen to the inizialization of the application
     * scope.
     *
     * @throws Exception the exception
     */
    @Override
    public void start() throws Exception {
        device.bindConnections();
        afterStart();
    }
    private void afterStart() {
        running = true;
        log.info("started service");
        auditRecordRepositoryServiceStartedCleanUp.fire(new StartCleanUpEvent(device));
        auditRecordRepositoryServiceStarted.fire(new StartStopEvent(true, device, new LocalSource()));
    }

    /**
     * Stop unbinds the connections registered in the AuditRecordRepository
     * device extension sends the stop event for the cleanup and if not using
     * singleton will need the startstop event with an observer the observer
     * specified will have to listen to the inizialization of the application
     * scope.
     */
    @Override
    public void stop() {

        device.unbindConnections();
        running = false;
        log.info("stopped service");
        auditRecordRepositoryServiceStoppedCleanUp.fire(new StopCleanUpEvent(device));
        auditRecordRepositoryServiceStopped.fire(new StartStopEvent(false, device, new LocalSource()));
    }

    @Override
    public void onConfigChange(@Observes InternalConfigChangeEvent configChange) {
        try {
            reload();
        } catch (Exception e) {
            log.error("Error while reloading configuration", e);
        }
    }

    /**
     * Reload calls the reconfigure, rebinds connections with the new
     * configuration and calls the reload event used by the cleanup.
     *
     * @throws Exception the exception
     */
    @Override
    public void reload() throws Exception {
        device.reconfigure(findDevice());
        device.rebindConnections();
        log.info("reloading service configuration");
        auditRecordRepositoryServiceReloaded.fire(new ReloadEvent(device, running));
    }

    @Override
    @Produces @ArrDevice
    public Device getDevice() {
        return device;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * getListenerInfo.
     * creates a response to the start event fired using the restful interface
     *
     * @return the listeners info
     */
    @Override
    public String getListenersInfo() {
        String info = "Device Connections Information:\n";
        AuditRecordRepository arrCfg = device.getDeviceExtension(AuditRecordRepository.class);
        if (arrCfg == null || arrCfg.getConnections().isEmpty()) {
            return "no Info available";
        } else {
            for (Connection c : arrCfg.getConnections()) {
                if (c.isTls()) {
                    info +=
                            "<br>Listener> [Trusted] at Address: " + c.getHostname() + " on port: " + c.getPort()
                                    + " using protocol: " + c.getProtocol() + " with set timeout: "
                                    + c.getConnectTimeout() + "<br>" + " Using TLS using <br>";
                } else {
                    info +=
                            "<br>Listener> at Address: " + c.getHostname() + " on port: " + c.getPort()
                                    + " using protocol: " + c.getProtocol() + " with set timeout: "
                                    + c.getConnectTimeout() + "<br>";
                }
            }
        }
        return info;
    }
}
