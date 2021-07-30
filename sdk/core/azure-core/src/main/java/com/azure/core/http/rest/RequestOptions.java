// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.annotation.QueryParam;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.BinaryData;

import java.util.function.Consumer;

/**
 * This class contains the options to customize a HTTP request. {@link RequestOptions} can be
 * used to configure the request headers, query params, the request body, or add a callback
 * to modify all aspects of the HTTP request.
 *
 * <p>
 * An instance of fully configured {@link RequestOptions} can be passed to a service method that
 * preconfigures known components of the request like URL, path params etc, further modifying both
 * un-configured, or preconfigured components.
 * </p>
 *
 * <p>
 * To demonstrate how this class can be used to construct a request, let's use a Pet Store service as an example. The
 * list of APIs available on this service are <a href="https://petstore.swagger.io/#/pet">documented in the swagger definition.</a>
 * </p>
 *
 * <p><strong>Creating an instance of RequestOptions</strong></p>
 * {@codesnippet com.azure.core.http.rest.requestoptions.instantiation}
 *
 * <p><strong>Configuring the request with JSON body and making a HTTP POST request</strong></p>
 * To <a href="https://petstore.swagger.io/#/pet/addPet">add a new pet to the pet store</a>, a HTTP POST call should
 * be made to the service with the details of the pet that is to be added. The details of the pet are included as the
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
 * {@codesnippet com.azure.core.http.rest.requestoptions.createjsonrequest}
 *
 * Now, this string representation of the JSON request can be set as body of RequestOptions
 *
 * {@codesnippet com.azure.core.http.rest.requestoptions.postrequest}
 */
public final class RequestOptions {
    private Consumer<HttpRequest> requestCallback = request -> { };
    private boolean throwOnError = true;
    private BinaryData requestBody;

    /**
     * Gets the request callback, applying all the configurations set on this RequestOptions.
     * @return the request callback
     */
    Consumer<HttpRequest> getRequestCallback() {
        return this.requestCallback;
    }

    /**
     * Gets whether or not to throw an exception when an HTTP response with a status code indicating an error
     * (400 or above) is received.
     *
     * @return true if to throw on status codes of 400 or above, false if not. Default is true.
     */
    boolean isThrowOnError() {
        return this.throwOnError;
    }

    /**
     * Adds a header to the HTTP request.
     * @param header the header key
     * @param value the header value
     *
     * @return the modified RequestOptions object
     */
    public RequestOptions addHeader(String header, String value) {
        this.requestCallback = this.requestCallback.andThen(request -> {
            HttpHeader httpHeader = request.getHeaders().get(header);
            if (httpHeader == null) {
                request.getHeaders().set(header, value);
            } else {
                httpHeader.addValue(value);
            }
        });
        return this;
    }

    /**
     * Adds a query parameter to the request URL. The parameter name and value will be URL encoded.
     * To use an already encoded parameter name and value, call {@code addQueryParam("name", "value", true)}.
     *
     * @param parameterName the name of the query parameter
     * @param value the value of the query parameter
     * @return the modified RequestOptions object
     */
    public RequestOptions addQueryParam(String parameterName, String value) {
        return addQueryParam(parameterName, value, false);
    }

    /**
     * Adds a query parameter to the request URL, specifying whether the parameter is already encoded.
     * A value true for this argument indicates that value of {@link QueryParam#value()} is already encoded
     * hence engine should not encode it, by default value will be encoded.
     *
     * @param parameterName the name of the query parameter
     * @param value the value of the query parameter
     * @param encoded whether or not this query parameter is already encoded
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
     * Adds a custom request callback to modify the HTTP request before it's sent by the HttpClient.
     * The modifications made on a RequestOptions object is applied in order on the request.
     *
     * @param requestCallback the request callback
     * @return the modified RequestOptions object
     */
    public RequestOptions addRequestCallback(Consumer<HttpRequest> requestCallback) {
        this.requestCallback = this.requestCallback.andThen(requestCallback);
        return this;
    }

    /**
     * Sets the body to send as part of the HTTP request.
     * @param requestBody the request body data
     * @return the modified RequestOptions object
     */
    public RequestOptions setBody(BinaryData requestBody) {
        this.requestCallback = this.requestCallback.andThen(request -> {
            request.setBody(requestBody.toBytes());
        });
        return this;
    }

    /**
     * Sets whether or not to throw an exception when an HTTP response with a status code indicating an error
     * (400 or above) is received. By default an exception will be thrown when an error response is received.
     *
     * @param throwOnError true if to throw on status codes of 400 or above, false if not. Default is true.
     * @return the modified RequestOptions object
     */
    public RequestOptions setThrowOnError(boolean throwOnError) {
        this.throwOnError = throwOnError;
        return this;
    }
}
