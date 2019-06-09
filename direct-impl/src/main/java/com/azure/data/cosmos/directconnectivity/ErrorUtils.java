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

package com.azure.data.cosmos.directconnectivity;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Single;

import java.net.URI;

public class ErrorUtils {
    private static final Logger logger = LoggerFactory.getLogger(TransportClient.class);

    protected static Single<String> getErrorResponseAsync(HttpClientResponse<ByteBuf> responseMessage) {

        if (responseMessage.getContent() == null) {
            return Single.just(StringUtils.EMPTY);
        }

        return getErrorFromStream(responseMessage.getContent());
    }

    protected static Single<String> getErrorFromStream(Observable<ByteBuf> stream) {
        return ResponseUtils.toString(stream).toSingle();
    }

    protected static void logGoneException(URI physicalAddress, String activityId) {
        logger.trace("Listener not found. Store Physical Address {} ActivityId {}",
                physicalAddress, activityId);
    }

    protected static void logGoneException(String physicalAddress, String activityId) {
        logger.trace("Listener not found. Store Physical Address {} ActivityId {}",
                physicalAddress, activityId);
    }

    protected static void logException(URI physicalAddress, String activityId) {
        logger.trace("Store Request Failed. Store Physical Address {} ActivityId {}",
                physicalAddress, activityId);
    }

    protected static void logException(String physicalAddress, String activityId) {
        logger.trace("Store Request Failed. Store Physical Address {} ActivityId {}",
                physicalAddress, activityId);
    }
}
