// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.LabelSelector;

import java.util.concurrent.TimeUnit;

/**
 * A sample demonstrate how to list labels asynchronously.
 */
public class ListLabelsAsync {
    /**
     * Runs the sample algorithm and demonstrates how to add, get, and delete a configuration setting asynchronous.
     *
     * @param args Unused. Arguments to the program.
     * @throws InterruptedException when a thread is waiting, sleeping, or otherwise occupied,
     * and the thread is interrupted, either before or during the activity.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";

        // Asynchronous sample
        // Instantiate a client that will be used to call the service.
        final ConfigurationAsyncClient client = new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .buildAsyncClient();

        client.listLabels(new LabelSelector().setLabelFilter("prod*"))
                .subscribe(label -> {
                    System.out.println("label name = " + label);
                });

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.MINUTES.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
