// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.spring.data.cosmos.common.CosmosUtils;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
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
        if (ObjectUtils.isEmpty(message)) {
            message = "Failed to access cosmos db database";
        }
        //  Unwrap the exception in case if it is a reactive exception
        final Throwable unwrappedThrowable = Exceptions.unwrap(throwable);
        if (unwrappedThrowable instanceof CosmosException cosmosException) {
            CosmosUtils.fillAndProcessCosmosExceptionDiagnostics(responseDiagnosticsProcessor, cosmosException);

            switch (cosmosException.getStatusCode()) {
                case HttpConstants.StatusCodes.BADREQUEST -> throw new CosmosBadRequestException(message, cosmosException);
                case HttpConstants.StatusCodes.CONFLICT -> throw new CosmosConflictException(message, cosmosException);
                case HttpConstants.StatusCodes.FORBIDDEN -> throw new CosmosForbiddenException(message, cosmosException);
                case HttpConstants.StatusCodes.GONE-> {
                    switch(cosmosException.getSubStatusCode()) {
                        case HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE -> throw new CosmosInvalidPartitionException(message, cosmosException);
                        case HttpConstants.SubStatusCodes.COMPLETING_PARTITION_MIGRATION -> throw new CosmosPartitionIsMigratingException(message, cosmosException);
                        case HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE -> throw new CosmosPartitionKeyRangeGoneException(message, cosmosException);
                        case HttpConstants.SubStatusCodes.COMPLETING_SPLIT_OR_MERGE -> throw new CosmosPartitionKeyRangeIsSplittingException(message, cosmosException);
                        default -> throw new CosmosGoneException(message, cosmosException);
                    }
                }
                case HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR -> throw new CosmosInternalServerErrorException(message, cosmosException);
                case HttpConstants.StatusCodes.METHOD_NOT_ALLOWED -> throw new CosmosMethodNotAllowedException(message, cosmosException);
                case HttpConstants.StatusCodes.NOTFOUND -> throw new CosmosNotFoundException(message, unwrappedThrowable);
                case HttpConstants.StatusCodes.REQUEST_TIMEOUT -> {
                    if (((CosmosException) unwrappedThrowable).getSubStatusCode() == HttpConstants.SubStatusCodes.CLIENT_OPERATION_TIMEOUT) {
                        throw new CosmosOperationCancelledException(message, cosmosException);
                    }
                    throw new CosmosRequestTimeoutException(message, cosmosException);
                }
                case HttpConstants.StatusCodes.PRECONDITION_FAILED -> throw new CosmosPreconditionFailedException(message, cosmosException);
                case HttpConstants.StatusCodes.REQUEST_ENTITY_TOO_LARGE -> throw new CosmosRequestEntityTooLargeException(message, cosmosException);
                case HttpConstants.StatusCodes.TOO_MANY_REQUESTS -> throw new CosmosRequestRateTooLargeException(message, cosmosException);
                case HttpConstants.StatusCodes.RETRY_WITH -> throw new CosmosRetryWithException(message, cosmosException);
                case HttpConstants.StatusCodes.SERVICE_UNAVAILABLE -> throw new CosmosServiceUnavailableException(message, unwrappedThrowable);
                case HttpConstants.StatusCodes.UNAUTHORIZED -> throw new CosmosUnauthorizedException(message, unwrappedThrowable);
                default -> throw new CosmosAccessException(message, cosmosException);
            }

        }
        throw new CosmosAccessException(message, unwrappedThrowable);
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
