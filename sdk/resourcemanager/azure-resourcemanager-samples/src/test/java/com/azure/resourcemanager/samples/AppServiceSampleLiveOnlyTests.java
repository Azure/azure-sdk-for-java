// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.core.test.annotation.DoNotRecord;
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
import com.azure.resourcemanager.appservice.samples.ManageLinuxWebAppWithTrafficManager;
import com.azure.resourcemanager.appservice.samples.ManageWebAppCosmosDbByMsi;
import com.azure.resourcemanager.appservice.samples.ManageWebAppCosmosDbThroughKeyVault;
import com.azure.resourcemanager.appservice.samples.ManageWebAppLogs;
import com.azure.resourcemanager.appservice.samples.ManageWebAppSourceControl;
import com.azure.resourcemanager.appservice.samples.ManageWebAppStorageAccountConnection;
import com.azure.resourcemanager.appservice.samples.ManageWebAppWithDomainSsl;
import com.azure.resourcemanager.appservice.samples.ManageWebAppWithTrafficManager;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AppServiceSampleLiveOnlyTests extends SamplesTestBase {

    @Test
    @DoNotRecord
    public void testManageWebAppSourceControl() throws GitAPIException {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageWebAppSourceControl.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testManageWebAppStorageAccountConnection() {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageWebAppStorageAccountConnection.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testManageLinuxWebAppSourceControl() throws GitAPIException {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageLinuxWebAppSourceControl.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testManageLinuxWebAppStorageAccountConnection() {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageLinuxWebAppStorageAccountConnection.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testManageLinuxWebAppWithContainerRegistry() throws IOException, InterruptedException {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageLinuxWebAppWithContainerRegistry.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testManageFunctionAppWithAuthentication() throws GitAPIException {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageFunctionAppWithAuthentication.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testManageFunctionAppSourceControl() throws GitAPIException {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageFunctionAppSourceControl.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testManageLinuxWebAppCosmosDbByMsi() throws IOException, InterruptedException {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageLinuxWebAppCosmosDbByMsi.runSample(azureResourceManager, ""));
    }

    @Test
    @DoNotRecord
    public void testManageWebAppCosmosDbByMsi() {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageWebAppCosmosDbByMsi.runSample(azureResourceManager, credentialFromFile(), clientIdFromFile()));
    }

    @Test
    @DoNotRecord
    public void testManageWebAppCosmosDbThroughKeyVault() {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageWebAppCosmosDbThroughKeyVault.runSample(azureResourceManager, clientIdFromFile()));
    }

    @Test
    @DoNotRecord
    public void testManageFunctionAppLogs() throws IOException {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageFunctionAppLogs.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testManageWebAppLogs() throws IOException {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageWebAppLogs.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testManageLinuxFunctionAppSourceControl() {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageLinuxFunctionAppSourceControl.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testManageWebAppWithDomainSsl() throws IOException {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageWebAppWithDomainSsl.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testManageWebAppWithTrafficManager() throws IOException {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageWebAppWithTrafficManager.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testManageLinuxWebAppWithDomainSsl() throws IOException {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageLinuxWebAppWithDomainSsl.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testManageLinuxWebAppWithTrafficManager() throws IOException {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageLinuxWebAppWithTrafficManager.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testManageFunctionAppWithDomainSsl() throws IOException {
        if (skipInPlayback()) {
            return;
        }

        Assertions.assertTrue(ManageFunctionAppWithDomainSsl.runSample(azureResourceManager));
    }
}
