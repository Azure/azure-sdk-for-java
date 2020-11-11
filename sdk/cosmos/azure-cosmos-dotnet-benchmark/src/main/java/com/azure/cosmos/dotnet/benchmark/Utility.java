// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.dotnet.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Utility {
    private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void traceInformation(String payload) {
        LOGGER.info(payload);
    }
}