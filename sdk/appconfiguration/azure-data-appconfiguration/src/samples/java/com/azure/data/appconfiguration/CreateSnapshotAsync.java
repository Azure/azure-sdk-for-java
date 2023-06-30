// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.ConfigurationSettingSnapshot;
import com.azure.data.appconfiguration.models.SnapshotSettingFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to create, retrieve, archive, recover a configuration setting snapshot, and list settings
 * by snapshot name asynchronously.
 */
public class CreateSnapshotAsync {
    /**
     * Runs the sample demonstrates how to create, retrieve, archive, recover a configuration setting snapshot, and
     * list settings by snapshot name asynchronously.
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
        // Prepare first setting.
        client.setConfigurationSetting("TestKey1", null, "v1").subscribe(
            result -> {
                final ConfigurationSetting setting = result;
                System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue());
            },
            error -> System.err.println("There was an error adding the setting: " + error),
            () -> System.out.printf("Set setting with key=%s and value=%s added or updated.%n", "key1", "v1"));

        // Prepare second setting.
        client.setConfigurationSetting("TestKey2", null, "v2").subscribe(
            result -> {
                final ConfigurationSetting setting = result;
                System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s.%n", setting.getKey(), setting.getValue());
            },
            error -> System.err.println("There was an error adding the setting: " + error),
            () -> System.out.printf("Set setting with key=%s and value=%s added or updated.%n", "key2", "v2"));

        TimeUnit.MILLISECONDS.sleep(1000);

        // Prepare the snapshot filters
        List<SnapshotSettingFilter> filters = new ArrayList<>();
        // Key Name also supports RegExp but only support prefix end with "*", such as "k*" and is case-sensitive.
        filters.add(new SnapshotSettingFilter("Test*"));
        String snapshotName = "{snapshotName}";

        client.beginCreateSnapshot(snapshotName, new ConfigurationSettingSnapshot(filters))
            .flatMap(result -> result.getFinalResult())
            .subscribe(
                snapshot -> {
                    System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
                        snapshot.getName(), snapshot.getCreatedAt(), snapshot.getStatus());
                },
                ex -> System.out.printf("Error on creating a snapshot=%s, with error=%s.%n", snapshotName, ex.getMessage()),
                () -> System.out.println("Successfully created a snapshot."));

        // Get the snapshot status
        client.getSnapshot(snapshotName).subscribe(
            getSnapshot -> {
                System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
                    getSnapshot.getName(), getSnapshot.getCreatedAt(), getSnapshot.getStatus());
            }
        );

        TimeUnit.MILLISECONDS.sleep(1000);

        // Archive a READY snapshot
        client.archiveSnapshot(snapshotName).subscribe(
            archivedSnapshot -> {
                System.out.printf("Archived snapshot name=%s is created at %s, snapshot status is %s.%n",
                    archivedSnapshot.getName(), archivedSnapshot.getCreatedAt(), archivedSnapshot.getStatus());
            }
        );

        TimeUnit.MILLISECONDS.sleep(1000);

        // Recover the Archived snapshot
        client.recoverSnapshot(snapshotName).subscribe(
            recoveredSnapshot -> {
                System.out.printf("Recovered snapshot name=%s is created at %s, snapshot status is %s.%n",
                    recoveredSnapshot.getName(), recoveredSnapshot.getCreatedAt(), recoveredSnapshot.getStatus());
            }
        );

        TimeUnit.MILLISECONDS.sleep(1000);

        // List the configuration settings in the snapshot
        client.listConfigurationSettingsForSnapshot(snapshotName).subscribe(
            settingInSnapshot -> {
                System.out.printf("[ConfigurationSetting in snapshot] Key: %s, Value: %s.%n",
                    settingInSnapshot.getKey(), settingInSnapshot.getValue());
            }
        );

        TimeUnit.MILLISECONDS.sleep(1000);

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
