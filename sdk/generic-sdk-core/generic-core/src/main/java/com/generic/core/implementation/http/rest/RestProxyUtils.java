// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.rest;

import com.generic.core.models.HeaderName;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.implementation.http.serializer.DefaultJsonSerializer;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Context;
import com.generic.core.models.InputStreamBinaryData;
import com.generic.core.http.models.RequestOptions;
import com.generic.core.util.ClientLogger;
import com.generic.core.util.serializer.ObjectSerializer;

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

        final long expectedLength = Long.parseLong(request.getHeaders().getValue(HeaderName.CONTENT_LENGTH));

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
     * Merges the Context with the Context provided with Options.
     *
     * @param context the Context to merge
     * @param options the options holding the context to merge with
     *
     * @return the merged context.
     */
    public static Context mergeRequestOptionsContext(Context context, RequestOptions options) {
        if (options == null) {
            return context;
        }

        Context optionsContext = options.getContext();

        if (optionsContext != null && optionsContext != Context.NONE) {
            context = Context.mergeContexts(context, optionsContext);
        }

        return context;
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
