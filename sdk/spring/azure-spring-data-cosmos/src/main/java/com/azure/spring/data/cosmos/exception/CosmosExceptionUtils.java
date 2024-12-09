// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.CosmosException;
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
                case 400 -> throw new CosmosBadRequestException(message, cosmosException);
                case 409 -> throw new CosmosConflictException(message, cosmosException);
                case 403 -> throw new CosmosForbiddenException(message, cosmosException);
                case 410 -> {
                    switch(cosmosException.getSubStatusCode()) {
                        case 1000 -> throw new CosmosInvalidPartitionException(message, cosmosException);
                        case 1008 -> throw new CosmosPartitionIsMigratingException(message, cosmosException);
                        case 1002 -> throw new CosmosPartitionKeyRangeGoneException(message, cosmosException);
                        case 1007 -> throw new CosmosPartitionKeyRangeIsSplittingException(message, cosmosException);
                        default -> throw new CosmosGoneException(message, cosmosException);
                    }
                }
                case 500 -> throw new CosmosInternalServerErrorException(message, cosmosException);
                case 405 -> throw new CosmosMethodNotAllowedException(message, cosmosException);
                case 404 -> {
                    if (((CosmosException) unwrappedThrowable).getSubStatusCode() == 1002) {

                    } else {
                        throw new CosmosNotFoundException(message, cosmosException);
                    }
                }
                case 408 -> {
                    if (((CosmosException) unwrappedThrowable).getSubStatusCode() == 20008) {
                        throw new CosmosOperationCancelledException(message, cosmosException);
                    }
                    throw new CosmosRequestTimeoutException(message, cosmosException);
                }
                case 412 -> throw new CosmosPreconditionFailedException(message, cosmosException);
                case 413 -> throw new CosmosRequestEntityTooLargeException(message, cosmosException);
                case 429 -> throw new CosmosRequestRateTooLargeException(message, cosmosException);
                case 449 -> throw new CosmosRetryWithException(message, cosmosException);
                case 503 -> throw new CosmosServiceUnavailableException(message, cosmosException);
                case 401 -> throw new CosmosUnauthorizedException(message, cosmosException);
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
