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
     * @throws CosmosException | CosmosAccessException for operations on cosmos db
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

            switch (unwrappedThrowable.getClass().getSimpleName()) {
                case "BadRequestException" -> throw new CosmosBadRequestException(message, unwrappedThrowable);
                case "ConflictException" -> throw new CosmosConflictException(message, unwrappedThrowable);
                case "ForbiddenException" -> throw new CosmosForbiddenException(message, unwrappedThrowable);
                case "GoneException" -> throw new CosmosGoneException(message, unwrappedThrowable);
                case "InternalServerErrorException" -> throw new CosmosInternalServerErrorException(message, unwrappedThrowable);
                case "InvalidPartitionException" -> throw new CosmosInvalidPartitionException(message, unwrappedThrowable);
                case "MethodNotAllowedException" -> throw new CosmosMethodNotAllowedException(message, unwrappedThrowable);
                case "NonStreamingOrderByBadRequestException" -> throw new CosmosNonStreamingOrderByBadRequestException(message, unwrappedThrowable);
                case "NotFoundException" -> throw new CosmosNotFoundException(message, unwrappedThrowable);
                case "OperationCancelledException" -> throw new CosmosOperationCancelledException(message, unwrappedThrowable);
                case "PartitionIsMigratingException" -> throw new CosmosPartitionIsMigratingException(message, unwrappedThrowable);
                case "PartitionKeyRangeGoneException" -> throw new CosmosPartitionKeyRangeGoneException(message, unwrappedThrowable);
                case "PartitionKeyRangeIsSplittingException" -> throw new CosmosPartitionKeyRangeIsSplittingException(message, unwrappedThrowable);
                case "PreconditionFailedException" -> throw new CosmosPreconditionFailedException(message, unwrappedThrowable);
                case "RequestEntityTooLargeException" -> throw new CosmosRequestEntityTooLargeException(message, unwrappedThrowable);
                case "RequestRateTooLargeException" -> throw new CosmosRequestRateTooLargeException(message, unwrappedThrowable);
                case "RequestTimeoutException" -> throw new CosmosRequestTimeoutException(message, unwrappedThrowable);
                case "RetryWithException" -> throw new CosmosRetryWithException(message, unwrappedThrowable);
                case "ServiceUnavailableException" -> throw new CosmosServiceUnavailableException(message, unwrappedThrowable);
                case "UnauthorizedException" -> throw new CosmosUnauthorizedException(message, unwrappedThrowable);
                default -> throw new CosmosAccessException(message, unwrappedThrowable);
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
