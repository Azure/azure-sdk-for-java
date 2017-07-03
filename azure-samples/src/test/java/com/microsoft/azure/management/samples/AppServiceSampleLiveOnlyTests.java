/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.appservice.samples.ManageLinuxWebAppSourceControl;
import com.microsoft.azure.management.appservice.samples.ManageLinuxWebAppStorageAccountConnection;
import com.microsoft.azure.management.appservice.samples.ManageLinuxWebAppWithContainerRegistry;
import com.microsoft.azure.management.appservice.samples.ManageWebAppSourceControl;
import com.microsoft.azure.management.appservice.samples.ManageWebAppStorageAccountConnection;
import com.microsoft.azure.management.resources.core.TestBase;

import org.junit.Assert;
import org.junit.Test;

public class AppServiceSampleLiveOnlyTests extends SamplesTestBase {
    public AppServiceSampleLiveOnlyTests() {
        super(TestBase.RunCondition.LIVE_ONLY);
    }

    @Test
    public void testManageWebAppSourceControl() {
        Assert.assertTrue(ManageWebAppSourceControl.runSample(azure));
    }

    @Test
    public void testManageWebAppStorageAccountConnection() {
        Assert.assertTrue(ManageWebAppStorageAccountConnection.runSample(azure));
    }

    @Test
    public void testManageLinuxWebAppSourceControl() {
        Assert.assertTrue(ManageLinuxWebAppSourceControl.runSample(azure));
    }

    @Test
    public void testManageLinuxWebAppStorageAccountConnection() {
        Assert.assertTrue(ManageLinuxWebAppStorageAccountConnection.runSample(azure));
    }

    @Test
    public void testManageLinuxWebAppWithContainerRegistry() {
        Assert.assertTrue(ManageLinuxWebAppWithContainerRegistry.runSample(azure));
    }
}
