// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentsafety.models;

import com.azure.core.util.BinaryData;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class ProvenanceDetectOperationTests {
    @Test
    public void serializesOperationIdentityAndTimestamps() {
        String json = "{\"id\":\"operation-id\",\"status\":\"Succeeded\",\"kind\":\"Detect\","
            + "\"createdAt\":\"2026-09-15T12:00:00Z\",\"lastUpdatedAt\":\"2026-09-15T12:01:00Z\"}";

        ProvenanceDetectOperation operation = BinaryData.fromString(json).toObject(ProvenanceDetectOperation.class);
        ProvenanceDetectOperation roundTrip
            = BinaryData.fromObject(operation).toObject(ProvenanceDetectOperation.class);

        Assertions.assertEquals("operation-id", roundTrip.getId());
        Assertions.assertEquals(OperationState.SUCCEEDED, roundTrip.getStatus());
        Assertions.assertEquals(ProvenanceOperationKind.DETECT, roundTrip.getKind());
        Assertions.assertEquals(OffsetDateTime.parse("2026-09-15T12:00:00Z"), roundTrip.getCreatedAt());
        Assertions.assertEquals(OffsetDateTime.parse("2026-09-15T12:01:00Z"), roundTrip.getLastUpdatedAt());
    }
}
