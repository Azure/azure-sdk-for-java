// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.cosmos.samples.CreateCosmosDBTableWithVirtualNetworkRule;
import com.azure.resourcemanager.cosmos.samples.CreateCosmosDBWithEventualConsistency;
import com.azure.resourcemanager.cosmos.samples.CreateCosmosDBWithIPRange;
import com.azure.resourcemanager.cosmos.samples.CreateCosmosDBWithKindMongoDB;
import com.azure.resourcemanager.cosmos.samples.ManageHACosmosDB;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CosmosDBTests extends SamplesTestBase {

    @Test
    public void testCreateCosmosDBWithEventualConsistency() {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            Assertions.assertTrue(CreateCosmosDBWithEventualConsistency.runSample(azure));
        }
    }

    @Test
    public void testCreateCosmosDBWithIPRange() {
        Assertions.assertTrue(CreateCosmosDBWithIPRange.runSample(azure));
    }

    @Test
    public void testCreateCosmosDBTableWithVirtualNetworkRule() {
        Assertions.assertTrue(CreateCosmosDBTableWithVirtualNetworkRule.runSample(azure));
    }

    @Test
    public void testCreateCosmosDBWithKindMongoDB() {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            Assertions.assertTrue(CreateCosmosDBWithKindMongoDB.runSample(azure));
        }
    }

    @Test
    public void testManageHACosmosDB() {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            Assertions.assertTrue(ManageHACosmosDB.runSample(azure));
        }
    }
}
