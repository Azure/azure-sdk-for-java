// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.cdn.CdnManager;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.containerinstance.ContainerInstanceManager;
import com.azure.resourcemanager.containerregistry.ContainerRegistryManager;
import com.azure.resourcemanager.containerservice.ContainerServiceManager;
import com.azure.resourcemanager.cosmos.CosmosManager;
import com.azure.resourcemanager.dns.DnsZoneManager;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.msi.MsiManager;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.privatedns.PrivateDnsZoneManager;
import com.azure.resourcemanager.redis.RedisManager;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.search.SearchServiceManager;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.trafficmanager.TrafficManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.List;

public class ManagerLiveTests extends ResourceManagerTestBase {

    private HttpPipeline httpPipeline;

    @Override
    protected HttpPipeline buildHttpPipeline(
        TokenCredential credential,
        AzureProfile profile,
        HttpLogOptions httpLogOptions,
        List<HttpPipelinePolicy> policies,
        HttpClient httpClient) {
        return HttpPipelineProvider.buildHttpPipeline(
            credential,
            profile,
            null,
            httpLogOptions,
            null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS),
            policies,
            httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        this.httpPipeline = httpPipeline;
    }

    @Override
    protected void cleanUpResources() {

    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testAuthentication() {
        AppPlatformManager.authenticate(httpPipeline, profile()).springServices().list().stream().count();
        AppServiceManager.authenticate(httpPipeline, profile()).appServicePlans().list().stream().count();
        AuthorizationManager.authenticate(httpPipeline, profile()).roleDefinitions().listByScope("/subscriptions/" + profile().getSubscriptionId()).stream().count();
        CdnManager.authenticate(httpPipeline, profile()).profiles().list().stream().count();
        ComputeManager.authenticate(httpPipeline, profile()).disks().list().stream().count();
        ContainerInstanceManager.authenticate(httpPipeline, profile()).containerGroups().list().stream().count();
        ContainerRegistryManager.authenticate(httpPipeline, profile()).containerRegistries().list().stream().count();
        ContainerServiceManager.authenticate(httpPipeline, profile()).kubernetesClusters().list().stream().count();
        CosmosManager.authenticate(httpPipeline, profile()).databaseAccounts().list().stream().count();
        DnsZoneManager.authenticate(httpPipeline, profile()).zones().list().stream().count();
        EventHubsManager.authenticate(httpPipeline, profile()).namespaces().list().stream().count();
        KeyVaultManager.authenticate(httpPipeline, profile()).vaults().listDeleted();
        //MonitorManager.authenticate(httpPipeline, profile()).metricDefinitions().listByResource();
        MsiManager.authenticate(httpPipeline, profile()).identities().list().stream().count();
        NetworkManager.authenticate(httpPipeline, profile()).networks().list().stream().count();
        PrivateDnsZoneManager.authenticate(httpPipeline, profile()).privateZones().list().stream().count();
        RedisManager.authenticate(httpPipeline, profile()).redisCaches().list().stream().count();
        ResourceManager.authenticate(httpPipeline, profile()).subscriptions().list().stream().count();
        ResourceManager.authenticate(httpPipeline, profile()).withDefaultSubscription().resourceGroups().list().stream().count();
        SearchServiceManager.authenticate(httpPipeline, profile()).searchServices().list().stream().count();
        ServiceBusManager.authenticate(httpPipeline, profile()).namespaces().list().stream().count();
        SqlServerManager.authenticate(httpPipeline, profile()).sqlServers().list().stream().count();
        StorageManager.authenticate(httpPipeline, profile()).storageAccounts().list().stream().count();
        TrafficManager.authenticate(httpPipeline, profile()).profiles().list().stream().count();

        Assertions.assertNotNull(AzureResourceManager.authenticate(httpPipeline, profile()).withDefaultSubscription()
            .genericResources().manager().httpPipeline());
    }
}
