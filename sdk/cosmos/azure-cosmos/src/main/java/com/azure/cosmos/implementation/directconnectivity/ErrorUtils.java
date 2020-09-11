// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.URI;

public class ErrorUtils {
    private static final Logger logger = LoggerFactory.getLogger(ErrorUtils.class);

    static Mono<String> getErrorResponseAsync(HttpResponse responseMessage, HttpRequest request) {
        return responseMessage.bodyAsString().switchIfEmpty(Mono.just(StringUtils.EMPTY));
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
