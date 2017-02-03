package org.dcm4chee.arr.cdi.cleanup.singleton;

import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceReloaded;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceStartedCleanUp;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceStoppedCleanUp;
import org.dcm4chee.arr.cdi.Impl.ReloadEvent;
import org.dcm4chee.arr.cdi.Impl.StartCleanUpEvent;
import org.dcm4chee.arr.cdi.Impl.StopCleanUpEvent;
import org.dcm4chee.arr.cdi.cleanup.AuditRecordRepositoryCleanup;
import org.jboss.msc.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by umberto on 01.02.17.
 */
@ApplicationScoped
public class StandaloneCleanupService {

    private static final Logger LOG = LoggerFactory.getLogger(StandaloneCleanupService.class);

    @Inject
    AuditRecordRepositoryCleanup cleanup;

    public void start() {
        cleanup.enable();
        LOG.info("Start StandaloneCleanupService cleanup service '" + this.getClass().getName() + "' started");
    }

    public void stop() {
        cleanup.disable();
        LOG.info("StandaloneCleanupService cleanup service '" + this.getClass().getName() + "' stopped");
    }
}
