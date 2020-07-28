// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.appservice.samples.ManageFunctionAppLogs;
import com.azure.resourcemanager.appservice.samples.ManageFunctionAppSourceControl;
import com.azure.resourcemanager.appservice.samples.ManageFunctionAppWithAuthentication;
import com.azure.resourcemanager.appservice.samples.ManageFunctionAppWithDomainSsl;
import com.azure.resourcemanager.appservice.samples.ManageLinuxFunctionAppSourceControl;
import com.azure.resourcemanager.appservice.samples.ManageLinuxWebAppCosmosDbByMsi;
import com.azure.resourcemanager.appservice.samples.ManageLinuxWebAppSourceControl;
import com.azure.resourcemanager.appservice.samples.ManageLinuxWebAppStorageAccountConnection;
import com.azure.resourcemanager.appservice.samples.ManageLinuxWebAppWithContainerRegistry;
import com.azure.resourcemanager.appservice.samples.ManageLinuxWebAppWithDomainSsl;
import com.azure.resourcemanager.appservice.samples.ManageWebAppCosmosDbByMsi;
import com.azure.resourcemanager.appservice.samples.ManageWebAppCosmosDbThroughKeyVault;
import com.azure.resourcemanager.appservice.samples.ManageWebAppLogs;
import com.azure.resourcemanager.appservice.samples.ManageWebAppSourceControl;
import com.azure.resourcemanager.appservice.samples.ManageWebAppStorageAccountConnection;
import com.azure.resourcemanager.appservice.samples.ManageWebAppWithDomainSsl;
import com.azure.resourcemanager.resources.core.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AppServiceSampleLiveOnlyTests extends SamplesTestBase {
    public AppServiceSampleLiveOnlyTests() {
        super(TestBase.RunCondition.LIVE_ONLY);
    }

    @Test
    public void testManageWebAppSourceControl() {
        Assertions.assertTrue(ManageWebAppSourceControl.runSample(azure));
    }

    @Test
    public void testManageWebAppStorageAccountConnection() {
        Assertions.assertTrue(ManageWebAppStorageAccountConnection.runSample(azure));
    }

    @Test
    public void testManageLinuxWebAppSourceControl() {
        Assertions.assertTrue(ManageLinuxWebAppSourceControl.runSample(azure));
    }

    @Test
    public void testManageLinuxWebAppStorageAccountConnection() {
        Assertions.assertTrue(ManageLinuxWebAppStorageAccountConnection.runSample(azure));
    }

    @Test
    public void testManageLinuxWebAppWithContainerRegistry() {
        Assertions.assertTrue(ManageLinuxWebAppWithContainerRegistry.runSample(azure));
    }

    @Test
    public void testManageFunctionAppWithAuthentication() {
        Assertions.assertTrue(ManageFunctionAppWithAuthentication.runSample(azure));
    }

    @Test
    public void testManageFunctionAppSourceControl() {
        Assertions.assertTrue(ManageFunctionAppSourceControl.runSample(azure));
    }

    @Test
    public void testManageLinuxWebAppCosmosDbByMsi() {
        Assertions.assertTrue(ManageLinuxWebAppCosmosDbByMsi.runSample(azure));
    }

    @Test
    public void testManageWebAppCosmosDbByMsi() {
        Assertions.assertTrue(ManageWebAppCosmosDbByMsi.runSample(azure, credentialFromFile(), clientIdFromFile()));
    }

    @Test
    public void testManageWebAppCosmosDbThroughKeyVault() {
        Assertions.assertTrue(ManageWebAppCosmosDbThroughKeyVault.runSample(azure, clientIdFromFile()));
    }

    @Test
    public void testManageFunctionAppLogs() {
        Assertions.assertTrue(ManageFunctionAppLogs.runSample(azure));
    }

    @Test
    public void testManageWebAppLogs() {
        Assertions.assertTrue(ManageWebAppLogs.runSample(azure));
    }

    @Test
    public void testManageLinuxFunctionAppSourceControl() {
        Assertions.assertTrue(ManageLinuxFunctionAppSourceControl.runSample(azure));
    }

    @Test
    public void testManageWebAppWithDomainSsl() {
        Assertions.assertTrue(ManageWebAppWithDomainSsl.runSample(azure));
    }

//    @Test
//    public void testManageWebAppWithTrafficManager() {
//        Assertions.assertTrue(ManageWebAppWithTrafficManager.runSample(azure));
//    }

    @Test
    public void testManageLinuxWebAppWithDomainSsl() {
        Assertions.assertTrue(ManageLinuxWebAppWithDomainSsl.runSample(azure));
    }

//    @Test
//    public void testManageLinuxWebAppWithTrafficManager() {
//        Assertions.assertTrue(ManageLinuxWebAppWithTrafficManager.runSample(azure));
//    }

    @Test
    public void testManageFunctionAppWithDomainSsl() {
        Assertions.assertTrue(ManageFunctionAppWithDomainSsl.runSample(azure));
    }
}
