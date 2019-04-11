// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.implementation.serializer;

import com.azure.common.annotations.HeaderCollection;
import com.azure.common.exception.ServiceRequestException;
import com.azure.common.http.HttpHeader;
import com.azure.common.http.HttpHeaders;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.rest.ResponseBase;
import com.azure.common.implementation.util.TypeUtil;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Decoder to decode header of HTTP response.
 */
final class HttpResponseHeaderDecoder {
    /**
     * Decode headers of the http response.
     *
     * The decoding happens when caller subscribed to the returned {@code Mono<Object>},
     * if the response header is not decodable then {@code Mono.empty()} will be returned.
     *
     * @param httpResponse the response containing the headers to be decoded
     * @param serializer the adapter to use for decoding
     * @param decodeData the necessary data required to decode a Http response
     * @return publisher that emits decoded response header upon subscription if header is decodable,
     * no emission if the header is not-decodable
     */
    static Mono<Object> decode(HttpResponse httpResponse, SerializerAdapter serializer, HttpResponseDecodeData decodeData) {
        Type headerType = decodeData.headersType();
        if (headerType == null) {
            return Mono.empty();
        } else {
            return Mono.defer(() -> {
                try {
                    return Mono.just(deserializeHeaders(httpResponse.headers(), serializer, decodeData));
                } catch (IOException e) {
                    return Mono.error(new ServiceRequestException("HTTP response has malformed headers", httpResponse, e));
                }
            });
        }
    }

    /**
     * Deserialize the provided headers returned from a REST API to an entity instance declared as
     * the model to hold 'Matching' headers.
     *
     * 'Matching' headers are the REST API returned headers those with:
     *      1. header names same as name of a properties in the entity.
     *      2. header names start with value of {@link HeaderCollection} annotation applied to the properties in the entity.
     *
     * When needed, the 'header entity' types must be declared as first generic argument of {@link ResponseBase} returned
     * by java proxy method corresponding to the REST API.
     * e.g.
     * {@code Mono<RestResponseBase<FooMetadataHeaders, Void>> getMetadata(args);}
     * {@code
     *      class FooMetadataHeaders {
     *          String name;
     *          @HeaderCollection("header-collection-prefix-")
     *          Map<String,String> headerCollection;
     *      }
     * }
     *
     * in the case of above example, this method produces an instance of FooMetadataHeaders from provided {@headers}.
     *
     * @param headers the REST API returned headers
     * @return instance of header entity type created based on provided {@headers}, if header entity model does
     * not exists then return null
     * @throws IOException
     */
    private static Object deserializeHeaders(HttpHeaders headers, SerializerAdapter serializer, HttpResponseDecodeData decodeData) throws IOException {
        final Type deserializedHeadersType = decodeData.headersType();
        if (deserializedHeadersType == null) {
            return null;
        } else {
            final String headersJsonString = serializer.serialize(headers, SerializerEncoding.JSON);
            Object deserializedHeaders = serializer.deserialize(headersJsonString, deserializedHeadersType, SerializerEncoding.JSON);

            final Class<?> deserializedHeadersClass = TypeUtil.getRawClass(deserializedHeadersType);
            final Field[] declaredFields = deserializedHeadersClass.getDeclaredFields();
            for (final Field declaredField : declaredFields) {
                if (declaredField.isAnnotationPresent(HeaderCollection.class)) {
                    final Type declaredFieldType = declaredField.getGenericType();
                    if (TypeUtil.isTypeOrSubTypeOf(declaredField.getType(), Map.class)) {
                        final Type[] mapTypeArguments = TypeUtil.getTypeArguments(declaredFieldType);
                        if (mapTypeArguments.length == 2 && mapTypeArguments[0] == String.class && mapTypeArguments[1] == String.class) {
                            final HeaderCollection headerCollectionAnnotation = declaredField.getAnnotation(HeaderCollection.class);
                            final String headerCollectionPrefix = headerCollectionAnnotation.value().toLowerCase(Locale.ROOT);
                            final int headerCollectionPrefixLength = headerCollectionPrefix.length();
                            if (headerCollectionPrefixLength > 0) {
                                final Map<String, String> headerCollection = new HashMap<>();
                                for (final HttpHeader header : headers) {
                                    final String headerName = header.name();
                                    if (headerName.toLowerCase(Locale.ROOT).startsWith(headerCollectionPrefix)) {
                                        headerCollection.put(headerName.substring(headerCollectionPrefixLength), header.value());
                                    }
                                }

                                final boolean declaredFieldAccessibleBackup = declaredField.isAccessible();
                                try {
                                    if (!declaredFieldAccessibleBackup) {
                                        declaredField.setAccessible(true);
                                    }
                                    declaredField.set(deserializedHeaders, headerCollection);
                                } catch (IllegalAccessException ignored) {
                                } finally {
                                    if (!declaredFieldAccessibleBackup) {
                                        declaredField.setAccessible(declaredFieldAccessibleBackup);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return deserializedHeaders;
        }
    }
}
