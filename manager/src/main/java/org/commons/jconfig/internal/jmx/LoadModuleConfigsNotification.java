package org.commons.jconfig.internal.jmx;


/**
 * Notification that loading configurations for a module in an application is complete.<BR>
 * Emitted by @ConfigLoaderMXBean. 
 * 
 * @author aabed
 *
 */
public class LoadModuleConfigsNotification extends LoadConfigsNotification {

    
    /**
     * 
     */
    private static final long serialVersionUID = -6923690434070249402L;
    
    /**
     * 
     */
    public static final String MODULE_CONFIGS_TYPE = "configLoader.LoadModuleConfigsDone";
    public static final String MODULE_CONFIGS_DESC = "config loading for module of application is complete";
    
    /**
     * Constructs LoadModuleConfigsNotification object.
     *
     * @param source The notification producer, that is, the MBean the attribute belongs to.
     * @param sequenceNumber The notification sequence number within the source object.
     * @param timeStamp The date at which the notification is being sent.
     * @param msg A String containing the message of the notification.
     * @param appName name of the application the module belongs to 
     * @param moduleName name of the module the configuration loading was completed for.
     */
    public LoadModuleConfigsNotification(Object source, long sequenceNumber, long timeStamp, String msg, 
                                            String appName, String moduleName, boolean result) {
        
        super(LoadModuleConfigsNotification.MODULE_CONFIGS_TYPE, source, sequenceNumber, timeStamp, msg, appName, result);
        this.moduleName = moduleName;
    }
    
    /**
     * @return the module name for this instance of the notification
     */
    public String getModuleName() {
        return moduleName;
    }
    
    /**
     * the module name for this instance of the notification
     */
    private String moduleName;
}
