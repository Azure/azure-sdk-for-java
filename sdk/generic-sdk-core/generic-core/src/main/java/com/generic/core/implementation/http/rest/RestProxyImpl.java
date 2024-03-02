// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.rest;

import com.generic.core.http.Response;
import com.generic.core.http.client.HttpClient;
import com.generic.core.http.exception.HttpResponseException;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.models.RequestOptions;
import com.generic.core.http.pipeline.HttpPipeline;
import com.generic.core.implementation.TypeUtil;
import com.generic.core.implementation.http.serializer.HttpResponseDecodeData;
import com.generic.core.implementation.http.serializer.HttpResponseDecoder;
import com.generic.core.implementation.util.Base64Url;
import com.generic.core.models.BinaryData;
import com.generic.core.util.serializer.ObjectSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.function.Consumer;

public class RestProxyImpl extends RestProxyBase {

    /**
     * Create a {@link RestProxyImpl}.
     *
     * @param httpPipeline The {@link HttpPipeline pipeline} that will be used to send {@link HttpRequest requests}.
     * @param serializer The {@link ObjectSerializer serializer} that will be used to convert POJOs to and from request
     * and response bodies.
     * @param interfaceParser The {@link SwaggerInterfaceParser parser} that contains information about the interface
     * describing REST API methods that this {@link RestProxyImpl} implements.
     */
    public RestProxyImpl(HttpPipeline httpPipeline, ObjectSerializer serializer,
                         SwaggerInterfaceParser interfaceParser) {
        super(httpPipeline, serializer, interfaceParser);
    }

    /**
     * Send the provided request, applying any request policies provided to the {@link HttpClient} instance.
     *
     * @param request The {@link HttpRequest} to send.
     *
     * @return The resulting {@link HttpResponse} from sending the {@link HttpRequest}.
     */
    HttpResponse<?> send(HttpRequest request) {
        return httpPipeline.send(request);
    }

    @SuppressWarnings({"try", "unused"})
    @Override
    public Object invoke(Object proxy, Method method, RequestOptions options, EnumSet<ErrorOptions> errorOptions,
                         Consumer<HttpRequest> requestCallback, SwaggerMethodParser methodParser, HttpRequest request) {
        HttpResponse<?> decodedResponse;

        // If RequestOptions were provided, apply the request callback operations before validating the body. This is
        // because the callbacks may mutate the request body.
        if (options != null && requestCallback != null) {
            requestCallback.accept(request);
        }

        if (request.getBody() != null) {
            request.setBody(RestProxyUtils.validateLength(request));
        }

        final HttpResponse<?> response = send(request);
        decodedResponse = this.decoder.decode(response, methodParser);

        int statusCode = decodedResponse.getStatusCode();

        try {
            return handleRestReturnType(decodedResponse, methodParser, methodParser.getReturnType(), options,
                errorOptions);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Ensures the provided {@code decodedResponse} has an allowed status code, or create an
     * {@link HttpResponseException} if it does not.
     *
     * <p>'allowed status code' is one of the status code defined in the provided {@link SwaggerMethodParser} or is in
     * the int[] of additional allowed status codes.</p>
     *
     * @param decodedResponse The {@link HttpResponse} to check.
     * @param methodParser The {@link SwaggerMethodParser} that contains information about the service interface method
     * that initiated the {@link HttpRequest}.
     *
     * @return The {@link HttpResponse decodedResponse}.
     */
    private HttpResponse<?> ensureExpectedStatus(HttpResponse<?> decodedResponse,
                                                                         SwaggerMethodParser methodParser,
                                                                         RequestOptions options,
                                                                         EnumSet<ErrorOptions> errorOptions) {
        int responseStatusCode = decodedResponse.getStatusCode();

        // If the response was success or configured to not return an error status when the request fails, return the
        // decoded response.
        if (methodParser.isExpectedResponseStatusCode(responseStatusCode)
            || (options != null && errorOptions.contains(ErrorOptions.NO_THROW))) {

            return decodedResponse;
        }

        // Otherwise, the response wasn't successful and the error object needs to be parsed.
        BinaryData responseData = decodedResponse.getBody();
        byte[] responseBytes = responseData == null ? null : responseData.toBytes();

        if (responseBytes == null || responseBytes.length == 0) {
            // No body, create an exception response with an empty body.
            throw instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode),
                decodedResponse, null, null);
        } else {
            Object decodedBody = decodedResponse.getDecodedBody();
            // Create an exception response containing the decoded response body.
            throw instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode),
                decodedResponse, responseBytes, decodedBody);
        }
    }

    private Object handleRestResponseReturnType(HttpResponse<?> decodedResponse,
                                                SwaggerMethodParser methodParser, Type entityType) {
        if (TypeUtil.isTypeOrSubTypeOf(entityType, Response.class)) {
            final Type bodyType = TypeUtil.getRestResponseBodyType(entityType);

            if (TypeUtil.isTypeOrSubTypeOf(bodyType, Void.class)) {
                try {
                    decodedResponse.close();
                } catch (IOException e) {
                    throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
                }

                return createResponse(decodedResponse, entityType, null);
            } else {
                Object bodyAsObject = handleBodyReturnType(decodedResponse, methodParser, bodyType);
                Response<?> response = createResponse(decodedResponse, entityType, bodyAsObject);

                if (response == null) {
                    return createResponse(decodedResponse, entityType, null);
                }

                return response;
            }
        } else {
            return handleBodyReturnType(decodedResponse, methodParser, entityType);
        }
    }

    private Object handleBodyReturnType(HttpResponse<?> decodedResponse,
                                        SwaggerMethodParser methodParser, Type entityType) {
        final int responseStatusCode = decodedResponse.getStatusCode();
        final HttpMethod httpMethod = methodParser.getHttpMethod();
        final Type returnValueWireType = methodParser.getReturnValueWireType();

        final Object result;

        if (httpMethod == HttpMethod.HEAD
            && (TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.TYPE)
                || TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.class))) {
            result = (responseStatusCode / 100) == 2;
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, byte[].class)) {
            // byte[]
            BinaryData binaryData = decodedResponse.getBody();
            byte[] responseBodyBytes = binaryData != null ? binaryData.toBytes() : null;

            if (returnValueWireType == Base64Url.class) {
                // Base64Url
                responseBodyBytes = new Base64Url(responseBodyBytes).decodedBytes();
            }

            result = responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null;
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, InputStream.class)) {
            result = decodedResponse.getBody().toStream();
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, BinaryData.class)) {
            // BinaryData
            //
            // The raw response is directly used to create an instance of BinaryData which then provides different
            // methods to read the response. The reading of the response is delayed until BinaryData is read and
            // depending on which format the content is converted into, the response is not necessarily fully copied
            // into memory resulting in lesser overall memory usage.
            result = decodedResponse.getBody();
        } else {
            // Deserialized Object
            result = decodedResponse.getDecodedBody();
        }

        return result;
    }

    /**
     * Handle the provided {@link HttpResponse decoded response} and return the deserialized
     * value.
     *
     * @param decodedResponse The {@link HttpResponse decoded response} to the original
     * {@link HttpRequest}.
     * @param methodParser The {@link SwaggerMethodParser} that the request originates from.
     * @param returnType The {@link Type} of value that will be returned.
     *
     * @return The deserialized result.
     */
    private Object handleRestReturnType(HttpResponse<?> decodedResponse,
                                        SwaggerMethodParser methodParser, Type returnType,
                                        RequestOptions options, EnumSet<ErrorOptions> errorOptions) throws IOException {
        final HttpResponse<?> expectedResponse =
            ensureExpectedStatus(decodedResponse, methodParser, options, errorOptions);
        final Object result;

        if (TypeUtil.isTypeOrSubTypeOf(returnType, void.class) || TypeUtil.isTypeOrSubTypeOf(returnType, Void.class)) {
            try {
                expectedResponse.close();
            } catch (IOException e) {
                throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
            }

            result = null;
        } else {
            result = handleRestResponseReturnType(decodedResponse, methodParser, returnType);
        }

        return result;
    }

    public void updateRequest(RequestDataConfiguration requestDataConfiguration, ObjectSerializer objectSerializer) {
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
            request.setBody(BinaryData.fromObject(bodyContentObject, objectSerializer));
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
            request.setBody(BinaryData.fromObject(bodyContentObject, objectSerializer));
        }
    }
}
