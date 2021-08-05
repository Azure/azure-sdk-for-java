// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.AzureResourceManager;
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
import java.time.Duration;

public class AppServiceSampleLiveOnlyTests extends SamplesTestBase {
    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageWebAppSourceControl() throws GitAPIException {
        Assertions.assertTrue(ManageWebAppSourceControl.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageWebAppStorageAccountConnection() {
        Assertions.assertTrue(ManageWebAppStorageAccountConnection.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageLinuxWebAppSourceControl() throws GitAPIException {
        Assertions.assertTrue(ManageLinuxWebAppSourceControl.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageLinuxWebAppStorageAccountConnection() {
        Assertions.assertTrue(ManageLinuxWebAppStorageAccountConnection.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageLinuxWebAppWithContainerRegistry() throws IOException, InterruptedException {
        Assertions.assertTrue(ManageLinuxWebAppWithContainerRegistry.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageFunctionAppWithAuthentication() throws GitAPIException {
        Assertions.assertTrue(ManageFunctionAppWithAuthentication.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageFunctionAppSourceControl() throws GitAPIException {
        Assertions.assertTrue(ManageFunctionAppSourceControl.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageLinuxWebAppCosmosDbByMsi() throws IOException, InterruptedException {
        Assertions.assertTrue(ManageLinuxWebAppCosmosDbByMsi.runSample(azureResourceManager, ""));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageWebAppCosmosDbByMsi() throws IOException {
        Assertions.assertTrue(ManageWebAppCosmosDbByMsi.runSample(azureResourceManager, clientIdFromFile()));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageWebAppCosmosDbThroughKeyVault() {
        Assertions.assertTrue(ManageWebAppCosmosDbThroughKeyVault.runSample(azureResourceManager, clientIdFromFile()));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageFunctionAppLogs() throws IOException {
        azureResourceManager = buildManager(
            AzureResourceManager.class,
            setReadTimeout(azureResourceManager.storageAccounts().manager().httpPipeline(), Duration.ofMinutes(10)),
            new AzureProfile(azureResourceManager.tenantId(), azureResourceManager.subscriptionId(), AzureEnvironment.AZURE)
        );
        Assertions.assertTrue(ManageFunctionAppLogs.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageWebAppLogs() throws IOException {
        if (skipInPlayback()) {
            return;
        }
        azureResourceManager = buildManager(
            AzureResourceManager.class,
            setReadTimeout(azureResourceManager.storageAccounts().manager().httpPipeline(), Duration.ofMinutes(10)),
            new AzureProfile(azureResourceManager.tenantId(), azureResourceManager.subscriptionId(), AzureEnvironment.AZURE)
        );
        Assertions.assertTrue(ManageWebAppLogs.runSample(azureResourceManager));
    }

    private HttpPipeline setReadTimeout(HttpPipeline httpPipeline, Duration timeout) {
        HttpPipelineBuilder builder = new HttpPipelineBuilder();
        for (int i = 0; i < httpPipeline.getPolicyCount(); ++i) {
            builder.policies(httpPipeline.getPolicy(i));
        }
        builder.httpClient(
            super.generateHttpClientWithProxy(
                new NettyAsyncHttpClientBuilder()
                    .readTimeout(timeout),
                null
            )
        );
        return builder.build();
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageLinuxFunctionAppSourceControl() {
        Assertions.assertTrue(ManageLinuxFunctionAppSourceControl.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageWebAppWithDomainSsl() throws IOException {
        Assertions.assertTrue(ManageWebAppWithDomainSsl.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageWebAppWithTrafficManager() throws IOException {
        Assertions.assertTrue(ManageWebAppWithTrafficManager.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageLinuxWebAppWithDomainSsl() throws IOException {
        Assertions.assertTrue(ManageLinuxWebAppWithDomainSsl.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageLinuxWebAppWithTrafficManager() throws IOException {
        Assertions.assertTrue(ManageLinuxWebAppWithTrafficManager.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testManageFunctionAppWithDomainSsl() throws IOException {
        Assertions.assertTrue(ManageFunctionAppWithDomainSsl.runSample(azureResourceManager));
    }
}
