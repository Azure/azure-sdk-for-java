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

    // Uses Traffic Manager with DNS, which cannot be recorded.
    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testScaleWebAppWithTrafficManager() {
        Assertions.assertTrue(ScaleWebAppWithTrafficManager.runSample(azureResourceManager));
    }

    // Configures a storage account connection, which makes calls that cannot be recorded.
    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testConnectWebAppToStorageAccount() {
        Assertions.assertTrue(ConnectWebAppToStorageAccount.runSample(azureResourceManager));
    }

    // Depends on an image in Azure Container Registry, which cannot be recorded.
    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testDeployImageFromAcrToLinuxWebApp() {
        Assertions.assertTrue(DeployImageFromAcrToLinuxWebApp.runSample(azureResourceManager));
    }
}
