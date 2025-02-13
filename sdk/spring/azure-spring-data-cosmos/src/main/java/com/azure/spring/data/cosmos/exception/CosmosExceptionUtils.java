// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.CosmosException;
import com.azure.spring.data.cosmos.common.CosmosUtils;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

/**
 * To handle and throw a cosmos db exception when access the database
 */
public class CosmosExceptionUtils {

    /**
     * Creates an instance of {@link CosmosExceptionUtils}
     */
    public CosmosExceptionUtils() {
    }

    /**
     * To throw a CosmosDBAccessException
     *
     * @param message the detail message
     * @param throwable exception
     * @param responseDiagnosticsProcessor response diagnostics processor
     * @param <T> type class of Mono
     * @return Mono instance
     * @throws CosmosAccessException for operations on cosmos db
     */
    public static <T> Mono<T> exceptionHandler(String message, Throwable throwable,
                                               ResponseDiagnosticsProcessor responseDiagnosticsProcessor) {
        CosmosAccessException cosmosAccessException;
        if (ObjectUtils.isEmpty(message)) {
            message = "Failed to access cosmos db database";
        }
        //  Unwrap the exception in case if it is a reactive exception
        final Throwable unwrappedThrowable = Exceptions.unwrap(throwable);
        if (unwrappedThrowable instanceof CosmosException) {
            CosmosException cosmosException = (CosmosException) unwrappedThrowable;
            CosmosUtils.fillAndProcessCosmosExceptionDiagnostics(responseDiagnosticsProcessor, cosmosException);

            int statusCode = cosmosException.getStatusCode();
            int subStatusCode = ((CosmosException) unwrappedThrowable).getSubStatusCode();
            if (statusCode == HttpStatus.BAD_REQUEST.value()) {
                cosmosAccessException = new CosmosBadRequestException(message, cosmosException);
            } else if (statusCode == HttpStatus.CONFLICT.value()) {
                cosmosAccessException = new CosmosConflictException(message, cosmosException);
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                cosmosAccessException = new CosmosForbiddenException(message, cosmosException);
            } else if (statusCode == HttpStatus.GONE.value()) {
                if (subStatusCode == HttpConstants.CosmosExceptionSubStatusCodes.NAME_CACHE_IS_STALE) {
                    cosmosAccessException = new CosmosInvalidPartitionException(message, cosmosException);
                } else if (subStatusCode == HttpConstants.CosmosExceptionSubStatusCodes.COMPLETING_PARTITION_MIGRATION) {
                    cosmosAccessException = new CosmosPartitionIsMigratingException(message, cosmosException);
                } else if (subStatusCode == HttpConstants.CosmosExceptionSubStatusCodes.PARTITION_KEY_RANGE_GONE) {
                    cosmosAccessException = new CosmosPartitionKeyRangeGoneException(message, cosmosException);
                } else if (subStatusCode == HttpConstants.CosmosExceptionSubStatusCodes.COMPLETING_SPLIT_OR_MERGE) {
                    cosmosAccessException = new CosmosPartitionKeyRangeIsSplittingException(message, cosmosException);
                } else {
                    cosmosAccessException = new CosmosGoneException(message, cosmosException);
                }
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                cosmosAccessException = new CosmosInternalServerErrorException(message, cosmosException);
            } else if (statusCode == HttpStatus.METHOD_NOT_ALLOWED.value()) {
                cosmosAccessException = new CosmosMethodNotAllowedException(message, cosmosException);
            } else if (statusCode == HttpStatus.NOT_FOUND.value()) {
                cosmosAccessException = new CosmosNotFoundException(message, cosmosException);
            } else if (statusCode == HttpStatus.REQUEST_TIMEOUT.value()) {
                if (subStatusCode == HttpConstants.CosmosExceptionSubStatusCodes.CLIENT_OPERATION_TIMEOUT) {
                    cosmosAccessException = new CosmosOperationCancelledException(message, cosmosException);
                } else {
                    cosmosAccessException = new CosmosRequestTimeoutException(message, cosmosException);
                }
            } else if (statusCode == HttpStatus.PRECONDITION_FAILED.value()) {
                cosmosAccessException = new CosmosPreconditionFailedException(message, cosmosException);
            } else if (statusCode == HttpStatus.PAYLOAD_TOO_LARGE.value()) {
                cosmosAccessException = new CosmosRequestEntityTooLargeException(message, cosmosException);
            } else if (statusCode == HttpStatus.TOO_MANY_REQUESTS.value()) {
                cosmosAccessException = new CosmosRequestRateTooLargeException(message, cosmosException);
            } else if (statusCode == HttpConstants.CosmosExceptionStatusCodes.RETRY_WITH) {
                cosmosAccessException = new CosmosRetryWithException(message, cosmosException);
            } else if (statusCode == HttpStatus.SERVICE_UNAVAILABLE.value()) {
                cosmosAccessException = new CosmosServiceUnavailableException(message, cosmosException);
            } else if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                cosmosAccessException = new CosmosUnauthorizedException(message, cosmosException);
            } else {
                cosmosAccessException = new CosmosAccessException(message, cosmosException);
            }
        } else {
            cosmosAccessException = new CosmosAccessException(message, unwrappedThrowable);
        }

        throw cosmosAccessException;
    }

    /**
     * To find an exceptionHandler for a exception and return empty Mono if the exception status code is not found
     *
     * @param message the detail message
     * @param throwable exception
     * @param responseDiagnosticsProcessor Response Diagnostics Processor
     * @param <T> type class of Mono
     * @return Mono instance
     * @throws CosmosAccessException for operations on cosmos db
     */
    public static <T> Mono<T> findAPIExceptionHandler(String message, Throwable throwable,
                                                      ResponseDiagnosticsProcessor responseDiagnosticsProcessor) {
        if (ObjectUtils.isEmpty(message)) {
            message = "Failed to find item";
        }
        //  Unwrap the exception in case if it is a reactive exception
        final Throwable unwrappedThrowable = Exceptions.unwrap(throwable);
        if (unwrappedThrowable instanceof CosmosException) {
            final CosmosException cosmosClientException = (CosmosException) unwrappedThrowable;
            CosmosUtils.fillAndProcessCosmosExceptionDiagnostics(responseDiagnosticsProcessor, cosmosClientException);
            if (cosmosClientException.getStatusCode() == 404) {
                return Mono.empty();
            }
        }
        throw new CosmosAccessException(message, unwrappedThrowable);
    }
}
