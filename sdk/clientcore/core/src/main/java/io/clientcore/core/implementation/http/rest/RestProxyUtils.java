// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.implementation.http.serializer.DefaultJsonSerializer;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.core.util.binarydata.InputStreamBinaryData;
import io.clientcore.core.util.serializer.ObjectSerializer;

import java.io.InputStream;

/**
 * Utility methods that aid processing in RestProxy.
 */
public final class RestProxyUtils {
    public static final ClientLogger LOGGER = new ClientLogger(RestProxyUtils.class);

    private RestProxyUtils() {
    }

    /**
     * Validates the Length of the input request matches its configured Content Length.
     *
     * @param request the input request to validate.
     *
     * @return the requests body as BinaryData on successful validation.
     */
    public static BinaryData validateLength(final HttpRequest request) {
        final BinaryData binaryData = request.getBody();

        if (binaryData == null) {
            return null;
        }

        final long expectedLength = Long.parseLong(request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));

        if (binaryData instanceof InputStreamBinaryData) {
            InputStream inputStream = binaryData.toStream();
            LengthValidatingInputStream lengthValidatingInputStream =
                new LengthValidatingInputStream(inputStream, expectedLength);

            return BinaryData.fromStream(lengthValidatingInputStream, expectedLength);
        } else {
            if (binaryData.getLength() == null) {
                byte[] b = binaryData.toBytes();

                validateLengthInternal(b.length, expectedLength);

                return BinaryData.fromBytes(b);
            } else {
                validateLengthInternal(binaryData.getLength(), expectedLength);

                return binaryData;
            }
        }
    }

    private static void validateLengthInternal(long length, long expectedLength) {
        if (length > expectedLength) {
            throw new IllegalStateException(bodyTooLarge(length, expectedLength));
        }

        if (length < expectedLength) {
            throw new IllegalStateException(bodyTooSmall(length, expectedLength));
        }
    }

    static String bodyTooLarge(long length, long expectedLength) {
        return "Request body emitted " + length + " bytes, more than the expected " + expectedLength + " bytes.";
    }

    static String bodyTooSmall(long length, long expectedLength) {
        return "Request body emitted " + length + " bytes, less than the expected " + expectedLength + " bytes.";
    }

    /**
     * Create an instance of the default serializer.
     *
     * @return the default serializer
     */
    public static ObjectSerializer createDefaultSerializer() {
        return new DefaultJsonSerializer();
    }
}
