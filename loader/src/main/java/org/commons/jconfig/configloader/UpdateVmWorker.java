package org.commons.jconfig.configloader;

import java.io.IOException;

import javax.management.JMX;

import org.apache.log4j.Logger;
import org.commons.jconfig.config.ConfigException;
import org.commons.jconfig.internal.Worker;
import org.commons.jconfig.internal.WorkerException;
import org.commons.jconfig.internal.jmx.ConfigManagerJmxMXBean;
import org.commons.jconfig.internal.jmx.ConfigManagerJvm;
import org.commons.jconfig.internal.jmx.VirtualMachineException;


/**
 * Checks version of config from different sources and compares it ConfigManager
 * JVM. If version does not matches, it pumps in new value and flips the cache
 * at ConfigManager.
 * 
 * @author jaikit
 * 
 */
public class UpdateVmWorker implements Worker<Object> {
    private final Logger logger = Logger.getLogger(UpdateVmWorker.class);
    private final ConfigManagerJvm managerVm;
    private final ConfigLoaderJmx loaderJmx;
    private final ConfigManagerJmxMXBean managerMbean;
    private Exception cause = null;

    /**
     * ctor for {@link UpdateVmWorker}
     * 
     * @param mbean
     *            {@link ConfigLoaderJmx}
     * @param vm
     *            {@link ConfigManagerJvm} Assumes vm is already attached.
     * @throws WorkerException
     */
    public UpdateVmWorker(final ConfigLoaderJmx mbean, final ConfigManagerJvm vm) throws WorkerException {
        this.managerVm = vm;
        this.loaderJmx = mbean;

        try {
            this.managerMbean = JMX.newMBeanProxy(managerVm.getJMXConnector().getMBeanServerConnection(),
                    vm.getObjectName(), ConfigManagerJmxMXBean.class, true);
        } catch (IOException e1) {
            throw new WorkerException(e1);
        }
    }

    @Override
    public boolean execute() throws WorkerException {
        try {
            String applicationName = managerMbean.getVMName();

            int loaderConfigHashCode = loaderJmx.getAppConfigHash(managerVm, applicationName);
            int managerConfigHashCode = managerMbean.getConfigHashCode();
            // since config manager is not uptodate load new values 
            if (loaderConfigHashCode != managerConfigHashCode) {
                try {
                    logger.info("Loading application " + applicationName + " with new configs");
                    loaderJmx.loadAppConfigs(managerVm.getObjectName(), true);
                    managerMbean.flipCache();
                    managerMbean.updateConfigHashCode(loaderConfigHashCode);
                } catch (VirtualMachineException e) {
                    throw new WorkerException(e);
                } catch (ConfigException e) {
                    throw new WorkerException(e);
                }
            }
        } catch (WorkerException e) {
            cause = e;
        } finally {
            try {
                managerVm.close();
            } catch (VirtualMachineException e1) {
                // ignore exception to allow gc to collect resources
            }            
        }
        return true;
    }

    @Override
    public Exception getCause() {
        return cause;
    }

    @Override
    public boolean hasErrors() {
        return cause != null;
    }

    @Override
    public Object getData() {
        // TODO Auto-generated method stub
        return null;
    }

}
