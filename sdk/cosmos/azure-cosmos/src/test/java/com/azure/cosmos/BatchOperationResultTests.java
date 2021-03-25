// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.batch.ItemBatchOperation;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;

import java.time.Duration;
import static org.assertj.core.api.Assertions.assertThat;

public class BatchOperationResultTests {

    private static final int TIMEOUT = 40000;
    private ObjectNode objectNode = Utils.getSimpleObjectMapper().createObjectNode();
    private ItemBatchOperation<?> operation = new ItemBatchOperation<>(
        CosmosItemOperationType.READ,
        null,
        null,
        null,
        null
        );

    private TransactionalBatchOperationResult createTestResult() {
        TransactionalBatchOperationResult result = BridgeInternal.createTransactionBatchResult(
            "TestETag",
            1.4,
            objectNode,
            HttpResponseStatus.OK.code(),
            Duration.ofMillis(1234),
            HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE,
            operation
        );

        return result;
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void propertiesAreSetThroughCtor() {
        TransactionalBatchOperationResult result = createTestResult();

        assertThat(result.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(result.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE);
        assertThat(result.getETag()).isEqualTo("TestETag");
        assertThat(result.getRequestCharge()).isEqualTo(1.4);
        assertThat(result.getRetryAfterDuration()).isEqualTo(Duration.ofMillis(1234));
        assertThat(result.getResourceObject()).isSameAs(objectNode);
        assertThat(result.getOperation()).isSameAs(operation);
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void isSuccessStatusCodeTrueFor200To299() {
        for (int x = 100; x < 999; ++x) {
            TransactionalBatchOperationResult result = BridgeInternal.createTransactionBatchResult(
                null,
                0.0,
                null,
                x,
                null,
                0,
                operation
            );

            boolean success = x >= 200 && x <= 299;
            assertThat(result.isSuccessStatusCode()).isEqualTo(success);
        }
    }
}
