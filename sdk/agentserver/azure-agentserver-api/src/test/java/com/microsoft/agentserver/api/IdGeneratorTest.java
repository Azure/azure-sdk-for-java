// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.microsoft.agentserver.api.implementation.IdGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link IdGenerator} ID format, focusing on the Responses
 * protocol prefix table.
 */
class IdGeneratorTest {

    @Test
    @DisplayName("generateResponseId uses the caresp_ prefix")
    void responseIdUsesCarespPrefix() {
        IdGenerator idGen = new IdGenerator(null);
        String id = idGen.generateResponseId();

        assertTrue(id.startsWith("caresp_"),
            "Response ID must use the 'caresp' prefix, was: " + id);
        // body total = 18 (partition key) + 32 (entropy) = 50 chars after `caresp_`.
        assertEquals(50, id.substring("caresp_".length()).length(),
            "Response ID body must be 50 chars (18 partition + 32 entropy)");
    }

    @Test
    @DisplayName("generateResponseId reuses the supplied partition key for co-location")
    void responseIdReusesPartitionKey() {
        IdGenerator idGen = new IdGenerator("AAAAAAAAAAAAAAAAAA");
        String id = idGen.generateResponseId();

        assertEquals("AAAAAAAAAAAAAAAAAA", IdGenerator.extractPartitionKey(id));
    }

    @Test
    @DisplayName("Two generated response IDs differ in entropy")
    void responseIdsAreUnique() {
        IdGenerator idGen = new IdGenerator(null);
        assertNotEquals(idGen.generateResponseId(), idGen.generateResponseId());
    }
}

