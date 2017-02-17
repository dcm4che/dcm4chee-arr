package org.dcm4chee.arr.cdi.cleanup.singleton;

import org.dcm4chee.arr.cdi.cleanup.AuditRecordRepositoryCleanup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
