// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.rest;

import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.RequestOptions;
import com.generic.core.implementation.http.serializer.DefaultJsonSerializer;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Context;
import com.generic.core.models.HeaderName;
import com.generic.core.models.InputStreamBinaryData;
import com.generic.core.util.ClientLogger;
import com.generic.core.util.serializer.ObjectSerializer;

import java.io.InputStream;
import java.util.Objects;

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
     * Merges data from a {@link Context} provided with {@link RequestOptions} into another {@link Context}.
     *
     * @param context The {@link Context} to merge data into.
     * @param options The {@link RequestOptions} holding the {@link Context} to merge data from.
     *
     * @return The merged {@link Context}.
     */
    public static Context mergeRequestOptionsContext(Context context, RequestOptions options) {
        if (options == null) {
            return context;
        }

        Context optionsContext = options.getContext();

        if (optionsContext != null && optionsContext != Context.NONE) {
            context = mergeContexts(context, optionsContext);
        }

        return context;
    }

    /**
     * Merges data from two {@link Context} instances.
     *
     * @param into The {@link Context} to merge data into.
     * @param from The {@link Context} to merge data from.
     *
     * @return The merged {@link Context}.
     */
    private static Context mergeContexts(Context into, Context from) {
        Objects.requireNonNull(into, "'into' cannot be null.");
        Objects.requireNonNull(from, "'from' cannot be null.");

        // If the 'into' Context is the NONE Context just return the 'from' Context.
        // This is safe as Context is immutable and prevents needing to create any new Contexts and temporary arrays.
        if (into == Context.NONE) {
            return from;
        }

        // Same goes the other way, where if the 'from' Context is the NONE Context just return the 'into' Context.
        if (from == Context.NONE) {
            return into;
        }

        Context[] contextChain = getContextChain(from);

        Context returnContext = into;

        for (Context toAdd : contextChain) {
            if (toAdd != null) {
                returnContext = returnContext.addData(toAdd.getKey(), toAdd.getValue());
            }
        }

        return returnContext;
    }

    /**
     * Gets the {@link Context Contexts} in the chain of {@link Context Contexts} that this {@link Context} is the tail
     * of.
     *
     * @return The {@link Context Contexts}, in oldest-to-newest order, in the chain of {@link Context Contexts} that
     * this {@link Context} is the tail of.
     */
    private static Context[] getContextChain(Context context) {
        Context[] chain = new Context[context.getContextCount()];

        int chainPosition = context.getContextCount() - 1;

        for (Context pointer = context; pointer != null; pointer = pointer.getParent()) {
            chain[chainPosition--] = pointer;

            // If the contextCount is 1 that means the next parent Context is the NONE Context.
            // Break out of the loop to prevent a meaningless check.
            if (pointer.getContextCount() == 1) {
                break;
            }
        }

        return chain;
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
