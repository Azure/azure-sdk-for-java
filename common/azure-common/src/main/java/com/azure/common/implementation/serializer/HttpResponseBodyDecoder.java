/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.implementation.serializer;

import com.azure.common.implementation.Base64Url;
import com.azure.common.implementation.DateTimeRfc1123;
import com.azure.common.http.rest.RestException;
import com.azure.common.http.rest.RestResponse;
import com.azure.common.http.rest.RestResponseBase;
import com.azure.common.implementation.UnixTime;
import com.azure.common.annotations.ReturnValueWireType;
import com.azure.common.http.HttpMethod;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.rest.SimpleRestResponse;
import com.azure.common.implementation.util.FluxUtil;
import com.azure.common.implementation.util.TypeUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Decoder to decode body of HTTP response.
 */
final class HttpResponseBodyDecoder {
    /**
     * Decodes body of a http response.
     *
     * The content reading and decoding happens when caller subscribe to the returned {@code Mono<Object>},
     * if the response body is not decodable then {@code Mono.empty()} will be returned.
     *
     * @param httpResponse the response containing the body to be decoded
     * @param serializer the adapter to use for decoding
     * @param decodeData the necessary data required to decode a Http response
     * @return publisher that emits decoded response body upon subscription if body is decodable,
     * no emission if the body is not-decodable
     */
    static Mono<Object> decode(HttpResponse httpResponse, SerializerAdapter serializer, HttpResponseDecodeData decodeData) {
        ensureRequestSet(httpResponse);
        //
        return Mono.defer(() -> {
            if (isErrorStatus(httpResponse, decodeData)) {
                return httpResponse.bodyAsString()
                        .flatMap(bodyString -> {
                            try {
                                final Object decodedErrorEntity = deserializeBody(bodyString,
                                        decodeData.exceptionBodyType(),
                                        null,
                                        serializer,
                                        SerializerEncoding.fromHeaders(httpResponse.headers()));
                                return decodedErrorEntity == null ? Mono.empty() : Mono.just(decodedErrorEntity);
                            } catch (IOException | MalformedValueException ignored) {
                                // This translates in RestProxy as a RestException with no deserialized body.
                                // The response content will still be accessible via the .response() member.
                            }
                            return Mono.empty();
                        });
            } else if (httpResponse.request().httpMethod() == HttpMethod.HEAD) {
                // RFC: A response to a HEAD method should not have a body. If so, it must be ignored
                return Mono.empty();
            } else if (!isReturnTypeDecodable(decodeData)) {
                return Mono.empty();
            } else {
                return httpResponse.bodyAsString()
                        .flatMap(bodyString -> {
                            try {
                                final Object decodedSuccessEntity = deserializeBody(bodyString,
                                        extractEntityTypeFromReturnType(decodeData),
                                        decodeData.returnValueWireType(),
                                        serializer,
                                        SerializerEncoding.fromHeaders(httpResponse.headers()));
                                return decodedSuccessEntity == null ? Mono.empty() : Mono.just(decodedSuccessEntity);
                            } catch (MalformedValueException e) {
                                return Mono.error(new RestException("HTTP response has a malformed body.", httpResponse, e));
                            } catch (IOException e) {
                                return Mono.error(new RestException("Deserialization Failed.", httpResponse, e));
                            }
                        });
            }
        });
    }

    /**
     * @return true if the body is decodable, false otherwise
     */
    static boolean isDecodable(HttpResponse httpResponse, HttpResponseDecodeData decodeData) {
        ensureRequestSet(httpResponse);
        //
        if (isErrorStatus(httpResponse, decodeData)) {
            // For error cases we always try to decode the non-empty response body
            // either to a strongly typed exception model or to Object
            return true;
        } else if (httpResponse.request().httpMethod() == HttpMethod.HEAD) {
            // RFC: A response to a HEAD method should not have a body. If so, it must be ignored
            return false;
        } else {
            return isReturnTypeDecodable(decodeData);
        }
    }

    /**
     * @return the decoded type used to decode the response body, null if the body is not decodable.
     */
    static Type decodedType(HttpResponse httpResponse, HttpResponseDecodeData decodeData) {
        ensureRequestSet(httpResponse);
        //
        if (isErrorStatus(httpResponse, decodeData)) {
            // For error cases we always try to decode the non-empty response body
            // either to a strongly typed exception model or to Object
            return decodeData.exceptionBodyType();
        } else if (httpResponse.request().httpMethod() == HttpMethod.HEAD) {
            // RFC: A response to a HEAD method should not have a body. If so, it must be ignored
            return null;
        } else if (!isReturnTypeDecodable(decodeData)) {
            return null;
        } else {
            return extractEntityTypeFromReturnType(decodeData);
        }
    }

    /**
     * Checks the response status code is considered as error.
     *
     * @param httpResponse the response to check
     * @param decodeData the response metadata
     * @return true if the response status code is considered as error, false otherwise.
     */
    static boolean isErrorStatus(HttpResponse httpResponse, HttpResponseDecodeData decodeData) {
        final int[] expectedStatuses = decodeData.expectedStatusCodes();
        if (expectedStatuses != null) {
            return !contains(expectedStatuses, httpResponse.statusCode());
        } else {
            return httpResponse.statusCode() / 100 != 2;
        }
    }

    /**
     * Deserialize the given string value representing content of a REST API response.
     *
     * @param value the string value to deserialize
     * @param resultType the return type of the java proxy method
     * @param wireType value of optional {@link ReturnValueWireType} annotation present in java proxy method indicating
     *                 'entity type' (wireType) of REST API wire response body
     * @param encoding the encoding format of value
     * @return Deserialized object
     * @throws IOException
     */
    private static Object deserializeBody(String value, Type resultType, Type wireType, SerializerAdapter serializer, SerializerEncoding encoding) throws IOException {
        final Object result;

        if (wireType == null) {
            result = serializer.deserialize(value, resultType, encoding);
        } else {
            final Type wireResponseType = constructWireResponseType(resultType, wireType);
            final Object wireResponse = serializer.deserialize(value, wireResponseType, encoding);
            result = convertToResultType(wireResponse, resultType, wireType);
        }
        return result;
    }

    /**
     * Given:
     * (1). the {@code java.lang.reflect.Type} (resultType) of java proxy method return value
     * (2). and {@link ReturnValueWireType} annotation value indicating 'entity type' (wireType)
     *      of same REST API's wire response body
     * this method construct 'response body Type'.
     *
     * Note: When {@link ReturnValueWireType} annotation is applied to a proxy method, then the raw
     * HTTP response content will need to parsed using the derived 'response body Type' then converted
     * to actual {@code returnType}.
     *
     * @param resultType the {@code java.lang.reflect.Type} of java proxy method return value
     * @param wireType the {@code java.lang.reflect.Type} of entity in REST API response body
     * @return the {@code java.lang.reflect.Type} of REST API response body
     */
    private static Type constructWireResponseType(Type resultType, Type wireType) {
        Objects.requireNonNull(resultType);
        Objects.requireNonNull(wireType);
        //
        Type wireResponseType = resultType;

        if (resultType == byte[].class) {
            if (wireType == Base64Url.class) {
                wireResponseType = Base64Url.class;
            }
        } else if (resultType == OffsetDateTime.class) {
            if (wireType == DateTimeRfc1123.class) {
                wireResponseType = DateTimeRfc1123.class;
            } else if (wireType == UnixTime.class) {
                wireResponseType = UnixTime.class;
            }
        } else {
            if (TypeUtil.isTypeOrSubTypeOf(resultType, List.class)) {
                final Type resultElementType = TypeUtil.getTypeArgument(resultType);
                final Type wireResponseElementType = constructWireResponseType(resultElementType, wireType);

                wireResponseType = TypeUtil.createParameterizedType(
                        (Class<?>) ((ParameterizedType) resultType).getRawType(), wireResponseElementType);
            } else if (TypeUtil.isTypeOrSubTypeOf(resultType, Map.class) || TypeUtil.isTypeOrSubTypeOf(resultType, RestResponse.class)) {
                Type[] typeArguments = TypeUtil.getTypeArguments(resultType);
                final Type resultValueType = typeArguments[1];
                final Type wireResponseValueType = constructWireResponseType(resultValueType, wireType);

                wireResponseType = TypeUtil.createParameterizedType(
                        (Class<?>) ((ParameterizedType) resultType).getRawType(), typeArguments[0], wireResponseValueType);
            }
        }
        return wireResponseType;
    }

    /**
     * Converts the object {@code wireResponse} that was deserialized using 'response body Type'
     * (produced by {@code constructWireResponseType(args)} method) to resultType.
     *
     * @param wireResponse the object to convert
     * @param resultType the {@code java.lang.reflect.Type} to convert wireResponse to
     * @param wireType the {@code java.lang.reflect.Type} of the wireResponse
     * @return converted object
     */
    private static Object convertToResultType(Object wireResponse, Type resultType, Type wireType) {
        Object result = wireResponse;

        if (wireResponse != null) {
            if (resultType == byte[].class) {
                if (wireType == Base64Url.class) {
                    result = ((Base64Url) wireResponse).decodedBytes();
                }
            } else if (resultType == OffsetDateTime.class) {
                if (wireType == DateTimeRfc1123.class) {
                    result = ((DateTimeRfc1123) wireResponse).dateTime();
                } else if (wireType == UnixTime.class) {
                    result = ((UnixTime) wireResponse).dateTime();
                }
            } else {
                if (TypeUtil.isTypeOrSubTypeOf(resultType, List.class)) {
                    final Type resultElementType = TypeUtil.getTypeArgument(resultType);

                    final List<Object> wireResponseList = (List<Object>) wireResponse;

                    final int wireResponseListSize = wireResponseList.size();
                    for (int i = 0; i < wireResponseListSize; ++i) {
                        final Object wireResponseElement = wireResponseList.get(i);
                        final Object resultElement = convertToResultType(wireResponseElement, resultElementType, wireType);
                        if (wireResponseElement != resultElement) {
                            wireResponseList.set(i, resultElement);
                        }
                    }
                    //
                    result = wireResponseList;
                } else if (TypeUtil.isTypeOrSubTypeOf(resultType, Map.class)) {
                    final Type resultValueType = TypeUtil.getTypeArguments(resultType)[1];

                    final Map<String, Object> wireResponseMap = (Map<String, Object>) wireResponse;

                    final Set<String> wireResponseKeys = wireResponseMap.keySet();
                    for (String wireResponseKey : wireResponseKeys) {
                        final Object wireResponseValue = wireResponseMap.get(wireResponseKey);
                        final Object resultValue = convertToResultType(wireResponseValue, resultValueType, wireType);
                        if (wireResponseValue != resultValue) {
                            wireResponseMap.put(wireResponseKey, resultValue);
                        }
                    }
                    //
                    result = wireResponseMap;
                } else if (TypeUtil.isTypeOrSubTypeOf(resultType, RestResponseBase.class)) {
                    RestResponseBase<?, ?> restResponseBase = (RestResponseBase<?, ?>) wireResponse;
                    Object wireResponseBody = restResponseBase.body();

                    // TODO: anuchan - RestProxy is always in charge of creating RestResponseBase--so this doesn't seem right
                    Object resultBody = convertToResultType(wireResponseBody, TypeUtil.getTypeArguments(resultType)[1], wireType);
                    if (wireResponseBody != resultBody) {
                        result = new RestResponseBase<>(restResponseBase.request(), restResponseBase.statusCode(), restResponseBase.headers(), resultBody, restResponseBase.deserializedHeaders());
                    } else {
                        result = restResponseBase;
                    }
                } else if (TypeUtil.isTypeOrSubTypeOf(resultType, RestResponse.class)) {
                    RestResponse<?> restResponse = (RestResponse<?>) wireResponse;
                    Object wireResponseBody = restResponse.body();

                    // TODO: anuchan - RestProxy is always in charge of creating RestResponseBase--so this doesn't seem right
                    Object resultBody = convertToResultType(wireResponseBody, TypeUtil.getTypeArguments(resultType)[1], wireType);
                    if (wireResponseBody != resultBody) {
                        result = new SimpleRestResponse<>(restResponse.request(), restResponse.statusCode(), restResponse.headers(), resultBody);
                    } else {
                        result = restResponse;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get the {@link Type} of the REST API 'returned entity'.
     *
     * In the declaration of a java proxy method corresponding to the REST API, the 'returned entity' can be:
     *
     *      1. emission value of the reactor publisher returned by proxy method
     *
     *          e.g. {@code Mono<Foo> getFoo(args);}
     *               {@code Flux<Foo> getFoos(args);}
     *          where Foo is the REST API 'returned entity'.
     *
     *      2. OR content (body) of {@link RestResponseBase} emitted by the reactor publisher returned from proxy method
     *
     *          e.g. {@code Mono<RestResponseBase<headers, Foo>> getFoo(args);}
     *               {@code Flux<RestResponseBase<headers, Foo>> getFoos(args);}
     *          where Foo is the REST API return entity.
     *
     * @return the entity type.
     */
    private static Type extractEntityTypeFromReturnType(HttpResponseDecodeData decodeData) {
        Type token = decodeData.returnType();
        if (token != null) {
            if (TypeUtil.isTypeOrSubTypeOf(token, Mono.class)) {
                token = TypeUtil.getTypeArgument(token);
            } else if (TypeUtil.isTypeOrSubTypeOf(token, Flux.class)) {
                Type t = TypeUtil.getTypeArgument(token);
                try {
                    // TODO: anuchan - unwrap OperationStatus a different way
                    // Check for OperationStatus<?>
                    if (TypeUtil.isTypeOrSubTypeOf(t, Class.forName("com.azure.common.mgmt.OperationStatus"))) {
                        token = t;
                    }
                } catch (ClassNotFoundException ignored) {
                }
            }

            if (TypeUtil.isTypeOrSubTypeOf(token, RestResponse.class)) {
                token = TypeUtil.getRestResponseBodyType(token);
            }

            try {
                // TODO: anuchan - unwrap OperationStatus a different way
                if (TypeUtil.isTypeOrSubTypeOf(token, Class.forName("com.azure.common.mgmt.OperationStatus"))) {
                    // Get Type of 'T' from OperationStatus<T>
                    token = TypeUtil.getTypeArgument(token);
                }
            } catch (Exception ignored) {
            }
        }
        return token;
    }

    /**
     * Checks the return type represents a decodable type.
     *
     * @param decodeData the decode metadata
     * @return true if decodable, false otherwise.
     */
    private static boolean isReturnTypeDecodable(HttpResponseDecodeData decodeData) {
        Type returnType = decodeData.returnType();
        if (returnType == null) {
            return false;
        } else {
            return !FluxUtil.isFluxByteBuf(returnType)
                    && !(TypeUtil.isTypeOrSubTypeOf(returnType, Mono.class) && TypeUtil.isTypeOrSubTypeOf(TypeUtil.getTypeArgument(returnType), Void.class))
                    && !TypeUtil.isTypeOrSubTypeOf(returnType, byte[].class)
                    && !TypeUtil.isTypeOrSubTypeOf(returnType, Void.TYPE) && !TypeUtil.isTypeOrSubTypeOf(returnType, Void.class);
        }
    }

    /**
     * Checks an given value exists in an array.
     *
     * @param values array of ints
     * @param searchValue value to check for existence
     * @return true if value exists in the array, false otherwise
     */
    private static boolean contains(int[] values, int searchValue) {
        Objects.requireNonNull(values);
        for (int value : values) {
            if (searchValue == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ensure that request property and method is set in the response.
     *
     * @param httpResponse the response to validate
     * @return the validated response
     */
    private static HttpResponse ensureRequestSet(HttpResponse httpResponse) {
        Objects.requireNonNull(httpResponse.request());
        Objects.requireNonNull(httpResponse.request().httpMethod());
        return httpResponse;
    }
}

