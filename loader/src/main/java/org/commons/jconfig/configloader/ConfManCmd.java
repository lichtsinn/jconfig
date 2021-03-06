package org.commons.jconfig.configloader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.commons.jconfig.internal.jmx.JmxManager;
import org.commons.jconfig.internal.jmx.VirtualMachineException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * The Configuration command line application.
 * 
 */
public class ConfManCmd {

    private final Logger logger = Logger.getLogger(ConfManCmd.class);

    /**
     * 
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) {
        ConfManCmd cmd = new ConfManCmd();
        cmd.init();
        cmd.execute(args);
    }

    private final Options options = new Options();

    void init() {
        options.addOption("h", "help", false, "print this message");
        options.addOption("v", "version", false,
                "print the version information and exit");
        options.addOption("l", "list", false,
                "list all jmx beans from all jvms");
        options.addOption("j", "jvms", false, "list all jvms");
        options.addOption("u", "unformatted", false, "unformatted json output");
        options.addOption("s", "save", true, "save json to a file");
        options.getOption("save").setArgName("file");
        // options.addOption("o", "objectName", true,
        // "set a jmx bean value. Exp: java.util.logging:type=Logging");
        // options.getOption("objectName").setArgName("objectName");
        // options.getOption("objectName").setValueSeparator('=');
        // options.addOption("r", "read", true, "read a jmx bean value");
        // options.addOption("w", "write", true, "write a jmx bean value");

    }

    private static String VERSION = "v1.0";

    void execute(final String[] args) {
        try {
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("list")) {
                JmxManager manager = new JmxManager();
                JsonObject json = manager.readApplicationMbeans();
                manager.close();
                if (cmd.hasOption("unformatted")) {
                    logger.info(json.toString());
                } else {
                    Gson gson = new GsonBuilder().create();
                    logger.info(gson.toJson(json));
                }
            } else if (cmd.hasOption("save")) {
                JmxManager manager = new JmxManager();
                JsonObject json = manager.readApplicationMbeans();
                manager.close();
                String data = "";
                if (cmd.hasOption("unformatted")) {
                    data = json.toString();
                } else {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    data = gson.toJson(json);
                }
                String filename = cmd.getOptionValue("save");
                File currFile = new File(filename);
                File tmpFile = File.createTempFile("jmx", null,
                        currFile.getParentFile());
                Writer writer = new FileWriter(tmpFile);
                writer.write(data);
                writer.close();

                if (currFile.exists()) {
                    if (currFile.delete()) {
                        tmpFile.renameTo(currFile);
                    } else {
                        logger.error("Error deleting file "
                                + currFile.getName()
                                + ". Cannot rename temp file "
                                + tmpFile.getName());
                    }
                } else {
                    tmpFile.renameTo(currFile);
                }
            } else if (cmd.hasOption("jvms")) {
                JmxManager manager = new JmxManager();
                JsonObject json = manager.listAllJvms();
                manager.close();
                if (cmd.hasOption("unformatted")) {
                    logger.info(json.toString());
                } else {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    logger.info(gson.toJson(json));
                }
            } else if (cmd.hasOption("version")) {
                logger.info("Version " + VERSION);
            } else {
                // automatically generate the help statement
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("config", options);
            }
        } catch (ParseException e) {
            logger.error("Unexpected exception.", e);
        } catch (VirtualMachineException e) {
            logger.error("Unexpected exception.", e);
        } catch (IOException e) {
            logger.error("Unexpected exception.", e);
        }
    }

}
