// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.util.polling.SyncPoller;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.ConfigurationSettingSnapshot;
import com.azure.data.appconfiguration.models.CreateSnapshotOperationDetail;
import com.azure.data.appconfiguration.models.SnapshotSettingFilter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrates how to create a configuration setting snapshot.
 */
public class CreateSnapshot {
    /**
     * Runs the sample algorithm and demonstrates how to create a configuration setting snapshot.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";

        // Instantiate a client that will be used to call the service.
        final ConfigurationClient client = new ConfigurationClientBuilder()
                                               .connectionString(connectionString)
                                               .buildClient();

        System.out.println("Beginning of synchronous sample...");
        // Prepare first setting.
        ConfigurationSetting setting = client.setConfigurationSetting("key1", null, "value1");
        System.out.printf(String.format("[SetConfigurationSetting] Key: %s, Value: %s.%n", setting.getKey(), setting.getValue()));
        // Prepare second setting.
        ConfigurationSetting setting2 = client.setConfigurationSetting("key2", null, "value2");
        System.out.printf(String.format("[SetConfigurationSetting] Key: %s, Value: %s.%n", setting2.getKey(), setting2.getValue()));
        // Prepare the snapshot filters
        List<SnapshotSettingFilter> filters = new ArrayList<>();
        // Key Name also supports RegExp but only support prefix end with "*", such as "k*" and is case-sensitive.
        filters.add(new SnapshotSettingFilter("k*"));

        String snapshotName = "{snapshotName}";
        SyncPoller<CreateSnapshotOperationDetail, ConfigurationSettingSnapshot> poller =
            client.beginCreateSnapShot(snapshotName, filters);
        poller.setPollInterval(Duration.ofSeconds(10));
        poller.waitForCompletion();

        ConfigurationSettingSnapshot snapshot= poller.getFinalResult();
        System.out.printf("Snapshot name=%s is created at %s%n", snapshot.getName(), snapshot.getCreatedAt());

        System.out.println("End of synchronous sample.");
    }
}
