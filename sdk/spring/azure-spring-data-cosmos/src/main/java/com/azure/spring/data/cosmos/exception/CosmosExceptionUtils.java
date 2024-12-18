// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.CosmosException;
import com.azure.spring.data.cosmos.Constants;
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
        CosmosAccessException cosmosAccessException;
        if (ObjectUtils.isEmpty(message)) {
            message = "Failed to access cosmos db database";
        }
        //  Unwrap the exception in case if it is a reactive exception
        final Throwable unwrappedThrowable = Exceptions.unwrap(throwable);
        if (unwrappedThrowable instanceof CosmosException) {
            CosmosException cosmosException = (CosmosException) unwrappedThrowable;
            CosmosUtils.fillAndProcessCosmosExceptionDiagnostics(responseDiagnosticsProcessor, cosmosException);

            switch (cosmosException.getStatusCode()) {
                case Constants.CosmosExceptionStatusCodes.BADREQUEST:
                    cosmosAccessException = new CosmosBadRequestException(message, cosmosException);
                    break;
                case Constants.CosmosExceptionStatusCodes.CONFLICT:
                    cosmosAccessException = new CosmosConflictException(message, cosmosException);
                    break;
                case Constants.CosmosExceptionStatusCodes.FORBIDDEN:
                    cosmosAccessException = new CosmosForbiddenException(message, cosmosException);
                    break;
                case Constants.CosmosExceptionStatusCodes.GONE:
                    if (cosmosException.getSubStatusCode() == Constants.CosmosExceptionSubStatusCodes.NAME_CACHE_IS_STALE) {
                        cosmosAccessException = new CosmosInvalidPartitionException(message, cosmosException);
                    } else if (cosmosException.getSubStatusCode() == Constants.CosmosExceptionSubStatusCodes.COMPLETING_PARTITION_MIGRATION) {
                        cosmosAccessException = new CosmosPartitionIsMigratingException(message, cosmosException);
                    } else if (cosmosException.getSubStatusCode() == Constants.CosmosExceptionSubStatusCodes.PARTITION_KEY_RANGE_GONE) {
                        cosmosAccessException = new CosmosPartitionKeyRangeGoneException(message, cosmosException);
                    } else if (cosmosException.getSubStatusCode() == Constants.CosmosExceptionSubStatusCodes.COMPLETING_SPLIT_OR_MERGE) {
                        cosmosAccessException = new CosmosPartitionKeyRangeIsSplittingException(message, cosmosException);
                    } else {
                        cosmosAccessException = new CosmosGoneException(message, cosmosException);
                    }
                    break;
                case Constants.CosmosExceptionStatusCodes.INTERNAL_SERVER_ERROR:
                    cosmosAccessException = new CosmosInternalServerErrorException(message, cosmosException);
                    break;
                case Constants.CosmosExceptionStatusCodes.METHOD_NOT_ALLOWED:
                    cosmosAccessException = new CosmosMethodNotAllowedException(message, cosmosException);
                    break;
                case Constants.CosmosExceptionStatusCodes.NOTFOUND:
                    cosmosAccessException = new CosmosNotFoundException(message, cosmosException);
                    break;
                case Constants.CosmosExceptionStatusCodes.REQUEST_TIMEOUT:
                    if (((CosmosException) unwrappedThrowable).getSubStatusCode() == Constants.CosmosExceptionSubStatusCodes.CLIENT_OPERATION_TIMEOUT) {
                        cosmosAccessException = new CosmosOperationCancelledException(message, cosmosException);
                    } else {
                        cosmosAccessException = new CosmosRequestTimeoutException(message, cosmosException);
                    }
                    break;
                case Constants.CosmosExceptionStatusCodes.PRECONDITION_FAILED:
                    cosmosAccessException = new CosmosPreconditionFailedException(message, cosmosException);
                    break;
                case Constants.CosmosExceptionStatusCodes.REQUEST_ENTITY_TOO_LARGE:
                    cosmosAccessException = new CosmosRequestEntityTooLargeException(message, cosmosException);
                    break;
                case Constants.CosmosExceptionStatusCodes.TOO_MANY_REQUESTS:
                    cosmosAccessException = new CosmosRequestRateTooLargeException(message, cosmosException);
                    break;
                case Constants.CosmosExceptionStatusCodes.RETRY_WITH:
                    cosmosAccessException = new CosmosRetryWithException(message, cosmosException);
                    break;
                case Constants.CosmosExceptionStatusCodes.SERVICE_UNAVAILABLE:
                    cosmosAccessException = new CosmosServiceUnavailableException(message, cosmosException);
                    break;
                case Constants.CosmosExceptionStatusCodes.UNAUTHORIZED:
                    cosmosAccessException = new CosmosUnauthorizedException(message, cosmosException);
                    break;
                default:
                    cosmosAccessException = new CosmosAccessException(message, cosmosException);
                    break;
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
