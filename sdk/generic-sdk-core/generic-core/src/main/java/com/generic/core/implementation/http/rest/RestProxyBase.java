// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.rest;

import com.generic.core.http.Response;
import com.generic.core.http.exception.HttpExceptionType;
import com.generic.core.http.exception.HttpResponseException;
import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.pipeline.HttpPipeline;
import com.generic.core.implementation.ReflectionSerializable;
import com.generic.core.implementation.ReflectiveInvoker;
import com.generic.core.implementation.TypeUtil;
import com.generic.core.implementation.http.ContentType;
import com.generic.core.implementation.http.SimpleResponse;
import com.generic.core.implementation.http.UnexpectedExceptionInformation;
import com.generic.core.implementation.http.serializer.HttpResponseDecoder;
import com.generic.core.implementation.http.serializer.MalformedValueException;
import com.generic.core.implementation.util.UrlBuilder;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Context;
import com.generic.core.models.Headers;
import com.generic.core.models.RequestOptions;
import com.generic.core.util.logging.ClientLogger;
import com.generic.core.util.serializer.ObjectSerializer;
import com.generic.json.JsonSerializable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.function.Consumer;

public abstract class RestProxyBase {
    static final ResponseConstructorsCache RESPONSE_CONSTRUCTORS_CACHE = new ResponseConstructorsCache();
    private static final ResponseExceptionConstructorCache RESPONSE_EXCEPTION_CONSTRUCTOR_CACHE =
        new ResponseExceptionConstructorCache();

    // RestProxy is a commonly used class, use a static logger.
    static final ClientLogger LOGGER = new ClientLogger(RestProxyBase.class);

    final HttpPipeline httpPipeline;
    final ObjectSerializer serializer;
    final SwaggerInterfaceParser interfaceParser;
    final HttpResponseDecoder decoder;

    /**
     * Create a RestProxy.
     *
     * @param httpPipeline The HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
     * @param serializer The serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser The parser that contains information about the interface describing REST API methods that
     * this RestProxy "implements".
     */
    public RestProxyBase(HttpPipeline httpPipeline, ObjectSerializer serializer,
                         SwaggerInterfaceParser interfaceParser) {
        this.httpPipeline = httpPipeline;
        this.serializer = serializer;
        this.interfaceParser = interfaceParser;
        this.decoder = new HttpResponseDecoder(this.serializer);
    }

    public final Object invoke(Object proxy, final Method method, RequestOptions options,
                               EnumSet<ErrorOptions> errorOptions, Consumer<HttpRequest> requestCallback,
                               SwaggerMethodParser methodParser, Object[] args) {
        try {
            HttpRequest request = createHttpRequest(methodParser, serializer, args);

            Context context = methodParser.setContext(args);
            context = RestProxyUtils.mergeRequestOptionsContext(context, options);

            request.getMetadata().setContext(context);
            request.getMetadata().setRequestLogger(methodParser.getMethodLogger());
            request.getMetadata().setEagerlyConvertHeaders(methodParser.isHeadersEagerlyConverted());
            request.getMetadata().setEagerlyReadResponse(methodParser.isResponseEagerlyRead());
            request.getMetadata().setIgnoreResponseBody(methodParser.isResponseBodyIgnored());

            return invoke(proxy, method, options, errorOptions, requestCallback, methodParser, request);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }
    }

    protected abstract Object invoke(Object proxy, Method method, RequestOptions options,
                                     EnumSet<ErrorOptions> errorOptions, Consumer<HttpRequest> httpRequestConsumer,
                                     SwaggerMethodParser methodParser, HttpRequest request);

    public abstract void updateRequest(RequestDataConfiguration requestDataConfiguration,
                                       ObjectSerializer objectSerializer) throws IOException;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Response createResponse(HttpResponseDecoder.HttpDecodedResponse response, Type entityType,
                                   Object bodyAsObject) {
        final Class<? extends Response<?>> cls = (Class<? extends Response<?>>) TypeUtil.getRawClass(entityType);
        final HttpResponse httpResponse = response.getSourceResponse();
        final HttpRequest request = httpResponse.getRequest();
        final int statusCode = httpResponse.getStatusCode();
        final Headers headers = httpResponse.getHeaders();

        // Inspection of the response type needs to be performed to determine which course of action should be taken to
        // instantiate the Response<?> from the HttpResponse.
        //
        // If the type is either the Response or PagedResponse interface from azure-core a new instance of either
        // ResponseBase or PagedResponseBase can be returned.
        if (cls.equals(Response.class)) {
            // For Response return a new instance of SimpleResponse cast to the class.
            return cls.cast(new SimpleResponse<>(request, statusCode, headers, bodyAsObject));
        }

        // Otherwise, rely on reflection, for now, to get the best constructor to use to create the Response subtype.
        //
        // Ideally, in the future the SDKs won't need to dabble in reflection here as the Response subtypes should be
        // given a way to register their constructor as a callback method that consumes HttpDecodedResponse and the body
        // as an Object.
        ReflectiveInvoker constructorReflectiveInvoker = RESPONSE_CONSTRUCTORS_CACHE.get(cls);

        return RESPONSE_CONSTRUCTORS_CACHE.invoke(constructorReflectiveInvoker, response, bodyAsObject);
    }

    /**
     * Create an HttpRequest for the provided Swagger method using the provided arguments.
     *
     * @param methodParser The Swagger method parser to use.
     * @param args The arguments to use to populate the method's annotation values.
     *
     * @return An HttpRequest.
     *
     * @throws IOException If the body contents cannot be serialized.
     */
    HttpRequest createHttpRequest(SwaggerMethodParser methodParser, ObjectSerializer ObjectSerializer, Object[] args)
        throws IOException {

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

            // Set the path after host, concatenating the path segment in the host.
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
        final HttpRequest request =
            configRequest(new HttpRequest(methodParser.getHttpMethod(), url), methodParser, ObjectSerializer, args);
        // Headers from Swagger method arguments always take precedence over inferred headers from body types
        Headers httpHeaders = request.getHeaders();

        methodParser.setHeaders(args, httpHeaders, serializer);

        return request;
    }

    private HttpRequest configRequest(final HttpRequest request, final SwaggerMethodParser methodParser,
                                      ObjectSerializer ObjectSerializer, final Object[] args) throws IOException {
        final Object bodyContentObject = methodParser.setBody(args, serializer);

        if (bodyContentObject == null) {
            request.setHeader(HttpHeaderName.CONTENT_LENGTH, "0");
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

            request.setHeader(HttpHeaderName.CONTENT_TYPE, contentType);

            if (bodyContentObject instanceof BinaryData) {
                BinaryData binaryData = (BinaryData) bodyContentObject;

                if (binaryData.getLength() != null) {
                    request.setHeader(HttpHeaderName.CONTENT_LENGTH, binaryData.getLength().toString());
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

            updateRequest(new RequestDataConfiguration(request, methodParser, isJson, bodyContentObject),
                ObjectSerializer);
        }

        return request;
    }

    /**
     * Creates an HttpResponseException exception using the details provided in http response and its content.
     *
     * @param unexpectedExceptionInformation The exception holding UnexpectedException's details.
     * @param httpResponse The http response to parse when constructing exception
     * @param responseContent The response body to use when constructing exception
     * @param responseDecodedContent The decoded response content to use when constructing exception
     *
     * @return The {@link HttpResponseException} created from the provided details.
     */
    public static HttpResponseException instantiateUnexpectedException(UnexpectedExceptionInformation unexpectedExceptionInformation,
                                                                       HttpResponse httpResponse,
                                                                       byte[] responseContent,
                                                                       Object responseDecodedContent) {
        StringBuilder exceptionMessage = new StringBuilder("Status code ")
            .append(httpResponse.getStatusCode())
            .append(", ");

        final String contentType = httpResponse.getHeaderValue(HttpHeaderName.CONTENT_TYPE);

        if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            String contentLength = httpResponse.getHeaderValue(HttpHeaderName.CONTENT_LENGTH);

            exceptionMessage.append("(").append(contentLength).append("-byte body)");
        } else if (responseContent == null || responseContent.length == 0) {
            exceptionMessage.append("(empty body)");
        } else {
            exceptionMessage.append('\"').append(new String(responseContent, StandardCharsets.UTF_8)).append('\"');
        }

        // If the decoded response content is on of these exception types there was a failure in creating the actual
        // exception body type. In this case return an HttpResponseException to maintain the exception having a
        // reference to the HttpResponse and information about what caused the deserialization failure.
        if (responseDecodedContent instanceof IOException
            || responseDecodedContent instanceof MalformedValueException
            || responseDecodedContent instanceof IllegalStateException) {

            return new HttpResponseException(exceptionMessage.toString(), httpResponse, null,
                (Throwable) responseDecodedContent);
        }

        HttpExceptionType exceptionType = unexpectedExceptionInformation.getExceptionType();

        return new HttpResponseException(exceptionMessage.toString(), httpResponse, exceptionType,
            responseDecodedContent);
    }

    /**
     * Whether {@code JsonSerializable} is supported and the {@code bodyContentClass} is an instance of it.
     *
     * @param bodyContentClass The body content class.
     *
     * @return Whether {@code bodyContentClass} can be used as {@code JsonSerializable}.
     */
    static boolean supportsJsonSerializable(Class<?> bodyContentClass) {
        return ReflectionSerializable.supportsJsonSerializable(bodyContentClass);
    }

    /**
     * Serializes the {@code jsonSerializable} as an instance of {@code JsonSerializable}.
     *
     * @param jsonSerializable The {@code JsonSerializable} body content.
     *
     * @return The {@link ByteBuffer} representing the serialized {@code jsonSerializable}.
     *
     * @throws IOException If an error occurs during serialization.
     */
    static ByteBuffer serializeAsJsonSerializable(JsonSerializable<?> jsonSerializable) throws IOException {
        return ReflectionSerializable.serializeJsonSerializableToByteBuffer(jsonSerializable);
    }
}
