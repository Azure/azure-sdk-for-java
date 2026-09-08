// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.appservice.samples.ConnectWebAppToSqlDatabase;
import com.azure.resourcemanager.appservice.samples.ConnectWebAppToStorageAccount;
import com.azure.resourcemanager.appservice.samples.DeployImageFromAcrToLinuxWebApp;
import com.azure.resourcemanager.appservice.samples.ManageWebAppSlots;
import com.azure.resourcemanager.appservice.samples.ManageWebAppWithCustomDomain;
import com.azure.resourcemanager.appservice.samples.ScaleWebAppWithTrafficManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class AppServiceSampleTests extends SamplesTestBase {

    @Test
    public void testManageWebAppSlots() {
        Assertions.assertTrue(ManageWebAppSlots.runSample(azureResourceManager));
    }

    @Test
    public void testConnectWebAppToSqlDatabase() {
        Assertions.assertTrue(ConnectWebAppToSqlDatabase.runSample(azureResourceManager));
    }

    // Registers a custom domain, which requires out-of-band DNS configuration that cannot be recorded.
    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageWebAppWithCustomDomain() {
        Assertions.assertTrue(ManageWebAppWithCustomDomain.runSample(azureResourceManager));
    }

    // Disabled: App Service's Traffic Manager integration requires the web apps to be in *different* regions
    // (same-region endpoints are rejected by the geomaster with "endpoints are not valid"), and the final
    // scale-up doubles the primary plan to 2 instances. Recording therefore needs a subscription with App
    // Service "Total VMs" quota >= 1 in three regions AND >= 2 in the primary region. Neither the shared test
    // subscription (quota 1 in a single region) nor a personal subscription (quota only in one region) satisfies
    // this. Re-enable once such quota is available.
    @Test
    @Disabled("Needs App Service 'Total VMs' quota across 3 regions (>=2 in the primary); test subscriptions lack it.")
    public void testScaleWebAppWithTrafficManager() {
        Assertions.assertTrue(ScaleWebAppWithTrafficManager.runSample(azureResourceManager));
    }

    // Recorded on a personal subscription (Japan East) because the shared test subscription's policy blocks
    // storage-account shared-key access and its US-region App Service "Total VMs" quota is 0.
    @Test
    public void testConnectWebAppToStorageAccount() {
        Assertions.assertTrue(ConnectWebAppToStorageAccount.runSample(azureResourceManager));
    }

    // Recorded on a personal subscription (Japan East) because the sample's AcrPull grant requires
    // Microsoft.Authorization/roleAssignments/write and the shared subscription's App Service quota is unavailable.
    @Test
    public void testDeployImageFromAcrToLinuxWebApp() {
        Assertions.assertTrue(DeployImageFromAcrToLinuxWebApp.runSample(azureResourceManager));
    }
}
