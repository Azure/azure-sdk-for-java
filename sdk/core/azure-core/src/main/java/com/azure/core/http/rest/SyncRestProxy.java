//// Copyright (c) Microsoft Corporation. All rights reserved.
//// Licensed under the MIT License.
//
//package com.azure.core.http.rest;
//
//import com.azure.core.exception.HttpResponseException;
//
//import com.azure.core.implementation.TypeUtil;
//import com.azure.core.implementation.http.rest.*;
//import com.azure.core.implementation.serializer.HttpResponseDecoder;
//import com.azure.core.implementation.serializer.HttpResponseDecoder.HttpDecodedResponse;
//import com.azure.core.util.Base64Url;
//import com.azure.core.util.BinaryData;
//import com.azure.core.util.Context;
//import com.azure.core.util.logging.ClientLogger;
//import com.azure.core.util.serializer.SerializerAdapter;
//import com.azure.core.util.tracing.Tracer;
//import com.azure.core.util.tracing.TracerProxy;
//import reactor.core.publisher.Mono;
//
//import java.io.IOException;
//import java.io.UncheckedIOException;
//import java.lang.invoke.MethodHandle;
//import java.lang.reflect.InvocationHandler;
//import java.lang.reflect.Method;
//import java.lang.reflect.Proxy;
//import java.lang.reflect.Type;
//import java.nio.ByteBuffer;
//
//import static com.azure.core.implementation.serializer.HttpResponseBodyDecoder.shouldEagerlyReadResponse;
//
////TODO (g2vinay): Address any Pending comments from this PR: https://github.com/Azure/azure-sdk-for-java/pull/27911/
///**
// * Type to create a proxy implementation for an interface describing REST API methods.
// *
// * RestProxy can create proxy implementations for interfaces with methods that return deserialized Java objects as well
// * as asynchronous Single objects that resolve to a deserialized Java object.
// */
//public final class SyncRestProxy implements InvocationHandler {
//    private static final ByteBuffer VALIDATION_BUFFER = ByteBuffer.allocate(0);
//    private static final String BODY_TOO_LARGE = "Request body emitted %d bytes, more than the expected %d bytes.";
//    private static final String BODY_TOO_SMALL = "Request body emitted %d bytes, less than the expected %d bytes.";
//    private static final String MUST_IMPLEMENT_PAGE_ERROR =
//        "Unable to create PagedResponse<T>. Body must be of a type that implements: " + Page.class;
//
//    private static final ResponseConstructorsCache RESPONSE_CONSTRUCTORS_CACHE = new ResponseConstructorsCache();
//
//    // RestProxy is a commonly used class, use a static logger.
//    private static final ClientLogger LOGGER = new ClientLogger(SyncRestProxy.class);
//
//    private final HttpPipeline httpPipeline;
//    private final SerializerAdapter serializer;
//    private final SwaggerInterfaceParser interfaceParser;
//    private final HttpResponseDecoder decoder;
//
//    /**
//     * Create a RestProxy.
//     *
//     * @param httpPipeline the HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
//     * @param serializer the serializer that will be used to convert response bodies to POJOs.
//     * @param interfaceParser the parser that contains information about the interface describing REST API methods that
//     * this RestProxy "implements".
//     */
//    private SyncRestProxy(HttpPipeline httpPipeline, SerializerAdapter serializer, SwaggerInterfaceParser interfaceParser) {
//        this.httpPipeline = httpPipeline;
//        this.serializer = serializer;
//        this.interfaceParser = interfaceParser;
//        this.decoder = new HttpResponseDecoder(this.serializer);
//    }
//
//    /**
//     * Get the SwaggerMethodParser for the provided method. The Method must exist on the Swagger interface that this
//     * RestProxy was created to "implement".
//     *
//     * @param method the method to get a SwaggerMethodParser for
//     * @return the SwaggerMethodParser for the provided method
//     */
//    private SwaggerMethodParser getMethodParser(Method method) {
//        return interfaceParser.getMethodParser(method);
//    }
//
//    /**
//     * Send the provided request asynchronously, applying any request policies provided to the HttpClient instance.
//     *
//     * @param request the HTTP request to send
//     * @param contextData the context
//     * @return a {@link Mono} that emits HttpResponse asynchronously
//     */
//    public HttpResponse send(HttpRequest request, Context contextData) {
//        return httpPipeline.sendSync(request, contextData);
//    }
//
//    @Override
//    public Object invoke(Object proxy, final Method method, Object[] args) {
//        RestProxyUtils.validateResumeOperationIsNotPresent(method);
//
//        final SwaggerMethodParser methodParser = getMethodParser(method);
//        HttpRequest request = null;
////        try {
////            request = RestProxyUtils.createHttpRequest(methodParser, serializer, false, args);
////        } catch (IOException e) {
////            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
////        }
//
//        Context context = methodParser.setContext(args);
//
//        RequestOptions options = methodParser.setRequestOptions(args);
//        context = RestProxyUtils.mergeRequestOptionsContext(context, options);
//
//        context = context.addData("caller-method", methodParser.getFullyQualifiedMethodName())
//            .addData("azure-eagerly-read-response", shouldEagerlyReadResponse(methodParser.getReturnType()));
//
//        HttpDecodedResponse decodedResponse = null;
//        Throwable throwable = null;
//        try {
//            context = startTracingSpan(method, context);
//
//            // If there is 'RequestOptions' apply its request callback operations before validating the body.
//            // This is because the callbacks may mutate the request body.
//            if (options != null) {
//                options.getRequestCallback().accept(request);
//            }
//
//            if (request.getBodyAsBinaryData() != null) {
//                request.setBody(RestProxyUtils.validateLengthSync(request));
//            }
//
//            final HttpResponse response = send(request, context);
//            decodedResponse = this.decoder.decodeSync(response, methodParser);
//            return handleRestReturnType(decodedResponse, methodParser, methodParser.getReturnType(), context, options);
//        } catch (RuntimeException e) {
//            throwable = e;
//            throw LOGGER.logExceptionAsError(e);
//        } finally {
//            if (decodedResponse != null || throwable != null) {
//                endTracingSpan(decodedResponse, throwable, context);
//            }
//        }
//    }
//
//    /**
//     * Starts the tracing span for the current service call, additionally set metadata attributes on the span by passing
//     * additional context information.
//     *
//     * @param method Service method being called.
//     * @param context Context information about the current service call.
//     * @return The updated context containing the span context.
//     */
//    private Context startTracingSpan(Method method, Context context) {
//        // First check if tracing is enabled. This is an optimized operation, so it is done first.
//        if (!TracerProxy.isTracingEnabled()) {
//            return context;
//        }
//
//        // Then check if this method disabled tracing. This requires walking a linked list, so do it last.
//        if ((boolean) context.getData(Tracer.DISABLE_TRACING_KEY).orElse(false)) {
//            return context;
//        }
//
//        String spanName = interfaceParser.getServiceName() + "." + method.getName();
//        context = TracerProxy.setSpanName(spanName, context);
//        return TracerProxy.start(spanName, context);
//    }
//
//    /**
//     * Create a publisher that (1) emits error if the provided response {@code decodedResponse} has 'disallowed status
//     * code' OR (2) emits provided response if it's status code ia allowed.
//     *
//     * 'disallowed status code' is one of the status code defined in the provided SwaggerMethodParser or is in the int[]
//     * of additional allowed status codes.
//     *
//     * @param decodedResponse The HttpResponse to check.
//     * @param methodParser The method parser that contains information about the service interface method that initiated
//     * the HTTP request.
//     * @return An async-version of the provided decodedResponse.
//     */
//    private HttpDecodedResponse ensureExpectedStatus(final HttpDecodedResponse decodedResponse,
//        final SwaggerMethodParser methodParser, RequestOptions options) {
//        final int responseStatusCode = decodedResponse.getSourceResponse().getStatusCode();
//
//        // If the response was success or configured to not return an error status when the request fails, return the
//        // decoded response.
//        if (methodParser.isExpectedResponseStatusCode(responseStatusCode)
//            || (options != null && options.getErrorOptions().contains(ErrorOptions.NO_THROW))) {
//            return decodedResponse;
//        }
//
//        // Otherwise, the response wasn't successful and the error object needs to be parsed.
//        Exception e;
//        BinaryData responseData = decodedResponse.getSourceResponse().getBodyAsBinaryData();
//        byte[] responseBytes = responseData == null ? null : responseData.toBytes();
//        if (responseBytes == null || responseBytes.length == 0) {
//            //  No body, create exception empty content string no exception body object.
//            e = instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode),
//                decodedResponse.getSourceResponse(), null, null);
//        } else {
//            Object decodedBody =  decodedResponse.getDecodedBodySync(responseBytes);
//            // create exception with un-decodable content string and without exception body object.
//            e = RestProxyUtils.instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode),
//                decodedResponse.getSourceResponse(), responseBytes, decodedBody);
//        }
//
//        if (e instanceof RuntimeException) {
//            throw LOGGER.logExceptionAsError((RuntimeException) e);
//        } else {
//            throw LOGGER.logExceptionAsError(new RuntimeException(e));
//        }
//    }
//
//    private Object handleRestResponseReturnType(final HttpDecodedResponse response,
//        final SwaggerMethodParser methodParser,
//        final Type entityType) {
//        if (TypeUtil.isTypeOrSubTypeOf(entityType, Response.class)) {
//            if (entityType.equals(StreamResponse.class)) {
//                return createResponseSync(response, entityType, null);
//            }
//
//            final Type bodyType = TypeUtil.getRestResponseBodyType(entityType);
//            if (TypeUtil.isTypeOrSubTypeOf(bodyType, Void.class)) {
//                response.getSourceResponse().close();
//                return createResponseSync(response, entityType, null);
//            } else {
//                Object bodyAsObject =  handleBodyReturnTypeSync(response, methodParser, bodyType);
//                Response<?> httpResponse = createResponseSync(response, entityType, bodyAsObject);
//                if (httpResponse == null) {
//                    return createResponseSync(response, entityType, null);
//                }
//                return httpResponse;
//            }
//        } else {
//            // For now, we're just throwing if the Maybe didn't emit a value.
//            return handleBodyReturnTypeSync(response, methodParser, entityType);
//        }
//    }
//
//
//    @SuppressWarnings("unchecked")
//    private Response<?> createResponseSync(HttpDecodedResponse response, Type entityType, Object bodyAsObject) {
//        final Class<? extends Response<?>> cls = (Class<? extends Response<?>>) TypeUtil.getRawClass(entityType);
//
//        final HttpResponse httpResponse = response.getSourceResponse();
//        final HttpRequest request = httpResponse.getRequest();
//        final int statusCode = httpResponse.getStatusCode();
//        final HttpHeaders headers = httpResponse.getHeaders();
//        final Object decodedHeaders = response.getDecodedHeaders();
//        // Inspection of the response type needs to be performed to determine which course of action should be taken to
//        // instantiate the Response<?> from the HttpResponse.
//        //
//        // If the type is either the Response or PagedResponse interface from azure-core a new instance of either
//        // ResponseBase or PagedResponseBase can be returned.
//        if (cls.equals(Response.class)) {
//            // For Response return a new instance of ResponseBase cast to the class.
//            return cls.cast(new ResponseBase<>(request, statusCode, headers, bodyAsObject, decodedHeaders));
//        } else if (cls.equals(PagedResponse.class)) {
//            // For PagedResponse return a new instance of PagedResponseBase cast to the class.
//            //
//            // PagedResponse needs an additional check that the bodyAsObject implements Page.
//            //
//            // If the bodyAsObject is null use the constructor that take items and continuation token with null.
//            // Otherwise, use the constructor that take Page.
//            if (bodyAsObject != null && !TypeUtil.isTypeOrSubTypeOf(bodyAsObject.getClass(), Page.class)) {
//                throw LOGGER.logExceptionAsError(new RuntimeException(MUST_IMPLEMENT_PAGE_ERROR));
//            } else if (bodyAsObject == null) {
//                return (cls.cast(new PagedResponseBase<>(request, statusCode, headers, null, null,
//                    decodedHeaders)));
//            } else {
//                return (cls.cast(new PagedResponseBase<>(request, statusCode, headers, (Page<?>) bodyAsObject,
//                    decodedHeaders)));
//            }
//        } else if (cls.equals(StreamResponse.class)) {
//            return new StreamResponse(request, httpResponse);
//        }
//
//        // Otherwise, rely on reflection, for now, to get the best constructor to use to create the Response sub-type.
//        //
//        // Ideally, in the future the SDKs won't need to dabble in reflection here as the Response sub-types should be
//        // given a way to register their constructor as a callback method that consumes HttpDecodedResponse and the
//        // body as an Object.
//
//        MethodHandle ctr = RESPONSE_CONSTRUCTORS_CACHE.get(cls);
//
//        if (ctr == null) {
//            throw LOGGER.logExceptionAsError(new RuntimeException("Cannot find suitable constructor for class " + cls));
//        }
//        return RESPONSE_CONSTRUCTORS_CACHE.invokeSync(ctr, response, bodyAsObject);
//    }
//
//    private Object handleBodyReturnTypeSync(final HttpDecodedResponse response,
//                                         final SwaggerMethodParser methodParser, final Type entityType) {
//        final int responseStatusCode = response.getSourceResponse().getStatusCode();
//        final HttpMethod httpMethod = methodParser.getHttpMethod();
//        final Type returnValueWireType = methodParser.getReturnValueWireType();
//
//        final Object result;
//        if (httpMethod == HttpMethod.HEAD
//            && (TypeUtil.isTypeOrSubTypeOf(
//            entityType, Boolean.TYPE) || TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.class))) {
//            boolean isSuccess = (responseStatusCode / 100) == 2;
//            result = isSuccess;
//        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, byte[].class)) {
//            // byte[]
//            byte[] responseBodyBytes = response.getSourceResponse().getBodyAsBinaryData().toBytes();
//            if (returnValueWireType == Base64Url.class) {
//                // Base64Url
//                responseBodyBytes = new Base64Url(responseBodyBytes).decodedBytes();
//            }
//            result = responseBodyBytes;
//        }  else if (TypeUtil.isTypeOrSubTypeOf(entityType, BinaryData.class)) {
//            // BinaryData
//            // The raw response is directly used to create an instance of BinaryData which then provides
//            // different methods to read the response. The reading of the response is delayed until BinaryData
//            // is read and depending on which format the content is converted into, the response is not necessarily
//            // fully copied into memory resulting in lesser overall memory usage.
//            result = response.getSourceResponse().getBodyAsBinaryData();
//        } else {
//            // Object or Page<T>
//            result = response.getDecodedBodySync((byte[]) null);
//        }
//        return result;
//    }
//
//    /**
//     * Handle the provided asynchronous HTTP response and return the deserialized value.
//     *
//     * @param httpDecodedResponse the asynchronous HTTP response to the original HTTP request
//     * @param methodParser the SwaggerMethodParser that the request originates from
//     * @param returnType the type of value that will be returned
//     * @param context Additional context that is passed through the Http pipeline during the service call.
//     * @return the deserialized result
//     */
//    private Object handleRestReturnType(final HttpDecodedResponse httpDecodedResponse,
//        final SwaggerMethodParser methodParser,
//        final Type returnType,
//        final Context context,
//        final RequestOptions options) {
//        final HttpDecodedResponse expectedResponse =
//            ensureExpectedStatus(httpDecodedResponse, methodParser, options);
//        final Object result;
//
//        if (TypeUtil.isTypeOrSubTypeOf(returnType, void.class) || TypeUtil.isTypeOrSubTypeOf(returnType,
//            Void.class)) {
//            // ProxyMethod ReturnType: Void
//            result = expectedResponse;
//        } else {
//            // ProxyMethod ReturnType: T where T != async (Mono, Flux) or sync Void
//            // Block the deserialization until a value T is received
//            result = handleRestResponseReturnType(httpDecodedResponse, methodParser, returnType);
//        }
//        return result;
//    }
//
//    // This handles each onX for the response mono.
//    // The signal indicates the status and contains the metadata we need to end the tracing span.
//    private static void endTracingSpan(HttpDecodedResponse httpDecodedResponse, Throwable throwable, Context tracingContext) {
//        if (tracingContext == null) {
//            return;
//        }
//
//        // Get the context that was added to the mono, this will contain the information needed to end the span.
//        Object disableTracingValue = (tracingContext.getData(Tracer.DISABLE_TRACING_KEY).isPresent()
//            ? tracingContext.getData(Tracer.DISABLE_TRACING_KEY).get() : null);
//        boolean disableTracing = Boolean.TRUE.equals(disableTracingValue != null ? disableTracingValue : false);
//
//        if (disableTracing) {
//            return;
//        }
//
//        int statusCode = 0;
//
//        // On next contains the response information.
//        if (httpDecodedResponse != null) {
//            //noinspection ConstantConditions
//            statusCode = httpDecodedResponse.getSourceResponse().getStatusCode();
//        } else if (throwable != null) {
//            // The last status available is on error, this contains the error thrown by the REST response.
//            // Only HttpResponseException contain a status code, this is the base REST response.
//            if (throwable instanceof HttpResponseException) {
//                HttpResponseException exception = (HttpResponseException) throwable;
//                statusCode = exception.getResponse().getStatusCode();
//            }
//        }
//        TracerProxy.end(statusCode, throwable, tracingContext);
//    }
//
//    /**
//     * Create a proxy implementation of the provided Swagger interface.
//     *
//     * @param swaggerInterface the Swagger interface to provide a proxy implementation for
//     * @param <A> the type of the Swagger interface
//     * @return a proxy implementation of the provided Swagger interface
//     */
//    public static <A> A create(Class<A> swaggerInterface) {
//        return create(swaggerInterface, RestProxyUtils.createDefaultPipeline(), RestProxyUtils.createDefaultSerializer());
//    }
//
//    /**
//     * Create a proxy implementation of the provided Swagger interface.
//     *
//     * @param swaggerInterface the Swagager interface to provide a proxy implementation for
//     * @param httpPipeline the HttpPipelinePolicy and HttpClient pipeline that will be used to send Http requests
//     * @param <A> the type of the Swagger interface
//     * @return a proxy implementation of the provided Swagger interface
//     */
//    public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline) {
//        return create(swaggerInterface, httpPipeline, RestProxyUtils.createDefaultSerializer());
//    }
//
//    /**
//     * Create a proxy implementation of the provided Swagger interface.
//     *
//     * @param swaggerInterface the Swagger interface to provide a proxy implementation for
//     * @param httpPipeline the HttpPipelinePolicy and HttpClient pipline that will be used to send Http requests
//     * @param serializer the serializer that will be used to convert POJOs to and from request and response bodies
//     * @param <A> the type of the Swagger interface.
//     * @return a proxy implementation of the provided Swagger interface
//     */
//    @SuppressWarnings("unchecked")
//    public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline, SerializerAdapter serializer) {
//        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(swaggerInterface, serializer);
//        final SyncRestProxy restProxy = new SyncRestProxy(httpPipeline, serializer, interfaceParser);
//        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class<?>[]{swaggerInterface},
//            restProxy);
//    }
//}
