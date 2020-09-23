// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.dotnet.benchmark;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.fusesource.jansi.Ansi.ansi;

final class Utility {
    private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void traceInformation(String payload) {
        AnsiConsole.out().println(payload);
        LOGGER.info(payload);
    }

    public static void traceInformation(String payload, Ansi.Color color) {
        AnsiConsole.out().println(ansi().fg(color).a(payload).reset());
        LOGGER.info(payload);
    }
}