// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.ConfigurationSettingSnapshot;
import com.azure.data.appconfiguration.models.SnapshotSelector;
import com.azure.data.appconfiguration.models.SnapshotSettingFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to list snapshots asynchronously.
 */
public class ListSnapshotsAsync {
    /**
     * Runs the sample demonstrates how to list snapshots asynchronously.
     *
     * @param args Unused. Arguments to the program.
     * @throws InterruptedException when a thread is waiting, sleeping, or otherwise occupied,
     * and the thread is interrupted, either before or during the activity.
     */
    public static void main(String[] args) throws InterruptedException {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";

        // Asynchronous sample

        // Instantiate a client that will be used to call the service.
        final ConfigurationAsyncClient client = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
        // 1. Prepare first setting.
        client.setConfigurationSetting("TestKey1", null, "value1").subscribe(
            result -> {
                final ConfigurationSetting setting = result;
                System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s.%n", setting.getKey(), setting.getValue());
            },
            error -> System.err.println("There was an error adding the setting: " + error),
            () -> System.out.printf("Set setting with key=%s and value=%s added or updated.%n", "TestKey1", "value1"));
        // 1. Prepare second setting.
        client.setConfigurationSetting("TestKey2", null, "value2").subscribe(
            result -> {
                final ConfigurationSetting setting = result;
                System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s.%n", setting.getKey(), setting.getValue());
            },
            error -> System.err.println("There was an error adding the setting: " + error),
            () -> System.out.printf("Set setting with key=%s and value=%s added or updated.%n", "TestKey2", "value2"));

        TimeUnit.MILLISECONDS.sleep(1000);

        // 1. Prepare the snapshot filters
        List<SnapshotSettingFilter> filters = new ArrayList<>();
        // Key Name also supports RegExp but only support prefix end with "*", such as "k*" and is case-sensitive.
        filters.add(new SnapshotSettingFilter("Test*"));
        String snapshotNameTest = "{snapshotNameInTest}";

        // 1. Create first snapshot
        client.beginCreateSnapshot(snapshotNameTest, new ConfigurationSettingSnapshot(filters))
            .flatMap(result -> result.getFinalResult())
            .subscribe(
                snapshot -> {
                    System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
                        snapshot.getName(), snapshot.getCreatedAt(), snapshot.getStatus());
                },
                ex -> System.out.printf("Error on creating a snapshot=%s, with error=%s.%n", snapshotNameTest, ex.getMessage()),
                () -> System.out.println("Successfully created a snapshot."));

        TimeUnit.MILLISECONDS.sleep(1000);

        // 2. Prepare third setting.
        client.setConfigurationSetting("ProductKey1", null, "value1").subscribe(
            result -> {
                final ConfigurationSetting setting = result;
                System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s.%n", setting.getKey(), setting.getValue());
            },
            error -> System.err.println("There was an error adding the setting: " + error),
            () -> System.out.printf("Set setting with key=%s and value=%s added or updated.%n", "ProductKey1", "value1"));
        // 1. Prepare fourth setting.
        client.setConfigurationSetting("ProductKey2", null, "value2").subscribe(
            result -> {
                final ConfigurationSetting setting = result;
                System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s.%n", setting.getKey(), setting.getValue());
            },
            error -> System.err.println("There was an error adding the setting: " + error),
            () -> System.out.printf("Set setting with key=%s and value=%s added or updated.%n", "ProductKey2", "value2"));

        TimeUnit.MILLISECONDS.sleep(1000);

        // 2. Prepare the snapshot filters
        List<SnapshotSettingFilter> filtersInProduct = new ArrayList<>();
        // Key Name also supports RegExp but only support prefix end with "*", such as "k*" and is case-sensitive.
        filtersInProduct.add(new SnapshotSettingFilter("Product*"));
        String snapshotNameProduct = "{snapshotNameInProduct}";

        // 2. Create first snapshot
        client.beginCreateSnapshot(snapshotNameTest, new ConfigurationSettingSnapshot(filters))
            .flatMap(result -> result.getFinalResult())
            .subscribe(
                snapshot -> {
                    System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
                        snapshot.getName(), snapshot.getCreatedAt(), snapshot.getStatus());
                },
                ex -> System.out.printf("Error on creating a snapshot=%s, with error=%s.%n", snapshotNameProduct, ex.getMessage()),
                () -> System.out.println("Successfully created a snapshot."));

        TimeUnit.MILLISECONDS.sleep(1000);

        // List only the snapshot with name = snapshotNameInProduct
        client.listSnapshots(new SnapshotSelector().setName(snapshotNameProduct))
            .subscribe(snapshotResult -> {
                System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
                    snapshotResult.getName(), snapshotResult.getCreatedAt(), snapshotResult.getStatus());
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
