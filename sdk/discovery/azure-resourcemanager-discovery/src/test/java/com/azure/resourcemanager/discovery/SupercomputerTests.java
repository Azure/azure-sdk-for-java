// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.discovery.models.Identity;
import com.azure.resourcemanager.discovery.models.Supercomputer;
import com.azure.resourcemanager.discovery.models.SupercomputerIdentities;
import com.azure.resourcemanager.discovery.models.SupercomputerProperties;
import com.azure.resourcemanager.discovery.models.UserAssignedIdentity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live tests for Supercomputer operations against EUAP endpoint.
 *
 * Tests match the comprehensive coverage in Python SDK.
 * Java-specific resource name: test-sc-java01 (different from Python's test-sc-2bbb25b8).
 */
public class SupercomputerTests extends DiscoveryManagementTest {

    private static final String SUPERCOMPUTER_RESOURCE_GROUP = "olawal";
    private static final String SUPERCOMPUTER_NAME = "test-sc-java01";
    private static final String SUBSCRIPTION_ID = "31b0b6a5-2647-47eb-8a38-7d12047ee8ec";
    private static final String MI_ID = "/subscriptions/" + SUBSCRIPTION_ID
        + "/resourcegroups/olawal/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myidentity";

    @Test
    public void testListSupercomputersBySubscription() {
        PagedIterable<Supercomputer> supercomputers = discoveryManager.supercomputers().list();
        assertNotNull(supercomputers);

        List<Supercomputer> supercomputerList = new ArrayList<>();
        for (Supercomputer supercomputer : supercomputers) {
            assertNotNull(supercomputer.name());
            assertNotNull(supercomputer.id());
            assertNotNull(supercomputer.type());
            supercomputerList.add(supercomputer);
        }

        assertNotNull(supercomputerList);
    }

    @Test
    public void testListSupercomputersByResourceGroup() {
        PagedIterable<Supercomputer> supercomputers
            = discoveryManager.supercomputers().listByResourceGroup(SUPERCOMPUTER_RESOURCE_GROUP);
        assertNotNull(supercomputers);

        List<Supercomputer> supercomputerList = new ArrayList<>();
        for (Supercomputer supercomputer : supercomputers) {
            assertNotNull(supercomputer.name());
            assertNotNull(supercomputer.id());
            supercomputerList.add(supercomputer);
        }

        assertNotNull(supercomputerList);
    }

    @Test
    public void testGetSupercomputer() {
        Supercomputer supercomputer
            = discoveryManager.supercomputers().getByResourceGroup(SUPERCOMPUTER_RESOURCE_GROUP, SUPERCOMPUTER_NAME);
        assertNotNull(supercomputer);
        assertNotNull(supercomputer.name());
        assertNotNull(supercomputer.location());
    }

    @Test
    public void testCreateSupercomputer() {
        SupercomputerProperties properties = new SupercomputerProperties()
            .withSubnetId("/subscriptions/" + SUBSCRIPTION_ID
                + "/resourceGroups/olawal/providers/Microsoft.Network/virtualNetworks/newapiv/subnets/default")
            .withIdentities(new SupercomputerIdentities().withClusterIdentity(new Identity().withId(MI_ID))
                .withKubeletIdentity(new Identity().withId(MI_ID))
                .withWorkloadIdentities(Collections.singletonMap(MI_ID, new UserAssignedIdentity())));

        Supercomputer supercomputer = discoveryManager.supercomputers()
            .define(SUPERCOMPUTER_NAME)
            .withRegion("uksouth")
            .withExistingResourceGroup(SUPERCOMPUTER_RESOURCE_GROUP)
            .withProperties(properties)
            .create();

        assertNotNull(supercomputer);
        assertNotNull(supercomputer.id());
        assertNotNull(supercomputer.name());
    }

    @Test
    public void testDeleteSupercomputer() {
        discoveryManager.supercomputers().deleteByResourceGroup(SUPERCOMPUTER_RESOURCE_GROUP, SUPERCOMPUTER_NAME);
    }
}
