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
import com.azure.resourcemanager.containerregistry.models.Registry;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.Provider;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ProviderRegistrationPolicyTests extends ResourceManagerTestBase {

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
        azureResourceManager = AzureResourceManager.authenticate(httpPipeline, profile).withDefaultSubscription();
        setInternalContext(internalContext, azureResourceManager);

        rgName = generateRandomResourceName("rg", 8);
    }

    @Override
    protected void cleanUpResources() {
        azureResourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void testProviderRegistrationPolicy() {
        final String acrName = generateRandomResourceName("acr", 10);

        final String namespace = "Microsoft.ContainerRegistry";

        Provider provider = azureResourceManager.providers().getByName(namespace);
        if (provider != null && "Registered".equalsIgnoreCase(provider.registrationState())) {
            provider = azureResourceManager.providers().unregister(namespace);

            // wait for unregister complete
            ResourceManagerUtils.sleep(Duration.ofMinutes(5));

            provider = azureResourceManager.providers().getByName(namespace);
        }
        Assertions.assertEquals("Unregistered", provider.registrationState());

        Registry registry =
            azureResourceManager
                .containerRegistries()
                .define(acrName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withPremiumSku()
                .withRegistryNameAsAdminUser()
                .withTag("tag1", "value1")
                .create();

        // above should success even when namespace "Microsoft.ContainerRegistry" not registered
        Assertions.assertNotNull(registry);
    }
}
