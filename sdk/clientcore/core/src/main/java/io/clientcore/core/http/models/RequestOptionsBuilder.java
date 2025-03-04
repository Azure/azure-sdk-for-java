// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.http.annotations.QueryParam;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.implementation.http.rest.UriEscapers;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.Context;
import io.clientcore.core.utils.ProgressReporter;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * The options to configure the HTTP request before HTTP client sends it.
 */
public class RequestOptionsBuilder {
    private Consumer<HttpRequest> requestCallback;
    private Context context;
    private ResponseBodyMode responseBodyMode;
    private ClientLogger logger;
    private InstrumentationContext instrumentationContext;
    private ProgressReporter progressReporter;

    /**
     * Creates an instance of the {@link RequestOptionsBuilder} class.
     */
    public RequestOptionsBuilder() {
        this.requestCallback = request -> {
            // No-op
        };
        this.context = Context.none();
        this.responseBodyMode = null;
        this.logger = null;
        this.instrumentationContext = null;
        this.progressReporter = null;
    }

    /**
     * Adds a header to the {@link HttpRequest}.
     *
     * <p>If a header with the given name exists, the {@code value} is added to the existing header (comma-separated),
     * otherwise a new header will be created.</p>
     *
     * @param header The header key.
     * @return The updated {@link RequestOptionsBuilder} object.
     * @throws NullPointerException If {@code header} is null.
     */
    public RequestOptionsBuilder addHeader(HttpHeader header) {
        Objects.requireNonNull(header, "'header' cannot be null.");
        this.requestCallback = this.requestCallback.andThen(request -> request.getHeaders().add(header));
        return this;
    }

    /**
     * Sets a header on the {@link HttpRequest}.
     *
     * <p>If a header with the given name exists it is overridden by the new {@code value}.</p>
     *
     * @param header The header key.
     * @param value The header value.
     * @return The updated {@link RequestOptionsBuilder} object.
     */
    public RequestOptionsBuilder setHeader(HttpHeaderName header, String value) {
        this.requestCallback = this.requestCallback.andThen(request -> request.getHeaders().set(header, value));
        return this;
    }

    /**
     * Adds a query parameter to the request URI. The parameter name and value will be URI encoded. To use an already
     * encoded parameter name and value, call {@code addQueryParam("name", "value", true)}.
     *
     * @param parameterName The name of the query parameter.
     * @param value The value of the query parameter.
     * @return The updated {@link RequestOptionsBuilder} object.
     */
    public RequestOptionsBuilder addQueryParam(String parameterName, String value) {
        return addQueryParam(parameterName, value, false);
    }

    /**
     * Adds a query parameter to the request URI, specifying whether the parameter is already encoded. A value
     * {@code true} for this argument indicates that value of {@link QueryParam#value()} is already encoded hence the
     * engine should not encode it. By default, the value will be encoded.
     *
     * @param parameterName The name of the query parameter.
     * @param value The value of the query parameter.
     * @param encoded Whether this query parameter is already encoded.
     * @return The updated {@link RequestOptionsBuilder} object.
     */
    public RequestOptionsBuilder addQueryParam(String parameterName, String value, boolean encoded) {
        this.requestCallback = this.requestCallback.andThen(request -> {
            String uri = request.getUri().toString();
            String encodedParameterName = encoded ? parameterName : UriEscapers.QUERY_ESCAPER.escape(parameterName);
            String encodedParameterValue = encoded ? value : UriEscapers.QUERY_ESCAPER.escape(value);

            request.setUri(uri + (uri.contains("?") ? "&" : "?") + encodedParameterName + "=" + encodedParameterValue);
        });

        return this;
    }

    /**
     * Adds a custom request callback to modify the {@link HttpRequest} before it's sent by the {@link HttpClient}. The
     * modifications made on a {@link RequestOptions} object are applied in order on the request.
     *
     * @param requestCallback The request callback.
     * @return The updated {@link RequestOptionsBuilder} object.
     * @throws NullPointerException If {@code requestCallback} is null.
     */
    public RequestOptionsBuilder addRequestCallback(Consumer<HttpRequest> requestCallback) {
        Objects.requireNonNull(requestCallback, "'requestCallback' cannot be null.");

        this.requestCallback = this.requestCallback.andThen(requestCallback);
        return this;
    }

    /**
     * Sets the body to send as part of the {@link HttpRequest}.
     *
     * @param requestBody the request body data
     * @return The updated {@link RequestOptionsBuilder} object.
     * @throws NullPointerException If {@code requestBody} is {@code null}.
     */
    public RequestOptionsBuilder setBody(BinaryData requestBody) {
        Objects.requireNonNull(requestBody, "'requestBody' cannot be null.");
        this.requestCallback = this.requestCallback.andThen(request -> request.setBody(requestBody));
        return this;
    }

    /**
     * Sets the additional context on the request that is passed during the service call.
     *
     * @param context Additional context that is passed during the service call.
     * @return The updated {@link RequestOptionsBuilder} object.
     */
    public RequestOptionsBuilder setContext(Context context) {
        this.context = context;
        return this;
    }

    /**
     * Sets the configuration indicating how the body of the resulting HTTP response should be handled. If {@code null},
     * the response body will be handled based on the content type of the response.
     *
     * <p>For more information about the options for handling an HTTP response body, see {@link ResponseBodyMode}.</p>
     *
     * @param responseBodyMode The configuration indicating how the body of the resulting HTTP response should be
     *                         handled.
     * @return The updated {@link RequestOptionsBuilder} object.
     */
    public RequestOptionsBuilder setResponseBodyMode(ResponseBodyMode responseBodyMode) {
        this.responseBodyMode = responseBodyMode;
        return this;
    }

    /**
     * Sets the {@link ClientLogger} used to log the request and response.
     *
     * @param logger The {@link ClientLogger} used to log the request and response.
     * @return The updated {@link RequestOptionsBuilder} object.
     */
    public RequestOptionsBuilder setLogger(ClientLogger logger) {
        this.logger = logger;
        return this;
    }

    /**
     * Sets the {@link ProgressReporter} used to track progress of I/O operations of the request.
     *
     * @param progressReporter The {@link ProgressReporter} used to track progress of I/O operations of the request.
     * @return The updated {@link RequestOptionsBuilder} object.
     */
    public RequestOptionsBuilder setProgressReporter(ProgressReporter progressReporter) {
        this.progressReporter = progressReporter;
        return this;
    }

    /**
     * Sets the {@link InstrumentationContext} used to instrument the request.
     *
     * @param instrumentationContext The {@link InstrumentationContext} used to instrument the request.
     * @return The updated {@link RequestOptionsBuilder} object.
     */
    public RequestOptionsBuilder setInstrumentationContext(InstrumentationContext instrumentationContext) {
        this.instrumentationContext = instrumentationContext;
        return this;
    }

    /**
     * Builds the {@link RequestOptions} object.
     *
     * @return The {@link RequestOptions} object.
     */
    public RequestOptions build() {
        return new RequestOptions(requestCallback, context, responseBodyMode, logger, instrumentationContext,
            progressReporter);
    }
}
