// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

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
 * <p><strong>Creating an instance of DynamicRequest using the constructor</strong></p>
 * {@codesnippet com.azure.core.experimental.http.requestoptions.instantiation}
 **
 * <p><strong>Configuring the request with a path param and making a HTTP GET request</strong></p>
 * Continuing with the pet store example, getting information about a pet requires making a
 * <a href="https://petstore.swagger.io/#/pet/getPetById">HTTP GET call
 * to the pet service</a> and setting the pet id in path param as shown in the sample below.
 *
 * {@codesnippet com.azure.core.experimental.http.dynamicrequest.getrequest}
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
 * {@codesnippet com.azure.core.experimental.http.requestoptions.createjsonrequest}
 *
 * Now, this string representation of the JSON request can be set as body of RequestOptions
 *
 * {@codesnippet com.azure.core.experimental.http.requestoptions.postrequest}
 */
public class RequestOptions {
    private Consumer<HttpRequest> requestCallback = request -> { };
    private ResponseStatusOption statusOption = ResponseStatusOption.DEFAULT;

    /**
     * Gets the request callback, applying all the configurations set on this RequestOptions.
     * @return the request callback
     */
    public Consumer<HttpRequest> getRequestCallback() {
        return this.requestCallback;
    }

    /**
     * Gets under what conditions the operation raises an exception if the underlying response indicates a failure.
     * @return the configured option
     */
    public ResponseStatusOption getStatusOption() {
        return this.statusOption;
    }

    /**
     * Adds a header to the HTTP request.
     * @param header the header key
     * @param value the header value
     *
     * @return the modified RequestOptions object
     */
    public RequestOptions addHeader(String header, String value) {
        this.requestCallback = this.requestCallback.andThen(request ->
            request.getHeaders().set(header, value));
        return this;
    }

    /**
     * Adds a query parameter to the request URL.
     * @param parameterName the name of the query parameter
     * @param value the value of the query parameter
     * @return the modified RequestOptions object
     */
    public RequestOptions addQueryParam(String parameterName, String value) {
        this.requestCallback = this.requestCallback.andThen(request -> {
            String url = request.getUrl().toString();
            request.setUrl(url + (url.contains("?") ? "&" : "?") + parameterName + "=" + value);
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
     * Sets under what conditions the operation raises an exception if the underlying response indicates a failure.
     * @param statusOption the option to control under what conditions the operation raises an exception
     * @return the modified RequestOptions object
     */
    public RequestOptions setStatusOption(ResponseStatusOption statusOption) {
        this.statusOption = statusOption;
        return this;
    }
}
