/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.appservice.samples.ManageWebAppSourceControl;
import com.microsoft.azure.management.appservice.samples.ManageWebAppStorageAccountConnection;
import com.microsoft.azure.management.resources.core.TestBase;

import org.junit.Assert;
import org.junit.Test;

public class AppServiceSampleTestsLiveOnly extends SamplesTestBase {
    public AppServiceSampleTestsLiveOnly() {
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
}
