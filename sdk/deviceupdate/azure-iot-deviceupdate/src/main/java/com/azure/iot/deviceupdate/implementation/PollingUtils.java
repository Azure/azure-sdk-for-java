// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.deviceupdate.implementation;

import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility class for handling polling responses.
 */
public final class PollingUtils {
    /**
     * Serialize a response to a {@link BinaryData}. If the response is already a {@link BinaryData}, return as is.
     *
     * @param response the response from an activation or polling call
     * @param serializer the object serializer to use
     * @return a Publisher emitting a {@link BinaryData} response
     */
    public static Mono<BinaryData> serializeResponse(Object response, ObjectSerializer serializer) {
        if (response instanceof BinaryData) {
            return Mono.just((BinaryData) response);
        } else {
            return BinaryData.fromObjectAsync(response, serializer);
        }
    }

    /**
     * Deserialize a {@link BinaryData} into a poll response type. If the poll response type is also a
     * {@link BinaryData}, return as is.
     *
     * @param binaryData the binary data to deserialize
     * @param serializer the object serializer to use
     * @param typeReference the {@link TypeReference} of the poll response type
     * @param <T> the generic parameter of the poll response type
     * @return a Publisher emitting the deserialized object
     */
    @SuppressWarnings("unchecked")
    public static <T> Mono<T> deserializeResponse(BinaryData binaryData, ObjectSerializer serializer,
                                                  TypeReference<T> typeReference) {
        if (typeReference.getJavaClass().isAssignableFrom(BinaryData.class)) {
            return Mono.just((T) binaryData);
        } else {
            return binaryData.toObjectAsync(typeReference, serializer);
        }
    }

    /**
     * Converts an object received from an activation or a polling call to another type requested by the user. If the
     * object type is identical to the type requested by the user, it's returned as is. If the response is null, an
     * empty publisher is returned.
     *
     * This is useful when an activation response needs to be converted to a polling response type, or a final result
     * type, if the long running operation completes upon activation.
     *
     * @param response the response from an activation or polling call
     * @param serializer the object serializer to use
     * @param typeReference the {@link TypeReference} of the user requested type
     * @param <T> the generic parameter of the user requested type
     * @return a Publisher emitting the converted object
     */
    @SuppressWarnings("unchecked")
    public static <T> Mono<T> convertResponse(Object response, ObjectSerializer serializer,
                                              TypeReference<T> typeReference) {
        if (response == null) {
            return Mono.empty();
        } else if (typeReference.getJavaClass().isAssignableFrom(BinaryData.class)) {
            return Mono.just((T) response);
        } else {
            return serializeResponse(response, serializer)
                .flatMap(binaryData -> deserializeResponse(binaryData, serializer, typeReference));
        }
    }

    /**
     * Create an absolute path from the endpoint if the 'path' is relative. Otherwise, return the 'path' as absolute
     * path.
     *
     * @param path an relative path or absolute path.
     * @param endpoint an endpoint to create the absolute path if the path is relative.
     * @return an absolute path.
     */
    public static String getAbsolutePath(String path, String endpoint, ClientLogger logger) {
        try {
            URI uri = new URI(path);
            if (!uri.isAbsolute()) {
                if (CoreUtils.isNullOrEmpty(endpoint)) {
                    throw logger.logExceptionAsError(new IllegalArgumentException(
                        "Relative path requires endpoint to be non-null or non-empty to create an absolute path."));
                }
                return endpoint + path;
            }
        } catch (URISyntaxException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'path' must be a valid URI.", ex));
        }
        return path;
    }

    private PollingUtils() {
    }
}
