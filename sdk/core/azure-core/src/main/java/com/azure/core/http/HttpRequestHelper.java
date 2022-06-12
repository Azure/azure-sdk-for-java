// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.ByteArrayContent;
import com.azure.core.implementation.util.FileContent;
import com.azure.core.implementation.util.FluxByteBufferContent;
import com.azure.core.implementation.util.InputStreamContent;
import com.azure.core.implementation.util.SerializableContent;
import com.azure.core.implementation.util.StringContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Utilities to handle HttpRequest.
 */
public final class HttpRequestHelper {

    private static final ClientLogger LOGGER = new ClientLogger(HttpRequestHelper.class);

    private HttpRequestHelper() {
    }

    /**
     * Prepares request for retransmission. E.g., for retries.
     * @param request the original request.
     * @return Request prepared for retransmission.
     */
    public static HttpRequest prepareForRetransmission(HttpRequest request) {
        HttpRequest requestCopy = request.copy();
        BinaryData requestBody = request.getBodyAsBinaryData();
        requestCopy.setBody(prepareBodyForRetransmission(requestBody, request));
        return requestCopy;
    }

    private static BinaryData prepareBodyForRetransmission(BinaryData requestBody, HttpRequest request) {
        if (requestBody == null) {
            return null;
        }

        BinaryDataContent content = BinaryDataHelper.getContent(requestBody);

        if (content instanceof ByteArrayContent
            || content instanceof SerializableContent
            || content instanceof StringContent
            || content instanceof FileContent) {
            return requestBody;
        } else if (content instanceof InputStreamContent) {
            InputStream inputStream = content.toStream();
            Long contentLength = getRequestContentLength(content, request.getHeaders());
            if (contentLength != null && contentLength < Integer.MAX_VALUE) {
                if (inputStream.markSupported()) {
                    try {
                        inputStream.reset();
                    } catch (IOException e) {
                        // ignore.
                    }
                    inputStream.mark(contentLength.intValue());
                    return requestBody;
                } else {
                    // If stream is not seekable buffer body and set it in both derived and original request.
                    byte[] bufferedContent = content.toBytes();
                    BinaryData bufferedBinaryData = BinaryData.fromBytes(bufferedContent);
                    request.setBody(bufferedBinaryData);
                    return bufferedBinaryData;
                }
            } else {
                throw LOGGER.logExceptionAsError(new IllegalStateException("Non retriable request body"));
            }
        } else {
            /*
             Clone the original request to ensure that each try starts with the original (unmutated) request. We cannot
             simply call httpRequest.buffer() because although the body will start emitting from the beginning of the
             stream, the buffers that were emitted will have already been consumed (their position set to their limit),
             so it is not a true reset. By adding the map function, we ensure that anything which consumes the
             ByteBuffers downstream will only actually consume a duplicate so the original is preserved. This only
             duplicates the ByteBuffer object, not the underlying data.
             */
            Flux<ByteBuffer> bufferedBody = requestBody.toFluxByteBuffer().map(ByteBuffer::duplicate);
            return BinaryDataHelper.createBinaryData(new FluxByteBufferContent(bufferedBody, requestBody.getLength()));
        }
    }

    private static Long getRequestContentLength(BinaryDataContent content, HttpHeaders headers) {
        Long contentLength = content.getLength();
        if (contentLength == null) {
            String contentLengthHeaderValue = headers.getValue("Content-Length");
            if (contentLengthHeaderValue != null) {
                contentLength = Long.valueOf(contentLengthHeaderValue);
            }
        }
        return contentLength;
    }
}
