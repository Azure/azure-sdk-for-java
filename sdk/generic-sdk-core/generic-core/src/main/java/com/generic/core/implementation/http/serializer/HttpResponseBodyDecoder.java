// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.serializer;

import com.generic.core.exception.HttpResponseException;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.util.logging.ClientLogger;
import com.generic.core.util.serializer.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Decoder to decode body of HTTP response.
 */
public final class HttpResponseBodyDecoder {
    // HttpResponseBodyDecoder is a commonly used class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(HttpResponseBodyDecoder.class);

    /**
     * Decodes the body of an {@link HttpResponse} into the type returned by the called API.
     * <p>
     * If the response body isn't able to be decoded null will be returned.
     *
     * @param body The response body retrieved from the {@code httpResponse} to decode.
     * @param httpResponse The {@link HttpResponse}.
     * @param serializer The {@link JsonSerializer} that performs decoding.
     * @param decodeData The API method metadata used during decoding of the response.
     * @return The decoded response body, or null if the body wasn't able to be decoded.
     * @throws HttpResponseException If the body fails to decode.
     */
    static Object decodeByteArray(byte[] body, HttpResponse httpResponse, JsonSerializer serializer,
        HttpResponseDecodeData decodeData) {
        ensureRequestSet(httpResponse);

        // Check for HEAD HTTP method first as it's possible for the underlying HttpClient to treat a non-existent
        // response body as an empty byte array.
        if (httpResponse.getRequest().getHttpMethod() == HttpMethod.HEAD) {
            // RFC: A response to a HEAD method should not have a body. If so, it must be ignored
            return null;
        }
        return null;
    }

    /**
     * @return the decoded type used to decode the response body, null if the body is not decodable.
     */
    static Type decodedType(final HttpResponse httpResponse, final HttpResponseDecodeData decodeData) {
        ensureRequestSet(httpResponse);

        if (httpResponse.getRequest().getHttpMethod() == HttpMethod.HEAD) {
            // RFC: A response to a HEAD method should not have a body. If so, it must be ignored
            return null;
        } else if (isErrorStatus(httpResponse.getStatusCode(), decodeData)) {
            // For error cases we always try to decode the non-empty response body
            // either to a strongly typed exception model or to Object
            return decodeData.getUnexpectedException(httpResponse.getStatusCode()).getExceptionBodyType();
        }
        return null;
    }

    /**
     * Checks the response status code is considered as error.
     *
     * @param statusCode The status code from the response.
     * @param decodeData Metadata about the API response.
     * @return true if the response status code is considered as error, false otherwise.
     */
    static boolean isErrorStatus(int statusCode, HttpResponseDecodeData decodeData) {
        return !decodeData.isExpectedResponseStatusCode(statusCode);
    }

    /**
     * Ensure that request property and method is set in the response.
     *
     * @param httpResponse the response to validate
     */
    private static void ensureRequestSet(HttpResponse httpResponse) {
        Objects.requireNonNull(httpResponse.getRequest());
        Objects.requireNonNull(httpResponse.getRequest().getHttpMethod());
    }
}

