// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.cosmos.samples.CreateCosmosDBTableWithVirtualNetworkRule;
import com.azure.resourcemanager.cosmos.samples.CreateCosmosDBWithEventualConsistency;
import com.azure.resourcemanager.cosmos.samples.CreateCosmosDBWithIPRange;
import com.azure.resourcemanager.cosmos.samples.CreateCosmosDBWithKindMongoDB;
import com.azure.resourcemanager.cosmos.samples.ManageHACosmosDB;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CosmosDBTests extends SamplesTestBase {

    // Most this error in runtime
    // Sorry, we are currently experiencing high demand in Central US region, and cannot fulfill your request at this time Thu, 27 Jul 2023 01:57:24 GMT.

    @DoNotRecord(skipInPlayback = true) // TODO(weidxu)
    @Test
    public void testCreateCosmosDBWithEventualConsistency() {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            Assertions.assertTrue(CreateCosmosDBWithEventualConsistency.runSample(azureResourceManager));
        }
    }

    @DoNotRecord(skipInPlayback = true) // TODO(weidxu)
    @Test
    public void testCreateCosmosDBWithIPRange() {
        Assertions.assertTrue(CreateCosmosDBWithIPRange.runSample(azureResourceManager));
    }

    @Test
    public void testCreateCosmosDBTableWithVirtualNetworkRule() {
        Assertions.assertTrue(CreateCosmosDBTableWithVirtualNetworkRule.runSample(azureResourceManager));
    }

    @DoNotRecord(skipInPlayback = true) // TODO(weidxu)
    @Test
    public void testCreateCosmosDBWithKindMongoDB() {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            Assertions.assertTrue(CreateCosmosDBWithKindMongoDB.runSample(azureResourceManager));
        }
    }

    @DoNotRecord(skipInPlayback = true) // TODO(weidxu)
    @Test
    public void testManageHACosmosDB() {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            Assertions.assertTrue(ManageHACosmosDB.runSample(azureResourceManager));
        }
    }
}
