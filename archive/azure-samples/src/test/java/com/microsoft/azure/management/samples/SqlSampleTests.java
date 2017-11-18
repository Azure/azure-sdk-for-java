/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.sql.samples.ManageSqlDatabase;
import com.microsoft.azure.management.sql.samples.ManageSqlDatabaseInElasticPool;
import com.microsoft.azure.management.sql.samples.ManageSqlDatabasesAcrossDifferentDataCenters;
import com.microsoft.azure.management.sql.samples.ManageSqlFirewallRules;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class SqlSampleTests extends SamplesTestBase {
    @Override
    protected RestClient buildRestClient(RestClient.Builder builder, boolean isMocked) {
        if (!isMocked) {
            return super.buildRestClient(builder, isMocked);
        }
        return super.buildRestClient(builder.withReadTimeout(200, TimeUnit.SECONDS), isMocked);
    }

    @Test
    public void testManageSqlDatabase() {
        Assert.assertTrue(ManageSqlDatabase.runSample(azure));
    }

    @Test
    public void testManageSqlDatabaseInElasticPool() {
        Assert.assertTrue(ManageSqlDatabaseInElasticPool.runSample(azure));
    }

    @Test
    public void testManageSqlDatabasesAcrossDifferentDataCenters() {
        Assert.assertTrue(ManageSqlDatabasesAcrossDifferentDataCenters.runSample(azure));
    }

    @Test
    public void testManageSqlFirewallRules() {
        Assert.assertTrue(ManageSqlFirewallRules.runSample(azure));
    }
}
