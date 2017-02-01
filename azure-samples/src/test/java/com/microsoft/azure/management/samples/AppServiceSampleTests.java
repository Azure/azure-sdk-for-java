/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.appservice.samples.ManageWebAppBasic;
import com.microsoft.azure.management.appservice.samples.ManageWebAppSlots;
import com.microsoft.azure.management.appservice.samples.ManageWebAppSourceControl;
import com.microsoft.azure.management.appservice.samples.ManageWebAppSqlConnection;
import com.microsoft.azure.management.appservice.samples.ManageWebAppStorageAccountConnection;
import com.microsoft.azure.management.appservice.samples.ManageWebAppWithDomainSsl;
import com.microsoft.azure.management.appservice.samples.ManageWebAppWithTrafficManager;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class AppServiceSampleTests extends SamplesTestBase {
    @Test
    public void testManageWebAppBasic() {
        Assert.assertTrue(ManageWebAppBasic.runSample(azure));
    }

    @Test
    public void testManageWebAppSlots() {
        Assert.assertTrue(ManageWebAppSlots.runSample(azure));
    }

    @Test
    @Ignore("Failing in playback - dependent on git")
    public void testManageWebAppSourceControl() {
        Assert.assertTrue(ManageWebAppSourceControl.runSample(azure));
    }

    @Test
    @Ignore("Stops in between for user input")
    public void testManageWebAppSqlConnection() {
        Assert.assertTrue(ManageWebAppSqlConnection.runSample(azure));
    }

    @Test
    @Ignore("Failing in Playback - Using storage data plan APIs")
    public void testManageWebAppStorageAccountConnection() {
        Assert.assertTrue(ManageWebAppStorageAccountConnection.runSample(azure));
    }

    @Test
    @Ignore("The subscription is not registered to use namespace 'Microsoft.DomainRegistration'")
    public void testManageWebAppWithDomainSsl() {
        Assert.assertTrue(ManageWebAppWithDomainSsl.runSample(azure));
    }

    @Test
    @Ignore("Failing - needs a subscription with Microsoft.DomainRegistration permissions")
    public void testManageWebAppWithTrafficManager() {
        Assert.assertTrue(ManageWebAppWithTrafficManager.runSample(azure));
    }
}
