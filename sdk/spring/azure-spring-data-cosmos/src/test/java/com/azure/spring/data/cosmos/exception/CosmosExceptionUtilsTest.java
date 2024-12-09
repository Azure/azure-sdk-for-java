// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.ConflictException;
import com.azure.cosmos.implementation.ForbiddenException;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.InvalidPartitionException;
import com.azure.cosmos.implementation.MethodNotAllowedException;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.OperationCancelledException;
import com.azure.cosmos.implementation.PartitionIsMigratingException;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneException;
import com.azure.cosmos.implementation.PartitionKeyRangeIsSplittingException;
import com.azure.cosmos.implementation.PreconditionFailedException;
import com.azure.cosmos.implementation.RequestEntityTooLargeException;
import com.azure.cosmos.implementation.RequestRateTooLargeException;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.RetryWithException;
import com.azure.cosmos.implementation.ServiceUnavailableException;
import com.azure.cosmos.implementation.UnauthorizedException;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertThrows;

public class CosmosExceptionUtilsTest {

    private final ResponseDiagnosticsProcessor responseDiagnosticsProcessor = Mockito.mock(ResponseDiagnosticsProcessor.class);

    @Test
    public void testBadRequestException() {
        BadRequestException badRequestException = new BadRequestException("Bad Request");
        assertThrows(CosmosBadRequestException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Bad Request", badRequestException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testConflictException() {
        ConflictException conflictException = new ConflictException("Conflict Exception", null, null);
        assertThrows(CosmosConflictException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Conflict", conflictException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testForbiddenException() {
        ForbiddenException forbiddenException = new ForbiddenException("Forbidden", null, null);
        assertThrows(CosmosForbiddenException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Forbidden", forbiddenException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testGoneException() {
        GoneException goneException = new GoneException("Gone");
        assertThrows(CosmosGoneException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Gone", goneException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testInternalServerErrorException() {
        InternalServerErrorException internalServerErrorException = new InternalServerErrorException("Internal Server Error", 500);
        assertThrows(CosmosInternalServerErrorException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Internal Server Error", internalServerErrorException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testInvalidPartitionException() {
        InvalidPartitionException invalidPartitionException = new InvalidPartitionException("Invalid Partition");
        assertThrows(CosmosInvalidPartitionException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Invalid Partition", invalidPartitionException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testMethodNotAllowedException() {
        MethodNotAllowedException methodNotAllowedException = new MethodNotAllowedException("Method Not Allowed", null, null, null);
        assertThrows(CosmosMethodNotAllowedException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Method Not Allowed", methodNotAllowedException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testNotFoundException() {
        NotFoundException notFoundException = new NotFoundException("Not Found");
        assertThrows(CosmosNotFoundException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Not Found", notFoundException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testOperationCancelledException() {
        OperationCancelledException operationCancelledException = new OperationCancelledException();
        assertThrows(CosmosOperationCancelledException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Operation Cancelled", operationCancelledException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testPartitionIsMigratingException() {
        PartitionIsMigratingException partitionIsMigratingException = new PartitionIsMigratingException();
        assertThrows(CosmosPartitionIsMigratingException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Partition Is Migrating", partitionIsMigratingException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testPartitionKeyRangeGoneException() {
        PartitionKeyRangeGoneException partitionKeyRangeGoneException = new PartitionKeyRangeGoneException();
        assertThrows(CosmosPartitionKeyRangeGoneException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Partition Key Range Gone", partitionKeyRangeGoneException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testPartitionKeyRangeIsSplittingException() {
        PartitionKeyRangeIsSplittingException partitionKeyRangeIsSplittingException = new PartitionKeyRangeIsSplittingException();
        assertThrows(CosmosPartitionKeyRangeIsSplittingException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Partition Key Range Is Splitting", partitionKeyRangeIsSplittingException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testPreconditionFailedException() {
        PreconditionFailedException preconditionFailedException = new PreconditionFailedException("Precondition Failed", null, null);
        assertThrows(CosmosPreconditionFailedException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Precondition Failed", preconditionFailedException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testRequestEntityTooLargeException() {
        RequestEntityTooLargeException requestEntityTooLargeException = new RequestEntityTooLargeException("Request Entity Too Large", null, null);
        assertThrows(CosmosRequestEntityTooLargeException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Request Entity Too Large", requestEntityTooLargeException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testRequestRateTooLargeException() {
        RequestRateTooLargeException requestRateTooLargeException = new RequestRateTooLargeException();
        assertThrows(CosmosRequestRateTooLargeException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Request Rate Too Large", requestRateTooLargeException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testRequestTimeoutException() {
        RequestTimeoutException requestTimeoutException = new RequestTimeoutException();
        assertThrows(CosmosRequestTimeoutException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Request Timeout", requestTimeoutException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testRetryWithException() {
        RetryWithException retryWithException = new RetryWithException("Retry With", null, null);
        assertThrows(CosmosRetryWithException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Retry With", retryWithException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testServiceUnavailableException() {
        ServiceUnavailableException serviceUnavailableException = new ServiceUnavailableException("Service Unavailable", null, null, 503);
        assertThrows(CosmosServiceUnavailableException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Service Unavailable", serviceUnavailableException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testUnauthorizedException() {
        UnauthorizedException unauthorizedException = new UnauthorizedException("Unauthorized", null, null);
        assertThrows(CosmosUnauthorizedException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Unauthorized", unauthorizedException, responseDiagnosticsProcessor).block();
        });
    }

    @Test
    public void testDefaultException() {
        OperationCancelledException operationCancelledException = new OperationCancelledException();
        assertThrows(CosmosAccessException.class, () -> {
            CosmosExceptionUtils.exceptionHandler("Random Cosmos Exception", operationCancelledException, responseDiagnosticsProcessor).block();
        });
    }
}
