// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.discovery.models.NodePool;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live tests for NodePool operations against EUAP endpoint.
 * 
 * NodePools are child resources of Supercomputers.
 */
public class NodePoolTests extends DiscoveryManagementTest {

    private static final String SUPERCOMPUTER_RESOURCE_GROUP = "olawal";
    private static final String SUPERCOMPUTER_NAME = "test-supercomputer";
    private static final String NODE_POOL_NAME = "test-nodepool";

    @Test
    @Disabled("Requires existing supercomputer with node pools")
    public void testListNodePoolsBySupercomputer() {
        PagedIterable<NodePool> nodePools
            = discoveryManager.nodePools().listBySupercomputer(SUPERCOMPUTER_RESOURCE_GROUP, SUPERCOMPUTER_NAME);
        assertNotNull(nodePools);

        List<NodePool> nodePoolList = new ArrayList<>();
        for (NodePool nodePool : nodePools) {
            assertNotNull(nodePool.name());
            assertNotNull(nodePool.id());
            nodePoolList.add(nodePool);
        }

        assertNotNull(nodePoolList);
    }

    @Test
    @Disabled("Requires existing supercomputer with node pool")
    public void testGetNodePool() {
        NodePool nodePool
            = discoveryManager.nodePools().get(SUPERCOMPUTER_RESOURCE_GROUP, SUPERCOMPUTER_NAME, NODE_POOL_NAME);
        assertNotNull(nodePool);
        assertNotNull(nodePool.name());
        assertNotNull(nodePool.id());
    }

    @Test
    @Disabled("Create is a mutating operation - requires supercomputer setup")
    public void testCreateNodePool() {
        // NodePool creation requires a valid supercomputer and proper configuration
        // This test is a placeholder for integration testing
    }

    @Test
    @Disabled("Update is a mutating operation - requires existing node pool")
    public void testUpdateNodePool() {
        // NodePool update requires an existing node pool
        // This test is a placeholder for integration testing
    }

    @Test
    @Disabled("Delete is a mutating operation - requires existing node pool")
    public void testDeleteNodePool() {
        // NodePool deletion requires an existing node pool
        // This test is a placeholder for integration testing
    }
}
