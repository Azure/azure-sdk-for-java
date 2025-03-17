// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;


import com.azure.v2.data.appconfiguration.AzureAppConfigurationClient;
import com.azure.v2.data.appconfiguration.AzureAppConfigurationClientBuilder;
import com.azure.v2.data.appconfiguration.models.KeyValue;
import com.azure.v2.data.appconfiguration.models.KeyValueFilter;
import io.clientcore.core.utils.configuration.Configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrates how to create, retrieve, archive, recover a configuration setting snapshot, and list settings
 * by snapshot name.
 */
public class CreateSnapshot {
    /**
     * Runs the sample demonstrates how to create, retrieve, archive, recover a configuration setting snapshot, and
     * list settings by snapshot name.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

        final AzureAppConfigurationClient client = new AzureAppConfigurationClientBuilder()
            .connectionString(connectionString)
//            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        System.out.println("Beginning of synchronous sample...");
        // Prepare first setting.
        KeyValue setting = client.putKeyValue("TestKey1", null, null, null, null, null, new KeyValue().setValue("value1"));
        System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s.%n", setting.getKey(), setting.getValue());

        // Prepare second setting.
        KeyValue setting2 = client.putKeyValue("TestKey2", null, null, null, null, null, new KeyValue().setValue("value2"));
        System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s.%n", setting.getKey(), setting.getValue());
        // Prepare the snapshot filters
        List<KeyValueFilter> filters = new ArrayList<>();
        // Key Name also supports RegExp but only support prefix end with "*", such as "k*" and is case-sensitive.
//        filters.add(new KeyValueFilter("Test*"));

        // Create a snapshot
//        String snapshotName = "{snapshotName}";
//        SyncPoller<PollOperationDetails, ConfigurationSnapshot> poller =
//            client.putLock(snapshotName, new ConfigurationSnapshot(filters), Context.NONE);
//        poller.setPollInterval(Duration.ofSeconds(10));
//        poller.waitForCompletion();
//        ConfigurationSnapshot snapshot = poller.getFinalResult();
//        System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
//            snapshot.getName(), snapshot.getCreatedAt(), snapshot.getStatus());
//
//        // Get the snapshot status
//        ConfigurationSnapshot getSnapshot = client.getSnapshot(snapshotName);
//        System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
//            getSnapshot.getName(), getSnapshot.getCreatedAt(), getSnapshot.getStatus());
//
//        // Archive a READY snapshot
//        ConfigurationSnapshot archivedSnapshot = client.archiveSnapshot(snapshotName);
//        System.out.printf("Archived snapshot name=%s is created at %s, snapshot status is %s.%n",
//            archivedSnapshot.getName(), archivedSnapshot.getCreatedAt(), archivedSnapshot.getStatus());
//
//        // Recover the Archived snapshot
//        ConfigurationSnapshot recoveredSnapshot = client.recoverSnapshot(snapshotName);
//        System.out.printf("Recovered snapshot name=%s is created at %s, snapshot status is %s.%n",
//            recoveredSnapshot.getName(), recoveredSnapshot.getCreatedAt(), recoveredSnapshot.getStatus());
//
//        // List the configuration settings in the snapshot
//        client.listConfigurationSettingsForSnapshot(snapshotName).forEach(
//            settingInSnapshot -> {
//                System.out.printf("[ConfigurationSetting In Snapshot] Key: %s, Value: %s.%n",
//                    settingInSnapshot.getKey(), settingInSnapshot.getValue());
//            }
//        );

        System.out.println("End of synchronous sample.");
    }
}
