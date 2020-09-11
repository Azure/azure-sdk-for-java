// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.dotnet.benchmark;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        AnsiConsole.systemInstall();
        try {
            LOGGER.debug("Parsing the arguments ...");
            BenchmarkConfig cfg = new BenchmarkConfig();

            JCommander jcommander = new JCommander(cfg, args);
            if (cfg.isHelp()) {
                // prints out the usage help
                jcommander.usage();
                return;
            }

            cfg.validate();



            // TODO FABIANM
            // wire up executor
        } catch (ParameterException e) {
            // if any error in parsing the cmd-line options print out the usage help
            System.err.println("INVALID Usage: " + e.getMessage());
            System.err.println("Try '-help' for more information.");
            throw e;
        }
        finally {
            AnsiConsole.systemUninstall();
        }
    }
}
