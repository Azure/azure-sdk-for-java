// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.http.HttpRequest;
import com.azure.data.cosmos.internal.http.HttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.URI;

public class ErrorUtils {
    private static final Logger logger = LoggerFactory.getLogger(ErrorUtils.class);

    static Mono<String> getErrorResponseAsync(HttpResponse responseMessage, HttpRequest request) {
        Mono<String> responseAsString = responseMessage.bodyAsString();
        if (request.httpMethod() == HttpMethod.DELETE) {
            return Mono.just(StringUtils.EMPTY);
        }
        return responseAsString;
    }

    static void logGoneException(URI physicalAddress, String activityId) {
        logger.trace("Listener not found. Store Physical Address {} ActivityId {}",
                physicalAddress, activityId);
    }

    protected static void logGoneException(String physicalAddress, String activityId) {
        logger.trace("Listener not found. Store Physical Address {} ActivityId {}",
                physicalAddress, activityId);
    }

    static void logException(URI physicalAddress, String activityId) {
        logger.trace("Store Request Failed. Store Physical Address {} ActivityId {}",
                physicalAddress, activityId);
    }

    protected static void logException(String physicalAddress, String activityId) {
        logger.trace("Store Request Failed. Store Physical Address {} ActivityId {}",
                physicalAddress, activityId);
    }
}
