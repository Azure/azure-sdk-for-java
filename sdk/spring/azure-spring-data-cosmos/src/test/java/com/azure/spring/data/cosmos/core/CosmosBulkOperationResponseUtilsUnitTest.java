// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.spring.data.cosmos.exception.CosmosConflictException;
import com.azure.spring.data.cosmos.exception.CosmosExceptionUtils;
import com.azure.spring.data.cosmos.exception.CosmosGoneException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
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
    public void transportFailurePreservesCauseAndDiagnosticsDuringSpringExceptionTranslation() {
        CosmosDiagnostics diagnostics = mock(CosmosDiagnostics.class);
        CosmosException exception = BridgeInternal.createCosmosException(410, "Bulk operation failed");
        BridgeInternal.setCosmosDiagnostics(exception, diagnostics);
        AtomicReference<ResponseDiagnostics> processedDiagnostics = new AtomicReference<>();
        CosmosBulkOperationResponse<Object> response = ModelBridgeInternal.createCosmosBulkOperationResponse(
            null, exception, null);

        StepVerifier.create(Flux.just(response)
                .handle(CosmosBulkOperationResponseUtils::emitErrorForFailedBulkOperation)
                .onErrorResume(error -> CosmosExceptionUtils.exceptionHandler(
                    "Failed to insert item(s)", error, processedDiagnostics::set)))
            .expectErrorSatisfies(error -> assertThat(error)
                .isInstanceOf(CosmosGoneException.class)
                .hasCause(exception))
            .verify();

        assertThat(processedDiagnostics.get()).isNotNull();
        assertThat(processedDiagnostics.get().getCosmosDiagnostics()).isSameAs(diagnostics);
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
        CosmosDiagnostics diagnostics = mock(CosmosDiagnostics.class);
        when(itemResponse.isSuccessStatusCode()).thenReturn(false);
        when(itemResponse.getStatusCode()).thenReturn(500);
        when(itemResponse.getSubStatusCode()).thenReturn(1002);
        when(itemResponse.getResponseHeaders()).thenReturn(Collections.emptyMap());
        when(itemResponse.getCosmosDiagnostics()).thenReturn(diagnostics);
        CosmosBulkOperationResponse<Object> response = ModelBridgeInternal.createCosmosBulkOperationResponse(
            null, itemResponse, null);

        StepVerifier.create(Flux.just(response)
                .handle(CosmosBulkOperationResponseUtils::emitErrorForFailedBulkOperation))
            .expectErrorSatisfies(error -> assertThat(error)
                .isInstanceOf(CosmosException.class)
                .satisfies(cosmosError -> {
                    CosmosException cosmosException = (CosmosException) cosmosError;
                    assertThat(cosmosException.getStatusCode()).isEqualTo(500);
                    assertThat(cosmosException.getSubStatusCode()).isEqualTo(1002);
                    assertThat(cosmosException.getDiagnostics()).isSameAs(diagnostics);
                }))
            .verify();
    }

    @Test
    public void serviceFailurePreservesDiagnosticsDuringSpringExceptionTranslation() {
        CosmosBulkItemResponse itemResponse = mock(CosmosBulkItemResponse.class);
        CosmosDiagnostics diagnostics = mock(CosmosDiagnostics.class);
        AtomicReference<ResponseDiagnostics> processedDiagnostics = new AtomicReference<>();
        when(itemResponse.isSuccessStatusCode()).thenReturn(false);
        when(itemResponse.getStatusCode()).thenReturn(409);
        when(itemResponse.getSubStatusCode()).thenReturn(0);
        when(itemResponse.getResponseHeaders()).thenReturn(Collections.emptyMap());
        when(itemResponse.getCosmosDiagnostics()).thenReturn(diagnostics);
        CosmosBulkOperationResponse<Object> response = ModelBridgeInternal.createCosmosBulkOperationResponse(
            null, itemResponse, null);

        StepVerifier.create(Flux.just(response)
                .handle(CosmosBulkOperationResponseUtils::emitErrorForFailedBulkOperation)
                .onErrorResume(error -> CosmosExceptionUtils.exceptionHandler(
                    "Failed to insert item(s)", error, processedDiagnostics::set)))
            .expectError(CosmosConflictException.class)
            .verify();

        assertThat(processedDiagnostics.get()).isNotNull();
        assertThat(processedDiagnostics.get().getCosmosDiagnostics()).isSameAs(diagnostics);
    }
}
