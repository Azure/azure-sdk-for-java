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

import java.io.IOException;

public class AppServiceSampleTests extends SamplesTestBase {
    @Test
    public void testManageWebAppBasic() {
        Assertions.assertTrue(ManageWebAppBasic.runSample(azureResourceManager));
    }

    @Test
    @Disabled("Fails randomly when creating one of the three slots")
    public void testManageWebAppSlots() {
        Assertions.assertTrue(ManageWebAppSlots.runSample(azureResourceManager));
    }

    @Test
    @Disabled("Failing in playback - dependent on git")
    public void testManageWebAppSourceControlAsync() {
        Assertions.assertTrue(ManageWebAppSourceControlAsync.runSample(azureResourceManager));
    }

    @Test
    @Disabled("Stops in between for user input")
    public void testManageWebAppSqlConnection() throws IOException {
        Assertions.assertTrue(ManageWebAppSqlConnection.runSample(azureResourceManager));
    }

    @Test
    public void testManageLinuxWebAppBasic() {
        Assertions.assertTrue(ManageLinuxWebAppBasic.runSample(azureResourceManager));
    }

    @Test
    @Disabled("Stops in between for user input")
    public void testManageLinuxWebAppSqlConnection() throws IOException {
        Assertions.assertTrue(ManageLinuxWebAppSqlConnection.runSample(azureResourceManager));
    }

    @Test
    public void testManageFunctionAppBasic() {
        Assertions.assertTrue(ManageFunctionAppBasic.runSample(azureResourceManager));
    }
}
