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
import com.azure.core.test.annotation.LiveOnly;
import com.azure.resourcemanager.appservice.fluent.models.SiteConfigResourceInner;
import com.azure.resourcemanager.appservice.models.DeploymentSlot;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.SkuName;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.containerregistry.models.Registry;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.test.ResourceManagerTestProxyTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Live-only tests for pulling a private Azure Container Registry image with an app's (web app, function app or
 * deployment slot) managed identity, using {@code withManagedIdentityCredentials()} instead of registry username and
 * password.
 *
 * <p>These tests span multiple services (App Service, Container Registry, Authorization) and assign the {@code AcrPull}
 * role to a managed identity, which requires {@code Microsoft.Authorization/roleAssignments/write} permission. That
 * permission is not available on the shared test subscription, so the tests are {@code @LiveOnly} and are expected to be
 * run against a personal subscription where the caller is an owner/administrator.</p>
 */
public class WebAppAcrManagedIdentityTests extends ResourceManagerTestProxyTestBase {

    // App Service quota on personal subscriptions is commonly only available in Japan East.
    private static final Region REGION = Region.JAPAN_EAST;

    private AzureResourceManager azureResourceManager;
    private String rgName;

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
        azureResourceManager = AzureResourceManager.authenticate(httpPipeline, profile).withDefaultSubscription();
        setInternalContext(internalContext, azureResourceManager);
        rgName = generateRandomResourceName("javacsmrg", 20);
    }

    @Override
    protected void cleanUpResources() {
        if (rgName != null) {
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    @Test
    @LiveOnly // Requires Microsoft.Authorization/roleAssignments/write (AcrPull grant); not available on the shared
                   // test subscription, so this cannot run in RECORD/PLAYBACK. Run against a personal subscription.
    public void canPullPrivateAcrImageWithManagedIdentity() {
        final String acrName = generateRandomResourceName("acrmi", 20);
        final String webAppName = generateRandomResourceName("webacrmi", 20);

        // Create an Azure Container Registry with the admin user disabled (admin credentials are discouraged).
        Registry registry = azureResourceManager.containerRegistries()
            .define(acrName)
            .withRegion(REGION)
            .withNewResourceGroup(rgName)
            .withBasicSku()
            .create();

        // Create a Linux web app that pulls a private image using its system-assigned managed identity instead of a
        // registry username and password.
        WebApp webApp = azureResourceManager.webApps()
            .define(webAppName)
            .withRegion(REGION)
            .withExistingResourceGroup(rgName)
            .withNewLinuxPlan(PricingTier.BASIC_B1)
            .withPrivateRegistryImage("samples/nginx:latest", "https://" + registry.loginServerUrl())
            .withManagedIdentityCredentials()
            .withSystemAssignedManagedServiceIdentity()
            .create();

        String principalId = webApp.systemAssignedManagedServiceIdentityPrincipalId();
        Assertions.assertNotNull(principalId);

        // Grant the web app's managed identity the AcrPull role, scoped to the registry (passwordless pull access).
        azureResourceManager.accessManagement()
            .roleAssignments()
            .define(UUID.randomUUID().toString())
            .forObjectId(principalId)
            .withBuiltInRole(BuiltInRole.ACR_PULL)
            .withResourceScope(registry)
            .create();

        // The configuration must record managed-identity pull, and no registry password must be stored in app settings.
        SiteConfigResourceInner config = azureResourceManager.webApps()
            .manager()
            .serviceClient()
            .getWebApps()
            .getConfiguration(rgName, webAppName);
        Assertions.assertEquals(Boolean.TRUE, config.acrUseManagedIdentityCreds());
        Assertions.assertFalse(webApp.getAppSettings().containsKey("DOCKER_REGISTRY_SERVER_PASSWORD"));
        Assertions.assertFalse(webApp.getAppSettings().containsKey("DOCKER_REGISTRY_SERVER_USERNAME"));
    }

    @Test
    @LiveOnly // Requires Microsoft.Authorization/roleAssignments/write (AcrPull grant); not available on the shared
                   // test subscription, so this cannot run in RECORD/PLAYBACK. Run against a personal subscription.
    public void canPullPrivateAcrImageWithManagedIdentityForSlot() {
        final String acrName = generateRandomResourceName("acrmi", 20);
        final String webAppName = generateRandomResourceName("webacrmi", 20);
        final String slotName = generateRandomResourceName("slot", 20);

        Registry registry = azureResourceManager.containerRegistries()
            .define(acrName)
            .withRegion(REGION)
            .withNewResourceGroup(rgName)
            .withBasicSku()
            .create();

        WebApp webApp = azureResourceManager.webApps()
            .define(webAppName)
            .withRegion(REGION)
            .withExistingResourceGroup(rgName)
            .withNewLinuxPlan(PricingTier.STANDARD_S1)
            .withPublicDockerHubImage("nginx")
            .create();

        // Create a deployment slot with its own system-assigned managed identity, then configure it to pull a private
        // image using that managed identity instead of a registry username and password.
        DeploymentSlot slot = webApp.deploymentSlots()
            .define(slotName)
            .withConfigurationFromParent()
            .withSystemAssignedManagedServiceIdentity()
            .create();

        slot.update()
            .withPrivateRegistryImage("samples/nginx:latest", "https://" + registry.loginServerUrl())
            .withManagedIdentityCredentials()
            .apply();

        String principalId = slot.systemAssignedManagedServiceIdentityPrincipalId();
        Assertions.assertNotNull(principalId);

        azureResourceManager.accessManagement()
            .roleAssignments()
            .define(UUID.randomUUID().toString())
            .forObjectId(principalId)
            .withBuiltInRole(BuiltInRole.ACR_PULL)
            .withResourceScope(registry)
            .create();

        SiteConfigResourceInner config = azureResourceManager.webApps()
            .manager()
            .serviceClient()
            .getWebApps()
            .getConfigurationSlot(rgName, webAppName, slotName);
        Assertions.assertEquals(Boolean.TRUE, config.acrUseManagedIdentityCreds());
        Assertions.assertFalse(slot.getAppSettings().containsKey("DOCKER_REGISTRY_SERVER_PASSWORD"));
        Assertions.assertFalse(slot.getAppSettings().containsKey("DOCKER_REGISTRY_SERVER_USERNAME"));
    }

    @Test
    @LiveOnly // Requires Microsoft.Authorization/roleAssignments/write (AcrPull grant); not available on the shared
                   // test subscription, so this cannot run in RECORD/PLAYBACK. Run against a personal subscription.
    public void canPullPrivateAcrImageWithManagedIdentityForFunctionApp() {
        final String acrName = generateRandomResourceName("acrmi", 20);
        final String functionAppName = generateRandomResourceName("funcacrmi", 20);

        Registry registry = azureResourceManager.containerRegistries()
            .define(acrName)
            .withRegion(REGION)
            .withNewResourceGroup(rgName)
            .withBasicSku()
            .create();

        // Create a Linux (Elastic Premium) function app that pulls a private image using its system-assigned managed
        // identity instead of a registry username and password.
        FunctionApp functionApp = azureResourceManager.functionApps()
            .define(functionAppName)
            .withRegion(REGION)
            .withExistingResourceGroup(rgName)
            .withNewLinuxAppServicePlan(new PricingTier(SkuName.ELASTIC_PREMIUM.toString(), "EP1"))
            .withPrivateRegistryImage("samples/nginx:latest", "https://" + registry.loginServerUrl())
            .withManagedIdentityCredentials()
            .withRuntimeVersion("~4")
            .withSystemAssignedManagedServiceIdentity()
            .create();

        String principalId = functionApp.systemAssignedManagedServiceIdentityPrincipalId();
        Assertions.assertNotNull(principalId);

        azureResourceManager.accessManagement()
            .roleAssignments()
            .define(UUID.randomUUID().toString())
            .forObjectId(principalId)
            .withBuiltInRole(BuiltInRole.ACR_PULL)
            .withResourceScope(registry)
            .create();

        SiteConfigResourceInner config = azureResourceManager.webApps()
            .manager()
            .serviceClient()
            .getWebApps()
            .getConfiguration(rgName, functionAppName);
        Assertions.assertEquals(Boolean.TRUE, config.acrUseManagedIdentityCreds());
        Assertions.assertFalse(functionApp.getAppSettings().containsKey("DOCKER_REGISTRY_SERVER_PASSWORD"));
        Assertions.assertFalse(functionApp.getAppSettings().containsKey("DOCKER_REGISTRY_SERVER_USERNAME"));
    }
}
