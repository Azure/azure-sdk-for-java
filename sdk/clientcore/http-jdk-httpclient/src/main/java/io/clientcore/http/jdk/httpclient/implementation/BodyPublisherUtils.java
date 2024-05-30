// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.jdk.httpclient.implementation;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.core.util.binarydata.ByteArrayBinaryData;
import io.clientcore.core.util.binarydata.SerializableBinaryData;
import io.clientcore.core.util.binarydata.StringBinaryData;

import java.net.http.HttpRequest;
import java.time.Duration;

import static java.net.http.HttpRequest.BodyPublishers.fromPublisher;
import static java.net.http.HttpRequest.BodyPublishers.noBody;

/**
 * Utility class for BodyPublisher.
 */
public final class BodyPublisherUtils {
    private BodyPublisherUtils() {
    }

    /**
     * Creates BodyPublisher depending on underlying body content type.
     * If progress reporter is not null, configures it to track request body upload.
     *
     * @param request {@link io.clientcore.core.http.models.HttpRequest} instance
     * @param writeTimeout write timeout
     * @return the request BodyPublisher
     */
    public static HttpRequest.BodyPublisher toBodyPublisher(io.clientcore.core.http.models.HttpRequest request,
        Duration writeTimeout) {
        // TODO (alzimmer): azure-core was using Flux.timeout to handle write timeouts. The logic will need to be
        //  re-implemented to handle write timeouts in a similar manner.
        BinaryData body = request.getBody();
        if (body == null) {
            return noBody();
        }

        HttpRequest.BodyPublisher publisher;
        if (body instanceof ByteArrayBinaryData
            || body instanceof StringBinaryData
            || body instanceof SerializableBinaryData) {
            // String and serializable content also uses ofByteArray as ofString is just a wrapper for this,
            // so we might as well own the handling.
            byte[] bytes = body.toBytes();
            return toBodyPublisherWithLength(HttpRequest.BodyPublishers.ofByteArray(bytes),
                request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
        } else {
            return toBodyPublisherWithLength(HttpRequest.BodyPublishers.ofInputStream(body::toStream),
                request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
        }
    }

    /**
     * Creates BodyPublisher with content length
     *
     * @param publisher BodyPublisher representing request content that's not aware of content length
     * @return the request BodyPublisher
     */
    private static HttpRequest.BodyPublisher toBodyPublisherWithLength(HttpRequest.BodyPublisher publisher,
        String contentLength) {
        if (contentLength == null || contentLength.isEmpty()) {
            return publisher;
        } else {
            long contentLengthLong = Long.parseLong(contentLength);
            if (contentLengthLong < 1) {
                return noBody();
            } else {
                return fromPublisher(publisher, contentLengthLong);
            }
        }
    }
}
