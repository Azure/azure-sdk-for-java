// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.implementation.serializer.HttpResponseDecoder;
import com.azure.core.util.Base64Url;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.function.Consumer;

public class SyncRestProxy extends RestProxyBase {
    /**
     * Create a RestProxy.
     *
     * @param httpPipeline    the HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
     * @param serializer      the serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser the parser that contains information about the interface describing REST API methods that
     */
    public SyncRestProxy(HttpPipeline httpPipeline, SerializerAdapter serializer, SwaggerInterfaceParser interfaceParser) {
        super(httpPipeline, serializer, interfaceParser);
    }

    /**
     * Send the provided request asynchronously, applying any request policies provided to the HttpClient instance.
     *
     * @param request the HTTP request to send
     * @param contextData the context
     * @return a {@link Mono} that emits HttpResponse asynchronously
     */
    HttpResponse send(HttpRequest request, Context contextData) {
        return httpPipeline.sendSync(request, contextData);
    }

    public HttpRequest createHttpRequest(SwaggerMethodParser methodParser, Object[] args) throws IOException {
        return createHttpRequest(methodParser, serializer, false, args);
    }


    @Override
    public Object invoke(Object proxy, Method method, RequestOptions options, EnumSet<ErrorOptions> errorOptions, Consumer<HttpRequest> requestCallback, SwaggerMethodParser methodParser, HttpRequest request, Context context) {
        HttpResponseDecoder.HttpDecodedResponse decodedResponse = null;
        Throwable throwable = null;
        try {
            context = startTracingSpan(method, context);

            // If there is 'RequestOptions' apply its request callback operations before validating the body.
            // This is because the callbacks may mutate the request body.
            if (options != null && requestCallback != null) {
                requestCallback.accept(request);
            }

            if (request.getBodyAsBinaryData() != null) {
                request.setBody(RestProxyUtils.validateLengthSync(request));
            }

            final HttpResponse response = send(request, context);
            decodedResponse = this.decoder.decodeSync(response, methodParser);
            return handleRestReturnType(decodedResponse, methodParser, methodParser.getReturnType(), context, options, errorOptions);
        } catch (RuntimeException e) {
            throwable = e;
            throw LOGGER.logExceptionAsError(e);
        } finally {
            if (decodedResponse != null || throwable != null) {
                endTracingSpan(decodedResponse, throwable, context);
            }
        }
    }

    /**
     * Create a publisher that (1) emits error if the provided response {@code decodedResponse} has 'disallowed status
     * code' OR (2) emits provided response if it's status code ia allowed.
     *
     * 'disallowed status code' is one of the status code defined in the provided SwaggerMethodParser or is in the int[]
     * of additional allowed status codes.
     *
     * @param decodedResponse The HttpResponse to check.
     * @param methodParser The method parser that contains information about the service interface method that initiated
     * the HTTP request.
     * @return An async-version of the provided decodedResponse.
     */
    private HttpResponseDecoder.HttpDecodedResponse ensureExpectedStatus(final HttpResponseDecoder.HttpDecodedResponse decodedResponse,
                                                                         final SwaggerMethodParser methodParser, RequestOptions options, EnumSet<ErrorOptions> errorOptions) {
        final int responseStatusCode = decodedResponse.getSourceResponse().getStatusCode();

        // If the response was success or configured to not return an error status when the request fails, return the
        // decoded response.
        if (methodParser.isExpectedResponseStatusCode(responseStatusCode)
            || (options != null && errorOptions.contains(ErrorOptions.NO_THROW))) {
            return decodedResponse;
        }

        // Otherwise, the response wasn't successful and the error object needs to be parsed.
        Exception e;
        BinaryData responseData = decodedResponse.getSourceResponse().getBodyAsBinaryData();
        byte[] responseBytes = responseData == null ? null : responseData.toBytes();
        if (responseBytes == null || responseBytes.length == 0) {
            //  No body, create exception empty content string no exception body object.
            e = instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode),
                decodedResponse.getSourceResponse(), null, null);
        } else {
            Object decodedBody =  decodedResponse.getDecodedBody(responseBytes);
            // create exception with un-decodable content string and without exception body object.
            e = instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode),
                decodedResponse.getSourceResponse(), responseBytes, decodedBody);
        }

        if (e instanceof RuntimeException) {
            throw LOGGER.logExceptionAsError((RuntimeException) e);
        } else {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    private Object handleRestResponseReturnType(final HttpResponseDecoder.HttpDecodedResponse response,
                                                final SwaggerMethodParser methodParser,
                                                final Type entityType) {
        if (methodParser.isStreamResponse()) {
            return new StreamResponse(response.getSourceResponse());
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, Response.class)) {
            final Type bodyType = TypeUtil.getRestResponseBodyType(entityType);
            if (TypeUtil.isTypeOrSubTypeOf(bodyType, Void.class)) {
                response.getSourceResponse().close();
                return createResponse(response, entityType, null);
            } else {
                Object bodyAsObject =  handleBodyReturnType(response, methodParser, bodyType);
                Response<?> httpResponse = createResponse(response, entityType, bodyAsObject);
                if (httpResponse == null) {
                    return createResponse(response, entityType, null);
                }
                return httpResponse;
            }
        } else {
            // For now, we're just throwing if the Maybe didn't emit a value.
            return handleBodyReturnType(response, methodParser, entityType);
        }
    }

    private Object handleBodyReturnType(final HttpResponseDecoder.HttpDecodedResponse response,
                                        final SwaggerMethodParser methodParser, final Type entityType) {
        final int responseStatusCode = response.getSourceResponse().getStatusCode();
        final HttpMethod httpMethod = methodParser.getHttpMethod();
        final Type returnValueWireType = methodParser.getReturnValueWireType();

        final Object result;
        if (httpMethod == HttpMethod.HEAD
            && (TypeUtil.isTypeOrSubTypeOf(
            entityType, Boolean.TYPE) || TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.class))) {
            boolean isSuccess = (responseStatusCode / 100) == 2;
            result = isSuccess;
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, byte[].class)) {
            // byte[]
            BinaryData binaryData = response.getSourceResponse().getBodyAsBinaryData();
            byte[] responseBodyBytes = binaryData != null ? binaryData.toBytes() : null;
            if (returnValueWireType == Base64Url.class) {
                // Base64Url
                responseBodyBytes = new Base64Url(responseBodyBytes).decodedBytes();
            }
            result = responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null;
        }  else if (TypeUtil.isTypeOrSubTypeOf(entityType, BinaryData.class)) {
            // BinaryData
            // The raw response is directly used to create an instance of BinaryData which then provides
            // different methods to read the response. The reading of the response is delayed until BinaryData
            // is read and depending on which format the content is converted into, the response is not necessarily
            // fully copied into memory resulting in lesser overall memory usage.
            result = response.getSourceResponse().getBodyAsBinaryData();
        } else {
            // Object or Page<T>
            result = response.getDecodedBody((byte[]) null);
        }
        return result;
    }

    /**
     * Handle the provided asynchronous HTTP response and return the deserialized value.
     *
     * @param httpDecodedResponse the asynchronous HTTP response to the original HTTP request
     * @param methodParser the SwaggerMethodParser that the request originates from
     * @param returnType the type of value that will be returned
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return the deserialized result
     */
    private Object handleRestReturnType(final HttpResponseDecoder.HttpDecodedResponse httpDecodedResponse,
                                        final SwaggerMethodParser methodParser,
                                        final Type returnType,
                                        final Context context,
                                        final RequestOptions options,
                                        EnumSet<ErrorOptions> errorOptions) {
        final HttpResponseDecoder.HttpDecodedResponse expectedResponse =
            ensureExpectedStatus(httpDecodedResponse, methodParser, options, errorOptions);
        final Object result;

        if (TypeUtil.isTypeOrSubTypeOf(returnType, void.class) || TypeUtil.isTypeOrSubTypeOf(returnType,
            Void.class)) {
            // ProxyMethod ReturnType: Void
            result = expectedResponse;
        } else {
            // ProxyMethod ReturnType: T where T != async (Mono, Flux) or sync Void
            // Block the deserialization until a value T is received
            result = handleRestResponseReturnType(httpDecodedResponse, methodParser, returnType);
        }
        return result;
    }

    public void updateRequest(RequestDataConfiguration requestDataConfiguration, SerializerAdapter serializerAdapter) throws IOException {
        boolean isJson = requestDataConfiguration.isJson();
        HttpRequest request = requestDataConfiguration.getHttpRequest();
        Object bodyContentObject = requestDataConfiguration.getBodyContent();

        if (isJson) {
            request.setBody(serializerAdapter.serializeToBytes(bodyContentObject, SerializerEncoding.JSON));
        } else if (bodyContentObject instanceof byte[]) {
            request.setBody((byte[]) bodyContentObject);
        } else if (bodyContentObject instanceof String) {
            final String bodyContentString = (String) bodyContentObject;
            if (!bodyContentString.isEmpty()) {
                request.setBody(bodyContentString);
            }
        } else if (bodyContentObject instanceof ByteBuffer) {
            if (((ByteBuffer) bodyContentObject).hasArray()) {
                request.setBody(((ByteBuffer) bodyContentObject).array());
            } else {
                byte[] array = new byte[((ByteBuffer) bodyContentObject).remaining()];
                ((ByteBuffer) bodyContentObject).get(array);
                request.setBody(array);
            }
        } else {
            byte[] serializedBytes = serializerAdapter
                .serializeToBytes(bodyContentObject, SerializerEncoding.fromHeaders(request.getHeaders()));
            request.setBody(serializedBytes);
        }
    }
}
