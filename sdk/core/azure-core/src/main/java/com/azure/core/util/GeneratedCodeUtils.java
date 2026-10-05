// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.implementation.http.UnexpectedExceptionInformation;
import com.azure.core.implementation.http.rest.RequestOptionsAccessHelper;
import com.azure.core.implementation.http.rest.RestProxyBase;
import com.azure.core.implementation.http.rest.RestProxyUtils;
import com.azure.core.implementation.http.rest.UrlEscapers;
import com.azure.core.implementation.serializer.HttpResponseDecodeData;
import com.azure.core.implementation.serializer.HttpResponseDecoder;
import com.azure.core.implementation.util.HttpUtils;
import com.azure.core.util.serializer.SerializerAdapter;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Utility methods used by generated clients to send HTTP requests without a runtime REST proxy.
 */
public final class GeneratedCodeUtils {
    /**
     * Applies the request customizations configured on the supplied {@link RequestOptions}.
     * <p>
     * Customizations are applied in the order they were added to the options. This method should be called after
     * configuring the generated request and before sending it through the HTTP pipeline. The context on the options
    * must be merged using {@link #createRequestContext(Context, RequestOptions, String, boolean, boolean, boolean)}
    * and passed separately to the pipeline.
     *
     * @param request The HTTP request to customize.
     * @param options The request options, or {@code null} if there are no customizations.
     * @throws NullPointerException If {@code request} is {@code null}.
     */
    public static void applyRequestOptions(HttpRequest request, RequestOptions options) {
        Objects.requireNonNull(request, "'request' cannot be null.");
        if (options != null) {
            RequestOptionsAccessHelper.applyRequestOptions(request, options);
        }
    }

    /**
     * Creates the context to pass to the HTTP pipeline for a generated service method.
     * <p>
     * The context on the request options takes precedence over the supplied context. Response-consumption controls
     * are only added when enabled; existing controls in either context are retained when the corresponding argument
     * is {@code false}.
     *
     * @param context The caller's context, or {@code null} to use {@link Context#NONE}.
     * @param options The request options, or {@code null} if no options were supplied.
     * @param callerMethod The fully qualified name of the generated service method.
     * @param eagerlyReadResponse Whether the transport should eagerly read the response body.
     * @param ignoreResponseBody Whether the transport should drain and ignore the response body.
     * @param eagerlyConvertHeaders Whether the transport should eagerly convert the response headers.
     * @return The context to pass to the HTTP pipeline.
     * @throws NullPointerException If {@code callerMethod} is {@code null}.
     */
    public static Context createRequestContext(Context context, RequestOptions options, String callerMethod,
        boolean eagerlyReadResponse, boolean ignoreResponseBody, boolean eagerlyConvertHeaders) {
        Objects.requireNonNull(callerMethod, "'callerMethod' cannot be null.");
        Context requestContext
            = RestProxyUtils.mergeRequestOptionsContext(context == null ? Context.NONE : context, options)
                .addData("caller-method", callerMethod);
        if (eagerlyReadResponse) {
            requestContext = requestContext.addData(HttpUtils.AZURE_EAGERLY_READ_RESPONSE, true);
        }
        if (ignoreResponseBody) {
            requestContext = requestContext.addData(HttpUtils.AZURE_IGNORE_RESPONSE_BODY, true);
        }
        if (eagerlyConvertHeaders) {
            requestContext = requestContext.addData(HttpUtils.AZURE_EAGERLY_CONVERT_HEADERS, true);
        }
        return requestContext;
    }

    /**
     * Determines whether an unexpected response should produce an exception.
     *
     * @param options The request options, or {@code null} to use the default error behavior.
     * @return Whether an unexpected response should produce an exception.
     */
    public static boolean shouldThrowException(RequestOptions options) {
        return options == null || RequestOptionsAccessHelper.shouldThrowException(options);
    }

    /**
     * Encodes a path parameter using the REST proxy's path escaping rules.
     *
     * @param value The unencoded path parameter value.
     * @return The encoded path parameter value.
     * @throws NullPointerException If {@code value} is {@code null}.
     */
    public static String encodePathParameter(String value) {
        return UrlEscapers.PATH_ESCAPER.escape(Objects.requireNonNull(value, "'value' cannot be null."));
    }

    /**
     * Encodes a query parameter name or value using the REST proxy's query escaping rules.
     *
     * @param value The unencoded query parameter name or value.
     * @return The encoded query parameter name or value.
     * @throws NullPointerException If {@code value} is {@code null}.
     */
    public static String encodeQueryParameter(String value) {
        return UrlEscapers.QUERY_ESCAPER.escape(Objects.requireNonNull(value, "'value' cannot be null."));
    }

    /**
     * Adds a serialized query parameter to a URL builder.
     * <p>
     * A {@code null} value is omitted. Repeated calls with the same name append additional query parameter values.
     *
     * @param urlBuilder The URL builder to update.
     * @param name The query parameter name.
     * @param escapeName Whether the name needs encoding.
     * @param value The serialized query parameter value, or {@code null} to omit it.
     * @param escapeValue Whether the value needs encoding.
     * @throws NullPointerException If {@code urlBuilder} or {@code name} is {@code null}.
     */
    public static void addQueryParameter(UrlBuilder urlBuilder, String name, boolean escapeName, String value,
        boolean escapeValue) {
        Objects.requireNonNull(urlBuilder, "'urlBuilder' cannot be null.");
        Objects.requireNonNull(name, "'name' cannot be null.");
        if (value != null) {
            urlBuilder.addQueryParameter(escapeName ? encodeQueryParameter(name) : name,
                escapeValue ? encodeQueryParameter(value) : value);
        }
    }

    /**
     * Validates the request body against its Content-Length header before a synchronous pipeline call.
     * <p>
     * For streaming bodies, validation is performed as the body is consumed. This method should be called after
     * applying request options, as those options may change the body or its Content-Length header.
     *
     * @param request The request to validate.
     * @return The request with any streaming body wrapped for length validation.
     * @throws NullPointerException If {@code request} is {@code null}.
     * @throws IllegalArgumentException If a request body is present but Content-Length is not a valid integer.
     * @throws IllegalStateException If a reactive request body is used in a synchronous call.
     * @throws UnexpectedLengthException If a known body length differs from Content-Length.
     */
    public static HttpRequest validateRequestBodyLength(HttpRequest request) {
        Objects.requireNonNull(request, "'request' cannot be null.");
        if (request.getBodyAsBinaryData() != null) {
            request.setBody(RestProxyUtils.validateLengthSync(request));
        }
        return request;
    }

    /**
     * Validates the request body against its Content-Length header before an asynchronous pipeline call.
     * <p>
     * For streaming bodies, validation is performed as the body is consumed. This method should be called after
     * applying request options, as those options may change the body or its Content-Length header.
     *
     * @param request The request to validate.
     * @return A publisher emitting the request with any streaming body wrapped for length validation.
     * @throws NullPointerException If {@code request} is {@code null}.
     */
    public static Mono<HttpRequest> validateRequestBodyLengthAsync(HttpRequest request) {
        return RestProxyUtils.validateLengthAsync(Objects.requireNonNull(request, "'request' cannot be null."));
    }

    /**
     * Deserializes a response body using the REST proxy's body decoding and wire-type conversion rules.
     * <p>
     * The caller must check the response status before calling this method. This method does not close the response.
     * If {@code body} is {@code null}, a decodable body may be read synchronously from the response. Raw body types,
     * such as streams and {@link BinaryData}, must be obtained directly from the response instead of deserialized.
     *
     * @param response The HTTP response.
     * @param body The retrieved response body, or {@code null} to read it from the response.
     * @param bodyType The body type to deserialize, including any generic type arguments.
     * @param wireType The optional wire type to convert to the body type, or {@code null} for no conversion.
     * @param serializer The serializer to use.
     * @param <T> The deserialized body type.
     * @return The deserialized body, or {@code null} for an empty, ignored, or non-decodable body.
     * @throws NullPointerException If {@code response}, {@code bodyType}, or {@code serializer} is {@code null}.
     * @throws HttpResponseException If the response body cannot be deserialized.
     */
    @SuppressWarnings("unchecked")
    public static <T> T decodeResponseBody(HttpResponse response, byte[] body, Type bodyType, Type wireType,
        SerializerAdapter serializer) {
        Objects.requireNonNull(response, "'response' cannot be null.");
        Objects.requireNonNull(bodyType, "'bodyType' cannot be null.");
        Objects.requireNonNull(serializer, "'serializer' cannot be null.");
        return (T) new HttpResponseDecoder(serializer)
            .decodeSync(response, new DecodeData(bodyType, wireType, null, null))
            .getDecodedBody(body);
    }

    /**
     * Deserializes response headers using the REST proxy's typed-header decoding rules.
     * <p>
     * This method does not read the response body or close the response.
     *
     * @param response The HTTP response.
     * @param headersType The type to deserialize the headers into, or {@code null} if no typed headers are needed.
     * @param serializer The serializer to use.
     * @param <T> The deserialized headers type.
     * @return The deserialized headers, or {@code null} if {@code headersType} is {@code null}.
     * @throws NullPointerException If {@code response} or {@code serializer} is {@code null}.
     * @throws HttpResponseException If the response headers cannot be deserialized.
     */
    @SuppressWarnings("unchecked")
    public static <T> T decodeResponseHeaders(HttpResponse response, Type headersType, SerializerAdapter serializer) {
        Objects.requireNonNull(response, "'response' cannot be null.");
        Objects.requireNonNull(serializer, "'serializer' cannot be null.");
        return (T) new HttpResponseDecoder(serializer)
            .decodeSync(response, new DecodeData(Void.class, null, headersType, null))
            .getDecodedHeaders();
    }

    /**
     * Creates an exception for an unexpected response using the REST proxy's error decoding and exception factory.
     * <p>
     * The error body type is determined from the exception class's {@code getValue} method. A malformed error body is
     * retained as the cause of an {@link HttpResponseException}. This method does not close the response; the caller
     * retains ownership of it and is responsible for throwing or returning the exception.
     *
     * @param response The unexpected HTTP response.
     * @param body The retrieved response body, or {@code null} if the response body is empty.
     * @param exceptionType The exception type to create, or {@code null} to use {@link HttpResponseException}.
     * @param serializer The serializer to use.
     * @return The exception representing the unexpected response.
     * @throws NullPointerException If {@code response} or {@code serializer} is {@code null}.
     */
    public static HttpResponseException createUnexpectedResponseException(HttpResponse response, byte[] body,
        Class<? extends HttpResponseException> exceptionType, SerializerAdapter serializer) {
        Objects.requireNonNull(response, "'response' cannot be null.");
        Objects.requireNonNull(serializer, "'serializer' cannot be null.");
        UnexpectedExceptionInformation exceptionInformation
            = new UnexpectedExceptionInformation(exceptionType == null ? HttpResponseException.class : exceptionType);
        Object decodedBody = new HttpResponseDecoder(serializer)
            .decodeSync(response, new DecodeData(Object.class, null, null, exceptionInformation))
            .getDecodedBody(body);
        return RestProxyBase.instantiateUnexpectedException(exceptionInformation, response, body, decodedBody);
    }

    private static final class DecodeData implements HttpResponseDecodeData {
        private final Type bodyType;
        private final Type wireType;
        private final Type headersType;
        private final UnexpectedExceptionInformation exceptionInformation;

        private DecodeData(Type bodyType, Type wireType, Type headersType,
            UnexpectedExceptionInformation exceptionInformation) {
            this.bodyType = bodyType;
            this.wireType = wireType;
            this.headersType = headersType;
            this.exceptionInformation = exceptionInformation;
        }

        @Override
        public Type getReturnType() {
            return bodyType;
        }

        @Override
        public Type getHeadersType() {
            return headersType;
        }

        @Override
        public Type getReturnValueWireType() {
            return wireType;
        }

        @Override
        public boolean isExpectedResponseStatusCode(int statusCode) {
            return exceptionInformation == null;
        }

        @Override
        public UnexpectedExceptionInformation getUnexpectedException(int statusCode) {
            return exceptionInformation == null
                ? HttpResponseDecodeData.super.getUnexpectedException(statusCode)
                : exceptionInformation;
        }

        @Override
        public boolean isReturnTypeDecodeable() {
            return wireType != null || HttpResponseDecodeData.super.isReturnTypeDecodeable();
        }

        @Override
        public boolean isHeadersEagerlyConverted() {
            return headersType != null;
        }
    }

    private GeneratedCodeUtils() {
    }
}
