package com.microsoft.azure.cosmosdb.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Main {

    private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        org.apache.log4j.Logger.getLogger("io.netty").setLevel(org.apache.log4j.Level.OFF);

        try {
            LOGGER.debug("Parsing the arguments ...");
            Configuration cfg = new Configuration();
            cfg.tryGetValuesFromSystem();

            JCommander jcommander = new JCommander(cfg, args);
            if (cfg.isHelp()) {
                // prints out the usage help
                jcommander.usage();
                return;
            }

            AsyncBenchmark benchmark;
            switch (cfg.getOperationType()) {
            case Write:
                benchmark = new AsyncWriteBenchmark(cfg);
                break;

            case Read:
                benchmark = new AsyncReadBenchmark(cfg);
                break;

            case QueryCross:
            case QuerySingle:
            case QueryParallel:
            case QueryOrderby:
            case QueryAggregate:
            case QueryTopOrderby:
            case QueryAggregateTopOrderby:
                benchmark = new AsyncQueryBenchmark(cfg);
                break;

            case Mixed:
                benchmark = new AsyncMixedBenchmark(cfg);
                break;

            case QuerySingleMany:
                benchmark = new AsyncQuerySinglePartitionMultiple(cfg);
                break;

            default:
                throw new RuntimeException(cfg.getOperationType() + " is not supported");
            }

            benchmark.run();
            benchmark.shutdown();

        } catch (ParameterException e) {
            // if any error in parsing the cmd-line options print out the usage help
            System.err.println("Invalid Usage: " + e.getMessage());
            System.err.println("Try '-help' for more information.");
            System.exit(1);
        }
        System.exit(0);
    }
}
