// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.ConfigurationSettingSnapshot;
import com.azure.data.appconfiguration.models.CreateSnapshotOperationDetail;
import com.azure.data.appconfiguration.models.SnapshotSelector;
import com.azure.data.appconfiguration.models.SnapshotSettingFilter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrates how to list snapshots.
 */
public class ListSnapshots {
    /**
     * Runs the sample demonstrates how to list snapshots.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

        // Instantiate a client that will be used to call the service.
        final ConfigurationClient client = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        System.out.println("Beginning of synchronous sample...");

        // 1. Prepare first setting.
        ConfigurationSetting setting = client.setConfigurationSetting("TestKey1", null, "value1");
        System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s.%n", setting.getKey(), setting.getValue());
        // 1. Prepare second setting.
        ConfigurationSetting setting2 = client.setConfigurationSetting("TestKey2", null, "value2");
        System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s.%n", setting2.getKey(), setting2.getValue());
        // 1. Prepare the snapshot filters
        List<SnapshotSettingFilter> filters = new ArrayList<>();
        // Key Name also supports RegExp but only support prefix end with "*", such as "k*" and is case-sensitive.
        filters.add(new SnapshotSettingFilter("Test*"));

        // 1. Create first snapshot
        String snapshotNameTest = "{snapshotNameInTest}";
        SyncPoller<CreateSnapshotOperationDetail, ConfigurationSettingSnapshot> poller =
            client.beginCreateSnapshot(snapshotNameTest, new ConfigurationSettingSnapshot(filters), null);
        poller.setPollInterval(Duration.ofSeconds(10));
        poller.waitForCompletion();
        ConfigurationSettingSnapshot snapshot = poller.getFinalResult();
        System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
            snapshot.getName(), snapshot.getCreatedAt(), snapshot.getStatus());

        // 2. Prepare third setting.
        ConfigurationSetting setting3 = client.setConfigurationSetting("ProductKey1", null, "value1");
        System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s.%n", setting.getKey(), setting.getValue());
        // 2. Prepare fourth setting.
        ConfigurationSetting setting4 = client.setConfigurationSetting("ProductKey2", null, "value2");
        System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s.%n", setting2.getKey(), setting2.getValue());
        // 2. Prepare the snapshot filters
        List<SnapshotSettingFilter> filters2 = new ArrayList<>();
        // Key Name also supports RegExp but only support prefix end with "*", such as "k*" and is case-sensitive.
        filters.add(new SnapshotSettingFilter("Product*"));

        // 2. Create second snapshot
        String snapshotNameProduct = "{snapshotNameInProduct}";
        SyncPoller<CreateSnapshotOperationDetail, ConfigurationSettingSnapshot> pollerProduct =
            client.beginCreateSnapshot(snapshotNameProduct, new ConfigurationSettingSnapshot(filters), Context.NONE);
        pollerProduct.setPollInterval(Duration.ofSeconds(10));
        pollerProduct.waitForCompletion();
        ConfigurationSettingSnapshot productSnapshot = pollerProduct.getFinalResult();
        System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
            productSnapshot.getName(), productSnapshot.getCreatedAt(), productSnapshot.getStatus());

        // List only the snapshot with name = snapshotNameInProduct
        client.listSnapshots(new SnapshotSelector().setName(snapshotNameProduct))
            .forEach(snapshotResult -> {
                System.out.printf("Listed Snapshot name = %s is created at %s, snapshot status is %s.%n",
                    snapshotResult.getName(), snapshotResult.getCreatedAt(), snapshotResult.getStatus());
            });

        // Get the snapshot status
        ConfigurationSettingSnapshot getSnapshot = client.getSnapshot(snapshotNameProduct);
        System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
            getSnapshot.getName(), getSnapshot.getCreatedAt(), getSnapshot.getStatus());

        // Archive a READY snapshot
        ConfigurationSettingSnapshot archivedSnapshot = client.archiveSnapshot(snapshotNameProduct);
        System.out.printf("Archived snapshot name=%s is created at %s, snapshot status is %s.%n",
            archivedSnapshot.getName(), archivedSnapshot.getCreatedAt(), archivedSnapshot.getStatus());

        // Recover the Archived snapshot
        ConfigurationSettingSnapshot recoveredSnapshot = client.recoverSnapshot(snapshotNameProduct);
        System.out.printf("Recovered snapshot name=%s is created at %s, snapshot status is %s.%n",
            recoveredSnapshot.getName(), recoveredSnapshot.getCreatedAt(), recoveredSnapshot.getStatus());

        // List the configuration settings in the snapshot
        client.listConfigurationSettingsForSnapshot(snapshotNameProduct).forEach(
            settingInSnapshot -> {
                System.out.printf("[ConfigurationSetting in snapshot] Key: %s, Value: %s%n",
                    settingInSnapshot.getKey(), settingInSnapshot.getValue());
            }
        );

        System.out.println("End of synchronous sample.");
    }
}
