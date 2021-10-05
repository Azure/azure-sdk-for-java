// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.ContentType;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.implementation.AccessibleByteArrayOutputStream;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.implementation.http.UnexpectedExceptionInformation;
import com.azure.core.implementation.serializer.HttpResponseDecoder;
import com.azure.core.implementation.serializer.HttpResponseDecoder.HttpDecodedResponse;
import com.azure.core.util.Base64Url;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProxy;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.util.context.ContextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.azure.core.implementation.serializer.HttpResponseBodyDecoder.shouldEagerlyReadResponse;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * Type to create a proxy implementation for an interface describing REST API methods.
 *
 * RestProxy can create proxy implementations for interfaces with methods that return deserialized Java objects as well
 * as asynchronous Single objects that resolve to a deserialized Java object.
 */
public final class RestProxy implements InvocationHandler {
    private static final ByteBuffer VALIDATION_BUFFER = ByteBuffer.allocate(0);
    private static final String BODY_TOO_LARGE = "Request body emitted %d bytes, more than the expected %d bytes.";
    private static final String BODY_TOO_SMALL = "Request body emitted %d bytes, less than the expected %d bytes.";
    private static final String MUST_IMPLEMENT_PAGE_ERROR =
        "Unable to create PagedResponse<T>. Body must be of a type that implements: " + Page.class;

    private static final ResponseConstructorsCache RESPONSE_CONSTRUCTORS_CACHE = new ResponseConstructorsCache();

    private final ClientLogger logger = new ClientLogger(RestProxy.class);
    private final HttpPipeline httpPipeline;
    private final SerializerAdapter serializer;
    private final SwaggerInterfaceParser interfaceParser;
    private final HttpResponseDecoder decoder;

    /**
     * Create a RestProxy.
     *
     * @param httpPipeline the HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
     * @param serializer the serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser the parser that contains information about the interface describing REST API methods that
     * this RestProxy "implements".
     */
    private RestProxy(HttpPipeline httpPipeline, SerializerAdapter serializer, SwaggerInterfaceParser interfaceParser) {
        this.httpPipeline = httpPipeline;
        this.serializer = serializer;
        this.interfaceParser = interfaceParser;
        this.decoder = new HttpResponseDecoder(this.serializer);
    }

    /**
     * Get the SwaggerMethodParser for the provided method. The Method must exist on the Swagger interface that this
     * RestProxy was created to "implement".
     *
     * @param method the method to get a SwaggerMethodParser for
     * @return the SwaggerMethodParser for the provided method
     */
    private SwaggerMethodParser getMethodParser(Method method) {
        return interfaceParser.getMethodParser(method);
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

    @Override
    public Object invoke(Object proxy, final Method method, Object[] args) {
        validateResumeOperationIsNotPresent(method);

        try {
            final SwaggerMethodParser methodParser = getMethodParser(method);
            final HttpRequest request = createHttpRequest(methodParser, args);
            Context context = methodParser.setContext(args)
                .addData("caller-method", methodParser.getFullyQualifiedMethodName())
                .addData("azure-eagerly-read-response", shouldEagerlyReadResponse(methodParser.getReturnType()));
            context = startTracingSpan(method, context);

            if (request.getBody() != null) {
                request.setBody(validateLength(request));
            }

            RequestOptions options = methodParser.setRequestOptions(args);
            if (options != null) {
                options.getRequestCallback().accept(request);
            }

            final Mono<HttpResponse> asyncResponse = send(request, context);

            Mono<HttpDecodedResponse> asyncDecodedResponse = this.decoder.decode(asyncResponse, methodParser);

            return handleRestReturnType(asyncDecodedResponse, methodParser,
                methodParser.getReturnType(), context, options);
        } catch (IOException e) {
            throw logger.logExceptionAsError(Exceptions.propagate(e));
        }
    }

    @SuppressWarnings("deprecation")
    void validateResumeOperationIsNotPresent(Method method) {
        // Use the fully-qualified class name as javac will throw deprecation warnings on imports when the class is
        // marked as deprecated.
        if (method.isAnnotationPresent(com.azure.core.annotation.ResumeOperation.class)) {
            throw logger.logExceptionAsError(Exceptions.propagate(new Exception("'ResumeOperation' isn't supported.")));
        }
    }

    static Flux<ByteBuffer> validateLength(final HttpRequest request) {
        final Flux<ByteBuffer> bbFlux = request.getBody();
        if (bbFlux == null) {
            return Flux.empty();
        }

        final long expectedLength = Long.parseLong(request.getHeaders().getValue("Content-Length"));

        return Flux.defer(() -> {
            final long[] currentTotalLength = new long[1];
            return Flux.concat(bbFlux, Flux.just(VALIDATION_BUFFER)).handle((buffer, sink) -> {
                if (buffer == null) {
                    return;
                }

                if (buffer == VALIDATION_BUFFER) {
                    if (expectedLength != currentTotalLength[0]) {
                        sink.error(new UnexpectedLengthException(String.format(BODY_TOO_SMALL,
                            currentTotalLength[0], expectedLength), currentTotalLength[0], expectedLength));
                    } else {
                        sink.complete();
                    }
                    return;
                }

                currentTotalLength[0] += buffer.remaining();
                if (currentTotalLength[0] > expectedLength) {
                    sink.error(new UnexpectedLengthException(String.format(BODY_TOO_LARGE,
                        currentTotalLength[0], expectedLength), currentTotalLength[0], expectedLength));
                    return;
                }

                sink.next(buffer);
            });
        });
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
     * Create a HttpRequest for the provided Swagger method using the provided arguments.
     *
     * @param methodParser the Swagger method parser to use
     * @param args the arguments to use to populate the method's annotation values
     * @return a HttpRequest
     * @throws IOException thrown if the body contents cannot be serialized
     */
    private HttpRequest createHttpRequest(SwaggerMethodParser methodParser, Object[] args) throws IOException {
        // Sometimes people pass in a full URL for the value of their PathParam annotated argument.
        // This definitely happens in paging scenarios. In that case, just use the full URL and
        // ignore the Host annotation.
        final String path = methodParser.setPath(args);
        final UrlBuilder pathUrlBuilder = UrlBuilder.parse(path);

        final UrlBuilder urlBuilder;
        if (pathUrlBuilder.getScheme() != null) {
            urlBuilder = pathUrlBuilder;
        } else {
            urlBuilder = new UrlBuilder();

            methodParser.setSchemeAndHost(args, urlBuilder);

            // Set the path after host, concatenating the path
            // segment in the host.
            if (path != null && !path.isEmpty() && !"/".equals(path)) {
                String hostPath = urlBuilder.getPath();
                if (hostPath == null || hostPath.isEmpty() || "/".equals(hostPath) || path.contains("://")) {
                    urlBuilder.setPath(path);
                } else {
                    if (path.startsWith("/")) {
                        urlBuilder.setPath(hostPath + path);
                    } else {
                        urlBuilder.setPath(hostPath + "/" + path);
                    }
                }
            }
        }

        methodParser.setEncodedQueryParameters(args, urlBuilder);

        final URL url = urlBuilder.toUrl();
        final HttpRequest request = configRequest(new HttpRequest(methodParser.getHttpMethod(), url),
            methodParser, args);

        // Headers from Swagger method arguments always take precedence over inferred headers from body types
        HttpHeaders httpHeaders = request.getHeaders();
        methodParser.setHeaders(args, httpHeaders);

        return request;
    }

    @SuppressWarnings("unchecked")
    private HttpRequest configRequest(final HttpRequest request, final SwaggerMethodParser methodParser,
        final Object[] args) throws IOException {
        final Object bodyContentObject = methodParser.setBody(args);
        if (bodyContentObject == null) {
            request.getHeaders().set("Content-Length", "0");
        } else {
            // We read the content type from the @BodyParam annotation
            String contentType = methodParser.getBodyContentType();

            // If this is null or empty, the service interface definition is incomplete and should
            // be fixed to ensure correct definitions are applied
            if (contentType == null || contentType.isEmpty()) {
                if (bodyContentObject instanceof byte[] || bodyContentObject instanceof String) {
                    contentType = ContentType.APPLICATION_OCTET_STREAM;
                } else {
                    contentType = ContentType.APPLICATION_JSON;
                }
            }

            request.getHeaders().set("Content-Type", contentType);
            if (bodyContentObject instanceof BinaryData) {
                BinaryData binaryData = (BinaryData) bodyContentObject;
                if (binaryData.getLength() != null) {
                    request.setHeader("Content-Length", binaryData.getLength().toString());
                }
                // The request body is not read here. The call to `toFluxByteBuffer()` lazily converts the underlying
                // content of BinaryData to a Flux<ByteBuffer> which is then read by HttpClient implementations when
                // sending the request to the service. There is no memory copy that happens here. Sources like
                // InputStream, File and Flux<ByteBuffer> will not be eagerly copied into memory until it's required
                // by the HttpClient implementations.
                request.setBody(binaryData.toFluxByteBuffer());
                return request;
            }

            // TODO(jogiles) this feels hacky
            boolean isJson = false;
            final String[] contentTypeParts = contentType.split(";");
            for (final String contentTypePart : contentTypeParts) {
                if (contentTypePart.trim().equalsIgnoreCase(ContentType.APPLICATION_JSON)) {
                    isJson = true;
                    break;
                }
            }

            if (isJson) {
                ByteArrayOutputStream stream = new AccessibleByteArrayOutputStream();
                serializer.serialize(bodyContentObject, SerializerEncoding.JSON, stream);

                request.setHeader("Content-Length", String.valueOf(stream.size()));
                request.setBody(Flux.defer(() -> Flux.just(ByteBuffer.wrap(stream.toByteArray(), 0, stream.size()))));
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
                ByteArrayOutputStream stream = new AccessibleByteArrayOutputStream();
                serializer.serialize(bodyContentObject, SerializerEncoding.fromHeaders(request.getHeaders()), stream);

                request.setHeader("Content-Length", String.valueOf(stream.size()));
                request.setBody(Flux.defer(() -> Flux.just(ByteBuffer.wrap(stream.toByteArray(), 0, stream.size()))));
            }
        }

        return request;
    }

    private Mono<HttpDecodedResponse> ensureExpectedStatus(final Mono<HttpDecodedResponse> asyncDecodedResponse,
        final SwaggerMethodParser methodParser, RequestOptions options) {
        return asyncDecodedResponse
            .flatMap(decodedHttpResponse -> ensureExpectedStatus(decodedHttpResponse, methodParser, options));
    }

    private static Exception instantiateUnexpectedException(final UnexpectedExceptionInformation exception,
        final HttpResponse httpResponse, final byte[] responseContent, final Object responseDecodedContent) {
        final int responseStatusCode = httpResponse.getStatusCode();
        final String contentType = httpResponse.getHeaderValue("Content-Type");
        final String bodyRepresentation;
        if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            bodyRepresentation = "(" + httpResponse.getHeaderValue("Content-Length") + "-byte body)";
        } else {
            bodyRepresentation = responseContent == null || responseContent.length == 0
                ? "(empty body)"
                : "\"" + new String(responseContent, StandardCharsets.UTF_8) + "\"";
        }

        Exception result;
        try {
            final Constructor<? extends HttpResponseException> exceptionConstructor = exception.getExceptionType()
                .getConstructor(String.class, HttpResponse.class, exception.getExceptionBodyType());
            result = exceptionConstructor.newInstance("Status code " + responseStatusCode + ", " + bodyRepresentation,
                httpResponse, responseDecodedContent);
        } catch (ReflectiveOperationException e) {
            String message = "Status code " + responseStatusCode + ", but an instance of "
                + exception.getExceptionType().getCanonicalName() + " cannot be created."
                + " Response body: " + bodyRepresentation;

            result = new IOException(message, e);
        }
        return result;
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
    private Mono<HttpDecodedResponse> ensureExpectedStatus(final HttpDecodedResponse decodedResponse,
        final SwaggerMethodParser methodParser, RequestOptions options) {
        final int responseStatusCode = decodedResponse.getSourceResponse().getStatusCode();

        // If the response was success or configured to not return an error status when the request fails, return the
        // decoded response.
        if (methodParser.isExpectedResponseStatusCode(responseStatusCode)
            || (options != null && !options.isThrowOnError())) {
            return Mono.just(decodedResponse);
        }

        // Otherwise, the response wasn't successful and the error object needs to be parsed.
        return decodedResponse.getSourceResponse().getBodyAsByteArray()
            .switchIfEmpty(Mono.defer(() -> {
                // bodyAsString() emits empty, indicating an empty body, create exception empty content string no
                // exception body object.
                return Mono.error(instantiateUnexpectedException(
                    methodParser.getUnexpectedException(responseStatusCode), decodedResponse.getSourceResponse(),
                    null, null));
            }))
            .flatMap(responseBytes -> decodedResponse.getDecodedBody(responseBytes)
                .switchIfEmpty(Mono.defer(() -> {
                    // decodedBody() emits empty, indicate unable to decode 'responseContent',
                    // create exception with un-decodable content string and without exception body object.
                    return Mono.error(instantiateUnexpectedException(
                        methodParser.getUnexpectedException(responseStatusCode),
                        decodedResponse.getSourceResponse(), responseBytes, null));
                }))
                .flatMap(decodedBody -> {
                    // decodedBody() emits empty, indicate unable to decode 'responseContent',
                    // create exception with un-decodable content string and without exception body object.
                    return Mono.error(instantiateUnexpectedException(
                        methodParser.getUnexpectedException(responseStatusCode),
                        decodedResponse.getSourceResponse(), responseBytes, decodedBody));
                }));
    }

    private Mono<?> handleRestResponseReturnType(final HttpDecodedResponse response,
        final SwaggerMethodParser methodParser,
        final Type entityType) {
        if (TypeUtil.isTypeOrSubTypeOf(entityType, Response.class)) {
            final Type bodyType = TypeUtil.getRestResponseBodyType(entityType);

            if (TypeUtil.isTypeOrSubTypeOf(bodyType, Void.class)) {
                return response.getSourceResponse().getBody().ignoreElements()
                    .then(createResponse(response, entityType, null));
            } else {
                return handleBodyReturnType(response, methodParser, bodyType)
                    .flatMap(bodyAsObject -> createResponse(response, entityType, bodyAsObject))
                    .switchIfEmpty(Mono.defer((Supplier<Mono<Response<?>>>) () -> createResponse(response,
                        entityType, null)));
            }
        } else {
            // For now, we're just throwing if the Maybe didn't emit a value.
            return handleBodyReturnType(response, methodParser, entityType);
        }
    }

    @SuppressWarnings("unchecked")
    private Mono<Response<?>> createResponse(HttpDecodedResponse response, Type entityType, Object bodyAsObject) {
        // determine the type of response class. If the type is the 'RestResponse' interface, we will use the
        // 'RestResponseBase' class instead.
        Class<? extends Response<?>> cls = (Class<? extends Response<?>>) TypeUtil.getRawClass(entityType);
        if (cls.equals(Response.class)) {
            cls = (Class<? extends Response<?>>) (Object) ResponseBase.class;
        } else if (cls.equals(PagedResponse.class)) {
            cls = (Class<? extends Response<?>>) (Object) PagedResponseBase.class;

            if (bodyAsObject != null && !TypeUtil.isTypeOrSubTypeOf(bodyAsObject.getClass(), Page.class)) {
                return monoError(logger, new RuntimeException(MUST_IMPLEMENT_PAGE_ERROR));
            }
        }

        return Mono.just(RESPONSE_CONSTRUCTORS_CACHE.get(cls))
            .switchIfEmpty(Mono.error(new RuntimeException("Cannot find suitable constructor for class " + cls)))
            .flatMap(ctr -> RESPONSE_CONSTRUCTORS_CACHE.invoke(ctr, response, bodyAsObject));
    }

    private Mono<?> handleBodyReturnType(final HttpDecodedResponse response,
        final SwaggerMethodParser methodParser, final Type entityType) {
        final int responseStatusCode = response.getSourceResponse().getStatusCode();
        final HttpMethod httpMethod = methodParser.getHttpMethod();
        final Type returnValueWireType = methodParser.getReturnValueWireType();

        final Mono<?> asyncResult;
        if (httpMethod == HttpMethod.HEAD
            && (TypeUtil.isTypeOrSubTypeOf(
            entityType, Boolean.TYPE) || TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.class))) {
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
            asyncResult = response.getDecodedBody((byte[]) null);
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
    private Object handleRestReturnType(final Mono<HttpDecodedResponse> asyncHttpDecodedResponse,
        final SwaggerMethodParser methodParser,
        final Type returnType,
        final Context context,
        final RequestOptions options) {
        final Mono<HttpDecodedResponse> asyncExpectedResponse =
            ensureExpectedStatus(asyncHttpDecodedResponse, methodParser, options)
                .doOnEach(RestProxy::endTracingSpan)
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
    private static void endTracingSpan(Signal<HttpDecodedResponse> signal) {
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
        HttpDecodedResponse httpDecodedResponse;
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

    /**
     * Create an instance of the default serializer.
     *
     * @return the default serializer
     */
    private static SerializerAdapter createDefaultSerializer() {
        return JacksonAdapter.createDefaultSerializerAdapter();
    }

    /**
     * Create the default HttpPipeline.
     *
     * @return the default HttpPipeline
     */
    private static HttpPipeline createDefaultPipeline() {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy());
        policies.add(new RetryPolicy());
        policies.add(new CookiePolicy());

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     *
     * @param swaggerInterface the Swagger interface to provide a proxy implementation for
     * @param <A> the type of the Swagger interface
     * @return a proxy implementation of the provided Swagger interface
     */
    public static <A> A create(Class<A> swaggerInterface) {
        return create(swaggerInterface, createDefaultPipeline(), createDefaultSerializer());
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     *
     * @param swaggerInterface the Swagger interface to provide a proxy implementation for
     * @param httpPipeline the HttpPipelinePolicy and HttpClient pipeline that will be used to send Http requests
     * @param <A> the type of the Swagger interface
     * @return a proxy implementation of the provided Swagger interface
     */
    public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline) {
        return create(swaggerInterface, httpPipeline, createDefaultSerializer());
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     *
     * @param swaggerInterface the Swagger interface to provide a proxy implementation for
     * @param httpPipeline the HttpPipelinePolicy and HttpClient pipline that will be used to send Http requests
     * @param serializer the serializer that will be used to convert POJOs to and from request and response bodies
     * @param <A> the type of the Swagger interface.
     * @return a proxy implementation of the provided Swagger interface
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline, SerializerAdapter serializer) {
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(swaggerInterface, serializer);
        final RestProxy restProxy = new RestProxy(httpPipeline, serializer, interfaceParser);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class<?>[]{swaggerInterface},
            restProxy);
    }
}
