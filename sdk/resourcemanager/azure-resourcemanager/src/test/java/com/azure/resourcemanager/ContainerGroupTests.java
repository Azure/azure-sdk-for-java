// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.containerinstance.models.ContainerGroup;
import com.azure.resourcemanager.containerregistry.models.ImportImageParameters;
import com.azure.resourcemanager.containerregistry.models.ImportMode;
import com.azure.resourcemanager.containerregistry.models.ImportSource;
import com.azure.resourcemanager.containerregistry.models.Registry;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.test.ResourceManagerTestProxyTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ContainerGroupTests extends ResourceManagerTestProxyTestBase {

    private AzureResourceManager azureResourceManager;

    protected String rgName = "";
    protected final Region region = Region.US_WEST3;

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

    @Disabled("Admin in ACR is disallowed by policy, but it is required to deploy the image to ACI.")
    // https://learn.microsoft.com/azure/container-registry/container-registry-authentication?tabs=azure-cli#admin-account
    @Test
    public void testContainerGroupWithPrivateImageRegistryAndMsi() {
        // acr
        final String acrName = generateRandomResourceName("acr", 10);
        Registry registry = azureResourceManager.containerRegistries()
            .define(acrName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withBasicSku()
            .withRegistryNameAsAdminUser()
            .create();

        azureResourceManager.containerRegistries()
            .manager()
            .serviceClient()
            .getRegistries()
            .importImage(rgName, registry.name(),
                new ImportImageParameters()
                    .withSource(new ImportSource().withRegistryUri("docker.io").withSourceImage("library/nginx:latest"))
                    .withTargetTags(Arrays.asList("nginx:latest"))
                    .withMode(ImportMode.NO_FORCE));

        // msi
        final String msiName = generateRandomResourceName("msi", 10);
        Identity identity = azureResourceManager.identities()
            .define(msiName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .create();

        // rbac
        RoleAssignment roleAssignment = azureResourceManager.accessManagement()
            .roleAssignments()
            .define(UUID.randomUUID().toString())
            .forObjectId(identity.principalId())
            .withBuiltInRole(BuiltInRole.ACR_PULL)
            .withScope(registry.id())
            .create();

        // aci
        final String containerGroupName = generateRandomResourceName("acg", 10);
        ContainerGroup containerGroup = azureResourceManager.containerGroups()
            .define(containerGroupName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withLinux()
            .withPrivateImageRegistry(registry.loginServerUrl(), identity)
            .withoutVolume()
            .withContainerInstance("nginx", 80)
            .withNewVirtualNetwork("10.0.0.0/24")
            .withExistingUserAssignedManagedServiceIdentity(identity)
            .create();
    }
}
