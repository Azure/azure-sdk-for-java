// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.serializer;

import com.typespec.core.annotation.ReturnValueWireType;
import com.typespec.core.exception.HttpResponseException;
import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.http.rest.Page;
import com.typespec.core.http.rest.Response;
import com.typespec.core.http.rest.ResponseBase;
import com.typespec.core.implementation.TypeUtil;
import com.typespec.core.implementation.ReflectionSerializable;
import com.typespec.core.util.Base64Url;
import com.typespec.core.util.DateTimeRfc1123;
import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.serializer.SerializerAdapter;
import com.typespec.core.util.serializer.SerializerEncoding;
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
     * @param serializer The {@link SerializerAdapter} that performs decoding.
     * @param decodeData The API method metadata used during decoding of the response.
     * @return The decoded response body, or null if the body wasn't able to be decoded.
     * @throws HttpResponseException If the body fails to decode.
     */
    static Object decodeByteArray(byte[] body, HttpResponse httpResponse, SerializerAdapter serializer,
        HttpResponseDecodeData decodeData) {
        ensureRequestSet(httpResponse);

        // Check for HEAD HTTP method first as it's possible for the underlying HttpClient to treat a non-existent
        // response body as an empty byte array.
        if (httpResponse.getRequest().getHttpMethod() == HttpMethod.HEAD) {
            // RFC: A response to a HEAD method should not have a body. If so, it must be ignored
            return null;
        } else if (isErrorStatus(httpResponse.getStatusCode(), decodeData)) {
            try {
                return deserializeBody(body,
                    decodeData.getUnexpectedException(httpResponse.getStatusCode()).getExceptionBodyType(),
                    null, serializer, SerializerEncoding.fromHeaders(httpResponse.getHeaders()));
            } catch (IOException | MalformedValueException | IllegalStateException ex) {
                // MalformedValueException is thrown by Jackson, IllegalStateException is thrown by the TEXT
                // serialization encoding handler, and IOException can be thrown by both Jackson and TEXT.
                //
                // There has been an issue deserializing the error response body. This may be an error in the service
                // return.
                //
                // Return the exception as the body type, RestProxyBase will handle this later.
                LOGGER.warning("Failed to deserialize the error entity.", ex);
                return ex;
            }
        } else {
            if (!decodeData.isReturnTypeDecodeable()) {
                return null;
            }

            byte[] bodyAsByteArray = body == null ? httpResponse.getBodyAsBinaryData().toBytes() : body;
            try {
                return deserializeBody(bodyAsByteArray,
                    extractEntityTypeFromReturnType(decodeData), decodeData.getReturnValueWireType(),
                    serializer, SerializerEncoding.fromHeaders(httpResponse.getHeaders()));
            } catch (MalformedValueException e) {
                throw new HttpResponseException("HTTP response has a malformed body.", httpResponse, e);
            } catch (IOException e) {
                throw new HttpResponseException("Deserialization Failed.", httpResponse, e);
            }
        }
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
        } else {
            return decodeData.isReturnTypeDecodeable() ? extractEntityTypeFromReturnType(decodeData) : null;
        }
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
     * Deserialize the given string value representing content of a REST API response.
     * <p>
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
    private static Object deserializeBody(byte[] value, Type resultType, Type wireType, SerializerAdapter serializer,
        SerializerEncoding encoding) throws IOException {
        if (wireType == null) {
            return deserialize(value, resultType, serializer, encoding);
        } else if (TypeUtil.isTypeOrSubTypeOf(wireType, Page.class)) {
            // If the type is the 'Page' interface [@ReturnValueWireType(Page.class)] we will use the 'ItemPage' class.
            Type wireResponseType = (wireType == Page.class)
                ? TypeUtil.createParameterizedType(ItemPage.class, resultType)
                : wireType;

            return deserialize(value, wireResponseType, serializer, encoding);
        } else {
            Type wireResponseType = constructWireResponseType(resultType, wireType);
            Object wireResponse = deserialize(value, wireResponseType, serializer, encoding);

            return convertToResultType(wireResponse, resultType, wireType);
        }
    }

    private static Object deserialize(byte[] value, Type type, SerializerAdapter serializer,
        SerializerEncoding encoding) throws IOException {
        Class<?> rawType = TypeUtil.getRawClass(type);
        if (encoding == SerializerEncoding.JSON && ReflectionSerializable.supportsJsonSerializable(rawType)) {
            return ReflectionSerializable.deserializeAsJsonSerializable(rawType, value);
        }

        if (encoding == SerializerEncoding.XML && ReflectionSerializable.supportsXmlSerializable(rawType)) {
            return ReflectionSerializable.deserializeAsXmlSerializable(rawType, value);
        }

        return serializer.deserialize(value, type, encoding);
    }

    /**
     * Given: (1). the {@code java.lang.reflect.Type} (resultType) of java proxy method return value (2). and
     * {@link ReturnValueWireType} annotation value indicating 'entity type' (wireType) of same REST API's wire response
     * body this method construct 'response body Type'.
     * <p>
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
     * Converts the object {@code wireResponse} that was deserialized using 'response body Type' (produced by
     * {@code constructWireResponseType(args)} method) to resultType.
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
     * <p>
     * In the declaration of a java proxy method corresponding to the REST API, the 'returned entity' can be:
     * <p>
     * 1. emission value of the reactor publisher returned by proxy method, e.g. {@code Mono<Foo> getFoo(args);}
     * {@code Flux<Foo> getFoos(args);} where Foo is the REST API 'returned entity'.
     * <p>
     * 2. OR content (value) of {@link ResponseBase} emitted by the reactor publisher returned from proxy method, e.g.
     * {@code Mono<RestResponseBase<headers, Foo>> getFoo(args);}
     * {@code Flux<RestResponseBase<headers, Foo>> getFoos(args);} where Foo is the REST API return entity.
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
     * Ensure that request property and method is set in the response.
     *
     * @param httpResponse the response to validate
     */
    private static void ensureRequestSet(HttpResponse httpResponse) {
        Objects.requireNonNull(httpResponse.getRequest());
        Objects.requireNonNull(httpResponse.getRequest().getHttpMethod());
    }
}

