// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.resources.models.Provider;
import com.azure.resourcemanager.resources.models.ProviderResourceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ProvidersTests extends TestBase {
    protected ResourceManager resourceManager;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        resourceManager = ResourceManager
                .authenticate(httpPipeline, profile)
                .withSdkContext(sdkContext)
                .withDefaultSubscription();
    }

    @Override
    protected void cleanUpResources() {
    }

    @Test
    public void canUnregisterAndRegisterProvider() throws Exception {
        PagedIterable<Provider> providers = resourceManager.providers().list();
        int size = TestUtilities.getSize(providers);
        Assertions.assertTrue(size > 0);
        Provider provider = providers.iterator().next();
        resourceManager.providers().unregister(provider.namespace());
        provider = resourceManager.providers().getByName(provider.namespace());
        while (provider.registrationState().equals("Unregistering")) {
            SdkContext.sleep(5000);
            provider = resourceManager.providers().getByName(provider.namespace());
        }
        resourceManager.providers().register(provider.namespace());
        while (provider.registrationState().equals("Unregistered")
                || provider.registrationState().equalsIgnoreCase("Registering")) {
            SdkContext.sleep(5 * 1000);
            provider = resourceManager.providers().getByName(provider.namespace());
        }
        Assertions.assertEquals("Registered", provider.registrationState());
        List<ProviderResourceType> resourceTypes = provider.resourceTypes();
        Assertions.assertTrue(resourceTypes.size() > 0);
    }
}
