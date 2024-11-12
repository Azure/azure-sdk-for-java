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

            // Figure out what type of CosmosException it is and create a CosmosException to wrap and return it
            // Maybe extend CosmosAccessException to create subclasses
            // Figure out type by StatusCode
            // Add comments to CosmosException and HttpConstants mentioning usage in spring and need to update
            switch (unwrappedThrowable.getClass().getSimpleName()) {
                case "BadRequestException" -> throw new CosmosBadRequestException(message, cosmosException, unwrappedThrowable);
                case "ConflictException" -> throw new CosmosConflictException(message, null, null, unwrappedThrowable);
                case "ForbiddenException" -> throw new CosmosForbiddenException(message, null, null, unwrappedThrowable);
                case "GoneException" -> throw new CosmosGoneException(message, cosmosException, null, null, HttpConstants.StatusCodes.GONE, unwrappedThrowable);
                case "InternalServerErrorException" -> throw new CosmosInternalServerErrorException(message, cosmosException, HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, unwrappedThrowable);
                case "InvalidPartitionException" -> throw new CosmosInvalidPartitionException(message, unwrappedThrowable);
                case "MethodNotAllowedException" -> throw new CosmosMethodNotAllowedException(message, cosmosException, null, null, unwrappedThrowable);
                case "NonStreamingOrderByBadRequestException" -> throw new CosmosNonStreamingOrderByBadRequestException(HttpConstants.StatusCodes.BADREQUEST, message, unwrappedThrowable);
                case "NotFoundException" -> throw new CosmosNotFoundException(message, unwrappedThrowable);
                case "PartitionIsMigratingException" -> throw new CosmosPartitionIsMigratingException(message, null, null, unwrappedThrowable);
                case "PartitionKeyRangeGoneException" -> throw new CosmosPartitionKeyRangeGoneException(message, unwrappedThrowable);
                case "PartitionKeyRangeIsSplittingException" -> throw new CosmosPartitionKeyRangeIsSplittingException(message, null, null, unwrappedThrowable);
                case "PreconditionFailedException" -> throw new CosmosPreconditionFailedException(message, null, null, unwrappedThrowable);
                case "RequestEntityTooLargeException" -> throw new CosmosRequestEntityTooLargeException(message, null, null, unwrappedThrowable);
                case "RequestRateTooLargeException" -> throw new CosmosRequestRateTooLargeException(message, null, null, unwrappedThrowable);
                case "RequestTimeoutException" -> throw new CosmosRequestTimeoutException(message, null, HttpConstants.StatusCodes.REQUEST_TIMEOUT, unwrappedThrowable);
                case "RetryWithException" -> throw new CosmosRetryWithException(message, null, null, unwrappedThrowable);
                case "ServiceUnavailableException" -> throw new CosmosServiceUnavailableException(message, cosmosException, null, null, HttpConstants.StatusCodes.SERVICE_UNAVAILABLE, unwrappedThrowable);
                case "UnauthorizedException" -> throw new CosmosUnauthorizedException(message, null, null, unwrappedThrowable);
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
