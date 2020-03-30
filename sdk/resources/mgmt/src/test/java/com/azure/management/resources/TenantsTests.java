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
import com.azure.management.resources.implementation.ResourceManager;
import com.azure.management.resources.models.TenantIdDescriptionInner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TenantsTests extends TestBase {
    protected ResourceManager.Authenticated resourceManager;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSdkContext(sdkContext);
    }

    @Override
    protected void cleanUpResources() {

    }

    @Test
    public void canListTenants() throws Exception {
        PagedIterable<TenantIdDescriptionInner> tenants = resourceManager.tenants().list();
        Assertions.assertTrue(TestUtilities.getSize(tenants) > 0);
    }
}
