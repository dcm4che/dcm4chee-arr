package org.dcm4chee.arr.cdi.cleanup.singleton;

import org.dcm4chee.arr.cdi.cleanup.AuditRecordRepositoryCleanup;
import org.dcm4chee.arr.cdi.cleanup.Impl.AuditRecordRepositoryExportImpl;
import org.jboss.as.clustering.singleton.SingletonService;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.net.InetAddress;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by umberto on 01.02.17.
 */
public class HACleanupCDIExtension implements Extension{

    private final static Logger LOG = LoggerFactory.getLogger(HACleanupCDIExtension.class);
    private HACleanupService singletonCdiService;
    private StandaloneCleanupService standaloneCleanupService;

    @SuppressWarnings("unused")
    private void afterDeploymentValidation(@Observes AfterDeploymentValidation afterDeploymentValidation, BeanManager beanManager) {
        if (isHA()) {
            singletonCdiService = new HACleanupService(beanManager);
            SingletonService<String> singleton = new SingletonService<String>(singletonCdiService, HACleanupService.SINGLETON_SERVICE_NAME);
            singleton.build(CurrentServiceContainer.getServiceContainer(), SingletonService.DEFAULT_CONTAINER)
                    .install()
                    .setMode(ServiceController.Mode.ACTIVE);
        }
        else {
            //if not HA, just enable the standaloneService
            Bean<StandaloneCleanupService> bean =
                    (Bean<StandaloneCleanupService>) beanManager.getBeans(StandaloneCleanupService.class).iterator().next();
            standaloneCleanupService = beanManager.getContext(bean.getScope()).get(bean, beanManager.createCreationalContext(bean));
            standaloneCleanupService.start();
        }
    }

    @SuppressWarnings("unused")
    private void beforeShutDown(@Observes BeforeShutdown beforeShutdown) {
        if (isHA()) {
            try {
                CurrentServiceContainer.getServiceContainer().
                        getRequiredService(HACleanupService.SINGLETON_SERVICE_NAME)
                            .setMode(ServiceController.Mode.REMOVE);
            } catch (Exception x) {
                LOG.error(x.getMessage());
            }
        }
        else {
            //if not HA, just disable the standaloneService
            if (standaloneCleanupService!=null)
                standaloneCleanupService.stop();
        }
    }

    /**
     *  checks if a jgoups binding is available, if yes, suppose HA is available
     */
    private boolean isHA () {
        ServiceController tcp_bind =
                CurrentServiceContainer.getServiceContainer().
                        getService(ServiceName.JBOSS.append("binding", "jgroups-tcp-fd"));
        ServiceController udp_bind =
                CurrentServiceContainer.getServiceContainer().
                        getService(ServiceName.JBOSS.append("binding", "jgroups-udp-fd"));

        if (tcp_bind!=null || udp_bind!=null)
            return true;
        else return false;
    }
}
