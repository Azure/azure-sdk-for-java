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

package com.microsoft.azure.cosmosdb.internal.directconnectivity;

import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.Error;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.client.HttpResponseHeaders;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Single;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class ResponseUtils {
    private final static int INITIAL_RESPONSE_BUFFER_SIZE = 1024;
    private final static Logger logger = LoggerFactory.getLogger(ResponseUtils.class);

    public static Observable<String> toString(Observable<ByteBuf> contentObservable) {
        return contentObservable
                .reduce(
                        new ByteArrayOutputStream(INITIAL_RESPONSE_BUFFER_SIZE),
                        (out, bb) -> {
                            try {
                                bb.readBytes(out, bb.readableBytes());
                                return out;
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                .map(out -> {
                    return new String(out.toByteArray(), StandardCharsets.UTF_8);
                });
    }

    public static Single<StoreResponse> toStoreResponse(HttpClientResponse<ByteBuf> clientResponse) {

        HttpResponseHeaders httpResponseHeaders = clientResponse.getHeaders();
        HttpResponseStatus httpResponseStatus = clientResponse.getStatus();

        Observable<String> contentObservable;

        if (clientResponse.getContent() == null) {
            // for delete we don't expect any body
            contentObservable = Observable.just(null);
        } else {
            // transforms the observable<ByteBuf> to Observable<InputStream>
            contentObservable = toString(clientResponse.getContent());
        }

        Observable<StoreResponse> storeResponseObservable = contentObservable
                .flatMap(content -> {
                    try {
                        // transforms to Observable<StoreResponse>
                        StoreResponse rsp = new StoreResponse(httpResponseStatus.code(), HttpUtils.unescape(httpResponseHeaders.entries()), content);
                        return Observable.just(rsp);
                    } catch (Exception e) {
                        return Observable.error(e);
                    }
                });

        return storeResponseObservable.toSingle();
    }

    private static void validateOrThrow(RxDocumentServiceRequest request, HttpResponseStatus status, HttpResponseHeaders headers, String body,
                                        InputStream inputStream) throws DocumentClientException {

        int statusCode = status.code();

        if (statusCode >= HttpConstants.StatusCodes.MINIMUM_STATUSCODE_AS_ERROR_GATEWAY) {
            if (body == null && inputStream != null) {
                try {
                    body = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    logger.error("Failed to get content from the http response", e);
                    throw new IllegalStateException("Failed to get content from the http response", e);
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
            }

            String statusCodeString = status.reasonPhrase() != null
                    ? status.reasonPhrase().replace(" ", "")
                    : "";
            Error error = null;
            error = (body != null) ? new Error(body) : new Error();
            error = new Error(statusCodeString,
                    String.format("%s, StatusCode: %s", error.getMessage(), statusCodeString),
                    error.getPartitionedQueryExecutionInfo());

            throw new DocumentClientException(statusCode, error, HttpUtils.asMap(headers));
        }
    }
}
