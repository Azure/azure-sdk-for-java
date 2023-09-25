//// Copyright (c) Microsoft Corporation. All rights reserved.
//// Licensed under the MIT License.
//
//package com.azure.android.appconfiguration;
//
//import android.util.Log;
//
//import com.azure.core.util.Context;
//import com.azure.data.appconfiguration.ConfigurationClient;
//import com.azure.data.appconfiguration.ConfigurationClientBuilder;
//import com.azure.data.appconfiguration.models.ConfigurationSetting;
//import com.azure.identity.ClientSecretCredential;
//
//
//import java.time.Duration;
//import java.util.ArrayList;
//
///**
// * Sample demonstrates how to create, retrieve, archive, recover a configuration setting snapshot, and list settings
// * by snapshot name.
// */
//public class CreateSnapshot {
//    /**
//     * Runs the sample demonstrates how to create, retrieve, archive, recover a configuration setting snapshot, and
//     * list settings by snapshot name.
//     *
//     * @param args Unused. Arguments to the program.
//     */
//
//    private static final String TAG = "CreateSnapshotOutput";
//
//    public static void main(String endpoint, ClientSecretCredential credential) {
//        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
//        // and navigating to "Access Keys" page under the "Settings" section.
//
//        // Instantiate a client that will be used to call the service.
//        final ConfigurationClient client = new ConfigurationClientBuilder()
//                .credential(credential)
//                .endpoint(endpoint)
//                .buildClient();
//
//        Log.i(TAG, "Beginning of synchronous sample...");
//        // Prepare first setting.
//        ConfigurationSetting setting = client.setConfigurationSetting("TestKey1", null, "value1");
//        Log.i(TAG, String.format("[SetConfigurationSetting] Key: %s, Value: %s.%n", setting.getKey(), setting.getValue()));
//        // Prepare second setting.
//        ConfigurationSetting setting2 = client.setConfigurationSetting("TestKey2", null, "value2");
//        Log.i(TAG, String.format("[SetConfigurationSetting] Key: %s, Value: %s.%n", setting2.getKey(), setting2.getValue()));
//        // Prepare the snapshot filters
//        List<SnapshotSettingFilter> filters = new ArrayList<>();
//        // Key Name also supports RegExp but only support prefix end with "*", such as "k*" and is case-sensitive.
//        filters.add(new SnapshotSettingFilter("Test*"));
//
//        // Create a snapshot
//        String snapshotName = "{snapshotName}";
//        SyncPoller<CreateSnapshotOperationDetail, ConfigurationSettingSnapshot> poller =
//            client.beginCreateSnapshot(snapshotName, new ConfigurationSettingSnapshot(filters), Context.NONE);
//        poller.setPollInterval(Duration.ofSeconds(10));
//        poller.waitForCompletion();
//        ConfigurationSettingSnapshot snapshot = poller.getFinalResult();
//        Log.i(TAG, String.format("Snapshot name=%s is created at %s, snapshot status is %s.%n",
//            snapshot.getName(), snapshot.getCreatedAt(), snapshot.getStatus()));
//
//        // Get the snapshot status
//        ConfigurationSettingSnapshot getSnapshot = client.getSnapshot(snapshotName);
//        Log.i(TAG, String.format("Snapshot name=%s is created at %s, snapshot status is %s.%n",
//            getSnapshot.getName(), getSnapshot.getCreatedAt(), getSnapshot.getStatus()));
//
//        // Archive a READY snapshot
//        ConfigurationSettingSnapshot archivedSnapshot = client.archiveSnapshot(snapshotName);
//        Log.i(TAG, String.format("Archived snapshot name=%s is created at %s, snapshot status is %s.%n",
//            archivedSnapshot.getName(), archivedSnapshot.getCreatedAt(), archivedSnapshot.getStatus()));
//
//        // Recover the Archived snapshot
//        ConfigurationSettingSnapshot recoveredSnapshot = client.recoverSnapshot(snapshotName);
//        Log.i(TAG, String.format("Recovered snapshot name=%s is created at %s, snapshot status is %s.%n",
//            recoveredSnapshot.getName(), recoveredSnapshot.getCreatedAt(), recoveredSnapshot.getStatus()));
//
//        // List the configuration settings in the snapshot
//        client.listConfigurationSettingsForSnapshot(snapshotName).forEach(
//            settingInSnapshot -> {
//                Log.i(TAG, String.format("[ConfigurationSetting In Snapshot] Key: %s, Value: %s.%n",
//                    settingInSnapshot.getKey(), settingInSnapshot.getValue()));
//            }
//        );
//
//        Log.i(TAG, "End of synchronous sample.");
//    }
//}
