// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.smoketest;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws IllegalArgumentException, IOException, Exception {
        LOGGER.info("================================");
        LOGGER.info("     AZURE SDK SMOKE TEST");
        LOGGER.info("================================");

        KeyVaultSecrets.main(null);
        StorageBlob.main(null);
        EventHubs.main(null);
        // Disabling until cosmos ships from master
        // CosmosDB.main(null);

    }
}
