// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.internal.HttpConstants;
import org.springframework.util.StringUtils;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

/**
 * To handle and throw a cosmosdb exception when access the database
 */
public class CosmosDBExceptionUtils {

    /**
     * To throw a CosmosDBAccessException
     *
     * @param message the detail message
     * @param throwable exception
     * @param <T> type class of Mono
     * @return Mono instance
     * @throws CosmosDBAccessException for operations on cosmosdb
     */
    public static <T> Mono<T> exceptionHandler(String message, Throwable throwable) {
        if (StringUtils.isEmpty(message)) {
            message = "Failed to access cosmosdb database";
        }
        //  Unwrap the exception in case if it is a reactive exception
        final Throwable unwrappedThrowable = Exceptions.unwrap(throwable);
        throw new CosmosDBAccessException(message, unwrappedThrowable);
    }

    /**
     * To find an exceptionHandler for a excetption and return empty Mono if the exception status code is not found
     *
     * @param message the detail message
     * @param throwable exception
     * @param <T> type class of Mono
     * @return Mono instance
     */
    public static <T> Mono<T> findAPIExceptionHandler(String message, Throwable throwable) {
        //  Unwrap the exception in case if it is a reactive exception
        final Throwable unwrappedThrowable = Exceptions.unwrap(throwable);
        if (unwrappedThrowable instanceof CosmosClientException) {
            final CosmosClientException cosmosClientException = (CosmosClientException) unwrappedThrowable;
            if (cosmosClientException.statusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                return Mono.empty();
            }
        }
        return exceptionHandler(message, unwrappedThrowable);
    }
}
