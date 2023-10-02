// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.polling.implementation;

import com.typespec.core.implementation.TypeUtil;
import com.typespec.core.util.BinaryData;
import com.typespec.core.util.CoreUtils;
import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.serializer.ObjectSerializer;
import com.typespec.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility class for handling polling responses.
 */
public final class PollingUtils {
    private static final String FORWARD_SLASH = "/";
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
     * Serialize a response to a {@link BinaryData}. If the response is already a {@link BinaryData}, return as is.
     *
     * @param response the response from an activation or polling call
     * @param serializer the object serializer to use
     * @return a {@link BinaryData} response
     */
    public static BinaryData serializeResponseSync(Object response, ObjectSerializer serializer) {
        if (response instanceof BinaryData) {
            return (BinaryData) response;
        } else {
            return BinaryData.fromObject(response, serializer);
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
        if (TypeUtil.isTypeOrSubTypeOf(BinaryData.class, typeReference.getJavaType())) {
            return Mono.just((T) binaryData);
        } else {
            return binaryData.toObjectAsync(typeReference, serializer);
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
     * @return the deserialized object
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserializeResponseSync(BinaryData binaryData, ObjectSerializer serializer,
        TypeReference<T> typeReference) {
        if (TypeUtil.isTypeOrSubTypeOf(BinaryData.class, typeReference.getJavaType())) {
            return (T) binaryData;
        } else {
            return binaryData.toObject(typeReference, serializer);
        }
    }

    /**
     * Converts an object received from an activation or a polling call to another type requested by the user. If the
     * object type is identical to the type requested by the user, it's returned as is. If the response is null, an
     * empty publisher is returned.
     * <p>
     * This is useful when an activation response needs to be converted to a polling response type, or a final result
     * type, if the long-running operation completes upon activation.
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
        } else if (TypeUtil.isTypeOrSubTypeOf(response.getClass(), typeReference.getJavaType())) {
            return Mono.just((T) response);
        } else {
            return serializeResponse(response, serializer)
                .flatMap(binaryData -> deserializeResponse(binaryData, serializer, typeReference));
        }
    }

    /**
     * Converts an object received from an activation or a polling call to another type requested by the user. If the
     * object type is identical to the type requested by the user, it's returned as is. If the response is null, null
     * is returned.
     * <p>
     * This is useful when an activation response needs to be converted to a polling response type, or a final result
     * type, if the long-running operation completes upon activation.
     *
     * @param response the response from an activation or polling call
     * @param serializer the object serializer to use
     * @param typeReference the {@link TypeReference} of the user requested type
     * @param <T> the generic parameter of the user requested type
     * @return the converted object
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertResponseSync(Object response, ObjectSerializer serializer,
        TypeReference<T> typeReference) {
        if (response == null) {
            return null;
        } else if (TypeUtil.isTypeOrSubTypeOf(response.getClass(), typeReference.getJavaType())) {
            return (T) response;
        } else {
            return deserializeResponseSync(serializeResponseSync(response, serializer), serializer, typeReference);
        }
    }

    /**
     * Create an absolute path from the endpoint if the 'path' is relative. Otherwise, return the 'path' as absolute
     * path.
     *
     * @param path a relative path or absolute path.
     * @param endpoint an endpoint to create the absolute path if the path is relative.
     * @return an absolute path.
     */
    public static String getAbsolutePath(String path, String endpoint, ClientLogger logger) {
        try {
            URI uri = new URI(path);
            if (!uri.isAbsolute()) {
                if (CoreUtils.isNullOrEmpty(endpoint)) {
                    throw logger.logExceptionAsError(new IllegalArgumentException(
                        "Relative path requires endpoint to be non-null and non-empty to create an absolute path."));
                }

                if (endpoint.endsWith(FORWARD_SLASH) && path.startsWith(FORWARD_SLASH)) {
                    return endpoint + path.substring(1);
                } else if (!endpoint.endsWith(FORWARD_SLASH) && !path.startsWith(FORWARD_SLASH)) {
                    return endpoint + FORWARD_SLASH + path;
                } else {
                    return endpoint + path;
                }
            }
        } catch (URISyntaxException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'path' must be a valid URI.", ex));
        }
        return path;
    }

    private PollingUtils() {
    }
}
