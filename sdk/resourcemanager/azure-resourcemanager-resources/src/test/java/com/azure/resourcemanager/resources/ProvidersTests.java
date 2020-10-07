// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.Provider;
import com.azure.resourcemanager.resources.models.ProviderResourceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

public class ProvidersTests extends ResourceManagementTest {

    @Override
    protected void cleanUpResources() {
    }

    @Test
    public void canUnregisterAndRegisterProvider() throws Exception {
        PagedIterable<Provider> providers = resourceClient.providers().list();
        int size = TestUtilities.getSize(providers);
        Assertions.assertTrue(size > 0);
        Provider provider = providers.iterator().next();
        resourceClient.providers().unregister(provider.namespace());
        provider = resourceClient.providers().getByName(provider.namespace());
        while (provider.registrationState().equals("Unregistering")) {
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
            provider = resourceClient.providers().getByName(provider.namespace());
        }
        resourceClient.providers().register(provider.namespace());
        while (provider.registrationState().equals("Unregistered")
                || provider.registrationState().equalsIgnoreCase("Registering")) {
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
            provider = resourceClient.providers().getByName(provider.namespace());
        }
        Assertions.assertEquals("Registered", provider.registrationState());
        List<ProviderResourceType> resourceTypes = provider.resourceTypes();
        Assertions.assertTrue(resourceTypes.size() > 0);
    }
}
