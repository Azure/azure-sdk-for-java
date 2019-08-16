// ------------------------------------
// Copyright(c) Microsoft Corporation.
// Licensed under the MIT License.
// ------------------------------------
package com.azure;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws IllegalArgumentException, IOException {
        logger.info("================================");
        logger.info("     AZURE SDK SMOKE TEST");
        logger.info("================================");

        KeyVaultSecrets.main(null);
        StorageBlob.main(null);
        EventHubs.main(null);
        CosmosDB.main(null);

    }
}
