// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.sql.samples.ManageSqlDatabase;
import com.azure.resourcemanager.sql.samples.ManageSqlDatabasesAcrossRegions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SqlSampleTests extends SamplesTestBase {

    @Test
    public void testManageSqlDatabase() {
        Assertions.assertTrue(ManageSqlDatabase.runSample(azureResourceManager));
    }

    // Creates SQL Servers across multiple regions with geo-replication; too long to record reliably.
    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageSqlDatabasesAcrossRegions() {
        Assertions.assertTrue(ManageSqlDatabasesAcrossRegions.runSample(azureResourceManager));
    }
}
