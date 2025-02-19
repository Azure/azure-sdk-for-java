// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.Provider;
import com.azure.resourcemanager.resources.models.ProviderResourceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

public class ProvidersTests extends ResourceManagementTest {

    // record data too big
    @DoNotRecord(skipInPlayback = true)
    @Test
    public void canUnregisterAndRegisterProvider() {
        // take a rare provider that usually not registered
        final String providerNamespace = "Microsoft.StandbyPool";

        // list
        PagedIterable<Provider> providers = resourceClient.providers().list();
        int size = TestUtilities.getSize(providers);
        Assertions.assertTrue(size > 0);

        // get and check if already registered
        Provider provider = resourceClient.providers().getByName(providerNamespace);
        List<ProviderResourceType> resourceTypes = provider.resourceTypes();
        Assertions.assertFalse(resourceTypes.isEmpty());
        Assertions.assertNotNull(provider);
        boolean providerAlreadyRegistered = false;
        if ("Registered".equals(provider.registrationState())) {
            providerAlreadyRegistered = true;
            // unregister it first
            unregisterProvider(provider);
        }

        // register
        provider = resourceClient.providers().register(providerNamespace);
        while (provider.registrationState().equals("Unregistered")
            || provider.registrationState().equalsIgnoreCase("Registering")) {
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
            provider = resourceClient.providers().getByName(provider.namespace());
        }
        Assertions.assertEquals("Registered", provider.registrationState());

        if (!providerAlreadyRegistered) {
            unregisterProvider(provider);
        }
    }

    private void unregisterProvider(Provider provider) {
        // unregister
        resourceClient.providers().unregister(provider.namespace());
        provider = resourceClient.providers().getByName(provider.namespace());
        while (provider.registrationState().equals("Unregistering")) {
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));
            provider = resourceClient.providers().getByName(provider.namespace());
        }
    }
}
