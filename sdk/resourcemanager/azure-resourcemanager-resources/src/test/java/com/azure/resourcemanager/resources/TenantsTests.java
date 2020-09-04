// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluent.inner.TenantIdDescriptionInner;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TenantsTests extends ResourceManagementTest {
    protected ResourceManager.Authenticated resourceManager;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        SdkContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        resourceManager = ResourceManager
                .authenticate(httpPipeline, profile);
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
