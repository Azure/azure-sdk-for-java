// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.discovery.models.Supercomputer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live tests for Supercomputer operations against EUAP endpoint.
 * 
 * Tests match the comprehensive coverage in Python SDK:
 * - test_list_supercomputers_by_subscription
 * - test_list_supercomputers_by_resource_group
 * 
 * NOTE: Tests are disabled because backend is unstable for supercomputers
 * endpoint in some regions.
 */
public class SupercomputerTests extends DiscoveryManagementTest {

    private static final String SUPERCOMPUTER_RESOURCE_GROUP = "olawal";

    @Test
    @Disabled("Backend unstable - centraluseuap region doesn't consistently support 2026-02-01-preview for supercomputers")
    public void testListSupercomputersBySubscription() {
        // Test listing supercomputers in the subscription
        // (matching Python test_list_supercomputers_by_subscription)
        PagedIterable<Supercomputer> supercomputers = discoveryManager.supercomputers().list();
        assertNotNull(supercomputers);

        // Collect all supercomputers
        List<Supercomputer> supercomputerList = new ArrayList<>();
        for (Supercomputer supercomputer : supercomputers) {
            assertNotNull(supercomputer.name());
            assertNotNull(supercomputer.id());
            assertNotNull(supercomputer.type());
            supercomputerList.add(supercomputer);
        }

        // Supercomputers list should be a valid list (may be empty)
        assertNotNull(supercomputerList);
    }

    @Test
    @Disabled("Backend unstable - centraluseuap region doesn't consistently support 2026-02-01-preview for supercomputers")
    public void testListSupercomputersByResourceGroup() {
        // Test listing supercomputers in a specific resource group
        // (matching Python test_list_supercomputers_by_resource_group)
        PagedIterable<Supercomputer> supercomputers
            = discoveryManager.supercomputers().listByResourceGroup(SUPERCOMPUTER_RESOURCE_GROUP);
        assertNotNull(supercomputers);

        // Collect all supercomputers
        List<Supercomputer> supercomputerList = new ArrayList<>();
        for (Supercomputer supercomputer : supercomputers) {
            assertNotNull(supercomputer.name());
            assertNotNull(supercomputer.id());
            supercomputerList.add(supercomputer);
        }

        // Supercomputers list should be a valid list (may be empty)
        assertNotNull(supercomputerList);
    }
}
