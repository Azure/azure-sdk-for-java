// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.exception;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.internal.HttpConstants;
import org.springframework.util.StringUtils;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

public class CosmosDBExceptionUtils {

    public static <T> Mono<T> exceptionHandler(String message, Throwable throwable) {
        if (StringUtils.isEmpty(message)) {
            message = "Failed to access cosmosdb database";
        }
        //  Unwrap the exception in case if it is a reactive exception
        final Throwable unwrappedThrowable = Exceptions.unwrap(throwable);
        throw new CosmosDBAccessException(message, unwrappedThrowable);
    }

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
