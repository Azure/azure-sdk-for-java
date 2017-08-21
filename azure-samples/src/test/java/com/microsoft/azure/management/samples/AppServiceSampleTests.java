/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.appservice.samples.ManageFunctionAppBasic;
import com.microsoft.azure.management.appservice.samples.ManageFunctionAppWithDomainSsl;
import com.microsoft.azure.management.appservice.samples.ManageLinuxWebAppBasic;
import com.microsoft.azure.management.appservice.samples.ManageLinuxWebAppSqlConnection;
import com.microsoft.azure.management.appservice.samples.ManageLinuxWebAppWithDomainSsl;
import com.microsoft.azure.management.appservice.samples.ManageLinuxWebAppWithTrafficManager;
import com.microsoft.azure.management.appservice.samples.ManageWebAppBasic;
import com.microsoft.azure.management.appservice.samples.ManageWebAppSlots;
import com.microsoft.azure.management.appservice.samples.ManageWebAppSourceControlAsync;
import com.microsoft.azure.management.appservice.samples.ManageWebAppSqlConnection;
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
    public void testManageWebAppSourceControlAsync() {
        Assert.assertTrue(ManageWebAppSourceControlAsync.runSample(azure));
    }

    @Test
    @Ignore("Stops in between for user input")
    public void testManageWebAppSqlConnection() {
        Assert.assertTrue(ManageWebAppSqlConnection.runSample(azure));
    }

    @Test
    public void testManageWebAppWithDomainSsl() {
        Assert.assertTrue(ManageWebAppWithDomainSsl.runSample(azure));
    }

    @Test
    public void testManageWebAppWithTrafficManager() {
        Assert.assertTrue(ManageWebAppWithTrafficManager.runSample(azure));
    }

    @Test
    public void testManageLinuxWebAppBasic() {
        Assert.assertTrue(ManageLinuxWebAppBasic.runSample(azure));
    }

    @Test
    @Ignore("Stops in between for user input")
    public void testManageLinuxWebAppSqlConnection() {
        Assert.assertTrue(ManageLinuxWebAppSqlConnection.runSample(azure));
    }

    @Test
    public void testManageLinuxWebAppWithDomainSsl() {
        Assert.assertTrue(ManageLinuxWebAppWithDomainSsl.runSample(azure));
    }

    @Test
    public void testManageLinuxWebAppWithTrafficManager() {
        Assert.assertTrue(ManageLinuxWebAppWithTrafficManager.runSample(azure));
    }

    @Test
    public void testManageFunctionAppBasic() {
        Assert.assertTrue(ManageFunctionAppBasic.runSample(azure));
    }

    @Test
    public void testManageFunctionAppWithDomainSsl() {
        Assert.assertTrue(ManageFunctionAppWithDomainSsl.runSample(azure));
    }
}
