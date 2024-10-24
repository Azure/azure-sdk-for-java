// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.serializer;

import io.clientcore.core.http.annotation.HttpRequestInformation;
import io.clientcore.core.http.exception.HttpResponseException;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.TypeUtil;
import io.clientcore.core.implementation.util.Base64Uri;
import io.clientcore.core.implementation.util.DateTimeRfc1123;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.core.util.serializer.ObjectSerializer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Decoder to decode the body of a {@link Response}.
 */
public final class HttpResponseBodyDecoder {
    // HttpResponseBodyDecoder is a commonly used class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(HttpResponseBodyDecoder.class);
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * Decodes the body of an {@link Response} into the type returned by the called API.
     *
     * <p>If the response body cannot be decoded, null will be returned.</p>
     *
     * @param body The response body retrieved from the {@link Response} to decode.
     * @param response The {@link Response}.
     * @param serializer The {@link ObjectSerializer} that performs the decoding.
     * @param decodeData The API method metadata used during decoding of the {@link Response response}.
     *
     * @return The decoded {@link Response response} body, or {@code null} if the body could not be decoded.
     *
     * @throws HttpResponseException If the body cannot be decoded.
     */
    public static Object decodeByteArray(BinaryData body, Response<?> response, ObjectSerializer serializer,
                                         HttpResponseDecodeData decodeData) {
        ensureRequestSet(response);

        // Check for the HEAD HTTP method first as it's possible for the underlying HttpClient to treat a non-existent
        // response body as an empty byte array.
        if (response.getRequest().getHttpMethod() == HttpMethod.HEAD) {
            // RFC: A response to a HEAD method should not have a body. If so, it must be ignored.
            return null;
        } else if (isErrorStatus(response.getStatusCode(), decodeData)) {
            try {
                return deserializeBody(body, decodeData.getUnexpectedException(
                    response.getStatusCode()).getExceptionBodyClass(), null, serializer);
            } catch (IOException e) {
                return LOGGER.atWarning().log("Failed to deserialize the error entity.", e);
            } catch (RuntimeException e) {
                Throwable cause = e.getCause();

                if (cause instanceof InvocationTargetException || cause instanceof IllegalAccessException
                    || cause instanceof NoSuchMethodException || cause instanceof IOException) {
                    // - InvocationTargetException is thrown by the deserializer when the fromJson() method in the
                    // type to deserialize to throws an exception.
                    // - IllegalAccessException is thrown when the deserializer cannot access said fromJson() method in
                    // the type to deserialize the body to.
                    // - NoSuchMethodException is thrown when said fromJson() method cannot be found.
                    // - IOException is thrown when the deserializer cannot read the response body.
                    //
                    // Return the exception as the body type, RestProxyBase will handle this later.
                    LOGGER.atWarning().log("Failed to deserialize the error entity.", e);

                    return e;
                } else {
                    throw e;
                }
            }
        } else {
            if (!decodeData.isReturnTypeDecodable()) {
                return null;
            }

            try {
                return deserializeBody(body == null ? response.getBody() : body,
                    extractEntityTypeFromReturnType(decodeData), decodeData.getReturnValueWireType(), serializer);
            } catch (MalformedValueException e) {
                throw new HttpResponseException("HTTP response has a malformed body.", response, null, e);
            } catch (IOException e) {
                throw new HttpResponseException("Deserialization failed.", response, null, e);
            }
        }
    }

    /**
     * @return The decoded type used to decode the response body, null if the body is not decodable.
     */
    public static Type decodedType(final Response<?> response, final HttpResponseDecodeData decodeData) {
        ensureRequestSet(response);

        if (response.getRequest().getHttpMethod() == HttpMethod.HEAD) {
            // RFC: A response to a HEAD method should not have a body. If so, it must be ignored
            return null;
        } else if (isErrorStatus(response.getStatusCode(), decodeData)) {
            // For error cases we always try to decode the non-empty response body either to a strongly typed exception
            // model or to Object.
            return decodeData.getUnexpectedException(response.getStatusCode()).getExceptionBodyClass();
        } else {
            return decodeData.isReturnTypeDecodable() ? extractEntityTypeFromReturnType(decodeData) : null;
        }
    }

    /**
     * Checks the {@link Response} status code is considered as error.
     *
     * @param statusCode The status code from the response.
     * @param decodeData Metadata about the API response.
     *
     * @return {@code true} if the {@link Response} status code is considered as error, {@code false}
     * otherwise.
     */
    static boolean isErrorStatus(int statusCode, HttpResponseDecodeData decodeData) {
        return !decodeData.isExpectedResponseStatusCode(statusCode);
    }

    /**
     * Deserialize the given string value representing content of a REST API response.
     *
     * @param value The string value to deserialize.
     * @param resultType The return type of the Java proxy method.
     * @param wireType Value of the optional {@link HttpRequestInformation#returnValueWireType()} annotation present in
     * the Java proxy method indicating 'entity type' (wireType) of REST API wire response body.
     *
     * @return Deserialized object.
     * @throws IOException If the deserialization fails.
     */
    private static Object deserializeBody(BinaryData value, Type resultType, Type wireType, ObjectSerializer serializer)
        throws IOException {
        if (wireType == null) {
            return deserialize(value, resultType, serializer);
        } else {
            Type wireResponseType = constructWireResponseType(resultType, wireType);
            Object wireResponse = deserialize(value, wireResponseType, serializer);

            return convertToResultType(wireResponse, resultType, wireType);
        }
    }

    private static Object deserialize(BinaryData value, Type type, ObjectSerializer serializer) throws IOException {
        return serializer.deserializeFromBytes(value == null ? EMPTY_BYTE_ARRAY : value.toBytes(), type);
    }

    /**
     * Given: (1). The {@link Type result type} of the Java proxy method return value and (2). The
     * {@link HttpRequestInformation#returnValueWireType()} annotation value indicating the 'entity type' (wireType) of
     * the same REST APIs wire response body, this method will construct the 'response body Type'.
     *
     * <p>Note: When the {@link HttpRequestInformation#returnValueWireType()} annotation is applied to a proxy method,
     * the raw HTTP response content will need to be parsed using the derived 'response body Type' and then converted to
     * the actual {@code returnType}.</p>
     *
     * @param resultType The {@link Type} of java proxy method return value.
     * @param wireType The {@link Type} of entity in REST API response body.
     *
     * @return The {@link Type} of REST API response body.
     */
    private static Type constructWireResponseType(Type resultType, Type wireType) {
        Objects.requireNonNull(wireType);

        if (resultType == byte[].class) {
            if (wireType == Base64Uri.class) {
                return Base64Uri.class;
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
     * Converts the object {@code wireResponse} that was deserialized using the 'response body Type' (produced by
     * {@link HttpResponseBodyDecoder#constructWireResponseType} method) to the {@code resultType}.
     *
     * @param wireResponse The object to convert.
     * @param resultType The {@link Type} to convert the {@code wireResponse} to.
     * @param wireType The {@link Type} of the {@code wireResponse}.
     *
     * @return The converted object.
     */
    private static Object convertToResultType(final Object wireResponse, final Type resultType, final Type wireType) {
        if (resultType == byte[].class) {
            if (wireType == Base64Uri.class) {
                return (new Base64Uri(wireResponse.toString())).decodedBytes();
            }
        } else if (resultType == OffsetDateTime.class) {
            if (wireType == DateTimeRfc1123.class) {
                return new DateTimeRfc1123(wireResponse.toString()).getDateTime();
            } else {
                return OffsetDateTime.parse(wireResponse.toString());
            }
        } else if (TypeUtil.isTypeOrSubTypeOf(resultType, List.class)) {
            final Type resultElementType = TypeUtil.getTypeArgument(resultType);

            @SuppressWarnings("unchecked") final List<Object> wireResponseList = (List<Object>) wireResponse;

            final int wireResponseListSize = wireResponseList.size();

            for (int i = 0; i < wireResponseListSize; ++i) {
                final Object wireResponseElement = wireResponseList.get(i);
                final Object resultElement = convertToResultType(wireResponseElement, resultElementType, wireType);
                if (wireResponseElement != resultElement) {
                    wireResponseList.set(i, resultElement);
                }
            }

            return wireResponseList;
        } else if (TypeUtil.isTypeOrSubTypeOf(resultType, Map.class)) {
            final Type resultValueType = TypeUtil.getTypeArguments(resultType)[1];

            @SuppressWarnings("unchecked") final Map<String, Object> wireResponseMap
                = (Map<String, Object>) wireResponse;

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
     * Get the {@link Type} entity returned by the REST API.
     *
     * @return The entity type.
     */
    private static Type extractEntityTypeFromReturnType(HttpResponseDecodeData decodeData) {
        Type token = decodeData.getReturnType();

        if (TypeUtil.isTypeOrSubTypeOf(token, Response.class)) {
            token = TypeUtil.getRestResponseBodyType(token);
        }

        return token;
    }

    /**
     * Ensure that the request property and method are set in the {@link Response}.
     *
     * @param response The {@link Response} to validate.
     */
    private static void ensureRequestSet(Response<?> response) {
        Objects.requireNonNull(response.getRequest());
    }
}

