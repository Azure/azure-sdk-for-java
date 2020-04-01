/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.samples;

import com.azure.management.appservice.samples.ManageFunctionAppLogs;
import com.azure.management.appservice.samples.ManageFunctionAppSourceControl;
import com.azure.management.appservice.samples.ManageFunctionAppWithAuthentication;
import com.azure.management.appservice.samples.ManageLinuxFunctionAppSourceControl;
import com.azure.management.appservice.samples.ManageLinuxWebAppCosmosDbByMsi;
import com.azure.management.appservice.samples.ManageLinuxWebAppSourceControl;
import com.azure.management.appservice.samples.ManageLinuxWebAppStorageAccountConnection;
import com.azure.management.appservice.samples.ManageLinuxWebAppWithContainerRegistry;
import com.azure.management.appservice.samples.ManageWebAppCosmosDbByMsi;
import com.azure.management.appservice.samples.ManageWebAppCosmosDbThroughKeyVault;
import com.azure.management.appservice.samples.ManageWebAppLogs;
import com.azure.management.appservice.samples.ManageWebAppSourceControl;
import com.azure.management.appservice.samples.ManageWebAppStorageAccountConnection;
import com.azure.management.resources.core.TestBase;
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
        Assertions.assertTrue(ManageWebAppCosmosDbByMsi.runSample(azure));
    }

    @Test
    public void testManageWebAppCosmosDbThroughKeyVault() {
        Assertions.assertTrue(ManageWebAppCosmosDbThroughKeyVault.runSample(azure));
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
}
