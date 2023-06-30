// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.ConfigurationSettingSnapshot;
import com.azure.data.appconfiguration.models.CreateSnapshotOperationDetail;
import com.azure.data.appconfiguration.models.SnapshotSettingFilter;

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
        String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";

        // Instantiate a client that will be used to call the service.
        final ConfigurationClient client = new ConfigurationClientBuilder()
                                               .connectionString(connectionString)
                                               .buildClient();

        System.out.println("Beginning of synchronous sample...");
        // Prepare first setting.
        ConfigurationSetting setting = client.setConfigurationSetting("TestKey1", null, "value1");
        System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s.%n", setting.getKey(), setting.getValue());
        // Prepare second setting.
        ConfigurationSetting setting2 = client.setConfigurationSetting("TestKey2", null, "value2");
        System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s.%n", setting2.getKey(), setting2.getValue());
        // Prepare the snapshot filters
        List<SnapshotSettingFilter> filters = new ArrayList<>();
        // Key Name also supports RegExp but only support prefix end with "*", such as "k*" and is case-sensitive.
        filters.add(new SnapshotSettingFilter("Test*"));

        // Create a snapshot
        String snapshotName = "{snapshotName}";
        SyncPoller<CreateSnapshotOperationDetail, ConfigurationSettingSnapshot> poller =
            client.beginCreateSnapshot(snapshotName, new ConfigurationSettingSnapshot(filters), Context.NONE);
        poller.setPollInterval(Duration.ofSeconds(10));
        poller.waitForCompletion();
        ConfigurationSettingSnapshot snapshot = poller.getFinalResult();
        System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
            snapshot.getName(), snapshot.getCreatedAt(), snapshot.getStatus());

        // Get the snapshot status
        ConfigurationSettingSnapshot getSnapshot = client.getSnapshot(snapshotName);
        System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
            getSnapshot.getName(), getSnapshot.getCreatedAt(), getSnapshot.getStatus());

        // Archive a READY snapshot
        ConfigurationSettingSnapshot archivedSnapshot = client.archiveSnapshot(snapshotName);
        System.out.printf("Archived snapshot name=%s is created at %s, snapshot status is %s.%n",
            archivedSnapshot.getName(), archivedSnapshot.getCreatedAt(), archivedSnapshot.getStatus());

        // Recover the Archived snapshot
        ConfigurationSettingSnapshot recoveredSnapshot = client.recoverSnapshot(snapshotName);
        System.out.printf("Recovered snapshot name=%s is created at %s, snapshot status is %s.%n",
            recoveredSnapshot.getName(), recoveredSnapshot.getCreatedAt(), recoveredSnapshot.getStatus());

        // List the configuration settings in the snapshot
        client.listConfigurationSettingsForSnapshot(snapshotName).forEach(
            settingInSnapshot -> {
                System.out.printf("[ConfigurationSetting In Snapshot] Key: %s, Value: %s.%n",
                    settingInSnapshot.getKey(), settingInSnapshot.getValue());
            }
        );

        System.out.println("End of synchronous sample.");
    }
}
