package org.dcm4chee.arr.cdi.cleanup.singleton;

import org.dcm4chee.arr.cdi.cleanup.AuditRecordRepositoryCleanup;
import org.jboss.msc.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by umberto on 01.02.17.
 */
public class HACleanupService implements Service<String> {

    private static final Logger LOG = LoggerFactory.getLogger(HACleanupService.class);
    public static final ServiceName SINGLETON_SERVICE_NAME = ServiceName.JBOSS.append("singleton", "cleanup");
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final BeanManager beanManager;
    private AuditRecordRepositoryCleanup cleanup;

    public HACleanupService(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    public String getValue() throws IllegalStateException, IllegalArgumentException {
        LOG.info("%s is %s at %s", HACleanupService.class.getSimpleName(),
                (started.get() ? "started" : "not started"), System.getProperty("jboss.node.name"));
        return "";
    }

    public void start(StartContext arg0) throws StartException {
        if (!started.compareAndSet(false, true)) {
            throw new StartException("The service is already started!");
        }
        LOG.info("Start HACleanupService cleanup service '" + this.getClass().getName() + "'");

        final String node = System.getProperty("jboss.node.name");
        getCleanup().enable();
        LOG.info("Start HACleanupService cleanup service '" + this.getClass().getName() + "' started on node:" + node);
    }

    public void stop(StopContext arg0) {
        if (!started.compareAndSet(true, false)) {
            LOG.warn("The service '" + this.getClass().getName() + "' is not active!");
        } else {
            LOG.info("Stop HACleanupService cleanup service '" + this.getClass().getName() + "'");
        }
        final String node = System.getProperty("jboss.node.name");
        getCleanup().disable();
        LOG.info("HACleanupService cleanup service '" + this.getClass().getName() + "' stopped on node:" + node);
    }

    /**
     * Gets a reference to the AuditRecordRepositoryCleanup bean through the BeanManager
     */
    private AuditRecordRepositoryCleanup getCleanup() {

        if (cleanup == null) {
            Bean<AuditRecordRepositoryCleanup> bean =
                    (Bean<AuditRecordRepositoryCleanup>) beanManager.getBeans(AuditRecordRepositoryCleanup.class).iterator().next();
            CreationalContext<AuditRecordRepositoryCleanup> ctx = beanManager.createCreationalContext(bean);
            cleanup = (AuditRecordRepositoryCleanup) beanManager.getReference(bean, AuditRecordRepositoryCleanup.class, ctx);
        }
        return cleanup;
    }
}
