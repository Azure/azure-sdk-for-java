// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.PublicNetworkAccess;
import com.azure.resourcemanager.compute.models.Snapshot;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SnapshotOptionsTests extends ComputeManagementTest {
    private String rgName = "";
    private Region region = Region.US_WEST_CENTRAL;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canCreateAndUpdatePublicNetworkAccess() {
        final String snapshotName = generateRandomResourceName("ss", 20);
        final String diskName1 = generateRandomResourceName("md-1", 20);
        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        Disk disk =
            computeManager
                .disks()
                .define(diskName1)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup.name())
                .withData()
                .withSizeInGB(100)
                .disablePublicNetworkAccess()
                .create();

        Snapshot snapshot = computeManager.snapshots()
            .define(snapshotName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withDataFromDisk(disk)
            .disablePublicNetworkAccess()
            .create();

        snapshot.refresh();
        Assertions.assertEquals(PublicNetworkAccess.DISABLED, snapshot.publicNetworkAccess());

        snapshot.update().enablePublicNetworkAccess().apply();
        snapshot.refresh();
        Assertions.assertEquals(PublicNetworkAccess.ENABLED, snapshot.publicNetworkAccess());

        snapshot.update().disablePublicNetworkAccess().apply();
        snapshot.refresh();
        Assertions.assertEquals(PublicNetworkAccess.DISABLED, snapshot.publicNetworkAccess());
    }
}
