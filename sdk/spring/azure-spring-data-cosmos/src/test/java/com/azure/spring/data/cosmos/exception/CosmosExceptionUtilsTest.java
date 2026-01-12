// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CosmosExceptionUtilsTest {

    private final ResponseDiagnosticsProcessor responseDiagnosticsProcessor = Mockito.mock(ResponseDiagnosticsProcessor.class);

    @Test
    public void testBadRequestException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(HttpStatus.BAD_REQUEST.value(), "Bad Request");
        assertThrows(CosmosBadRequestException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Bad Request", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testConflictException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(HttpStatus.CONFLICT.value(), "Conflict Exception");
        assertThrows(CosmosConflictException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Conflict", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testForbiddenException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(HttpStatus.FORBIDDEN.value(), "Forbidden Exception");
        assertThrows(CosmosForbiddenException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Frobidden", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testGoneException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(HttpStatus.GONE.value(), "Gone Exception");
        assertThrows(CosmosGoneException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Gone", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testInvalidPartitionException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(HttpStatus.GONE.value(), "Invalid Partition Exception");
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.CosmosExceptionSubStatusCodes.NAME_CACHE_IS_STALE);
        assertThrows(CosmosInvalidPartitionException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Invalid Partition", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testPartitionIsMigratingException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(HttpStatus.GONE.value(), "Partition is Migrating Exception");
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.CosmosExceptionSubStatusCodes.COMPLETING_PARTITION_MIGRATION);
        assertThrows(CosmosPartitionIsMigratingException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Partition is Migrating", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testPartitionKeyRangeGoneException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(HttpStatus.GONE.value(), "Partition Key Range Gone Exception");
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.CosmosExceptionSubStatusCodes.PARTITION_KEY_RANGE_GONE);
        assertThrows(CosmosPartitionKeyRangeGoneException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Partition Key Range Gone", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testPartitionKeyRangeIsSplittingException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(HttpStatus.GONE.value(), "Partition Key Range is Splitting Exception");
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.CosmosExceptionSubStatusCodes.COMPLETING_SPLIT_OR_MERGE);
        assertThrows(CosmosPartitionKeyRangeIsSplittingException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Partition Key Range is Splitting", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testInternalServerErrorException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error Exception");
        assertThrows(CosmosInternalServerErrorException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Internal Server Error", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testMethodNotAllowedException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(HttpStatus.METHOD_NOT_ALLOWED.value(), "Method Not Allowed Exception");
        assertThrows(CosmosMethodNotAllowedException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Method Not Allowed", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testNotFoundException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(HttpStatus.NOT_FOUND.value(), "Not Found Exception");
        assertThrows(CosmosNotFoundException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Not Found Allowed", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testRequestTimeoutException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(HttpStatus.REQUEST_TIMEOUT.value(), "Request Timeout Exception");
        assertThrows(CosmosRequestTimeoutException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Request Timeout", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testOperationCancelledException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(HttpStatus.REQUEST_TIMEOUT.value(), "Request Timeout Exception");
        BridgeInternal.setSubStatusCode(cosmosException, HttpConstants.CosmosExceptionSubStatusCodes.CLIENT_OPERATION_TIMEOUT);
        assertThrows(CosmosOperationCancelledException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Operation Cancelled", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testPreconditionFailedException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(HttpStatus.PRECONDITION_FAILED.value(), "Precondition Failed Exception");
        assertThrows(CosmosPreconditionFailedException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Precondition Failed", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testRequestEntityTooLargeException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(HttpStatus.PAYLOAD_TOO_LARGE.value(), "Request Entity Too Large Exception");
        assertThrows(CosmosRequestEntityTooLargeException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Request Entity Too Large", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testRequestRateTooLargeException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(HttpStatus.TOO_MANY_REQUESTS.value(), "Request Rate Too Large Exception");
        assertThrows(CosmosRequestRateTooLargeException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Request Rate Too Large", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testRetryWithException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(HttpConstants.CosmosExceptionStatusCodes.RETRY_WITH, "Retry With Exception");
        assertThrows(CosmosRetryWithException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Retry With", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testServiceUnavailableException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(HttpStatus.SERVICE_UNAVAILABLE.value(), "Service Unavailable Exception");
        assertThrows(CosmosServiceUnavailableException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Service Unavailable", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testUnauthorizedException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Exception");
        assertThrows(CosmosUnauthorizedException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Unauthorized", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testDefaultException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(999, "Random Exception");
        assertThrows(CosmosAccessException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Random", cosmosException, responseDiagnosticsProcessor).block();
        });
    }
}
