// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.RuntimeStack;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.compute.models.InstanceViewStatus;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.RunCommandResult;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccount;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PrivateDnsZoneGroup;
import com.azure.resourcemanager.network.models.PrivateEndpoint;
import com.azure.resourcemanager.network.models.PrivateLinkSubResourceName;
import com.azure.resourcemanager.privatedns.models.PrivateDnsZone;
import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointConnection;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListingPrivateEndpointConnection;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListingPrivateLinkResource;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsUpdatingPrivateEndpointConnection;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
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
        List<PrivateEndpointConnection> storageAccountConnections = storageAccount.listPrivateEndpointConnections().stream().collect(Collectors.toList());
        Assertions.assertEquals(1, storageAccountConnections.size());
        PrivateEndpointConnection storageAccountConnection = storageAccountConnections.iterator().next();
        Assertions.assertNotNull(storageAccountConnection.id());
        Assertions.assertNotNull(storageAccountConnection.privateEndpoint());
        Assertions.assertNotNull(storageAccountConnection.privateEndpoint().id());
        Assertions.assertNotNull(storageAccountConnection.privateLinkServiceConnectionState());
        Assertions.assertEquals(PrivateEndpointServiceConnectionStatus.PENDING, storageAccountConnection.privateLinkServiceConnectionState().status());
        storageAccount.approvePrivateEndpointConnection(storageAccountConnection.name());

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
        String pdzcName = generateRandomResourceName("pdzcName", 20);
        String pdzcName2 = generateRandomResourceName("pdzcName", 20);

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

    @Test
    public void testStoragePrivateLinkResources() {
        StorageAccount storageAccount = azureResourceManager.storageAccounts().define(saName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .create();

        validatePrivateLinkResource(storageAccount, PrivateLinkSubResourceName.STORAGE_BLOB.toString());
    }

    @Test
    public void testPrivateEndpointVault() {
        String vaultName = generateRandomResourceName("vault", 10);
        PrivateLinkSubResourceName subResourceName = PrivateLinkSubResourceName.VAULT;

        Vault vault = azureResourceManager.vaults().define(vaultName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withEmptyAccessPolicy()
            .create();

        validatePrivateLinkResource(vault, subResourceName.toString());

        validateApprovePrivatePrivateEndpointConnection(vault, subResourceName);
    }

    @Test
    public void testPrivateEndpointCosmos() {
        String cosmosName = generateRandomResourceName("cosmos", 10);
        PrivateLinkSubResourceName subResourceName = PrivateLinkSubResourceName.COSMOS_SQL;

        CosmosDBAccount cosmosDBAccount = azureResourceManager.cosmosDBAccounts().define(cosmosName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withDataModelSql()
            .withStrongConsistency()
            .create();

        PrivateEndpoint privateEndpoint = createPrivateEndpointForManualApproval(cosmosDBAccount, subResourceName);

        com.azure.resourcemanager.cosmos.models.PrivateEndpointConnection connection = cosmosDBAccount.listPrivateEndpointConnection().values().iterator().next();
        cosmosDBAccount.approvePrivateEndpointConnection(connection.name());

        privateEndpoint.refresh();
        Assertions.assertEquals("Approved", privateEndpoint.privateLinkServiceConnections().get(pecName).state().status());
    }

    @Test
    public void testPrivateEndpointAKS() {
        String clusterName = generateRandomResourceName("aks", 8);
        String apName = "ap" + clusterName;
        String dnsPrefix = "dns" + clusterName;

        String clientId = "clientId";
        String clientSecret = "secret";
        String envSecondaryServicePrincipal = System.getenv("AZURE_AUTH_LOCATION_2");
        if (envSecondaryServicePrincipal == null
            || envSecondaryServicePrincipal.isEmpty()
            || !(new File(envSecondaryServicePrincipal).exists())) {
            envSecondaryServicePrincipal = System.getenv("AZURE_AUTH_LOCATION");
        }
        try {
            HashMap<String, String> credentialsMap = parseAuthFile(envSecondaryServicePrincipal);
            clientId = credentialsMap.get("clientId");
            clientSecret = credentialsMap.get("clientSecret");
        } catch (Exception e) {
        }

        PrivateLinkSubResourceName subResourceName = PrivateLinkSubResourceName.KUBERNETES_MANAGEMENT;

        KubernetesCluster cluster = azureResourceManager.kubernetesClusters().define(clusterName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withDefaultVersion()
            .withRootUsername("aksadmin")
            .withSshKey(sshPublicKey())
            .withServicePrincipalClientId(clientId)
            .withServicePrincipalSecret(clientSecret)
            .defineAgentPool(apName)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2)
                .withAgentPoolVirtualMachineCount(1)
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
            .attach()
            .withDnsPrefix(dnsPrefix)
            .enablePrivateCluster()
            .create();

        validatePrivateLinkResource(cluster, subResourceName.toString());

        // private dns zone and private endpoint connection is created by AKS

        List<PrivateEndpointConnection> connections = cluster.listPrivateEndpointConnections().stream().collect(Collectors.toList());
        Assertions.assertEquals(1, connections.size());
        PrivateEndpointConnection connection = connections.iterator().next();
        Assertions.assertEquals(PrivateEndpointServiceConnectionStatus.APPROVED, connection.privateLinkServiceConnectionState().status());
    }

    @Test
    @Disabled("invalid response of list private endpoint connections")
    public void testPrivateEndpointRedis() {
        String redisName = generateRandomResourceName("redis", 10);
        PrivateLinkSubResourceName subResourceName = PrivateLinkSubResourceName.REDIS_CACHE;

        RedisCache redisCache = azureResourceManager.redisCaches().define(redisName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withPremiumSku()
            .create();

        validatePrivateLinkResource(redisCache, subResourceName.toString());

        validateListAndApprovePrivatePrivateEndpointConnection(redisCache, subResourceName);
    }

    @Test
    @Disabled("invalid response of WebAppsClient.getPrivateEndpointConnectionListAsync")
    public void testPrivateEndpointWeb() {
        String webappName = generateRandomResourceName("webapp", 20);

        PrivateLinkSubResourceName subResourceName = PrivateLinkSubResourceName.WEB_SITES;

        WebApp webapp = azureResourceManager.webApps().define(webappName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withNewLinuxPlan(PricingTier.PREMIUM_P2V3) // requires P2 or P3
            .withBuiltInImage(RuntimeStack.JAVA_11_JAVA11)
            .create();

        validatePrivateLinkResource(webapp, subResourceName.toString());

        validateListAndApprovePrivatePrivateEndpointConnection(webapp, subResourceName);
    }

//    @Test
//    public void testPrivateEndpointWebSlot() {
//        String webappName = generateRandomResourceName("webapp", 20);
//        String webappSlotName = generateRandomResourceName("webappslot", 20);
//
//        PrivateLinkSubResourceName subResourceName = PrivateLinkSubResourceName.WEB_SITES;
//
//        WebApp webapp = azureResourceManager.webApps().define(webappName)
//            .withRegion(region)
//            .withNewResourceGroup(rgName)
//            .withNewLinuxPlan(PricingTier.PREMIUM_P2V3) // requires P2 or P3
//            .withBuiltInImage(RuntimeStack.JAVA_11_JAVA11)
//            .create();
//
//        DeploymentSlot slot = webapp.deploymentSlots().define(webappSlotName)
//            .withConfigurationFromParent()
//            .create();
//
//        validatePrivateLinkResource(slot, subResourceName.toString());
//
//        validateListAndApprovePrivatePrivateEndpointConnection(slot, subResourceName);
//    }

    private void validatePrivateLinkResource(SupportsListingPrivateLinkResource resource, String requiredGroupId) {
        PagedIterable<PrivateLinkResource> privateLinkResources = resource.listPrivateLinkResources();
        List<PrivateLinkResource> privateLinkResourceList = privateLinkResources.stream().collect(Collectors.toList());

        Assertions.assertFalse(privateLinkResourceList.isEmpty());
        Assertions.assertTrue(privateLinkResourceList.stream().anyMatch(r -> requiredGroupId.equals(r.groupId())));
    }

    private PrivateEndpoint createPrivateEndpointForManualApproval(Resource resource, PrivateLinkSubResourceName subResourceName) {
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
            .withResource(resource)
            .withSubResource(subResourceName)
            .withManualApproval("request message")
            .attach()
            .create();
        Assertions.assertEquals("Pending", privateEndpoint.privateLinkServiceConnections().get(pecName).state().status());

        return privateEndpoint;
    }

    private <T extends Resource & SupportsUpdatingPrivateEndpointConnection> void validateApprovePrivatePrivateEndpointConnection(T resource, PrivateLinkSubResourceName subResourceName) {
        PrivateEndpoint privateEndpoint = createPrivateEndpointForManualApproval(resource, subResourceName);

        resource.approvePrivateEndpointConnection(pecName);

        // check again
        privateEndpoint.refresh();
        Assertions.assertEquals("Approved", privateEndpoint.privateLinkServiceConnections().get(pecName).state().status());
    }

    private <T extends Resource & SupportsUpdatingPrivateEndpointConnection & SupportsListingPrivateEndpointConnection> void validateListAndApprovePrivatePrivateEndpointConnection(T resource, PrivateLinkSubResourceName subResourceName) {
        PrivateEndpoint privateEndpoint = createPrivateEndpointForManualApproval(resource, subResourceName);

        List<PrivateEndpointConnection> connections = resource.listPrivateEndpointConnections().stream().collect(Collectors.toList());
        Assertions.assertEquals(1, connections.size());
        PrivateEndpointConnection connection = connections.iterator().next();

        resource.approvePrivateEndpointConnection(connection.name());

        // check again
        privateEndpoint.refresh();
        Assertions.assertEquals("Approved", privateEndpoint.privateLinkServiceConnections().get(pecName).state().status());
    }

    private static HashMap<String, String> parseAuthFile(String authFilename) throws Exception {
        String content = new String(Files.readAllBytes(new File(authFilename).toPath()), StandardCharsets.UTF_8).trim();
        HashMap<String, String> auth = new HashMap<>();
        auth = new JacksonAdapter().deserialize(content, auth.getClass(), SerializerEncoding.JSON);
        return auth;
    }
}
