// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Context;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PrivateEndpoint;
import com.azure.resourcemanager.network.models.PrivateLinkSubResourceName;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.storage.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.storage.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.storage.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PrivateLinkTests extends ResourceManagerTestBase {

    private AzureResourceManager azureResourceManager;
    private String rgName;

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
        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        ResourceManagerUtils.InternalRuntimeContext internalContext = new ResourceManagerUtils.InternalRuntimeContext();
        internalContext.setIdentifierFunction(name -> new TestIdentifierProvider(testResourceNamer));
        azureResourceManager = buildManager(AzureResourceManager.class, httpPipeline, profile);
        setInternalContext(internalContext, azureResourceManager);

        rgName = generateRandomResourceName("javacsmrg", 15);
    }

    @Override
    protected void cleanUpResources() {
        try {
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
        } catch (Exception e) {
        }
    }

    @Test
    public void testPrivateEndpoint() {
        String saName = generateRandomResourceName("sa", 10);
        String vnName = generateRandomResourceName("vn", 10);
        String subnetName = "default";
        String peName = generateRandomResourceName("pe", 10);
        String peName2 = generateRandomResourceName("pe", 10);
        String pecName = generateRandomResourceName("pec", 10);
        Region region = Region.US_EAST;

        StorageAccount storageAccount = azureResourceManager.storageAccounts().define(saName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .create();

        Network network = azureResourceManager.networks().define(vnName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/28")
            .defineSubnet("default")
                .withAddressPrefix("10.0.0.0/28")
                .disableNetworkPoliciesOnPrivateEndpoint()
                .attach()
            .create();

        // private endpoint with manual approval
        PrivateEndpoint privateEndpoint = azureResourceManager.privateEndpoints().define(peName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withSubnet(network.subnets().get(subnetName))
            .definePrivateLinkServiceConnection(pecName)
                .withResource(storageAccount)
                .withSubResource(PrivateLinkSubResourceName.STORAGE_BLOB)
                .withManualApproval("request message")
                .attach()
            .create();

        Assertions.assertNotNull(privateEndpoint.subnet());
        Assertions.assertEquals(network.subnets().get(subnetName).innerModel().id(), privateEndpoint.subnet().id());
        Assertions.assertEquals(1, privateEndpoint.networkInterfaces().size());
        Assertions.assertEquals(1, privateEndpoint.privateLinkServiceConnections().size());
        Assertions.assertTrue(privateEndpoint.privateLinkServiceConnections().values().iterator().next().isManualApproval());
        Assertions.assertEquals("Pending", privateEndpoint.privateLinkServiceConnections().get(pecName).state().status());
        Assertions.assertEquals("request message", privateEndpoint.privateLinkServiceConnections().get(pecName).requestMessage());

        // approve the connection
        List<PrivateEndpointConnectionInner> storageAccountConnections = azureResourceManager.storageAccounts().manager().serviceClient().getPrivateEndpointConnections().list(rgName, saName).stream().collect(Collectors.toList());
        Assertions.assertEquals(1, storageAccountConnections.size());
        PrivateEndpointConnectionInner storageAccountConnection = storageAccountConnections.iterator().next();
        Response<PrivateEndpointConnectionInner> approvalResponse = azureResourceManager.storageAccounts().manager().serviceClient().getPrivateEndpointConnections()
            .putWithResponse(rgName, saName, storageAccountConnection.name(),
                storageAccountConnection.privateEndpoint(),
                storageAccountConnection.privateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.APPROVED),
                Context.NONE);
        Assertions.assertEquals(200, approvalResponse.getStatusCode());

        // check again
        privateEndpoint.refresh();
        Assertions.assertEquals("Approved", privateEndpoint.privateLinkServiceConnections().get(pecName).state().status());

        azureResourceManager.privateEndpoints().deleteById(privateEndpoint.id());

        // private endpoint with auto-approval (RBAC based)
        privateEndpoint = azureResourceManager.privateEndpoints().define(peName2)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withSubnet(network.subnets().get(subnetName).innerModel().id())
            .definePrivateLinkServiceConnection(pecName)
            .withResource(storageAccount.id())
            .withSubResource(PrivateLinkSubResourceName.STORAGE_BLOB)
            .attach()
            .create();

        Assertions.assertNotNull(privateEndpoint.subnet());
        Assertions.assertEquals(network.subnets().get(subnetName).innerModel().id(), privateEndpoint.subnet().id());
        Assertions.assertEquals(1, privateEndpoint.networkInterfaces().size());
        Assertions.assertEquals(1, privateEndpoint.privateLinkServiceConnections().size());
        Assertions.assertEquals(storageAccount.id(), privateEndpoint.privateLinkServiceConnections().get(pecName).privateLinkResourceId());
        Assertions.assertEquals(Collections.singletonList(PrivateLinkSubResourceName.STORAGE_BLOB), privateEndpoint.privateLinkServiceConnections().get(pecName).subResourceNames());
        // auto-approved
        Assertions.assertFalse(privateEndpoint.privateLinkServiceConnections().values().iterator().next().isManualApproval());
        Assertions.assertEquals("Approved", privateEndpoint.privateLinkServiceConnections().get(pecName).state().status());

        List<PrivateEndpoint> privateEndpoints = azureResourceManager.privateEndpoints().listByResourceGroup(rgName).stream().collect(Collectors.toList());
        Assertions.assertEquals(1, privateEndpoints.size());
        Assertions.assertEquals(peName2, privateEndpoints.get(0).name());
    }
}
