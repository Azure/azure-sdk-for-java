/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
