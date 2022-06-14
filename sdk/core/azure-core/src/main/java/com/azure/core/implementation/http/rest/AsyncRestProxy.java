package com.azure.core.implementation.http.rest;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.*;
import com.azure.core.http.rest.*;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.implementation.serializer.HttpResponseDecoder;
import com.azure.core.util.Base64Url;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProxy;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.util.context.ContextView;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Consumer;

import static com.azure.core.implementation.serializer.HttpResponseBodyDecoder.shouldEagerlyReadResponse;

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
    public Mono<HttpResponse> send(HttpRequest request, Context contextData) {
        return httpPipeline.send(request, contextData);
    }


    public HttpRequest createHttpRequest(SwaggerMethodParser methodParser, Object[] args) throws IOException {
        return RestProxyUtils.createHttpRequest(methodParser, serializer, true, args);
    }

    @Override
    public Object invoke(Object proxy, final Method method, RequestOptions options, EnumSet<ErrorOptions> errorOptions,
                         Consumer<HttpRequest> requestCallback, SwaggerMethodParser methodParser, HttpRequest request,
                         Context context) {
        RestProxyUtils.validateResumeOperationIsNotPresent(method);

        //            final SwaggerMethodParser methodParser = getMethodParser(method);
//            final HttpRequest request = RestProxyUtils.createHttpRequest(methodParser, serializer, true, args);
//            Context context = methodParser.setContext(args);
//
//            RequestOptions options = methodParser.setRequestOptions(args);
//            context = RestProxyUtils.mergeRequestOptionsContext(context, options);
//
//            context = context.addData("caller-method", methodParser.getFullyQualifiedMethodName())
//                .addData("azure-eagerly-read-response", shouldEagerlyReadResponse(methodParser.getReturnType()));
//            context = startTracingSpan(method, context);

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
     * Starts the tracing span for the current service call, additionally set metadata attributes on the span by passing
     * additional context information.
     *
     * @param method Service method being called.
     * @param context Context information about the current service call.
     * @return The updated context containing the span context.
     */
    private Context startTracingSpan(Method method, Context context) {
        // First check if tracing is enabled. This is an optimized operation, so it is done first.
        if (!TracerProxy.isTracingEnabled()) {
            return context;
        }

        // Then check if this method disabled tracing. This requires walking a linked list, so do it last.
        if ((boolean) context.getData(Tracer.DISABLE_TRACING_KEY).orElse(false)) {
            return context;
        }

        String spanName = interfaceParser.getServiceName() + "." + method.getName();
        context = TracerProxy.setSpanName(spanName, context);
        return TracerProxy.start(spanName, context);
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
    private static Mono<HttpResponseDecoder.HttpDecodedResponse> ensureExpectedStatus(final Mono<HttpResponseDecoder.HttpDecodedResponse> asyncDecodedResponse,
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
                .map(bytes -> RestProxyUtils.instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode),
                    decodedResponse.getSourceResponse(), bytes, decodedResponse.getDecodedBody(bytes)))
                .switchIfEmpty(Mono.fromSupplier(() -> RestProxyUtils.instantiateUnexpectedException(
                    methodParser.getUnexpectedException(responseStatusCode), decodedResponse.getSourceResponse(),
                    null, null)))
                .flatMap(Mono::error);
        });
    }

    private static Mono<?> handleRestResponseReturnType(final HttpResponseDecoder.HttpDecodedResponse response,
                                                        final SwaggerMethodParser methodParser, final Type entityType) {
        if (TypeUtil.isTypeOrSubTypeOf(entityType, Response.class)) {
            if (entityType.equals(StreamResponse.class)) {
                return Mono.fromCallable(() -> createResponse(response, entityType, null));
            }

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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Response createResponse(HttpResponseDecoder.HttpDecodedResponse response, Type entityType,
                                           Object bodyAsObject) {
        final Class<? extends Response<?>> cls = (Class<? extends Response<?>>) TypeUtil.getRawClass(entityType);

        final HttpResponse httpResponse = response.getSourceResponse();
        final HttpRequest request = httpResponse.getRequest();
        final int statusCode = httpResponse.getStatusCode();
        final HttpHeaders headers = httpResponse.getHeaders();
        final Object decodedHeaders = response.getDecodedHeaders();
        // Inspection of the response type needs to be performed to determine which course of action should be taken to
        // instantiate the Response<?> from the HttpResponse.
        //
        // If the type is either the Response or PagedResponse interface from azure-core a new instance of either
        // ResponseBase or PagedResponseBase can be returned.
        if (cls.equals(Response.class)) {
            // For Response return a new instance of ResponseBase cast to the class.
            return cls.cast(new ResponseBase<>(request, statusCode, headers, bodyAsObject, decodedHeaders));
        } else if (cls.equals(PagedResponse.class)) {
            // For PagedResponse return a new instance of PagedResponseBase cast to the class.
            //
            // PagedResponse needs an additional check that the bodyAsObject implements Page.
            //
            // If the bodyAsObject is null use the constructor that take items and continuation token with null.
            // Otherwise, use the constructor that take Page.
            if (bodyAsObject != null && !TypeUtil.isTypeOrSubTypeOf(bodyAsObject.getClass(), Page.class)) {
                throw LOGGER.logExceptionAsError(new RuntimeException(MUST_IMPLEMENT_PAGE_ERROR));
            } else if (bodyAsObject == null) {
                return cls.cast(new PagedResponseBase<>(request, statusCode, headers, null, null, decodedHeaders));
            } else {
                return cls.cast(new PagedResponseBase<>(request, statusCode, headers, (Page<?>) bodyAsObject,
                    decodedHeaders));
            }
        } else if (cls.equals(StreamResponse.class)) {
            return new StreamResponse(request, httpResponse);
        }

        // Otherwise, rely on reflection, for now, to get the best constructor to use to create the Response subtype.
        //
        // Ideally, in the future the SDKs won't need to dabble in reflection here as the Response subtypes should be
        // given a way to register their constructor as a callback method that consumes HttpDecodedResponse and the
        // body as an Object.
        MethodHandle constructorHandle = RESPONSE_CONSTRUCTORS_CACHE.get(cls);
        return RESPONSE_CONSTRUCTORS_CACHE.invoke(constructorHandle, response, bodyAsObject);
    }

    private static Mono<?> handleBodyReturnType(final HttpResponseDecoder.HttpDecodedResponse response,
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
            if (methodParser.getReturnType().equals(StreamResponse.class)) {
                // TODO (kasobol-msft) this is a hack.
                // We don't need entity in that case but we can't change the else case yet
                // it somehow relies on eager consumption and tests hang otherwise.
                asyncResult = Mono.empty();
            } else {
                asyncResult = BinaryData.fromFlux(response.getSourceResponse().getBody());
            }
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
                .doOnEach(AsyncRestProxy::endTracingSpan)
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
    private static void endTracingSpan(Signal<HttpResponseDecoder.HttpDecodedResponse> signal) {
        if (!TracerProxy.isTracingEnabled()) {
            return;
        }

        // Ignore the on complete and on subscribe events, they don't contain the information needed to end the span.
        if (signal.isOnComplete() || signal.isOnSubscribe()) {
            return;
        }

        // Get the context that was added to the mono, this will contain the information needed to end the span.
        ContextView context = signal.getContextView();
        Optional<Context> tracingContext = context.getOrEmpty("TRACING_CONTEXT");
        boolean disableTracing = Boolean.TRUE.equals(context.getOrDefault(Tracer.DISABLE_TRACING_KEY, false));

        if (!tracingContext.isPresent() || disableTracing) {
            return;
        }

        int statusCode = 0;
        HttpResponseDecoder.HttpDecodedResponse httpDecodedResponse;
        Throwable throwable = null;

        // On next contains the response information.
        if (signal.hasValue()) {
            httpDecodedResponse = signal.get();
            //noinspection ConstantConditions
            statusCode = httpDecodedResponse.getSourceResponse().getStatusCode();
        } else if (signal.hasError()) {
            // The last status available is on error, this contains the error thrown by the REST response.
            throwable = signal.getThrowable();

            // Only HttpResponseException contain a status code, this is the base REST response.
            if (throwable instanceof HttpResponseException) {
                HttpResponseException exception = (HttpResponseException) throwable;
                statusCode = exception.getResponse().getStatusCode();
            }
        }

        TracerProxy.end(statusCode, throwable, tracingContext.get());
    }

}
