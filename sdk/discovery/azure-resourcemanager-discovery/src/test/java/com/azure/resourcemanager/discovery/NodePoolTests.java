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
 * Tests are disabled - matching Python SDK which skips node pool create tests.
 */
public class NodePoolTests extends DiscoveryManagementTest {

    private static final String SUPERCOMPUTER_RESOURCE_GROUP = "olawal";
    private static final String SUPERCOMPUTER_NAME = "test-sc-java01";
    private static final String NODE_POOL_NAME = "test-np-java01";

    @Test
    @Disabled("Requires existing supercomputer with node pools - skipped like Python SDK")
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
    @Disabled("Requires existing supercomputer with node pool - skipped like Python SDK")
    public void testGetNodePool() {
        NodePool nodePool
            = discoveryManager.nodePools().get(SUPERCOMPUTER_RESOURCE_GROUP, SUPERCOMPUTER_NAME, NODE_POOL_NAME);
        assertNotNull(nodePool);
        assertNotNull(nodePool.name());
        assertNotNull(nodePool.id());
    }

    @Test
    @Disabled("Node pool create fails server-side (400) - skipped like Python SDK")
    public void testCreateNodePool() {
        // NodePool creation requires a valid supercomputer and proper configuration
        // Skipped due to known server-side issues
    }

    @Test
    @Disabled("Requires existing node pool - skipped like Python SDK")
    public void testDeleteNodePool() {
        // NodePool deletion requires an existing node pool
    }
}
