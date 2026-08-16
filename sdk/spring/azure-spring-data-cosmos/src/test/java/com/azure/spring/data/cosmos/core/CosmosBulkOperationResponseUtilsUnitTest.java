// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CosmosBulkOperationResponseUtilsUnitTest {

    @Test
    public void emitErrorForFailedBulkOperationPropagatesException() {
        RuntimeException exception = new RuntimeException("Bulk operation failed");
        CosmosBulkOperationResponse<Object> response = ModelBridgeInternal.createCosmosBulkOperationResponse(
            null, exception, null);
        assertNull(response.getResponse());

        StepVerifier.create(Flux.just(response)
                .handle(CosmosBulkOperationResponseUtils::emitErrorForFailedBulkOperation))
            .expectErrorSatisfies(throwable -> assertSame(exception, throwable))
            .verify();
    }

    @Test
    public void emitErrorForFailedBulkOperationEmitsResponseWhenSuccessful() {
        CosmosBulkItemResponse itemResponse = mock(CosmosBulkItemResponse.class);
        when(itemResponse.isSuccessStatusCode()).thenReturn(true);
        CosmosBulkOperationResponse<Object> response = ModelBridgeInternal.createCosmosBulkOperationResponse(
            null, itemResponse, null);

        StepVerifier.create(Flux.just(response)
                .handle(CosmosBulkOperationResponseUtils::emitErrorForFailedBulkOperation))
            .expectNext(response)
            .verifyComplete();
    }

    @Test
    public void emitErrorForFailedBulkOperationRejectsMissingResponseWithoutException() {
        CosmosBulkOperationResponse<Object> response = ModelBridgeInternal.createCosmosBulkOperationResponse(
            null, (CosmosBulkItemResponse) null, null);

        StepVerifier.create(Flux.just(response)
                .handle(CosmosBulkOperationResponseUtils::emitErrorForFailedBulkOperation))
            .expectErrorMatches(error -> error instanceof IllegalStateException
                && error.getMessage().contains("without an item response or exception"))
            .verify();
    }

    @Test
    public void emitErrorForFailedBulkOperationRejectsUnsuccessfulResponse() {
        CosmosBulkItemResponse itemResponse = mock(CosmosBulkItemResponse.class);
        when(itemResponse.isSuccessStatusCode()).thenReturn(false);
        when(itemResponse.getStatusCode()).thenReturn(500);
        CosmosBulkOperationResponse<Object> response = ModelBridgeInternal.createCosmosBulkOperationResponse(
            null, itemResponse, null);

        StepVerifier.create(Flux.just(response)
                .handle(CosmosBulkOperationResponseUtils::emitErrorForFailedBulkOperation))
            .expectErrorMatches(error -> error instanceof IllegalStateException
                && error.getMessage().contains("status code 500"))
            .verify();
    }
}
