// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http;

import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.util.binarydata.BinaryData;

import java.util.function.Function;

/**
 * This class is used to access internal methods on {@link HttpResponse}.
 */
public final class HttpResponseAccessHelper {
    private static HttpResponseAccessor accessor;

    /**
     * Type defining the methods to access the internal methods on {@link HttpResponse}.
     */
    public interface HttpResponseAccessor {
        /**
         * Sets a {@link HttpResponse}'s value.
         *
         * @param httpResponse The {@link HttpResponse} to set the value of.
         * @param value The {@link Object value} to set.
         *
         * @return The modified {@link HttpResponse}.
         */
        HttpResponse<?> setValue(HttpResponse<?> httpResponse, Object value);

        /**
         * Sets a {@link HttpResponse}'s body.
         *
         * @param httpResponse The {@link HttpResponse} to set the body of.
         * @param body The {@link BinaryData body} to set.
         *
         * @return The modified {@link HttpResponse}.
         */
        HttpResponse<?> setBody(HttpResponse<?> httpResponse, BinaryData body);

        /**
         * Sets a function to deserialize the body of an {@link HttpResponse}.
         *
         * @param httpResponse The {@link HttpResponse} to set the body deserializer of.
         * @param bodyDeserializer The function that will deserialize the body of the response.
         *
         * @return The modified {@link HttpResponse}.
         */
        HttpResponse<?> setBodyDeserializer(HttpResponse<?> httpResponse,
                                            Function<BinaryData, Object> bodyDeserializer);
    }

    /**
     * Sets a {@link HttpResponse}'s {@link Object value}.
     *
     * @param httpResponse The {@link HttpResponse} to set the value of.
     * @param value The {@link Object value} to set.
     *
     * @return The modified {@link HttpResponse}.
     */
    public static HttpResponse<?> setValue(HttpResponse<?> httpResponse, Object value) {
        return accessor.setValue(httpResponse, value);
    }

    /**
     * Sets a {@link HttpResponse}'s {@link BinaryData body}.
     *
     * @param httpResponse The {@link HttpResponse} to set the body of.
     * @param body The {@link BinaryData body} to set.
     *
     * @return The modified {@link HttpResponse}.
     */
    public static HttpResponse<?> setBody(HttpResponse<?> httpResponse, BinaryData body) {
        return accessor.setBody(httpResponse, body);
    }

    /**
     * Sets a function to deserialize the body of an {@link HttpResponse}.
     *
     * @param httpResponse The {@link HttpResponse} to set the body deserializer of.
     * @param bodyDeserializer The function that will deserialize the body of the response.
     *
     * @return The modified {@link HttpResponse}.
     */
    public static HttpResponse<?> setBodyDeserializer(HttpResponse<?> httpResponse,
                                                      Function<BinaryData, Object> bodyDeserializer) {
        return accessor.setBodyDeserializer(httpResponse, bodyDeserializer);
    }

    /**
     * Sets the {@link HttpResponseAccessor}.
     *
     * @param accessor The {@link HttpResponseAccessor}.
     */
    public static void setAccessor(HttpResponseAccessor accessor) {
        HttpResponseAccessHelper.accessor = accessor;
    }

    private HttpResponseAccessHelper() {
    }
}
