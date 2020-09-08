// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.appservice.samples.ManageFunctionAppBasic;
import com.azure.resourcemanager.appservice.samples.ManageLinuxWebAppBasic;
import com.azure.resourcemanager.appservice.samples.ManageLinuxWebAppSqlConnection;
import com.azure.resourcemanager.appservice.samples.ManageWebAppBasic;
import com.azure.resourcemanager.appservice.samples.ManageWebAppSlots;
import com.azure.resourcemanager.appservice.samples.ManageWebAppSourceControlAsync;
import com.azure.resourcemanager.appservice.samples.ManageWebAppSqlConnection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class AppServiceSampleTests extends SamplesTestBase {
    @Test
    public void testManageWebAppBasic() throws Exception {
        Assertions.assertTrue(ManageWebAppBasic.runSample(azure));
    }

    @Test
    @Disabled("Fails randomly when creating one of the three slots")
    public void testManageWebAppSlots() throws Exception {
        Assertions.assertTrue(ManageWebAppSlots.runSample(azure));
    }

    @Test
    @Disabled("Failing in playback - dependent on git")
    public void testManageWebAppSourceControlAsync() throws Exception {
        Assertions.assertTrue(ManageWebAppSourceControlAsync.runSample(azure));
    }

    @Test
    @Disabled("Stops in between for user input")
    public void testManageWebAppSqlConnection() throws Exception {
        Assertions.assertTrue(ManageWebAppSqlConnection.runSample(azure));
    }

    @Test
    public void testManageLinuxWebAppBasic() throws Exception {
        Assertions.assertTrue(ManageLinuxWebAppBasic.runSample(azure));
    }

    @Test
    @Disabled("Stops in between for user input")
    public void testManageLinuxWebAppSqlConnection() throws Exception {
        Assertions.assertTrue(ManageLinuxWebAppSqlConnection.runSample(azure));
    }

    @Test
    public void testManageFunctionAppBasic() throws Exception {
        Assertions.assertTrue(ManageFunctionAppBasic.runSample(azure));
    }
}
