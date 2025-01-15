// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertThrows;

public class CosmosExceptionUtilsTest {

    private final ResponseDiagnosticsProcessor responseDiagnosticsProcessor = Mockito.mock(ResponseDiagnosticsProcessor.class);

    @Test
    public void testBadRequestException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(Constants.CosmosExceptionStatusCodes.BADREQUEST, "Bad Request");
        assertThrows(CosmosBadRequestException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Bad Request", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testConflictException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(Constants.CosmosExceptionStatusCodes.CONFLICT, "Conflict Exception");
        assertThrows(CosmosConflictException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Conflict", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testForbiddenException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(Constants.CosmosExceptionStatusCodes.FORBIDDEN, "Forbidden Exception");
        assertThrows(CosmosForbiddenException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Frobidden", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testGoneException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(Constants.CosmosExceptionStatusCodes.GONE, "Gone Exception");
        assertThrows(CosmosGoneException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Gone", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testInvalidPartitionException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(Constants.CosmosExceptionStatusCodes.GONE, "Invalid Partition Exception");
        BridgeInternal.setSubStatusCode(cosmosException, Constants.CosmosExceptionSubStatusCodes.NAME_CACHE_IS_STALE);
        assertThrows(CosmosInvalidPartitionException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Invalid Partition", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testPartitionIsMigratingException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(Constants.CosmosExceptionStatusCodes.GONE, "Partition is Migrating Exception");
        BridgeInternal.setSubStatusCode(cosmosException, Constants.CosmosExceptionSubStatusCodes.COMPLETING_PARTITION_MIGRATION);
        assertThrows(CosmosPartitionIsMigratingException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Partition is Migrating", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testPartitionKeyRangeGoneException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(Constants.CosmosExceptionStatusCodes.GONE, "Partition Key Range Gone Exception");
        BridgeInternal.setSubStatusCode(cosmosException, Constants.CosmosExceptionSubStatusCodes.PARTITION_KEY_RANGE_GONE);
        assertThrows(CosmosPartitionKeyRangeGoneException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Partition Key Range Gone", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testPartitionKeyRangeIsSplittingException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(Constants.CosmosExceptionStatusCodes.GONE, "Partition Key Range is Splitting Exception");
        BridgeInternal.setSubStatusCode(cosmosException, Constants.CosmosExceptionSubStatusCodes.COMPLETING_SPLIT_OR_MERGE);
        assertThrows(CosmosPartitionKeyRangeIsSplittingException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Partition Key Range is Splitting", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testInternalServerErrorException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(Constants.CosmosExceptionStatusCodes.INTERNAL_SERVER_ERROR, "Internal Server Error Exception");
        assertThrows(CosmosInternalServerErrorException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Internal Server Error", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testMethodNotAllowedException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(Constants.CosmosExceptionStatusCodes.METHOD_NOT_ALLOWED, "Method Not Allowed Exception");
        assertThrows(CosmosMethodNotAllowedException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Method Not Allowed", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testNotFoundException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(Constants.CosmosExceptionStatusCodes.NOTFOUND, "Not Found Exception");
        assertThrows(CosmosNotFoundException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Not Found Allowed", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testRequestTimeoutException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(Constants.CosmosExceptionStatusCodes.REQUEST_TIMEOUT, "Request Timeout Exception");
        assertThrows(CosmosRequestTimeoutException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Request Timeout", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testOperationCancelledException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(Constants.CosmosExceptionStatusCodes.REQUEST_TIMEOUT, "Request Timeout Exception");
        BridgeInternal.setSubStatusCode(cosmosException, Constants.CosmosExceptionSubStatusCodes.CLIENT_OPERATION_TIMEOUT);
        assertThrows(CosmosOperationCancelledException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Operation Cancelled", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testPreconditionFailedException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(Constants.CosmosExceptionStatusCodes.PRECONDITION_FAILED, "Precondition Failed Exception");
        assertThrows(CosmosPreconditionFailedException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Precondition Failed", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testRequestEntityTooLargeException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(Constants.CosmosExceptionStatusCodes.REQUEST_ENTITY_TOO_LARGE, "Request Entity Too Large Exception");
        assertThrows(CosmosRequestEntityTooLargeException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Request Entity Too Large", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testRequestRateTooLargeException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(Constants.CosmosExceptionStatusCodes.TOO_MANY_REQUESTS, "Request Rate Too Large Exception");
        assertThrows(CosmosRequestRateTooLargeException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Request Rate Too Large", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testRetryWithException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(Constants.CosmosExceptionStatusCodes.RETRY_WITH, "Retry With Exception");
        assertThrows(CosmosRetryWithException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Retry With", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testServiceUnavailableException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(Constants.CosmosExceptionStatusCodes.SERVICE_UNAVAILABLE, "Service Unavailable Exception");
        assertThrows(CosmosServiceUnavailableException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Service Unavailable", cosmosException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testUnauthorizedException() {
        CosmosException cosmosException = BridgeInternal.createCosmosException(Constants.CosmosExceptionStatusCodes.UNAUTHORIZED, "Unauthorized Exception");
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
