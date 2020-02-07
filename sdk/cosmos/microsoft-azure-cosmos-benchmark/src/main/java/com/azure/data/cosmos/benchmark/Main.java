// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.benchmark;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        org.apache.log4j.Logger.getLogger("io.netty").setLevel(org.apache.log4j.Level.OFF);
        AsyncBenchmark benchmark = null;
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

            switch (cfg.getOperationType()) {
            case WriteThroughput:
            case WriteLatency:
                benchmark = new AsyncWriteBenchmark(cfg);
                break;

            case ReadThroughput:
            case ReadLatency:
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

            case ReadMyWrites:
                benchmark = new ReadMyWriteWorkflow(cfg);
                break;

            default:
                throw new RuntimeException(cfg.getOperationType() + " is not supported");
            }

            LOGGER.info("Starting {}", cfg.getOperationType());
            benchmark.run();

        } catch (ParameterException e) {
            // if any error in parsing the cmd-line options print out the usage help
            System.err.println("INVALID Usage: " + e.getMessage());
            System.err.println("Try '-help' for more information.");
            throw e;
        } finally {
            if (benchmark != null) {
                benchmark.shutdown();
            }
        }
    }
}
