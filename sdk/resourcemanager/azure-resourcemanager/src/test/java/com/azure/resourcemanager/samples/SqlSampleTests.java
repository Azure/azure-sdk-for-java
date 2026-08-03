// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.sql.samples.ManageSqlDatabase;
import com.azure.resourcemanager.sql.samples.ManageSqlDatabasesAcrossRegions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SqlSampleTests extends SamplesTestBase {

    @Test
    public void testManageSqlDatabase() {
        Assertions.assertTrue(ManageSqlDatabase.runSample(azureResourceManager));
    }

    @Test
    public void testManageSqlDatabasesAcrossRegions() {
        Assertions.assertTrue(ManageSqlDatabasesAcrossRegions.runSample(azureResourceManager));
    }
}
