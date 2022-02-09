// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.sql.samples.GettingSqlServerMetrics;
import com.azure.resourcemanager.sql.samples.ManageSqlDatabase;
import com.azure.resourcemanager.sql.samples.ManageSqlDatabaseInElasticPool;
import com.azure.resourcemanager.sql.samples.ManageSqlDatabasesAcrossDifferentDataCenters;
import com.azure.resourcemanager.sql.samples.ManageSqlFailoverGroups;
import com.azure.resourcemanager.sql.samples.ManageSqlFirewallRules;
import com.azure.resourcemanager.sql.samples.ManageSqlImportExportDatabase;
import com.azure.resourcemanager.sql.samples.ManageSqlServerDnsAliases;
import com.azure.resourcemanager.sql.samples.ManageSqlServerKeysWithAzureKeyVaultKey;
import com.azure.resourcemanager.sql.samples.ManageSqlServerSecurityAlertPolicy;
import com.azure.resourcemanager.sql.samples.ManageSqlVirtualNetworkRules;
import com.azure.resourcemanager.sql.samples.ManageSqlWithRecoveredOrRestoredDatabase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

public class SqlSampleTests extends SamplesTestBase {

    @Test
    public void testManageSqlDatabase() {
        Assertions.assertTrue(ManageSqlDatabase.runSample(azureResourceManager));
    }

    @Test
    public void testManageSqlDatabaseInElasticPool() {
        Assertions.assertTrue(ManageSqlDatabaseInElasticPool.runSample(azureResourceManager));
    }

    @Test
    public void testManageSqlDatabasesAcrossDifferentDataCenters() {
        Assertions.assertTrue(ManageSqlDatabasesAcrossDifferentDataCenters.runSample(azureResourceManager));
    }

    @Test
    public void testManageSqlFirewallRules() {
        Assertions.assertTrue(ManageSqlFirewallRules.runSample(azureResourceManager));
    }

    @Test
    public void testManageSqlServerSecurityAlertPolicy() {
        Assertions.assertTrue(ManageSqlServerSecurityAlertPolicy.runSample(azureResourceManager));
    }

    @Test
    public void testManageSqlVirtualNetworkRules() {
        Assertions.assertTrue(ManageSqlVirtualNetworkRules.runSample(azureResourceManager));
    }

    @Test
    public void testManageSqlImportExportDatabase() {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            Assertions.assertTrue(ManageSqlImportExportDatabase.runSample(azureResourceManager));
        }
    }

    @Test
    public void testManageSqlWithRecoveredOrRestoredDatabase() {
        // This test can take significant time to run since it depends on the availability of certain resources on the service side.
        Assertions.assertTrue(ManageSqlWithRecoveredOrRestoredDatabase.runSample(azureResourceManager));
    }

    @Test
    public void testManageSqlFailoverGroups() {
        Assertions.assertTrue(ManageSqlFailoverGroups.runSample(azureResourceManager));
    }

    @Test
    public void testGettingSqlServerMetrics() throws SQLException, ClassNotFoundException {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            Assertions.assertTrue(GettingSqlServerMetrics.runSample(azureResourceManager));
        }
    }

    @Test
    public void testManageSqlServerDnsAliases() throws SQLException, ClassNotFoundException {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            Assertions.assertTrue(ManageSqlServerDnsAliases.runSample(azureResourceManager));
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

            Assertions.assertTrue(ManageSqlServerKeysWithAzureKeyVaultKey.runSample(azureResourceManager, servicePrincipalClientId));
        }
    }
}
