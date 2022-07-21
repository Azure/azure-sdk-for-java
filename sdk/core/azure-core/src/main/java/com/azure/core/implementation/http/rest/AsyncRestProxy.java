// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.implementation.serializer.HttpResponseDecoder;
import com.azure.core.util.Base64Url;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.tracing.TracerProxy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.util.context.ContextView;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.function.Consumer;

public class AsyncRestProxy extends RestProxyBase {
    /**
     * Create a RestProxy.
     *
     * @param httpPipeline    the HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
     * @param serializer      the serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser the parser that contains information about the interface describing REST API methods that
     */
    public AsyncRestProxy(HttpPipeline httpPipeline, SerializerAdapter serializer, SwaggerInterfaceParser interfaceParser) {
        super(httpPipeline, serializer, interfaceParser);
    }

    /**
     * Send the provided request asynchronously, applying any request policies provided to the HttpClient instance.
     *
     * @param request the HTTP request to send
     * @param contextData the context
     * @return a {@link Mono} that emits HttpResponse asynchronously
     */
    Mono<HttpResponse> send(HttpRequest request, Context contextData) {
        return httpPipeline.send(request, contextData);
    }


    public HttpRequest createHttpRequest(SwaggerMethodParser methodParser, Object[] args) throws IOException {
        return createHttpRequest(methodParser, serializer, true, args);
    }

    @Override
    public Object invoke(Object proxy, final Method method, RequestOptions options, EnumSet<ErrorOptions> errorOptions,
                         Consumer<HttpRequest> requestCallback, SwaggerMethodParser methodParser, HttpRequest request,
                         Context context) {
        RestProxyUtils.validateResumeOperationIsNotPresent(method);

        context = startTracingSpan(method, context);

        // If there is 'RequestOptions' apply its request callback operations before validating the body.
        // This is because the callbacks may mutate the request body.
        if (options != null && requestCallback != null) {
            requestCallback.accept(request);
        }

        Context finalContext = context;
        final Mono<HttpResponse> asyncResponse = RestProxyUtils.validateLengthAsync(request)
            .flatMap(r -> send(r, finalContext));

        Mono<HttpResponseDecoder.HttpDecodedResponse> asyncDecodedResponse = this.decoder.decode(asyncResponse, methodParser);

        return handleRestReturnType(asyncDecodedResponse, methodParser,
            methodParser.getReturnType(), context, options, errorOptions);
    }

    /**
     * Create a publisher that (1) emits error if the provided response {@code decodedResponse} has 'disallowed status
     * code' OR (2) emits provided response if it's status code ia allowed.
     *
     * 'disallowed status code' is one of the status code defined in the provided SwaggerMethodParser or is in the int[]
     * of additional allowed status codes.
     *
     * @param asyncDecodedResponse The HttpResponse to check.
     * @param methodParser The method parser that contains information about the service interface method that initiated
     * the HTTP request.
     * @param options Additional options passed as part of the request.
     * @return An async-version of the provided decodedResponse.
     */
    private Mono<HttpResponseDecoder.HttpDecodedResponse> ensureExpectedStatus(final Mono<HttpResponseDecoder.HttpDecodedResponse> asyncDecodedResponse,
                                                                               final SwaggerMethodParser methodParser, RequestOptions options, EnumSet<ErrorOptions> errorOptions) {
        return asyncDecodedResponse.flatMap(decodedResponse -> {
            int responseStatusCode = decodedResponse.getSourceResponse().getStatusCode();

            // If the response was success or configured to not return an error status when the request fails,
            // return the decoded response.
            if (methodParser.isExpectedResponseStatusCode(responseStatusCode)
                || (options != null && errorOptions.contains(ErrorOptions.NO_THROW))) {
                return Mono.just(decodedResponse);
            }

            // Otherwise, the response wasn't successful and the error object needs to be parsed.
            //
            // First, try to create an error with the response body.
            // If there is no response body create an error without the response body.
            // Finally, return the error reactively.
            return decodedResponse.getSourceResponse().getBodyAsByteArray()
                .map(bytes -> instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode),
                    decodedResponse.getSourceResponse(), bytes, decodedResponse.getDecodedBody(bytes)))
                .switchIfEmpty(Mono.fromSupplier(() -> instantiateUnexpectedException(
                    methodParser.getUnexpectedException(responseStatusCode), decodedResponse.getSourceResponse(),
                    null, null)))
                .flatMap(Mono::error);
        });
    }

    private Mono<?> handleRestResponseReturnType(final HttpResponseDecoder.HttpDecodedResponse response,
                                                 final SwaggerMethodParser methodParser, final Type entityType) {
        if (methodParser.isStreamResponse()) {
            return Mono.fromSupplier(() -> new StreamResponse(response.getSourceResponse()));
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, Response.class)) {
            final Type bodyType = TypeUtil.getRestResponseBodyType(entityType);
            if (TypeUtil.isTypeOrSubTypeOf(bodyType, Void.class)) {
                return response.getSourceResponse().getBody().ignoreElements()
                    .then(Mono.fromCallable(() -> createResponse(response, entityType, null)));
            } else {
                return handleBodyReturnType(response, methodParser, bodyType)
                    .map(bodyAsObject -> createResponse(response, entityType, bodyAsObject))
                    .switchIfEmpty(Mono.fromCallable(() -> createResponse(response, entityType, null)));
            }
        } else {
            // For now, we're just throwing if the Maybe didn't emit a value.
            return handleBodyReturnType(response, methodParser, entityType);
        }
    }

    private Mono<?> handleBodyReturnType(final HttpResponseDecoder.HttpDecodedResponse response,
                                         final SwaggerMethodParser methodParser, final Type entityType) {
        final int responseStatusCode = response.getSourceResponse().getStatusCode();
        final HttpMethod httpMethod = methodParser.getHttpMethod();
        final Type returnValueWireType = methodParser.getReturnValueWireType();

        final Mono<?> asyncResult;
        if (httpMethod == HttpMethod.HEAD
            && (TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.TYPE)
            || TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.class))) {
            boolean isSuccess = (responseStatusCode / 100) == 2;
            asyncResult = Mono.just(isSuccess);
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, byte[].class)) {
            // Mono<byte[]>
            Mono<byte[]> responseBodyBytesAsync = response.getSourceResponse().getBodyAsByteArray();
            if (returnValueWireType == Base64Url.class) {
                // Mono<Base64Url>
                responseBodyBytesAsync = responseBodyBytesAsync
                    .mapNotNull(base64UrlBytes -> new Base64Url(base64UrlBytes).decodedBytes());
            }
            asyncResult = responseBodyBytesAsync;
        } else if (FluxUtil.isFluxByteBuffer(entityType)) {
            // Mono<Flux<ByteBuffer>>
            asyncResult = Mono.just(response.getSourceResponse().getBody());
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, BinaryData.class)) {
            // Mono<BinaryData>
            // The raw response is directly used to create an instance of BinaryData which then provides
            // different methods to read the response. The reading of the response is delayed until BinaryData
            // is read and depending on which format the content is converted into, the response is not necessarily
            // fully copied into memory resulting in lesser overall memory usage.
            asyncResult = BinaryData.fromFlux(response.getSourceResponse().getBody());
        } else {
            // Mono<Object> or Mono<Page<T>>
            asyncResult = response.getSourceResponse().getBodyAsByteArray().mapNotNull(response::getDecodedBody);
        }
        return asyncResult;
    }

    /**
     * Handle the provided asynchronous HTTP response and return the deserialized value.
     *
     * @param asyncHttpDecodedResponse the asynchronous HTTP response to the original HTTP request
     * @param methodParser the SwaggerMethodParser that the request originates from
     * @param returnType the type of value that will be returned
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return the deserialized result
     */
    private Object handleRestReturnType(final Mono<HttpResponseDecoder.HttpDecodedResponse> asyncHttpDecodedResponse,
                                        final SwaggerMethodParser methodParser,
                                        final Type returnType,
                                        final Context context,
                                        final RequestOptions options,
                                        final EnumSet<ErrorOptions> errorOptionsSet) {
        final Mono<HttpResponseDecoder.HttpDecodedResponse> asyncExpectedResponse =
            ensureExpectedStatus(asyncHttpDecodedResponse, methodParser, options, errorOptionsSet)
                .doOnEach(this::endTracingSpan)
                .contextWrite(reactor.util.context.Context.of("TRACING_CONTEXT", context));

        final Object result;
        if (TypeUtil.isTypeOrSubTypeOf(returnType, Mono.class)) {
            final Type monoTypeParam = TypeUtil.getTypeArgument(returnType);
            if (TypeUtil.isTypeOrSubTypeOf(monoTypeParam, Void.class)) {
                // ProxyMethod ReturnType: Mono<Void>
                result = asyncExpectedResponse.then();
            } else {
                // ProxyMethod ReturnType: Mono<? extends RestResponseBase<?, ?>>
                result = asyncExpectedResponse.flatMap(response ->
                    handleRestResponseReturnType(response, methodParser, monoTypeParam));
            }
        } else if (FluxUtil.isFluxByteBuffer(returnType)) {
            // ProxyMethod ReturnType: Flux<ByteBuffer>
            result = asyncExpectedResponse.flatMapMany(ar -> ar.getSourceResponse().getBody());
        } else if (TypeUtil.isTypeOrSubTypeOf(returnType, void.class) || TypeUtil.isTypeOrSubTypeOf(returnType,
            Void.class)) {
            // ProxyMethod ReturnType: Void
            asyncExpectedResponse.block();
            result = null;
        } else {
            // ProxyMethod ReturnType: T where T != async (Mono, Flux) or sync Void
            // Block the deserialization until a value T is received
            result = asyncExpectedResponse
                .flatMap(httpResponse -> handleRestResponseReturnType(httpResponse, methodParser, returnType))
                .block();
        }
        return result;
    }

    // This handles each onX for the response mono.
    // The signal indicates the status and contains the metadata we need to end the tracing span.
    private void endTracingSpan(Signal<HttpResponseDecoder.HttpDecodedResponse> signal) {
        if (!TracerProxy.isTracingEnabled()) {
            return;
        }

        // Ignore the on complete and on subscribe events, they don't contain the information needed to end the span.
        if (signal.isOnComplete() || signal.isOnSubscribe()) {
            return;
        }

        // Get the context that was added to the mono, this will contain the information needed to end the span.
        ContextView context = signal.getContextView();
        HttpResponseDecoder.HttpDecodedResponse httpDecodedResponse = signal.hasValue() ? signal.get() : null;
        Throwable throwable = signal.hasError() ? signal.getThrowable() : null;

        endTracingSpan(httpDecodedResponse, throwable, (Context) context.getOrEmpty("TRACING_CONTEXT").get());
    }

    @SuppressWarnings("unchecked")
    public void updateRequest(RequestDataConfiguration requestDataConfiguration, SerializerAdapter serializerAdapter) throws IOException {
        boolean isJson = requestDataConfiguration.isJson();
        HttpRequest request = requestDataConfiguration.getHttpRequest();
        Object bodyContentObject = requestDataConfiguration.getBodyContent();
        SwaggerMethodParser methodParser = requestDataConfiguration.getMethodParser();

        if (isJson) {
            request.setBody(serializerAdapter.serializeToBytes(bodyContentObject, SerializerEncoding.JSON));
        } else if (FluxUtil.isFluxByteBuffer(methodParser.getBodyJavaType())) {
            // Content-Length or Transfer-Encoding: chunked must be provided by a user-specified header when a
            // Flowable<byte[]> is given for the body.
            request.setBody((Flux<ByteBuffer>) bodyContentObject);
        } else if (bodyContentObject instanceof byte[]) {
            request.setBody((byte[]) bodyContentObject);
        } else if (bodyContentObject instanceof String) {
            final String bodyContentString = (String) bodyContentObject;
            if (!bodyContentString.isEmpty()) {
                request.setBody(bodyContentString);
            }
        } else if (bodyContentObject instanceof ByteBuffer) {
            request.setBody(Flux.just((ByteBuffer) bodyContentObject));
        } else {
            request.setBody(serializerAdapter.serializeToBytes(bodyContentObject,
                SerializerEncoding.fromHeaders(request.getHeaders())));
        }
    }

}
