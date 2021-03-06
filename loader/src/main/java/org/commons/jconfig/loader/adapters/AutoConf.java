package org.commons.jconfig.loader.adapters;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.commons.jconfig.config.ConfigException;
import org.commons.jconfig.configloader.ConfigLoaderConfig;


/**
 *  AutoConf json configuration file source class.<BR>
 *  Handles config file changes on disk, Accessor methods return latest copy of configurations<BR><BR>
 * 
 * standard autoConf is defined by two tiers<BR><BR>
 * 
 * Application : all the module configurations belonging to a single application.<br>
 *               (as named in the @ConfigManagerMXBean)<BR>
 * Modules : all the configurations belonging to a module.  configurations in a module are defined per module. <BR><BR>
 * 
 * Also supports module configurations shared across multiple applications.<BR>
 * configurations share across multiple applications belong to a reserved appName "Modules"<br><br>
 * 
 *  Singleton usage model.<BR><BR>
 * 
 *  <B>Usage</B><BR>
 * 
 *  AutoConf config = AutoConf.instance();<BR>
 *  if ( config.hasModule(appName, module) ) {<BR><BR>
 * 
 *      JsonNode moduleNode = config.getModule("Imap", "FilerGateConfig");<BR>
 *      moduleNode.doSomething()<BR>
 *  }<BR><BR>
 * 
 */
public class AutoConf  {

    private final ConfigLoaderConfig config;

    /** Synchronize on this object before updating any of the static atomic
     *  variables in this class, or before doing file read. */
    private final Object autoConfLock = new Object();

    /** Last contents successfully loaded from the clusters.conf file.
     *  Null if we've never successfully read that file. */
    private final AtomicReference<JsonNode> confRef = new AtomicReference<JsonNode>(null);

    /** Next time we should check for a new clusters.conf file.
     *  Zero means: "check at next opportunity". */
    private final AtomicLong nextCheckTimeRef = new AtomicLong(0);

    /** The "last modified" time stamp of file {@link #confFilenameRef} the
     *  we last time we read it (even if that version of the file was
     *  unreadable).  Zero if we've never seen the file. */
    private final AtomicLong fileLastModifiedTimeRef = new AtomicLong(0);

    /**
     * 
     */
    public AutoConf(final ConfigLoaderConfig config) {
        this.config = config;
    }

    /**
     * Are there configurations available for appName
     * 
     * @param appName
     * @throws ConfigException 
     */
    public boolean hasApplication(final String appName) throws ConfigException {
        if (null == getApplication(appName)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Are there configurations available for module in appName
     * @param appName
     * @param module
     * @throws ConfigException 
     */
    public boolean hasModule(final String appName, final String module) throws ConfigException {
        if (null == getModule(appName, module)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get configuration Object for appName
     * 
     * @param appName
     * @return Json Object. null if missing
     * @throws ConfigSourceException
     */
    public JsonNode getApplication(final String appName) throws ConfigException {
        return getConf().get(appName);
    }

    /**
     * Get configuration Object for module in appName
     * 
     * @param appName
     * @return Json Object. null if missing
     * @throws ConfigSourceException
     */
    public JsonNode getModule(final String appName, final String module) throws ConfigException {

        JsonNode appNode = getApplication(appName);
        JsonNode modNode = null;
        if (appNode != null) {
            modNode = appNode.get(module);
        }
        return modNode;
    }

    /**
     * @return  The current conf dictionary.  Never null.
     * @throws Exception
     * @throws  AutoConfException  if clusters.conf
     *          does not exist, or cannot be parsed.
     */
    public JsonNode getConf() throws ConfigException
    {
        // If we should check for a new conf file,
        long now = System.currentTimeMillis();
        if (now > nextCheckTimeRef.get())
        {
            // Synchronize and check again.  ("Double-checked locking".
            // We only need to lock when its time to check the file again.)
            synchronized (autoConfLock)
            {
                // If we should check for a new conf file,
                if (now > nextCheckTimeRef.get())
                {
                    // Throw if conf file missing.
                    String filename = config.getConfigFileName();
                    File f = new File(filename);
                    long modTime = f.lastModified();
                    if (modTime == 0) {
                        throw new ConfigException("AutoConf file (" + filename + ") not found");
                    }

                    // If conf file has changed,
                    if (modTime != fileLastModifiedTimeRef.get())
                    {
                        try {

                            ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
                            mapper.configure( DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                            confRef.set(mapper.readValue(f, JsonNode.class));

                            fileLastModifiedTimeRef.set(modTime);
                        } catch (JsonParseException e) {
                            throw new ConfigException("Error parsing AutoConf file (" + filename + ")", e);
                        } catch (JsonMappingException e) {
                            throw new ConfigException("Error parsing AutoConf file (" + filename + ")", e);
                        } catch (IOException e) {
                            throw new ConfigException("Error parsing AutoConf file (" + filename + ")", e);
                        }
                    }

                    // Don't check again for x time interval
                    nextCheckTimeRef.set(now + config.getConfigSyncInterval().toMillis());
                }
            }
        }

        // Return current conf.  Throw if none.
        JsonNode conf = confRef.get();
        if (conf == null) {
            throw new ConfigException("AutoConf file (" + config.getConfigFileName() + ") is not loaded");
        }

        return conf;
    }
}