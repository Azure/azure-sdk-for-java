/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.samples;

import com.azure.management.appservice.samples.ManageFunctionAppBasic;
import com.azure.management.appservice.samples.ManageFunctionAppWithDomainSsl;
import com.azure.management.appservice.samples.ManageLinuxWebAppBasic;
import com.azure.management.appservice.samples.ManageLinuxWebAppSqlConnection;
import com.azure.management.appservice.samples.ManageLinuxWebAppWithDomainSsl;
import com.azure.management.appservice.samples.ManageLinuxWebAppWithTrafficManager;
import com.azure.management.appservice.samples.ManageWebAppBasic;
import com.azure.management.appservice.samples.ManageWebAppSlots;
import com.azure.management.appservice.samples.ManageWebAppSourceControlAsync;
import com.azure.management.appservice.samples.ManageWebAppSqlConnection;
import com.azure.management.appservice.samples.ManageWebAppWithDomainSsl;
import com.azure.management.appservice.samples.ManageWebAppWithTrafficManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class AppServiceSampleTests extends SamplesTestBase {
    @Test
    public void testManageWebAppBasic() {
        Assertions.assertTrue(ManageWebAppBasic.runSample(azure));
    }

    @Test
    @Disabled("Fails randomly when creating one of the three slots")
    public void testManageWebAppSlots() {
        Assertions.assertTrue(ManageWebAppSlots.runSample(azure));
    }

    @Test
    @Disabled("Failing in playback - dependent on git")
    public void testManageWebAppSourceControlAsync() {
        Assertions.assertTrue(ManageWebAppSourceControlAsync.runSample(azure));
    }

    @Test
    @Disabled("Stops in between for user input")
    public void testManageWebAppSqlConnection() {
        Assertions.assertTrue(ManageWebAppSqlConnection.runSample(azure));
    }

    @Test
    public void testManageWebAppWithDomainSsl() {
        Assertions.assertTrue(ManageWebAppWithDomainSsl.runSample(azure));
    }

    @Test
    public void testManageWebAppWithTrafficManager() {
        Assertions.assertTrue(ManageWebAppWithTrafficManager.runSample(azure));
    }

    @Test
    public void testManageLinuxWebAppBasic() {
        Assertions.assertTrue(ManageLinuxWebAppBasic.runSample(azure));
    }

    @Test
    @Disabled("Stops in between for user input")
    public void testManageLinuxWebAppSqlConnection() {
        Assertions.assertTrue(ManageLinuxWebAppSqlConnection.runSample(azure));
    }

    @Test
    public void testManageLinuxWebAppWithDomainSsl() {
        Assertions.assertTrue(ManageLinuxWebAppWithDomainSsl.runSample(azure));
    }

    @Test
    public void testManageLinuxWebAppWithTrafficManager() {
        Assertions.assertTrue(ManageLinuxWebAppWithTrafficManager.runSample(azure));
    }

    @Test
    public void testManageFunctionAppBasic() {
        Assertions.assertTrue(ManageFunctionAppBasic.runSample(azure));
    }

    @Test
    public void testManageFunctionAppWithDomainSsl() {
        Assertions.assertTrue(ManageFunctionAppWithDomainSsl.runSample(azure));
    }
}
