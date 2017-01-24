/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.sql.samples.ManageSqlDatabase;
import com.microsoft.azure.management.sql.samples.ManageSqlDatabaseInElasticPool;
import com.microsoft.azure.management.sql.samples.ManageSqlDatabasesAcrossDifferentDataCenters;
import com.microsoft.azure.management.sql.samples.ManageSqlFirewallRules;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class SqlSampleTests extends SamplesTestBase {
    @Test
    public void testManageSqlDatabase() {
        Assert.assertTrue(ManageSqlDatabase.runSample(azure));
    }

    @Test
    public void testManageSqlDatabaseInElasticPool() {
        Assert.assertTrue(ManageSqlDatabaseInElasticPool.runSample(azure));
    }

    @Test
    @Ignore("Failing")
    public void testManageSqlDatabasesAcrossDifferentDataCenters() {
        Assert.assertTrue(ManageSqlDatabasesAcrossDifferentDataCenters.runSample(azure));
    }

    @Test
    public void testManageSqlFirewallRules() {
        Assert.assertTrue(ManageSqlFirewallRules.runSample(azure));
    }
}
