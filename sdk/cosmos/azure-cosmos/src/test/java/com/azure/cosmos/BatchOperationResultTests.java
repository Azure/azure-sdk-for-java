// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.batch.ItemBatchOperation;
import com.azure.cosmos.models.CosmosBatchOperationResult;
import com.azure.cosmos.models.CosmosItemOperationType;
import com.azure.cosmos.models.ModelBridgeInternal;
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

    private CosmosBatchOperationResult createTestResult() {
        CosmosBatchOperationResult result = ModelBridgeInternal.createCosmosBatchResult(
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
        CosmosBatchOperationResult result = createTestResult();

        assertThat(result.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(result.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE);
        assertThat(result.getETag()).isEqualTo("TestETag");
        assertThat(result.getRequestCharge()).isEqualTo(1.4);
        assertThat(result.getRetryAfterDuration()).isEqualTo(Duration.ofMillis(1234));
        ObjectNode resourceObject = ImplementationBridgeHelpers
            .CosmosBatchOperationResultHelper
            .getCosmosBatchOperationResultAccessor()
            .getResourceObject(result);
        assertThat(resourceObject).isSameAs(objectNode);
        assertThat(result.getOperation()).isSameAs(operation);
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void isSuccessStatusCodeTrueFor200To299() {
        for (int x = 100; x < 999; ++x) {
            CosmosBatchOperationResult result = ModelBridgeInternal.createCosmosBatchResult(
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
