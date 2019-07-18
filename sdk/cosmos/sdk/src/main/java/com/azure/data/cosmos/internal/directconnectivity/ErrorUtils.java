/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
        Mono<String> responseAsString = ResponseUtils.toString(responseMessage.body());
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
