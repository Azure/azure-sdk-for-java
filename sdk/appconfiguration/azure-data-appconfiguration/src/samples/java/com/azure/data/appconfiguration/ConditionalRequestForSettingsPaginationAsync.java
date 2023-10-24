// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.util.Configuration;

public class ConditionalRequestForSettingsPaginationAsync {
    /**
     * Runs the sample algorithm and demonstrates how to add, get, and delete a configuration setting by conditional
     * request asynchronously
     * @param args Unused. Arguments to the program.
     * @throws InterruptedException when a thread is waiting, sleeping, or otherwise occupied,
     * and the thread is interrupted, either before or during the activity.
     */
    public static void main(String[] args) throws InterruptedException {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

        // Instantiate a client that will be used to call the service.
        final ConfigurationAsyncClient client = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

    }
}
