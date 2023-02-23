// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.data.appconfiguration.models.CompositionType;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.ConfigurationSettingSnapshot;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.data.appconfiguration.models.SnapshotFilter;
import com.azure.data.appconfiguration.models.SnapshotSelector;
import com.azure.data.appconfiguration.models.SnapshotStatus;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SnapshotSamples {
    public static void main(String[] args) {

        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";

        // Instantiate a client that will be used to call the service.
        final ConfigurationClient client = new ConfigurationClientBuilder()
                                               .connectionString(connectionString)
                                               .buildClient();

        // 1. Create
        ArrayList<SnapshotFilter> filter = new ArrayList<>();
        filter.add(new SnapshotFilter("key*"));
        filter.add(new SnapshotFilter("2020-01-01"));

        Map<String, String> tags = new HashMap<>();
        tags.put("tagName", "tagValue");

        ConfigurationSettingSnapshot snapShot = client.createSnapShot("2022-02-01", filter);
        // composition type, retention period, tags
        Response<ConfigurationSettingSnapshot> snapShotWithResponse = client.createSnapShotWithResponse(
            "2022-01-01",
            snapShot.setCompositionType(CompositionType.GROUP_BY_KEY)
                .setRetentionPeriod(Duration.ofSeconds(10000L))
                .setTags(tags),
            Context.NONE);

        // ConfigurationSetting Snapshots properties
        CompositionType compositionType = snapShot.getCompositionType();
        Duration retentionPeriod = snapShot.getRetentionPeriod();
        IterableStream<SnapshotFilter> filters = snapShot.getFilters();
        Map<String, String> tags1 = snapShot.getTags();
        // Ready-only
        String name = snapShot.getName();
        String eTag = snapShot.getETag();
        OffsetDateTime createdAt = snapShot.getCreatedAt();
        OffsetDateTime expiresAt = snapShot.getExpiresAt();
        Long size = snapShot.getSize();
        SnapshotStatus status = snapShot.getStatus();
        Long itemCount = snapShot.getItemCount();
        Integer statusCode = snapShot.getStatusCode();

        // 2. Get single snapshot
        ConfigurationSettingSnapshot getSnapshotByName = client.getSnapShot(name);
        Response<ConfigurationSettingSnapshot> getSnapshotWithETags = client.getSnapShotWithResponse(snapShot, true, Context.NONE);

        // 3. Update snapshot
        // 3.1 archive snapshot
        ConfigurationSettingSnapshot snapshotArchived = client.archiveSnapshot(snapShot.getName());
        Response<ConfigurationSettingSnapshot> snapshotArchivedWithResponse = client.archiveSnapshotWithResponse(snapShot, true, Context.NONE);

        // 3.2 recover snapshot
        ConfigurationSettingSnapshot snapshotRecovered = client.recoverSnapshot(snapshotArchived.getName());
        Response<ConfigurationSettingSnapshot> snapshotRecoveredWithResponse = client.recoverSnapshotWithResponse(snapshotArchived, true, Context.NONE);

        // 4. List snapshot
        PagedIterable<ConfigurationSettingSnapshot> listAllSnapshots = client.listSnapshots(null);
        PagedIterable<ConfigurationSettingSnapshot> snapshotsWithKeyAsPrefix = client.listSnapshots(new SnapshotSelector().setName("key*"));
        PagedIterable<ConfigurationSettingSnapshot> allReadySnapshots = client.listSnapshots(new SnapshotSelector().setSnapshotStatus(SnapshotStatus.READY));


        // 5 list all configuration settings by giving a snapshot name
        PagedIterable<ConfigurationSetting> configurationSettings = client.listConfigurationSettings(new SettingSelector().setSnapshotName("2022-02-01"));


    }
}
