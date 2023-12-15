// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import com.generic.core.http.annotation.QueryParam;
import com.generic.core.implementation.http.rest.ErrorOptions;
import com.generic.core.implementation.http.rest.UrlEscapers;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Context;
import com.generic.core.models.HeaderName;
import com.generic.core.util.ClientLogger;

import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * This class contains the options to customize an HTTP request. {@link RequestOptions} can be used to configure the
 * request headers, query params, the request body, or add a callback to modify all aspects of the HTTP request.
 *
 * <p>
 * An instance of fully configured {@link RequestOptions} can be passed to a service method that preconfigures known
 * components of the request like URL, path params etc, further modifying both un-configured, or preconfigured
 * components.
 * </p>
 *
 * <p>
 * To demonstrate how this class can be used to construct a request, let's use a Pet Store service as an example. The
 * list of APIs available on this service are <a href="https://petstore.swagger.io/#/pet">documented in the swagger
 * definition.</a>
 * </p>
 *
 * <p><strong>Creating an instance of RequestOptions</strong></p>
 * <!-- src_embed com.generic.core.http.rest.requestoptions.instantiation -->
 * <!-- end com.generic.core.http.rest.requestoptions.instantiation -->
 *
 * <p><strong>Configuring the request with JSON body and making a HTTP POST request</strong></p>
 * To <a href="https://petstore.swagger.io/#/pet/addPet">add a new pet to the pet store</a>, an HTTP POST call should be
 * made to the service with the details of the pet that is to be added. The details of the pet are included as the
 * request body in JSON format.
 *
 * The JSON structure for the request is defined as follows:
 * <pre>{@code
 * {
 *   "id": 0,
 *   "category": {
 *     "id": 0,
 *     "name": "string"
 *   },
 *   "name": "doggie",
 *   "photoUrls": [
 *     "string"
 *   ],
 *   "tags": [
 *     {
 *       "id": 0,
 *       "name": "string"
 *     }
 *   ],
 *   "status": "available"
 * }
 * }</pre>
 *
 * To create a concrete request, Json builder provided in javax package is used here for demonstration. However, any
 * other Json building library can be used to achieve similar results.
 *
 * <!-- src_embed com.generic.core.http.rest.requestoptions.createjsonrequest -->
 * <!-- end com.generic.core.http.rest.requestoptions.createjsonrequest -->
 *
 * Now, this string representation of the JSON request can be set as body of RequestOptions
 *
 * <!-- src_embed com.generic.core.http.rest.requestoptions.postrequest -->
 * <!-- end com.generic.core.http.rest.requestoptions.postrequest -->
 */
public final class RequestOptions {
    private static final ClientLogger LOGGER = new ClientLogger(RequestOptions.class);
    private static final EnumSet<ErrorOptions> DEFAULT = EnumSet.of(ErrorOptions.THROW);
    private Consumer<HttpRequest> requestCallback = request -> {
    };
    private EnumSet<ErrorOptions> errorOptions = DEFAULT;
    private Context context;

    /**
     * Creates a new instance of {@link RequestOptions}.
     */
    public RequestOptions() {
    }

    /**
     * Gets the request callback, applying all the configurations set on this RequestOptions.
     *
     * @return The request callback.
     */
    public Consumer<HttpRequest> getRequestCallback() {
        return this.requestCallback;
    }

    /**
     * Gets the {@link ErrorOptions} that determines how error responses (400 or above) are handled.
     * <p>
     * Default is to throw.
     *
     * @return The {@link ErrorOptions} that determines how error responses (400 or above) are handled. Default is to
     * throw.
     */
    public EnumSet<ErrorOptions> getErrorOptions() {
        return this.errorOptions;
    }

    /**
     * Gets the additional context on the request that is passed during the service call.
     *
     * @return The additional context that is passed during the service call.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Adds a header to the HTTP request.
     * <p>
     * If a header with the given name exists the {@code value} is added to the existing header (comma-separated),
     * otherwise a new header is created.
     *
     * @param header the header key
     * @param value the header value
     *
     * @return the modified RequestOptions object
     */
    public RequestOptions addHeader(HeaderName header, String value) {
        this.requestCallback = this.requestCallback.andThen(request -> request.getHeaders().add(header, value));

        return this;
    }

    /**
     * Sets a header on the HTTP request.
     * <p>
     * If a header with the given name exists it is overridden by the new {@code value}.
     *
     * @param header the header key
     * @param value the header value
     *
     * @return the modified RequestOptions object
     */
    public RequestOptions setHeader(HeaderName header, String value) {
        this.requestCallback = this.requestCallback.andThen(request -> request.getHeaders().set(header, value));

        return this;
    }

    /**
     * Adds a query parameter to the request URL. The parameter name and value will be URL encoded. To use an already
     * encoded parameter name and value, call {@code addQueryParam("name", "value", true)}.
     *
     * @param parameterName the name of the query parameter
     * @param value the value of the query parameter
     *
     * @return the modified RequestOptions object
     */
    public RequestOptions addQueryParam(String parameterName, String value) {
        return addQueryParam(parameterName, value, false);
    }

    /**
     * Adds a query parameter to the request URL, specifying whether the parameter is already encoded. A value true for
     * this argument indicates that value of {@link QueryParam#value()} is already encoded hence engine should not
     * encode it, by default value will be encoded.
     *
     * @param parameterName the name of the query parameter
     * @param value the value of the query parameter
     * @param encoded whether this query parameter is already encoded
     *
     * @return the modified RequestOptions object
     */
    public RequestOptions addQueryParam(String parameterName, String value, boolean encoded) {
        this.requestCallback = this.requestCallback.andThen(request -> {
            String url = request.getUrl().toString();
            String encodedParameterName = encoded ? parameterName : UrlEscapers.QUERY_ESCAPER.escape(parameterName);
            String encodedParameterValue = encoded ? value : UrlEscapers.QUERY_ESCAPER.escape(value);

            request.setUrl(url + (url.contains("?") ? "&" : "?") + encodedParameterName + "=" + encodedParameterValue);
        });

        return this;
    }

    /**
     * Adds a custom request callback to modify the HTTP request before it's sent by the HttpClient. The modifications
     * made on a RequestOptions object is applied in order on the request.
     *
     * @param requestCallback the request callback
     *
     * @return the modified RequestOptions object
     *
     * @throws NullPointerException If {@code requestCallback} is null.
     */
    public RequestOptions addRequestCallback(Consumer<HttpRequest> requestCallback) {
        Objects.requireNonNull(requestCallback, "'requestCallback' cannot be null.");

        this.requestCallback = this.requestCallback.andThen(requestCallback);

        return this;
    }

    /**
     * Sets the body to send as part of the HTTP request.
     *
     * @param requestBody the request body data
     *
     * @return the modified RequestOptions object
     *
     * @throws NullPointerException If {@code requestBody} is null.
     */
    public RequestOptions setBody(BinaryData requestBody) {
        Objects.requireNonNull(requestBody, "'requestBody' cannot be null.");

        this.requestCallback = this.requestCallback.andThen(request -> request.setBody(requestBody));

        return this;
    }

    /**
     * Sets the {@link ErrorOptions} that determines how error responses (400 or above) are handled.
     * <p>
     * Default is to throw.
     * <p>
     * If both {@link ErrorOptions#THROW} and {@link ErrorOptions#NO_THROW} are included in {@code errorOptions}
     * an exception will be thrown as they aren't compatible with each other.
     *
     * @param errorOptions The {@link ErrorOptions} that determines how error responses (400 or above) are handled.
     * @return the modified RequestOptions object
     * @throws NullPointerException If {@code errorOptions} is null.
     * @throws IllegalArgumentException If both {@link ErrorOptions#THROW} and {@link ErrorOptions#NO_THROW} are
     * included in {@code errorOptions}.
     */
    RequestOptions setErrorOptions(EnumSet<ErrorOptions> errorOptions) {
        Objects.requireNonNull(errorOptions, "'errorOptions' cannot be null.");

        if (errorOptions.contains(ErrorOptions.THROW) && errorOptions.contains(ErrorOptions.NO_THROW)) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException(
                "'errorOptions' cannot contain both 'ErrorOptions.THROW' and 'ErrorOptions.NO_THROW'."));
        }

        this.errorOptions = errorOptions;

        return this;
    }

    /**
     * Sets the additional context on the request that is passed during the service call.
     *
     * @param context Additional context that is passed during the service call.
     *
     * @return the modified RequestOptions object
     */
    public RequestOptions setContext(Context context) {
        this.context = context;

        return this;
    }
}
