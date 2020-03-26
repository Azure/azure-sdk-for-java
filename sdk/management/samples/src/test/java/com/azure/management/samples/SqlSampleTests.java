/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.samples;

import com.azure.management.sql.samples.GettingSqlServerMetrics;
import com.azure.management.sql.samples.ManageSqlDatabase;
import com.azure.management.sql.samples.ManageSqlDatabaseInElasticPool;
import com.azure.management.sql.samples.ManageSqlDatabasesAcrossDifferentDataCenters;
import com.azure.management.sql.samples.ManageSqlFailoverGroups;
import com.azure.management.sql.samples.ManageSqlFirewallRules;
import com.azure.management.sql.samples.ManageSqlImportExportDatabase;
import com.azure.management.sql.samples.ManageSqlServerDnsAliases;
import com.azure.management.sql.samples.ManageSqlServerKeysWithAzureKeyVaultKey;
import com.azure.management.sql.samples.ManageSqlServerSecurityAlertPolicy;
import com.azure.management.sql.samples.ManageSqlVirtualNetworkRules;
import com.azure.management.sql.samples.ManageSqlWithRecoveredOrRestoredDatabase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

public class SqlSampleTests extends SamplesTestBase {
//    @Override
//    protected RestClient buildRestClient(RestClient.Builder builder, boolean isMocked) {
//        if (!isMocked) {
//            return super.buildRestClient(builder, isMocked);
//        }
//        return super.buildRestClient(builder.withReadTimeout(200, TimeUnit.SECONDS), isMocked);
//    }

    @Test
    public void testManageSqlDatabase() {
        Assertions.assertTrue(ManageSqlDatabase.runSample(azure));
    }

    @Test
    public void testManageSqlDatabaseInElasticPool() {
        Assertions.assertTrue(ManageSqlDatabaseInElasticPool.runSample(azure));
    }

    @Test
    public void testManageSqlDatabasesAcrossDifferentDataCenters() {
        Assertions.assertTrue(ManageSqlDatabasesAcrossDifferentDataCenters.runSample(azure));
    }

    @Test
    public void testManageSqlFirewallRules() {
        Assertions.assertTrue(ManageSqlFirewallRules.runSample(azure));
    }

    @Test
    public void testManageSqlServerSecurityAlertPolicy() {
        Assertions.assertTrue(ManageSqlServerSecurityAlertPolicy.runSample(azure));
    }

    @Test
    public void testManageSqlVirtualNetworkRules() {
        Assertions.assertTrue(ManageSqlVirtualNetworkRules.runSample(azure));
    }

    @Test
    public void testManageSqlImportExportDatabase() {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            Assertions.assertTrue(ManageSqlImportExportDatabase.runSample(azure));
        }
    }

    @Test
    public void testManageSqlWithRecoveredOrRestoredDatabase() {
        // This test can take significant time to run since it depends on the availability of certain resources on the service side.
        Assertions.assertTrue(ManageSqlWithRecoveredOrRestoredDatabase.runSample(azure));
    }

    @Test
    public void testManageSqlFailoverGroups() {
        Assertions.assertTrue(ManageSqlFailoverGroups.runSample(azure));
    }

    @Test
    public void testGettingSqlServerMetrics() {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            Assertions.assertTrue(GettingSqlServerMetrics.runSample(azure));
        }
    }

    @Test
    public void testManageSqlServerDnsAliases() {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            Assertions.assertTrue(ManageSqlServerDnsAliases.runSample(azure));
        }
    }

    @Test
    public void testManageSqlServerKeysWithAzureKeyVaultKey() {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            //=============================================================
            // If service principal client id is not set via the local variables, attempt to read the service
            //     principal client id from a secondary ".azureauth" file set through an environment variable.
            //
            //     If the environment variable was not set then reuse the main service principal set for running this sample.

            String servicePrincipalClientId = System.getenv("AZURE_CLIENT_ID");
            if (servicePrincipalClientId == null || servicePrincipalClientId.isEmpty()) {
                String envSecondaryServicePrincipal = System.getenv("AZURE_AUTH_LOCATION_2");

                if (envSecondaryServicePrincipal == null || !envSecondaryServicePrincipal.isEmpty() || !Files.exists(Paths.get(envSecondaryServicePrincipal))) {
                    envSecondaryServicePrincipal = System.getenv("AZURE_AUTH_LOCATION");
                }
                try {
                    servicePrincipalClientId = Utils.getSecondaryServicePrincipalClientID(envSecondaryServicePrincipal);
                } catch (Exception e) {
                    Assertions.assertFalse(true, "Unexpected exception trying to retrieve the client ID");
                }
            }

            Assertions.assertTrue(ManageSqlServerKeysWithAzureKeyVaultKey.runSample(azure, servicePrincipalClientId));
        }
    }
}
