// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.discovery.models.Operation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live tests for Operations operations against EUAP endpoint.
 * 
 * Tests match the comprehensive coverage in Python SDK:
 * - test_list_operations
 */
public class OperationsTests extends DiscoveryManagementTest {

    @Test
    public void testListOperations() {
        // Test listing available API operations
        // (matching Python test_list_operations)
        PagedIterable<Operation> operations = discoveryManager.operations().list();
        assertNotNull(operations);

        // Collect all operations
        List<Operation> operationList = new ArrayList<>();
        for (Operation operation : operations) {
            assertNotNull(operation.name());
            operationList.add(operation);
        }

        // There should be at least one operation
        assertTrue(operationList.size() > 0, "Expected at least one operation");

        // First operation should have a name
        assertNotNull(operationList.get(0).name());
    }
}
