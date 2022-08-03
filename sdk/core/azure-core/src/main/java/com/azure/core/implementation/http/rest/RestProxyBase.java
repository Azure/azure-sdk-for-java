// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.DecodeException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.exception.TooManyRedirectsException;
import com.azure.core.http.ContentType;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.implementation.http.UnexpectedExceptionInformation;
import com.azure.core.implementation.serializer.HttpResponseDecoder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProxy;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

public abstract class RestProxyBase {
    static final String MUST_IMPLEMENT_PAGE_ERROR =
        "Unable to create PagedResponse<T>. Body must be of a type that implements: " + Page.class;

    static final ResponseConstructorsCache RESPONSE_CONSTRUCTORS_CACHE = new ResponseConstructorsCache();
    private static final ResponseExceptionConstructorCache RESPONSE_EXCEPTION_CONSTRUCTOR_CACHE =
        new ResponseExceptionConstructorCache();

    // RestProxy is a commonly used class, use a static logger.
    static final ClientLogger LOGGER = new ClientLogger(RestProxyBase.class);

    final HttpPipeline httpPipeline;
    final SerializerAdapter serializer;
    final SwaggerInterfaceParser interfaceParser;
    final HttpResponseDecoder decoder;

    /**
     * Create a RestProxy.
     *
     * @param httpPipeline    the HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
     * @param serializer      the serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser the parser that contains information about the interface describing REST API methods that
     *                        this RestProxy "implements".
     */
    public RestProxyBase(HttpPipeline httpPipeline, SerializerAdapter serializer, SwaggerInterfaceParser interfaceParser) {
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
    public SwaggerMethodParser getMethodParser(Method method) {
        return interfaceParser.getMethodParser(method);
    }

    public final Object invoke(Object proxy, final Method method, RequestOptions options,
        EnumSet<ErrorOptions> errorOptions, Consumer<HttpRequest> requestCallback, SwaggerMethodParser methodParser,
        boolean isAsync, Object[] args) {
        RestProxyUtils.validateResumeOperationIsNotPresent(method);

        try {
            HttpRequest request = createHttpRequest(methodParser, serializer, isAsync, args);

            Context context = methodParser.setContext(args);
            context = RestProxyUtils.mergeRequestOptionsContext(context, options);

            context = context.addData("caller-method", methodParser.getFullyQualifiedMethodName())
                .addData("azure-eagerly-read-response", methodParser.isResponseEagerlyRead());

            return invoke(proxy, method, options, errorOptions, requestCallback, methodParser, request, context);

        } catch (IOException e) {
            if (isAsync) {
                return Mono.error(LOGGER.logExceptionAsError(Exceptions.propagate(e)));
            } else {
                throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
            }
        }
    }

    protected abstract Object invoke(Object proxy, Method method, RequestOptions options, EnumSet<ErrorOptions> errorOptions, Consumer<HttpRequest> httpRequestConsumer, SwaggerMethodParser methodParser, HttpRequest request, Context context);

    public abstract void updateRequest(RequestDataConfiguration requestDataConfiguration, SerializerAdapter serializerAdapter) throws IOException;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Response createResponse(HttpResponseDecoder.HttpDecodedResponse response, Type entityType,
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
        }

        // Otherwise, rely on reflection, for now, to get the best constructor to use to create the Response subtype.
        //
        // Ideally, in the future the SDKs won't need to dabble in reflection here as the Response subtypes should be
        // given a way to register their constructor as a callback method that consumes HttpDecodedResponse and the
        // body as an Object.
        MethodHandle constructorHandle = RESPONSE_CONSTRUCTORS_CACHE.get(cls);
        return RESPONSE_CONSTRUCTORS_CACHE.invoke(constructorHandle, response, bodyAsObject);
    }

    /**
     * Starts the tracing span for the current service call, additionally set metadata attributes on the span by passing
     * additional context information.
     *
     * @param method Service method being called.
     * @param context Context information about the current service call.
     * @return The updated context containing the span context.
     */
    static Context startTracingSpan(SwaggerMethodParser method, Context context) {
        // First check if tracing is enabled. This is an optimized operation, so it is done first.
        if (!TracerProxy.isTracingEnabled()) {
            return context;
        }

        // Then check if this method disabled tracing. This requires walking a linked list, so do it last.
        if ((boolean) context.getData(Tracer.DISABLE_TRACING_KEY).orElse(false)) {
            return context;
        }

        String spanName = method.getSpanName();
        context = TracerProxy.setSpanName(spanName, context);
        return TracerProxy.start(spanName, context);
    }

    // This handles each onX for the response mono.
    // The signal indicates the status and contains the metadata we need to end the tracing span.
    void endTracingSpan(HttpResponseDecoder.HttpDecodedResponse httpDecodedResponse, Throwable throwable, Context tracingContext) {
        if (tracingContext == null) {
            return;
        }

        // Get the context that was added to the mono, this will contain the information needed to end the span.
        Object disableTracingValue = (tracingContext.getData(Tracer.DISABLE_TRACING_KEY).isPresent()
            ? tracingContext.getData(Tracer.DISABLE_TRACING_KEY).get() : null);
        boolean disableTracing = Boolean.TRUE.equals(disableTracingValue != null ? disableTracingValue : false);

        if (disableTracing) {
            return;
        }

        int statusCode = 0;

        // On next contains the response information.
        if (httpDecodedResponse != null) {
            //noinspection ConstantConditions
            statusCode = httpDecodedResponse.getSourceResponse().getStatusCode();
        } else if (throwable != null) {
            // The last status available is on error, this contains the error thrown by the REST response.
            // Only HttpResponseException contain a status code, this is the base REST response.
            if (throwable instanceof HttpResponseException) {
                HttpResponseException exception = (HttpResponseException) throwable;
                statusCode = exception.getResponse().getStatusCode();
            }
        }
        TracerProxy.end(statusCode, throwable, tracingContext);
    }


    /**
     * Create a HttpRequest for the provided Swagger method using the provided arguments.
     *
     * @param methodParser the Swagger method parser to use
     * @param args the arguments to use to populate the method's annotation values
     * @return a HttpRequest
     * @throws IOException thrown if the body contents cannot be serialized
     */
    HttpRequest createHttpRequest(SwaggerMethodParser methodParser, SerializerAdapter serializerAdapter, boolean isAsync, Object[] args) throws IOException {
        // Sometimes people pass in a full URL for the value of their PathParam annotated argument.
        // This definitely happens in paging scenarios. In that case, just use the full URL and
        // ignore the Host annotation.
        final String path = methodParser.setPath(args, serializer);
        final UrlBuilder pathUrlBuilder = UrlBuilder.parse(path);

        final UrlBuilder urlBuilder;
        if (pathUrlBuilder.getScheme() != null) {
            urlBuilder = pathUrlBuilder;
        } else {
            urlBuilder = new UrlBuilder();

            methodParser.setSchemeAndHost(args, urlBuilder, serializer);

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

        methodParser.setEncodedQueryParameters(args, urlBuilder, serializer);

        final URL url = urlBuilder.toUrl();
        final HttpRequest request = configRequest(new HttpRequest(methodParser.getHttpMethod(), url),
            methodParser, serializerAdapter, isAsync, args);

        // Headers from Swagger method arguments always take precedence over inferred headers from body types
        HttpHeaders httpHeaders = request.getHeaders();
        methodParser.setHeaders(args, httpHeaders, serializer);

        return request;
    }

    @SuppressWarnings("unchecked")
    private HttpRequest configRequest(final HttpRequest request, final SwaggerMethodParser methodParser,
                                      SerializerAdapter serializerAdapter, boolean isAsync, final Object[] args) throws IOException {
        final Object bodyContentObject = methodParser.setBody(args, serializer);
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
                request.setBody(binaryData);
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
            updateRequest(new RequestDataConfiguration(request, methodParser, isJson, bodyContentObject), serializerAdapter);
        }

        return request;
    }

    /**
     * Creates the Unexpected Exception using the details provided in http response and its content.
     *
     * @param exception the excepion holding UnexpectedException's details.
     * @param httpResponse the http response to parse when constructing exception
     * @param responseContent the response body to use when constructing exception
     * @param responseDecodedContent the decoded response content to use when constructing exception
     * @return the Unexpected Exception
     */
    public Exception instantiateUnexpectedException(final UnexpectedExceptionInformation exception,
                                                    final HttpResponse httpResponse, final byte[] responseContent, final Object responseDecodedContent) {
        StringBuilder exceptionMessage = new StringBuilder("Status code ")
            .append(httpResponse.getStatusCode())
            .append(", ");

        final String contentType = httpResponse.getHeaderValue("Content-Type");
        if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            exceptionMessage.append("(").append(httpResponse.getHeaderValue("Content-Length")).append("-byte body)");
        } else if (responseContent == null || responseContent.length == 0) {
            exceptionMessage.append("(empty body)");
        } else {
            exceptionMessage.append("\"").append(new String(responseContent, StandardCharsets.UTF_8)).append("\"");
        }

        // For HttpResponseException types that exist in azure-core, call the constructor directly.
        Class<? extends HttpResponseException> exceptionType = exception.getExceptionType();
        if (exceptionType == HttpResponseException.class) {
            return new HttpResponseException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == ClientAuthenticationException.class) {
            return new ClientAuthenticationException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == DecodeException.class) {
            return new DecodeException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == ResourceExistsException.class) {
            return new ResourceExistsException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == ResourceModifiedException.class) {
            return new ResourceModifiedException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == ResourceNotFoundException.class) {
            return new ResourceNotFoundException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == TooManyRedirectsException.class) {
            return new TooManyRedirectsException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else {
            // Finally, if the HttpResponseException subclass doesn't exist in azure-core, use reflection to create a
            // new instance of it.
            try {
                MethodHandle handle = RESPONSE_EXCEPTION_CONSTRUCTOR_CACHE.get(exceptionType,
                    exception.getExceptionBodyType());
                return ResponseExceptionConstructorCache.invoke(handle, exceptionMessage.toString(), httpResponse,
                    responseDecodedContent);
            } catch (RuntimeException e) {
                // And if reflection fails, return an IOException.
                // TODO (alzimmer): Determine if this should be an IOException or HttpResponseException.
                exceptionMessage.append(". An instance of ")
                    .append(exceptionType.getCanonicalName())
                    .append(" couldn't be created.");
                return new IOException(exceptionMessage.toString(), e);
            }
        }
    }

    /**
     * Create an instance of the default serializer.
     *
     * @return the default serializer
     */
    public static SerializerAdapter createDefaultSerializer() {
        return JacksonAdapter.createDefaultSerializerAdapter();
    }

    /**
     * Create the default HttpPipeline.
     *
     * @return the default HttpPipeline
     */
    public static HttpPipeline createDefaultPipeline() {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy());
        policies.add(new RetryPolicy());
        policies.add(new CookiePolicy());

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
    }
}

