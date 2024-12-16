// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.implementation.TypeUtil;
import io.clientcore.core.implementation.http.HttpResponseAccessHelper;
import io.clientcore.core.implementation.http.serializer.CompositeSerializer;
import io.clientcore.core.implementation.util.Base64Uri;
import io.clientcore.core.implementation.util.ImplUtils;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.core.util.serializer.ObjectSerializer;
import io.clientcore.core.util.serializer.SerializationFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;

import static io.clientcore.core.http.models.ResponseBodyMode.DESERIALIZE;
import static io.clientcore.core.implementation.http.serializer.HttpResponseBodyDecoder.decodeByteArray;

public class RestProxyImpl extends RestProxyBase {

    /**
     * Create a RestProxy.
     *
     * @param httpPipeline The HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
     * @param interfaceParser The parser that contains information about the interface describing REST API methods
     * to be used.
     * @param serializers The serializers that will be used to convert response bodies to POJOs.
     */
    public RestProxyImpl(HttpPipeline httpPipeline, SwaggerInterfaceParser interfaceParser,
        ObjectSerializer... serializers) {
        super(httpPipeline, interfaceParser, serializers);
    }

    /**
     * Send the provided request, applying any request policies provided to the HttpClient instance.
     *
     * @param request the HTTP request to send.
     *
     * @return A {@link Response}.
     */
    Response<?> send(HttpRequest request) {
        return httpPipeline.send(request);
    }

    @SuppressWarnings({ "try", "unused" })
    @Override
    public Object invoke(Object proxy, SwaggerMethodParser methodParser, HttpRequest request) {
        // If there is 'RequestOptions' apply its request callback operations before validating the body.
        // This is because the callbacks may mutate the request body.
        if (request.getRequestOptions() != null) {
            request.getRequestOptions().getRequestCallback().accept(request);
        }

        if (request.getBody() != null) {
            request.setBody(RestProxyUtils.validateLength(request));
        }

        final Response<?> response = send(request);

        return handleRestReturnType(response, methodParser, methodParser.getReturnType());
    }

    /**
     * Create a publisher that (1) emits error if the provided response {@code decodedResponse} has 'disallowed status
     * code' OR (2) emits provided response if it's status code ia allowed.
     *
     * <p>'disallowed status code' is one of the status code defined in the provided SwaggerMethodParser or is in the int[]
     * of additional allowed status codes.</p>
     *
     * @param response The Response to check.
     * @param methodParser The method parser that contains information about the service interface method that initiated
     * the HTTP request.
     *
     * @return The decodedResponse.
     */
    private Response<?> ensureExpectedStatus(Response<?> response, SwaggerMethodParser methodParser) {
        int responseStatusCode = response.getStatusCode();

        // If the response was success or configured to not return an error status when the request fails, return it.
        if (methodParser.isExpectedResponseStatusCode(responseStatusCode)) {
            return response;
        }

        // Otherwise, the response wasn't successful and the error object needs to be parsed.
        if (response.getBody() == null || response.getBody().toBytes().length == 0) {
            // No body, create an exception response with an empty body.
            throw instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode), response,
                null, null);
        } else {
            // Create an exception response containing the decoded response body.
            throw instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode), response,
                response.getBody(), decodeByteArray(response.getBody(), response, serializer, methodParser));
        }
    }

    private Object handleRestResponseReturnType(Response<?> response, SwaggerMethodParser methodParser,
        Type entityType) {
        if (TypeUtil.isTypeOrSubTypeOf(entityType, Response.class)) {
            final Type bodyType = TypeUtil.getRestResponseBodyType(entityType);

            if (TypeUtil.isTypeOrSubTypeOf(bodyType, Void.class)) {
                try {
                    response.close();
                } catch (IOException e) {
                    throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
                }

                return createResponseIfNecessary(response, entityType, null);
            } else {
                ResponseBodyMode responseBodyMode = null;
                RequestOptions requestOptions = response.getRequest().getRequestOptions();

                if (requestOptions != null) {
                    responseBodyMode = requestOptions.getResponseBodyMode();
                }

                if (responseBodyMode == DESERIALIZE) {
                    HttpResponseAccessHelper.setValue((HttpResponse<?>) response,
                        handleResponseBody(response, methodParser, bodyType, response.getBody()));
                } else {
                    HttpResponseAccessHelper.setBodyDeserializer((HttpResponse<?>) response,
                        (body) -> handleResponseBody(response, methodParser, bodyType, body));
                }

                Response<?> responseToReturn = createResponseIfNecessary(response, entityType, response.getBody());

                if (responseToReturn == null) {
                    return createResponseIfNecessary(response, entityType, null);
                }

                return responseToReturn;
            }
        } else {
            // When not handling a Response subtype, we need to eagerly read the response body to construct the correct
            // return type.
            return handleResponseBody(response, methodParser, entityType, response.getBody());
        }
    }

    private Object handleResponseBody(Response<?> response, SwaggerMethodParser methodParser, Type entityType,
        BinaryData responseBody) {
        final int responseStatusCode = response.getStatusCode();
        final HttpMethod httpMethod = methodParser.getHttpMethod();
        final Type returnValueWireType = methodParser.getReturnValueWireType();

        final Object result;

        if (httpMethod == HttpMethod.HEAD
            && (TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.TYPE)
                || TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.class))) {
            result = (responseStatusCode / 100) == 2;
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, byte[].class)) {
            byte[] responseBodyBytes = responseBody != null ? responseBody.toBytes() : null;

            if (returnValueWireType == Base64Uri.class) {
                responseBodyBytes = new Base64Uri(responseBodyBytes).decodedBytes();
            }

            result = responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null;
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, InputStream.class)) {
            result = responseBody.toStream();
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, BinaryData.class)) {
            // BinaryData
            //
            // The raw response is directly used to create an instance of BinaryData which then provides
            // different methods to read the response. The reading of the response is delayed until BinaryData
            // is read and depending on which format the content is converted into, the response is not necessarily
            // fully copied into memory resulting in lesser overall memory usage.
            result = responseBody;
        } else {
            result = decodeByteArray(responseBody, response, serializer, methodParser);
        }

        return result;
    }

    /**
     * Handle the provided HTTP response and return the deserialized value.
     *
     * @param response The HTTP response to the original HTTP request.
     * @param methodParser The SwaggerMethodParser that the request originates from.
     * @param returnType The type of value that will be returned.
     *
     * @return The deserialized result.
     */
    private Object handleRestReturnType(Response<?> response, SwaggerMethodParser methodParser, Type returnType) {
        final Response<?> expectedResponse = ensureExpectedStatus(response, methodParser);
        final Object result;

        if (TypeUtil.isTypeOrSubTypeOf(returnType, void.class) || TypeUtil.isTypeOrSubTypeOf(returnType, Void.class)) {
            try {
                expectedResponse.close();
            } catch (IOException e) {
                throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
            }

            result = null;
        } else {
            result = handleRestResponseReturnType(response, methodParser, returnType);
        }

        return result;
    }

    public void updateRequest(RequestDataConfiguration requestDataConfiguration, CompositeSerializer serializer) {

        boolean isJson = requestDataConfiguration.isJson();
        HttpRequest request = requestDataConfiguration.getHttpRequest();
        Object bodyContentObject = requestDataConfiguration.getBodyContent();

        if (bodyContentObject == null) {
            return;
        }

        // Attempt to use JsonSerializable or XmlSerializable in a separate block.
        if (supportsJsonSerializable(bodyContentObject.getClass())) {
            request.setBody(BinaryData.fromObject(bodyContentObject));
            return;
        }

        if (isJson) {
            request.setBody(
                BinaryData.fromObject(bodyContentObject, serializer.getSerializerForFormat(SerializationFormat.JSON)));
        } else if (bodyContentObject instanceof byte[]) {
            request.setBody(BinaryData.fromBytes((byte[]) bodyContentObject));
        } else if (bodyContentObject instanceof String) {
            request.setBody(BinaryData.fromString((String) bodyContentObject));
        } else if (bodyContentObject instanceof ByteBuffer) {
            if (((ByteBuffer) bodyContentObject).hasArray()) {
                request.setBody(BinaryData.fromBytes(((ByteBuffer) bodyContentObject).array()));
            } else {
                byte[] array = new byte[((ByteBuffer) bodyContentObject).remaining()];

                ((ByteBuffer) bodyContentObject).get(array);
                request.setBody(BinaryData.fromBytes(array));
            }
        } else {
            request.setBody(BinaryData.fromObject(bodyContentObject,
                serializer.getSerializerForFormat(serializationFormatFromContentType(request.getHeaders()))));
        }
    }

    /**
     * Determines the serializer encoding to use based on the Content-Type header.
     *
     * @param headers the headers to get the Content-Type to check the encoding for.
     * @return the serializer encoding to use for the body. {@link SerializationFormat#JSON} if there is no Content-Type
     * header or an unrecognized Content-Type encoding is given.
     */
    public static SerializationFormat serializationFormatFromContentType(HttpHeaders headers) {
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
