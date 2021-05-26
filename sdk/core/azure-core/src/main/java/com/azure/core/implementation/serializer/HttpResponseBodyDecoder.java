// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.serializer;

import com.azure.core.annotation.ReturnValueWireType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.implementation.UnixTime;
import com.azure.core.util.Base64Url;
import com.azure.core.util.BinaryData;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static com.azure.core.implementation.TypeUtil.getRawClass;
import static com.azure.core.implementation.TypeUtil.typeImplementsInterface;

/**
 * Decoder to decode body of HTTP response.
 */
public final class HttpResponseBodyDecoder {
    private static final Map<Type, Boolean> RETURN_TYPE_DECODEABLE_MAP = new ConcurrentHashMap<>();

    // TODO (jogiles) JavaDoc (even though it is non-public
    static Mono<Object> decode(final String body,
        final HttpResponse httpResponse,
        final SerializerAdapter serializer,
        final HttpResponseDecodeData decodeData) {
        return decodeByteArray(body == null ? null : body.getBytes(StandardCharsets.UTF_8),
            httpResponse, serializer, decodeData);
    }

    /**
     * Decodes body of a http response.
     *
     * The content reading and decoding happens when caller subscribe to the returned {@code Mono<Object>}, if the
     * response body is not decodable then {@code Mono.empty()} will be returned.
     *
     * @param body the response body to decode, null for this parameter indicate read body from {@code httpResponse}
     * parameter and decode it.
     * @param httpResponse the response containing the body to be decoded
     * @param serializer the adapter to use for decoding
     * @param decodeData the necessary data required to decode a Http response
     * @return publisher that emits decoded response body upon subscription if body is decodable, no emission if the
     * body is not-decodable
     */
    static Mono<Object> decodeByteArray(final byte[] body,
        final HttpResponse httpResponse,
        final SerializerAdapter serializer,
        final HttpResponseDecodeData decodeData) {
        ensureRequestSet(httpResponse);
        final ClientLogger logger = new ClientLogger(HttpResponseBodyDecoder.class);

        return Mono.defer(() -> {
            if (isErrorStatus(httpResponse, decodeData)) {
                Mono<byte[]> bodyMono = body == null ? httpResponse.getBodyAsByteArray() : Mono.just(body);
                return bodyMono.flatMap(bodyAsByteArray -> {
                    try {
                        final Object decodedErrorEntity = deserializeBody(bodyAsByteArray,
                            decodeData.getUnexpectedException(httpResponse.getStatusCode()).getExceptionBodyType(),
                            null, serializer, SerializerEncoding.fromHeaders(httpResponse.getHeaders()));

                        return Mono.justOrEmpty(decodedErrorEntity);
                    } catch (IOException | MalformedValueException ex) {
                        // This translates in RestProxy as a RestException with no deserialized body.
                        // The response content will still be accessible via the .response() member.
                        logger.warning("Failed to deserialize the error entity.", ex);
                        return Mono.empty();
                    }
                });
            } else if (httpResponse.getRequest().getHttpMethod() == HttpMethod.HEAD) {
                // RFC: A response to a HEAD method should not have a body. If so, it must be ignored
                return Mono.empty();
            } else {
                if (!isReturnTypeDecodable(decodeData.getReturnType())) {
                    return Mono.empty();
                }

                Mono<byte[]> bodyMono = body == null ? httpResponse.getBodyAsByteArray() : Mono.just(body);
                return bodyMono.flatMap(bodyAsByteArray -> {
                    try {
                        final Object decodedSuccessEntity = deserializeBody(bodyAsByteArray,
                            extractEntityTypeFromReturnType(decodeData), decodeData.getReturnValueWireType(),
                            serializer, SerializerEncoding.fromHeaders(httpResponse.getHeaders()));

                        return Mono.justOrEmpty(decodedSuccessEntity);
                    } catch (MalformedValueException e) {
                        return Mono.error(new HttpResponseException("HTTP response has a malformed body.",
                            httpResponse, e));
                    } catch (IOException e) {
                        return Mono.error(new HttpResponseException("Deserialization Failed.", httpResponse, e));
                    }
                });
            }
        });
    }

    /**
     * @return the decoded type used to decode the response body, null if the body is not decodable.
     */
    static Type decodedType(final HttpResponse httpResponse, final HttpResponseDecodeData decodeData) {
        ensureRequestSet(httpResponse);

        if (isErrorStatus(httpResponse, decodeData)) {
            // For error cases we always try to decode the non-empty response body
            // either to a strongly typed exception model or to Object
            return decodeData.getUnexpectedException(httpResponse.getStatusCode()).getExceptionBodyType();
        } else if (httpResponse.getRequest().getHttpMethod() == HttpMethod.HEAD) {
            // RFC: A response to a HEAD method should not have a body. If so, it must be ignored
            return null;
        } else {
            return isReturnTypeDecodable(decodeData.getReturnType())
                ? extractEntityTypeFromReturnType(decodeData)
                : null;
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
        return !decodeData.isExpectedResponseStatusCode(httpResponse.getStatusCode());
    }

    /**
     * Deserialize the given string value representing content of a REST API response.
     *
     * If the {@link ReturnValueWireType} is of type {@link Page}, then the returned object will be an instance of that
     * {@param wireType}. Otherwise, the returned object is converted back to its {@param resultType}.
     *
     * @param value the string value to deserialize
     * @param resultType the return type of the java proxy method
     * @param wireType value of optional {@link ReturnValueWireType} annotation present in java proxy method indicating
     * 'entity type' (wireType) of REST API wire response body
     * @param encoding the encoding format of value
     * @return Deserialized object
     * @throws IOException When the body cannot be deserialized
     */
    private static Object deserializeBody(final byte[] value, final Type resultType, final Type wireType,
        final SerializerAdapter serializer, final SerializerEncoding encoding) throws IOException {
        if (wireType == null) {
            return serializer.deserialize(value, resultType, encoding);
        } else if (TypeUtil.isTypeOrSubTypeOf(wireType, Page.class)) {
            return deserializePage(value, resultType, wireType, serializer, encoding);
        } else {
            final Type wireResponseType = constructWireResponseType(resultType, wireType);
            final Object wireResponse = serializer.deserialize(value, wireResponseType, encoding);

            return convertToResultType(wireResponse, resultType, wireType);
        }
    }

    /**
     * Given: (1). the {@code java.lang.reflect.Type} (resultType) of java proxy method return value (2). and {@link
     * ReturnValueWireType} annotation value indicating 'entity type' (wireType) of same REST API's wire response body
     * this method construct 'response body Type'.
     *
     * Note: When {@link ReturnValueWireType} annotation is applied to a proxy method, then the raw HTTP response
     * content will need to parsed using the derived 'response body Type' then converted to actual {@code returnType}.
     *
     * @param resultType the {@code java.lang.reflect.Type} of java proxy method return value
     * @param wireType the {@code java.lang.reflect.Type} of entity in REST API response body
     * @return the {@code java.lang.reflect.Type} of REST API response body
     */
    private static Type constructWireResponseType(Type resultType, Type wireType) {
        Objects.requireNonNull(wireType);

        if (resultType == byte[].class) {
            if (wireType == Base64Url.class) {
                return Base64Url.class;
            }
        } else if (resultType == OffsetDateTime.class) {
            if (wireType == DateTimeRfc1123.class) {
                return DateTimeRfc1123.class;
            } else if (wireType == UnixTime.class) {
                return UnixTime.class;
            }
        } else if (TypeUtil.isTypeOrSubTypeOf(resultType, List.class)) {
            final Type resultElementType = TypeUtil.getTypeArgument(resultType);
            final Type wireResponseElementType = constructWireResponseType(resultElementType, wireType);

            return TypeUtil.createParameterizedType(((ParameterizedType) resultType).getRawType(),
                wireResponseElementType);
        } else if (TypeUtil.isTypeOrSubTypeOf(resultType, Map.class)) {
            final Type[] typeArguments = TypeUtil.getTypeArguments(resultType);
            final Type resultValueType = typeArguments[1];
            final Type wireResponseValueType = constructWireResponseType(resultValueType, wireType);

            return TypeUtil.createParameterizedType(((ParameterizedType) resultType).getRawType(),
                typeArguments[0], wireResponseValueType);
        }

        return resultType;
    }

    /**
     * Deserializes a response body as a Page&lt;T&gt; given that {@param wireType} is either: 1. A type that implements
     * the interface 2. Is of {@link Page}
     *
     * @param value The data to deserialize
     * @param resultType The type T, of the page contents.
     * @param wireType The {@link Type} that either is, or implements {@link Page}
     * @param serializer The serializer used to deserialize the value.
     * @param encoding Encoding used to deserialize string
     * @return An object representing an instance of {@param wireType}
     * @throws IOException if the serializer is unable to deserialize the value.
     */
    private static Object deserializePage(final byte[] value,
        final Type resultType,
        final Type wireType,
        final SerializerAdapter serializer,
        final SerializerEncoding encoding) throws IOException {
        // If the type is the 'Page' interface [@ReturnValueWireType(Page.class)] we will use the 'ItemPage' class.
        final Type wireResponseType = (wireType == Page.class)
            ? TypeUtil.createParameterizedType(ItemPage.class, resultType)
            : wireType;

        return serializer.deserialize(value, wireResponseType, encoding);
    }

    /**
     * Converts the object {@code wireResponse} that was deserialized using 'response body Type' (produced by {@code
     * constructWireResponseType(args)} method) to resultType.
     *
     * @param wireResponse the object to convert
     * @param resultType the {@code java.lang.reflect.Type} to convert wireResponse to
     * @param wireType the {@code java.lang.reflect.Type} of the wireResponse
     * @return converted object
     */
    private static Object convertToResultType(final Object wireResponse,
        final Type resultType,
        final Type wireType) {
        if (resultType == byte[].class) {
            if (wireType == Base64Url.class) {
                return ((Base64Url) wireResponse).decodedBytes();
            }
        } else if (resultType == OffsetDateTime.class) {
            if (wireType == DateTimeRfc1123.class) {
                return ((DateTimeRfc1123) wireResponse).getDateTime();
            } else if (wireType == UnixTime.class) {
                return ((UnixTime) wireResponse).getDateTime();
            }
        } else if (TypeUtil.isTypeOrSubTypeOf(resultType, List.class)) {
            final Type resultElementType = TypeUtil.getTypeArgument(resultType);

            @SuppressWarnings("unchecked") final List<Object> wireResponseList = (List<Object>) wireResponse;

            final int wireResponseListSize = wireResponseList.size();
            for (int i = 0; i < wireResponseListSize; ++i) {
                final Object wireResponseElement = wireResponseList.get(i);
                final Object resultElement =
                    convertToResultType(wireResponseElement, resultElementType, wireType);
                if (wireResponseElement != resultElement) {
                    wireResponseList.set(i, resultElement);
                }
            }

            return wireResponseList;
        } else if (TypeUtil.isTypeOrSubTypeOf(resultType, Map.class)) {
            final Type resultValueType = TypeUtil.getTypeArguments(resultType)[1];

            @SuppressWarnings("unchecked") final Map<String, Object> wireResponseMap =
                (Map<String, Object>) wireResponse;

            final Set<Map.Entry<String, Object>> wireResponseEntries = wireResponseMap.entrySet();
            for (Map.Entry<String, Object> wireResponseEntry : wireResponseEntries) {
                final Object wireResponseValue = wireResponseEntry.getValue();
                final Object resultValue = convertToResultType(wireResponseValue, resultValueType, wireType);
                if (wireResponseValue != resultValue) {
                    wireResponseMap.put(wireResponseEntry.getKey(), resultValue);
                }
            }

            return wireResponseMap;
        }

        return wireResponse;
    }

    /**
     * Get the {@link Type} of the REST API 'returned entity'.
     *
     * In the declaration of a java proxy method corresponding to the REST API, the 'returned entity' can be:
     *
     * 1. emission value of the reactor publisher returned by proxy method
     *
     * e.g. {@code Mono<Foo> getFoo(args);} {@code Flux<Foo> getFoos(args);} where Foo is the REST API 'returned
     * entity'.
     *
     * 2. OR content (value) of {@link ResponseBase} emitted by the reactor publisher returned from proxy method
     *
     * e.g. {@code Mono<RestResponseBase<headers, Foo>> getFoo(args);} {@code Flux<RestResponseBase<headers, Foo>>
     * getFoos(args);} where Foo is the REST API return entity.
     *
     * @return the entity type.
     */
    private static Type extractEntityTypeFromReturnType(HttpResponseDecodeData decodeData) {
        Type token = decodeData.getReturnType();

        if (TypeUtil.isTypeOrSubTypeOf(token, Mono.class)) {
            token = TypeUtil.getTypeArgument(token);
        }

        if (TypeUtil.isTypeOrSubTypeOf(token, Response.class)) {
            token = TypeUtil.getRestResponseBodyType(token);
        }

        return token;
    }

    /**
     * Checks if the {@code returnType} is a decode-able type.
     * <p>
     * Types that aren't decode-able are the following (including sub-types):
     * <ul>
     * <li>BinaryData</li>
     * <li>byte[]</li>
     * <li>ByteBuffer</li>
     * <li>InputStream</li>
     * <li>Void</li>
     * <li>void</li>
     * </ul>
     *
     * Reactive, {@link Mono} and {@link Flux}, and Response, {@link Response} and {@link ResponseBase}, generics are
     * cracked open and their generic types are inspected for being one of the types above.
     *
     * @param returnType The return type of the method.
     * @return Flag indicating if the return type is decode-able.
     */
    public static boolean isReturnTypeDecodable(Type returnType) {
        if (returnType == null) {
            return false;
        }

        return RETURN_TYPE_DECODEABLE_MAP.computeIfAbsent(returnType, type -> {
            type = unwrapReturnType(type);

            return !TypeUtil.isTypeOrSubTypeOf(type, BinaryData.class)
                && !TypeUtil.isTypeOrSubTypeOf(type, byte[].class)
                && !TypeUtil.isTypeOrSubTypeOf(type, ByteBuffer.class)
                && !TypeUtil.isTypeOrSubTypeOf(type, InputStream.class)
                && !TypeUtil.isTypeOrSubTypeOf(type, Void.TYPE)
                && !TypeUtil.isTypeOrSubTypeOf(type, Void.class);
        });
    }

    /**
     * Checks if the network response body should be eagerly read based on its {@code returnType}.
     * <p>
     * The following types, including sub-types, aren't eagerly read from the network:
     * <ul>
     * <li>BinaryData</li>
     * <li>byte[]</li>
     * <li>ByteBuffer</li>
     * <li>InputStream</li>
     * </ul>
     *
     * Reactive, {@link Mono} and {@link Flux}, and Response, {@link Response} and {@link ResponseBase}, generics are
     * cracked open and their generic types are inspected for being one of the types above.
     *
     * @param returnType The return type of the method.
     * @return Flag indicating if the network response body should be eagerly read.
     */
    public static boolean shouldEagerlyReadResponse(Type returnType) {
        if (returnType == null) {
            return false;
        }

        return isReturnTypeDecodable(returnType)
            || TypeUtil.isTypeOrSubTypeOf(returnType, Void.TYPE)
            || TypeUtil.isTypeOrSubTypeOf(returnType, Void.class);
    }

    private static Type unwrapReturnType(Type returnType) {
        // First check if the return type is assignable, is a sub-type, to ResponseBase.
        // If it is begin walking up the super type hierarchy until ResponseBase is the raw type.
        // Then unwrap the second generic type (body type).
        if (TypeUtil.isTypeOrSubTypeOf(returnType, ResponseBase.class)) {
            returnType = walkSuperTypesUntil(returnType, type -> getRawClass(type) == ResponseBase.class);

            return unwrapReturnType(TypeUtil.getTypeArguments(returnType)[1]);
        }

        // Then, like ResponseBase, check if the return type is assignable to Response.
        // If it is begin walking up the super type hierarchy until the raw type implements Response.
        // Then unwrap its only generic type.
        if (TypeUtil.isTypeOrSubTypeOf(returnType, Response.class)) {
            // Handling for Response is slightly different as it is an interface unlike ResponseBase which is a class.
            // The super class hierarchy needs be walked until the super class itself implements Response.
            returnType = walkSuperTypesUntil(returnType, type -> typeImplementsInterface(type, Response.class));

            return unwrapReturnType(TypeUtil.getTypeArgument(returnType));
        }

        // Then check if the return type is a Mono or Flux and unwrap its only generic type.
        if (TypeUtil.isTypeOrSubTypeOf(returnType, Mono.class)) {
            returnType = walkSuperTypesUntil(returnType, type -> getRawClass(type) == Mono.class);

            return unwrapReturnType(TypeUtil.getTypeArgument(returnType));
        }

        if (TypeUtil.isTypeOrSubTypeOf(returnType, Flux.class)) {
            returnType = walkSuperTypesUntil(returnType, type -> getRawClass(type) == Flux.class);

            return unwrapReturnType(TypeUtil.getTypeArgument(returnType));
        }

        // Finally, there is no more unwrapping to perform and return the type as-is.
        return returnType;
    }

    /*
     * Helper method that walks up the super types until the type is an instance of the Class.
     */
    private static Type walkSuperTypesUntil(Type type, Predicate<Type> untilChecker) {
        while (!untilChecker.test(type)) {
            type = TypeUtil.getSuperType(type);
        }

        return type;
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

