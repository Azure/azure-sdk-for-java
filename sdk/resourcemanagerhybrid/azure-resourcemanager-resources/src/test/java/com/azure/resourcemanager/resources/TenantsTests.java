// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.models.Tenant;
import com.azure.resourcemanager.test.utils.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TenantsTests extends ResourceManagementTest {
    @Test
    public void canListTenants() throws Exception {
        PagedIterable<Tenant> tenants = resourceClient.tenants().list();
        Assertions.assertTrue(TestUtilities.getSize(tenants) > 0);
    }
}
