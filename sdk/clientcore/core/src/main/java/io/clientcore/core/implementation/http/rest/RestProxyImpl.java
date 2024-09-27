// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.implementation.TypeUtil;
import io.clientcore.core.implementation.http.HttpResponseAccessHelper;
import io.clientcore.core.implementation.util.Base64Uri;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.core.util.serializer.ObjectSerializer;

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
     * @param serializer The serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser The parser that contains information about the interface describing REST API methods
     * to be used.
     */
    public RestProxyImpl(HttpPipeline httpPipeline, ObjectSerializer serializer,
                         SwaggerInterfaceParser interfaceParser) {
        super(httpPipeline, serializer, interfaceParser);
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

    @SuppressWarnings({"try", "unused"})
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
            throw instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode),
                response, null, null);
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
                    HttpResponseAccessHelper.setBodyDeserializer((HttpResponse<?>) response, (body) ->
                        handleResponseBody(response, methodParser, bodyType, body));
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

    public void updateRequest(RequestDataConfiguration requestDataConfiguration, ObjectSerializer serializerAdapter) {

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
            request.setBody(BinaryData.fromObject(bodyContentObject, serializerAdapter));
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
            request.setBody(BinaryData.fromObject(bodyContentObject, serializerAdapter));
        }
    }
}
