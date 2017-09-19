package com.microsoft.azure.documentdb.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Main {

    private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        try {
            LOGGER.debug("Parsing the arguments ...");
            Configuration cfg = new Configuration();
            JCommander jcommander = new JCommander(cfg, args);

            if (cfg.isHelp()) {
                // prints out the usage help
                jcommander.usage();
                return;
            }
            
            cfg.validate();

            AbstractBulkInsertBenchmark benchmark;
            switch (cfg.getClientType()) {

            case rxNonBlocking:
                benchmark = RxAsyncBulkInsertBenchmark.fromConfiguration(cfg);
                break;
            case blocking:
                benchmark = SyncBulkInsertBenchmark.fromConfiguration(cfg);
                break;

            default:
                throw new RuntimeException(cfg.getClientType() + " is not supported");
            }

            benchmark.init();
            benchmark.run();
            benchmark.shutdown();

        } catch (ParameterException e) {
            // if any error in parsing the cmd-line options print out the usage help
            System.err.println("Invalid Usage: " + e.getMessage());
            System.err.println("Try '-help' for more information.");

            System.exit(1);
        }
    }
    
    private Main() {};
}
