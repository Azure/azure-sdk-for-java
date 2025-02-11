// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.core.http.annotation.HttpRequestInformation;
import io.clientcore.core.http.exception.HttpResponseException;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.TypeUtil;
import io.clientcore.core.implementation.http.rest.RestProxyImpl;
import io.clientcore.core.implementation.http.serializer.CompositeSerializer;
import io.clientcore.core.implementation.http.serializer.HttpResponseDecodeData;
import io.clientcore.core.implementation.http.serializer.MalformedValueException;
import io.clientcore.core.implementation.util.Base64Uri;
import io.clientcore.core.implementation.util.DateTimeRfc1123;
import io.clientcore.core.implementation.util.ImplUtils;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.core.util.serializer.SerializationFormat;

import java.io.IOException;
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
public final class HttpResponseBodyDecoderUtil {
    // HttpResponseBodyDecoder is a commonly used class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(HttpResponseBodyDecoderUtil.class);
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * Decodes the body of an {@link Response} into the type returned by the called API.
     *
     * <p>If the response body cannot be decoded, null will be returned.</p>
     *
     * @param body       The response body retrieved from the {@link Response} to decode.
     * @param response   The {@link Response}.
     * @param serializer The {@link CompositeSerializer} that performs the decoding.
     * @return The decoded {@link Response response} body, or {@code null} if the body could not be decoded.
     * @throws HttpResponseException If the body cannot be decoded.
     * @throws RuntimeException      If the body cannot be decoded.
     */
    public static Object decodeByteArray(BinaryData body, Response<?> response, CompositeSerializer serializer,
        Type type) {
        try {
            return deserializeBody(body == null ? response.getBody() : body, null, type,
                serializationFormatFromContentType(response.getHeaders()), serializer);
        } catch (MalformedValueException e) {
            throw new HttpResponseException("HTTP response has a malformed body.", response, null, e);
        } catch (IOException e) {
            throw new HttpResponseException("Deserialization failed.", response, null, e);
        }

    }

    /**
     * Get the decoded type used to decode the response body, or null if the body is not decodable.
     *
     * @param response   The response to decode.
     * @param decodeData Metadata about the API response.
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
     * @return {@code true} if the {@link Response} status code is considered as error, {@code false}
     * otherwise.
     */
    static boolean isErrorStatus(int statusCode, HttpResponseDecodeData decodeData) {
        return !decodeData.isExpectedResponseStatusCode(statusCode);
    }

    /**
     * Deserialize the given string value representing content of a REST API response.
     *
     * @param value      The string value to deserialize.
     * @param resultType The return type of the Java proxy method.
     * @param wireType   Value of the optional {@link HttpRequestInformation#returnValueWireType()} annotation present in
     *                   the Java proxy method indicating 'entity type' (wireType) of REST API wire response body.
     * @return Deserialized object.
     * @throws IOException If the deserialization fails.
     */
    private static Object deserializeBody(BinaryData value, Type resultType, Type wireType, SerializationFormat format,
        CompositeSerializer serializer) throws IOException {
        if (wireType == null) {
            return deserialize(value, resultType, format, serializer);
        } else {
            Type wireResponseType = constructWireResponseType(resultType, wireType);
            Object wireResponse = deserialize(value, wireResponseType, format, serializer);

            return convertToResultType(wireResponse, resultType, wireType);
        }
    }

    private static Object deserialize(BinaryData value, Type type, SerializationFormat format,
        CompositeSerializer serializer) throws IOException {
        return serializer.deserializeFromBytes(value == null ? EMPTY_BYTE_ARRAY : value.toBytes(), type, format);
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
     * @param wireType   The {@link Type} of entity in REST API response body.
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

            return TypeUtil.createParameterizedType(((ParameterizedType) resultType).getRawType(), typeArguments[0],
                wireResponseValueType);
        }

        return resultType;
    }

    /**
     * Converts the object {@code wireResponse} that was deserialized using the 'response body Type' (produced by
     * to the {@code resultType}.
     *
     * @param wireResponse The object to convert.
     * @param resultType   The {@link Type} to convert the {@code wireResponse} to.
     * @param wireType     The {@link Type} of the {@code wireResponse}.
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

            @SuppressWarnings("unchecked")
            final List<Object> wireResponseList = (List<Object>) wireResponse;

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

            @SuppressWarnings("unchecked")
            final Map<String, Object> wireResponseMap = (Map<String, Object>) wireResponse;

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

    /**
     * Determines the serializer encoding to use based on the Content-Type header.
     *
     * @param headers the headers to get the Content-Type to check the encoding for.
     * @return the serializer encoding to use for the body. {@link SerializationFormat#JSON} if there is no Content-Type
     * header or an unrecognized Content-Type encoding is given.
     */
    private static SerializationFormat serializationFormatFromContentType(HttpHeaders headers) {
        if (headers == null) {
            return SerializationFormat.JSON;
        }

        String contentType = headers.getValue(HttpHeaderName.CONTENT_TYPE);
        if (ImplUtils.isNullOrEmpty(contentType)) {
            // When in doubt, JSON!
            return SerializationFormat.JSON;
        }

        int contentTypeEnd = contentType.indexOf(';');
        contentType = (contentTypeEnd == -1) ? contentType : contentType.substring(0, contentTypeEnd);
        SerializationFormat encoding = checkForKnownEncoding(contentType);
        if (encoding != null) {
            return encoding;
        }

        int contentTypeTypeSplit = contentType.indexOf('/');
        if (contentTypeTypeSplit == -1) {
            return SerializationFormat.JSON;
        }

        // Check the suffix if it does not match the full types.
        // Suffixes are defined by the Structured Syntax Suffix Registry
        // https://www.rfc-editor.org/rfc/rfc6839
        final String subtype = contentType.substring(contentTypeTypeSplit + 1);
        final int lastIndex = subtype.lastIndexOf('+');
        if (lastIndex == -1) {
            return SerializationFormat.JSON;
        }

        // Only XML and JSON are supported suffixes, there is no suffix for TEXT.
        final String mimeTypeSuffix = subtype.substring(lastIndex + 1);
        if ("xml".equalsIgnoreCase(mimeTypeSuffix)) {
            return SerializationFormat.XML;
        } else if ("json".equalsIgnoreCase(mimeTypeSuffix)) {
            return SerializationFormat.JSON;
        }

        return SerializationFormat.JSON;
    }

    /*
     * There is a limited set of serialization encodings that are known ahead of time. Instead of using a TreeMap with
     * a case-insensitive comparator, use an optimized search specifically for the known encodings.
     */
    private static SerializationFormat checkForKnownEncoding(String contentType) {
        int length = contentType.length();

        // Check the length of the content type first as it is a quick check.
        if (length != 8 && length != 9 && length != 10 && length != 15 && length != 16) {
            return null;
        }

        if ("text/".regionMatches(true, 0, contentType, 0, 5)) {
            if (length == 8) {
                if ("xml".regionMatches(true, 0, contentType, 5, 3)) {
                    return SerializationFormat.XML;
                } else if ("csv".regionMatches(true, 0, contentType, 5, 3)) {
                    return SerializationFormat.TEXT;
                } else if ("css".regionMatches(true, 0, contentType, 5, 3)) {
                    return SerializationFormat.TEXT;
                }
            } else if (length == 9 && "html".regionMatches(true, 0, contentType, 5, 4)) {
                return SerializationFormat.TEXT;
            } else if (length == 10 && "plain".regionMatches(true, 0, contentType, 5, 5)) {
                return SerializationFormat.TEXT;
            } else if (length == 15 && "javascript".regionMatches(true, 0, contentType, 5, 10)) {
                return SerializationFormat.TEXT;
            }
        } else if ("application/".regionMatches(true, 0, contentType, 0, 12)) {
            if (length == 16 && "json".regionMatches(true, 0, contentType, 12, 4)) {
                return SerializationFormat.JSON;
            } else if (length == 15 && "xml".regionMatches(true, 0, contentType, 12, 3)) {
                return SerializationFormat.XML;
            }
        }

        return null;
    }
}
