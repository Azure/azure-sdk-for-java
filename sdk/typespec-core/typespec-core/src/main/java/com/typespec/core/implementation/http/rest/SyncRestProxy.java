// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.http.rest;

import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.HttpPipeline;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.http.rest.RequestOptions;
import com.typespec.core.http.rest.Response;
import com.typespec.core.http.rest.StreamResponse;
import com.typespec.core.implementation.TypeUtil;
import com.typespec.core.implementation.serializer.HttpResponseDecoder;
import com.typespec.core.util.Base64Url;
import com.typespec.core.util.BinaryData;
import com.typespec.core.util.Context;
import com.typespec.core.util.serializer.SerializerAdapter;
import com.typespec.core.util.serializer.SerializerEncoding;
import com.typespec.json.JsonSerializable;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.function.Consumer;

import static com.typespec.core.implementation.ReflectionSerializable.serializeJsonSerializableToBytes;

public class SyncRestProxy extends RestProxyBase {
    /**
     * Create a RestProxy.
     *
     * @param httpPipeline the HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
     * @param serializer the serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser the parser that contains information about the interface describing REST API methods that
     */
    public SyncRestProxy(HttpPipeline httpPipeline, SerializerAdapter serializer,
        SwaggerInterfaceParser interfaceParser) {
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

    @SuppressWarnings({"try", "unused"})
    @Override
    public Object invoke(Object proxy, Method method, RequestOptions options, EnumSet<ErrorOptions> errorOptions,
        Consumer<HttpRequest> requestCallback, SwaggerMethodParser methodParser, HttpRequest request, Context context) {
        HttpResponseDecoder.HttpDecodedResponse decodedResponse = null;

        context = startTracingSpan(methodParser, context);
        try (AutoCloseable scope = tracer.makeSpanCurrent(context)) {
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

            int statusCode = decodedResponse.getSourceResponse().getStatusCode();
            tracer.end(statusCode >= 400 ? "" : null, null, context);

            return handleRestReturnType(decodedResponse, methodParser, methodParser.getReturnType(), context, options,
                errorOptions);
        } catch (Exception e) {
            tracer.end(null, e, context);
            sneakyThrows(e);
            return null;
        }
    }

    /**
     * Create a publisher that (1) emits error if the provided response {@code decodedResponse} has 'disallowed status
     * code' OR (2) emits provided response if it's status code ia allowed.
     * <p>
     * 'disallowed status code' is one of the status code defined in the provided SwaggerMethodParser or is in the int[]
     * of additional allowed status codes.
     *
     * @param decodedResponse The HttpResponse to check.
     * @param methodParser The method parser that contains information about the service interface method that initiated
     * the HTTP request.
     * @return An async-version of the provided decodedResponse.
     */
    private HttpResponseDecoder.HttpDecodedResponse ensureExpectedStatus(
        HttpResponseDecoder.HttpDecodedResponse decodedResponse, SwaggerMethodParser methodParser,
        RequestOptions options, EnumSet<ErrorOptions> errorOptions) {
        int responseStatusCode = decodedResponse.getSourceResponse().getStatusCode();

        // If the response was success or configured to not return an error status when the request fails, return the
        // decoded response.
        if (methodParser.isExpectedResponseStatusCode(responseStatusCode)
            || (options != null && errorOptions.contains(ErrorOptions.NO_THROW))) {
            return decodedResponse;
        }

        // Otherwise, the response wasn't successful and the error object needs to be parsed.
        BinaryData responseData = decodedResponse.getSourceResponse().getBodyAsBinaryData();
        byte[] responseBytes = responseData == null ? null : responseData.toBytes();
        if (responseBytes == null || responseBytes.length == 0) {
            //  No body, create exception empty content string no exception body object.
            throw instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode),
                decodedResponse.getSourceResponse(), null, null);
        } else {
            Object decodedBody = decodedResponse.getDecodedBody(responseBytes);
            // create exception with un-decodable content string and without exception body object.
            throw instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode),
                decodedResponse.getSourceResponse(), responseBytes, decodedBody);
        }
    }

    private Object handleRestResponseReturnType(HttpResponseDecoder.HttpDecodedResponse response,
        SwaggerMethodParser methodParser, Type entityType) {
        if (methodParser.isStreamResponse()) {
            return new StreamResponse(response.getSourceResponse());
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, Response.class)) {
            final Type bodyType = TypeUtil.getRestResponseBodyType(entityType);
            if (TypeUtil.isTypeOrSubTypeOf(bodyType, Void.class)) {
                response.getSourceResponse().close();
                return createResponse(response, entityType, null);
            } else {
                Object bodyAsObject = handleBodyReturnType(response, methodParser, bodyType);
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

    private Object handleBodyReturnType(HttpResponseDecoder.HttpDecodedResponse response,
        SwaggerMethodParser methodParser, Type entityType) {
        final int responseStatusCode = response.getSourceResponse().getStatusCode();
        final HttpMethod httpMethod = methodParser.getHttpMethod();
        final Type returnValueWireType = methodParser.getReturnValueWireType();

        final Object result;
        if (httpMethod == HttpMethod.HEAD
            && (TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.TYPE)
            || TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.class))) {
            result = (responseStatusCode / 100) == 2;
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, byte[].class)) {
            // byte[]
            BinaryData binaryData = response.getSourceResponse().getBodyAsBinaryData();
            byte[] responseBodyBytes = binaryData != null ? binaryData.toBytes() : null;
            if (returnValueWireType == Base64Url.class) {
                // Base64Url
                responseBodyBytes = new Base64Url(responseBodyBytes).decodedBytes();
            }
            result = responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null;
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, InputStream.class)) {
            result = response.getSourceResponse().getBodyAsInputStream();
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, BinaryData.class)) {
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
    private Object handleRestReturnType(HttpResponseDecoder.HttpDecodedResponse httpDecodedResponse,
        SwaggerMethodParser methodParser, Type returnType, Context context, RequestOptions options,
        EnumSet<ErrorOptions> errorOptions) {
        final HttpResponseDecoder.HttpDecodedResponse expectedResponse =
            ensureExpectedStatus(httpDecodedResponse, methodParser, options, errorOptions);
        final Object result;

        if (TypeUtil.isTypeOrSubTypeOf(returnType, void.class) || TypeUtil.isTypeOrSubTypeOf(returnType,
            Void.class)) {
            // ProxyMethod ReturnType: Void
            expectedResponse.close();
            result = null;
        } else {
            // ProxyMethod ReturnType: T where T != async (Mono, Flux) or sync Void
            // Block the deserialization until a value T is received
            result = handleRestResponseReturnType(httpDecodedResponse, methodParser, returnType);
        }
        return result;
    }

    public void updateRequest(RequestDataConfiguration requestDataConfiguration,
        SerializerAdapter serializerAdapter) throws IOException {
        boolean isJson = requestDataConfiguration.isJson();
        HttpRequest request = requestDataConfiguration.getHttpRequest();
        Object bodyContentObject = requestDataConfiguration.getBodyContent();

        // Attempt to use JsonSerializable or XmlSerializable in a separate block.
        if (supportsJsonSerializable(bodyContentObject.getClass())) {
            request.setBody(serializeJsonSerializableToBytes((JsonSerializable<?>) bodyContentObject));
            return;
        }

        if (supportsXmlSerializable(bodyContentObject.getClass())) {
            request.setBody(BinaryData.fromByteBuffer(serializeAsXmlSerializable(bodyContentObject)));
            return;
        }

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
            SerializerEncoding encoding = SerializerEncoding.fromHeaders(request.getHeaders());
            request.setBody(serializerAdapter.serializeToBytes(bodyContentObject, encoding));
        }
    }
}
