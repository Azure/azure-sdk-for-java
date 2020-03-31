/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources;

import com.azure.core.http.rest.PagedIterable;
import com.azure.management.RestClient;
import com.azure.management.resources.core.TestBase;
import com.azure.management.resources.core.TestUtilities;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.resources.implementation.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ProvidersTests extends TestBase {
    protected ResourceManager resourceManager;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSdkContext(sdkContext)
                .withSubscription(defaultSubscription);
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
