// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlElasticPool;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.test.ResourceManagerTestProxyTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;

import java.time.temporal.ChronoUnit;
import java.util.List;

/** The base for Monitor manager tests. */
public class MonitorManagementTest extends ResourceManagerTestProxyTestBase {
    protected ResourceManager resourceManager;
    protected MonitorManager monitorManager;
    protected ComputeManager computeManager;
    protected StorageManager storageManager;
    protected EventHubsManager eventHubManager;
    protected AppServiceManager appServiceManager;
    protected KeyVaultManager keyVaultManager;
    protected SqlServerManager sqlServerManager;

    @Override
    protected HttpPipeline buildHttpPipeline(TokenCredential credential, AzureProfile profile,
        HttpLogOptions httpLogOptions, List<HttpPipelinePolicy> policies, HttpClient httpClient) {
        return HttpPipelineProvider.buildHttpPipeline(credential, profile, null, httpLogOptions, null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS), policies, httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        ResourceManagerUtils.InternalRuntimeContext internalContext = new ResourceManagerUtils.InternalRuntimeContext();
        internalContext.setIdentifierFunction(name -> new TestIdentifierProvider(testResourceNamer));
        appServiceManager = buildManager(AppServiceManager.class, httpPipeline, profile);
        monitorManager = buildManager(MonitorManager.class, httpPipeline, profile);
        computeManager = buildManager(ComputeManager.class, httpPipeline, profile);
        storageManager = buildManager(StorageManager.class, httpPipeline, profile);
        eventHubManager = buildManager(EventHubsManager.class, httpPipeline, profile);
        resourceManager = monitorManager.resourceManager();
        keyVaultManager = buildManager(KeyVaultManager.class, httpPipeline, profile);
        sqlServerManager = buildManager(SqlServerManager.class, httpPipeline, profile);
        setInternalContext(internalContext, computeManager);
    }

    @Override
    protected void cleanUpResources() {
    }

    protected VirtualMachine ensureVM(Region region, ResourceGroup resourceGroup, String vmName, String addressSpace) {
        return computeManager.virtualMachines()
            .define(vmName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withNewPrimaryNetwork(addressSpace)
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("Foo12")
            .withSsh(sshPublicKey())
            .create();
    }

    protected Vault ensureVault(Region region, String rgName) {
        return keyVaultManager.vaults()
            .define(generateRandomResourceName("jmonitorvt", 18))
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withEmptyAccessPolicy()
            .create();
    }

    protected SqlElasticPool ensureElasticPoolWithWhiteSpace(Region region, String rgName) {
        String sqlServerName = generateRandomResourceName("JMonitorSql-", 18);

        SqlServer sqlServer = sqlServerManager.sqlServers()
            .define(sqlServerName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withAdministratorLogin("admin123")
            .withAdministratorPassword(password())
            .create();

        // white space in pool name
        SqlElasticPool pool = sqlServer.elasticPools().define("name with space").withBasicPool().create();
        return pool;
    }
}
