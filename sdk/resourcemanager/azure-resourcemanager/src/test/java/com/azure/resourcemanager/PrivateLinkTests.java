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
import com.azure.resourcemanager.compute.models.InstanceViewStatus;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.RunCommandResult;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PrivateDnsZoneGroup;
import com.azure.resourcemanager.network.models.PrivateEndpoint;
import com.azure.resourcemanager.network.models.PrivateLinkSubResourceName;
import com.azure.resourcemanager.privatedns.models.PrivateDnsZone;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.storage.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.storage.models.PrivateEndpointServiceConnectionStatus;
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
    private String saName;
    private String peName;
    private String vnName;
    private String subnetName;
    private String pecName;

    private final Region region = Region.US_EAST;
    private final String vnAddressSpace = "10.0.0.0/28";

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
        saName = generateRandomResourceName("sa", 10);
        vnName = generateRandomResourceName("vn", 10);
        subnetName = "default";
        peName = generateRandomResourceName("pe", 10);
        pecName = generateRandomResourceName("pec", 10);
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
        String saName2 = generateRandomResourceName("sa", 10);
        String peName2 = generateRandomResourceName("pe", 10);
        String pecName2 = generateRandomResourceName("pec", 10);

        String saDomainName = saName + ".blob.core.windows.net";
        System.out.println("storage account domain name: " + saDomainName);

        StorageAccount storageAccount = azureResourceManager.storageAccounts().define(saName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .create();

        Network network = azureResourceManager.networks().define(vnName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace(vnAddressSpace)
            .defineSubnet(subnetName)
                .withAddressPrefix(vnAddressSpace)
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
        Assertions.assertEquals(network.subnets().get(subnetName).id(), privateEndpoint.subnet().id());
        Assertions.assertEquals(1, privateEndpoint.networkInterfaces().size());
        Assertions.assertEquals(1, privateEndpoint.privateLinkServiceConnections().size());
        Assertions.assertTrue(privateEndpoint.privateLinkServiceConnections().values().iterator().next().isManualApproval());
        Assertions.assertEquals(Collections.singletonList(PrivateLinkSubResourceName.STORAGE_BLOB), privateEndpoint.privateLinkServiceConnections().get(pecName).subResourceNames());
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

        // update private endpoint
        StorageAccount storageAccount2 = azureResourceManager.storageAccounts().define(saName2)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .create();

        privateEndpoint.update()
            .updatePrivateLinkServiceConnection(pecName)
                .withRequestMessage("request2")
                .parent()
            .apply();

        Assertions.assertEquals("Pending", privateEndpoint.privateLinkServiceConnections().get(pecName).state().status());
        Assertions.assertEquals("request2", privateEndpoint.privateLinkServiceConnections().get(pecName).requestMessage());

        privateEndpoint.update()
            .withoutPrivateLinkServiceConnection(pecName)
            .definePrivateLinkServiceConnection(pecName2)
                .withResource(storageAccount2)
                .withSubResource(PrivateLinkSubResourceName.STORAGE_FILE)
                .attach()
            .apply();

        Assertions.assertEquals(Collections.singletonList(PrivateLinkSubResourceName.STORAGE_FILE), privateEndpoint.privateLinkServiceConnections().get(pecName2).subResourceNames());
        Assertions.assertEquals("Approved", privateEndpoint.privateLinkServiceConnections().get(pecName2).state().status());

        // delete
        azureResourceManager.privateEndpoints().deleteById(privateEndpoint.id());

        // private endpoint with auto-approval (RBAC based)
        privateEndpoint = azureResourceManager.privateEndpoints().define(peName2)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withSubnetId(network.subnets().get(subnetName).id())
            .definePrivateLinkServiceConnection(pecName)
                .withResourceId(storageAccount.id())
                .withSubResource(PrivateLinkSubResourceName.STORAGE_BLOB)
                .attach()
            .create();

        Assertions.assertNotNull(privateEndpoint.subnet());
        Assertions.assertEquals(network.subnets().get(subnetName).id(), privateEndpoint.subnet().id());
        Assertions.assertEquals(1, privateEndpoint.networkInterfaces().size());
        Assertions.assertEquals(1, privateEndpoint.privateLinkServiceConnections().size());
        Assertions.assertEquals(storageAccount.id(), privateEndpoint.privateLinkServiceConnections().get(pecName).privateLinkResourceId());
        Assertions.assertEquals(Collections.singletonList(PrivateLinkSubResourceName.STORAGE_BLOB), privateEndpoint.privateLinkServiceConnections().get(pecName).subResourceNames());
        Assertions.assertNotNull(privateEndpoint.customDnsConfigurations());
        Assertions.assertFalse(privateEndpoint.customDnsConfigurations().isEmpty());
        // auto-approved
        Assertions.assertFalse(privateEndpoint.privateLinkServiceConnections().values().iterator().next().isManualApproval());
        Assertions.assertEquals("Approved", privateEndpoint.privateLinkServiceConnections().get(pecName).state().status());

        String saPrivateIp = privateEndpoint.customDnsConfigurations().get(0).ipAddresses().get(0);
        System.out.println("storage account private ip: " + saPrivateIp);

        // verify list
        List<PrivateEndpoint> privateEndpoints = azureResourceManager.privateEndpoints().listByResourceGroup(rgName).stream().collect(Collectors.toList());
        Assertions.assertEquals(1, privateEndpoints.size());
        Assertions.assertEquals(peName2, privateEndpoints.get(0).name());
    }

    @Test
    public void testPrivateEndpointE2E() {
        final boolean validateOnVirtualMachine = true;

        String vnlName = generateRandomResourceName("vnl", 10);
        String pdzgName = "default";
        String pdzcName = generateRandomResourceName("pdzcName", 10);
        String pdzcName2 = generateRandomResourceName("pdzcName", 10);

        String vmName = generateRandomResourceName("vm", 10);

        String saDomainName = saName + ".blob.core.windows.net";
        System.out.println("storage account domain name: " + saDomainName);

        StorageAccount storageAccount = azureResourceManager.storageAccounts().define(saName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .create();

        Network network = azureResourceManager.networks().define(vnName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace(vnAddressSpace)
            .defineSubnet(subnetName)
                .withAddressPrefix(vnAddressSpace)
                .disableNetworkPoliciesOnPrivateEndpoint()
                .attach()
            .create();

        // private endpoint
        PrivateEndpoint privateEndpoint = azureResourceManager.privateEndpoints().define(peName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withSubnetId(network.subnets().get(subnetName).id())
            .definePrivateLinkServiceConnection(pecName)
                .withResourceId(storageAccount.id())
                .withSubResource(PrivateLinkSubResourceName.STORAGE_BLOB)
                .attach()
            .create();

        Assertions.assertNotNull(privateEndpoint.subnet());
        Assertions.assertEquals(network.subnets().get(subnetName).id(), privateEndpoint.subnet().id());
        Assertions.assertEquals(1, privateEndpoint.networkInterfaces().size());
        Assertions.assertEquals(1, privateEndpoint.privateLinkServiceConnections().size());
        Assertions.assertEquals(storageAccount.id(), privateEndpoint.privateLinkServiceConnections().get(pecName).privateLinkResourceId());
        Assertions.assertEquals(Collections.singletonList(PrivateLinkSubResourceName.STORAGE_BLOB), privateEndpoint.privateLinkServiceConnections().get(pecName).subResourceNames());
        Assertions.assertNotNull(privateEndpoint.customDnsConfigurations());
        Assertions.assertFalse(privateEndpoint.customDnsConfigurations().isEmpty());
        // auto-approved
        Assertions.assertFalse(privateEndpoint.privateLinkServiceConnections().values().iterator().next().isManualApproval());
        Assertions.assertEquals("Approved", privateEndpoint.privateLinkServiceConnections().get(pecName).state().status());

        String saPrivateIp = privateEndpoint.customDnsConfigurations().get(0).ipAddresses().get(0);
        System.out.println("storage account private ip: " + saPrivateIp);

        VirtualMachine virtualMachine = null;
        if (validateOnVirtualMachine) {
            // create a test virtual machine
            virtualMachine = azureResourceManager.virtualMachines().define(vmName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withExistingPrimaryNetwork(network)
                .withSubnet(subnetName)
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
                .withRootUsername("testUser")
                .withSsh(sshPublicKey())
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2as_v4"))
                .create();

            // verify private endpoint not yet works
            RunCommandResult commandResult = virtualMachine.runShellScript(Collections.singletonList("nslookup " + saDomainName), null);
            for (InstanceViewStatus status : commandResult.value()) {
                System.out.println(status.message());
            }
            Assertions.assertFalse(commandResult.value().stream().anyMatch(status -> status.message().contains(saPrivateIp)));
        }

        // private dns zone
        PrivateDnsZone privateDnsZone = azureResourceManager.privateDnsZones().define("privatelink.blob.core.windows.net")
            .withExistingResourceGroup(rgName)
            .defineARecordSet(saName)
                .withIPv4Address(privateEndpoint.customDnsConfigurations().get(0).ipAddresses().get(0))
                .attach()
            .defineVirtualNetworkLink(vnlName)
                .withVirtualNetworkId(network.id())
                .attach()
            .create();

        // private dns zone group on private endpoint
        PrivateDnsZoneGroup privateDnsZoneGroup = privateEndpoint.privateDnsZoneGroups().define(pdzgName)
            .withPrivateDnsZoneConfigure(pdzcName, privateDnsZone.id())
            .create();

        if (validateOnVirtualMachine) {
            // verify private endpoint works
            RunCommandResult commandResult = virtualMachine.runShellScript(Collections.singletonList("nslookup " + saDomainName), null);
            for (InstanceViewStatus status : commandResult.value()) {
                System.out.println(status.message());
            }
            Assertions.assertTrue(commandResult.value().stream().anyMatch(status -> status.message().contains(saPrivateIp)));
        }

        // verify list and get for private dns zone group
        Assertions.assertEquals(1, privateEndpoint.privateDnsZoneGroups().list().stream().count());
        Assertions.assertEquals(pdzgName, privateEndpoint.privateDnsZoneGroups().getById(privateDnsZoneGroup.id()).name());

        // update private dns zone group
        PrivateDnsZone privateDnsZone2 = azureResourceManager.privateDnsZones().define("link.blob.core.windows.net")
            .withExistingResourceGroup(rgName)
            .create();

        privateDnsZoneGroup.update()
            .withoutPrivateDnsZoneConfigure(pdzcName)
            .withPrivateDnsZoneConfigure(pdzcName2, privateDnsZone2.id())
            .apply();

        // delete private dns zone group
        privateEndpoint.privateDnsZoneGroups().deleteById(privateDnsZoneGroup.id());
    }
}
