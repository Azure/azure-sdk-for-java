// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.http;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class facilitates constructing and sending a HTTP request. {@link DynamicRequest} can be
 * used to configure the endpoint to which the request is sent, the request headers, path params, query params
 * and the request body.
 *
 * <p>
 * An instance of {@link DynamicRequest} can be created by either directly calling the constructor with an
 * {@link ObjectSerializer} and {@link HttpPipeline} or obtained from a service client that preconfigures known
 * components of the request like URL, path params etc.
 * </p>
 *
 * <p>
 * To demonstrate how this class can be used to construct a request, let's use a Pet Store service as an example. The
 * list of APIs available on this service are <a href="https://petstore.swagger.io/#/pet">documented in the swagger definition.</a>
 * </p>
 *
 * <p><strong>Creating an instance of DynamicRequest using the constructor</strong></p>
 * <!-- src_embed com.azure.core.experimental.http.dynamicrequest.instantiation -->
 * <pre>
 * ObjectSerializer serializer = JsonSerializerProviders.createInstance&#40;true&#41;;
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;.build&#40;&#41;;
 * DynamicRequest dynamicRequest = new DynamicRequest&#40;serializer, pipeline&#41;;
 * </pre>
 * <!-- end com.azure.core.experimental.http.dynamicrequest.instantiation -->
 *
 * <p>An Azure service client may provide methods that are specific to the service which returns an instance
 * {@link DynamicRequest} that comes preconfigured with some request components like the endpoint, required path
 * params, headers etc. </p>
 *
 * <p><strong>Configuring the request with a path param and making a HTTP GET request</strong></p>
 * Continuing with the pet store example, getting information about a pet requires making a
 * <a href="https://petstore.swagger.io/#/pet/getPetById">HTTP GET call
 * to the pet service</a> and setting the pet id in path param as shown in the sample below.
 *
 * <!-- src_embed com.azure.core.experimental.http.dynamicrequest.getrequest -->
 * <pre>
 * DynamicResponse response = dynamicRequest
 *     .setUrl&#40;&quot;https:&#47;&#47;petstore.example.com&#47;pet&#47;&#123;petId&#125;&quot;&#41; &#47;&#47; may already be set if request is created from a client
 *     .setPathParam&#40;&quot;petId&quot;, &quot;2343245&quot;&#41;
 *     .send&#40;&#41;; &#47;&#47; makes the service call
 * </pre>
 * <!-- end com.azure.core.experimental.http.dynamicrequest.getrequest -->
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
 * <!-- src_embed com.azure.core.experimental.http.dynamicrequest.createjsonrequest -->
 * <pre>
 * JsonArray photoUrls = Json.createArrayBuilder&#40;&#41;
 *     .add&#40;&quot;https:&#47;&#47;imgur.com&#47;pet1&quot;&#41;
 *     .add&#40;&quot;https:&#47;&#47;imgur.com&#47;pet2&quot;&#41;
 *     .build&#40;&#41;;
 *
 * JsonArray tags = Json.createArrayBuilder&#40;&#41;
 *     .add&#40;Json.createObjectBuilder&#40;&#41;
 *         .add&#40;&quot;id&quot;, 0&#41;
 *         .add&#40;&quot;name&quot;, &quot;Labrador&quot;&#41;
 *         .build&#40;&#41;&#41;
 *     .add&#40;Json.createObjectBuilder&#40;&#41;
 *         .add&#40;&quot;id&quot;, 1&#41;
 *         .add&#40;&quot;name&quot;, &quot;2021&quot;&#41;
 *         .build&#40;&#41;&#41;
 *     .build&#40;&#41;;
 *
 * JsonObject requestBody = Json.createObjectBuilder&#40;&#41;
 *     .add&#40;&quot;id&quot;, 0&#41;
 *     .add&#40;&quot;name&quot;, &quot;foo&quot;&#41;
 *     .add&#40;&quot;status&quot;, &quot;available&quot;&#41;
 *     .add&#40;&quot;category&quot;, Json.createObjectBuilder&#40;&#41;.add&#40;&quot;id&quot;, 0&#41;.add&#40;&quot;name&quot;, &quot;dog&quot;&#41;&#41;
 *     .add&#40;&quot;photoUrls&quot;, photoUrls&#41;
 *     .add&#40;&quot;tags&quot;, tags&#41;
 *     .build&#40;&#41;;
 *
 * String requestBodyStr = requestBody.toString&#40;&#41;;
 * </pre>
 * <!-- end com.azure.core.experimental.http.dynamicrequest.createjsonrequest -->
 *
 * Now, this string representation of the JSON request can be set as body of DynamicRequest
 *
 * <!-- src_embed com.azure.core.experimental.http.dynamicrequest.postrequest -->
 * <pre>
 * DynamicResponse response = dynamicRequest
 *     .setUrl&#40;&quot;https:&#47;&#47;petstore.example.com&#47;pet&quot;&#41; &#47;&#47; may already be set if request is created from a client
 *     .addHeader&#40;&quot;Content-Type&quot;, &quot;application&#47;json&quot;&#41;
 *     .setBody&#40;requestBodyStr&#41;
 *     .send&#40;&#41;; &#47;&#47; makes the service call
 * </pre>
 * <!-- end com.azure.core.experimental.http.dynamicrequest.postrequest -->
 */
public final class DynamicRequest {
    private final ClientLogger logger = new ClientLogger(DynamicRequest.class);
    private final ObjectSerializer objectSerializer;
    private final HttpPipeline httpPipeline;
    private HttpHeaders headers = new HttpHeaders();
    private final Map<String, String> queries = new HashMap<>();
    private HttpMethod httpMethod;
    private String url;
    private byte[] body;

    /**
     * Creates an instance of the Dynamic request. The {@code objectSerializer} provided to this constructor will be
     * used to serialize the request and the {@code httpPipeline} configured with a series of
     * {@link HttpPipelinePolicy Http pipeline policies} will be applied before sending the request.
     * @param objectSerializer a serializer for serializing and deserializing payloads
     * @param httpPipeline the pipeline to send the actual HTTP request
     *
     * @throws NullPointerException if either of objectSerializer or httpPipeline is null
     */
    public DynamicRequest(ObjectSerializer objectSerializer, HttpPipeline httpPipeline) {
        if (objectSerializer == null) {
            throw logger.logExceptionAsError(new NullPointerException("objectSerializer cannot be null"));
        }
        if (httpPipeline == null) {
            throw logger.logExceptionAsError(new NullPointerException("httpPipeline cannot be null"));
        }
        this.objectSerializer = objectSerializer;
        this.httpPipeline = httpPipeline;
    }

    /**
     * Returns the {@link ObjectSerializer} used for serializing this request.
     * @return the underlying serializer used by this DynamicRequest
     */
    public ObjectSerializer getObjectSerializer() {
        return objectSerializer;
    }

    /**
     * Returns the {@link HttpPipeline} used for sending this request.
     * @return the pipeline to sending HTTP requests used by this DynamicRequest
     */
    public HttpPipeline getHttpPipeline() {
        return httpPipeline;
    }

    /**
     * Sets the URL for the HTTP request.
     * @param url the URL for the request
     *
     * @return the modified DynamicRequest object
     */
    public DynamicRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Sets the HTTP method for this request.
     * @param httpMethod the HTTP method for the request
     *
     * @return the modified DynamicRequest object
     */
    public DynamicRequest setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    /**
     * Adds a header to the HTTP request.
     * @param header the header key
     * @param value the header value
     *
     * @return the modified DynamicRequest object
     */
    public DynamicRequest addHeader(String header, String value) {
        headers.set(header, value);
        return this;
    }

    /**
     * Adds a header to the HTTP request
     * @param httpHeader the header to add
     *
     * @return the modified DynamicRequest object
     * @throws NullPointerException if the httpHeader is null
     */
    public DynamicRequest addHeader(HttpHeader httpHeader) {
        if (httpHeader == null) {
            throw logger.logExceptionAsError(new NullPointerException("httpHeader cannot be null"));
        }
        headers.set(httpHeader.getName(), httpHeader.getValue());
        return this;
    }

    /**
     * Sets the headers on the HTTP request. This overwrites all existing HTTP headers for this request.
     * @param httpHeaders the new headers to replace all existing headers
     *
     * @return the modified DynamicRequest object
     */
    public DynamicRequest setHeaders(HttpHeaders httpHeaders) {
        this.headers = httpHeaders;
        return this;
    }

    /**
     * Sets the string representation of the request body. The {@link ObjectSerializer} is not used if body is
     * represented as string.
     * @param body the serialized body content
     *
     * @return the modified DynamicRequest object
     */
    public DynamicRequest setBody(String body) {
        this.body = body.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    /**
     * Sets the body on the HTTP request. The object is serialized using {@link ObjectSerializer} provided in the
     * constructor of this request.
     * @param body the body object that will be serialized
     *
     * @return the modified DynamicRequest object
     * @throws UncheckedIOException if the body cannot be serialized
     */
    public DynamicRequest setBody(Object body) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            objectSerializer.serialize(outputStream, body);
            this.body = outputStream.toByteArray();
            outputStream.close();
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException("Unable to serialize the body", e));
        }
        return this;
    }

    /**
     * Sets the value for a specific path parameter in the URL. The path parameter must be wrapped in a pair of
     * curly braces, like "{paramName}".
     * @param parameterName the path parameter's name in the curly braces
     * @param value the String value to replace the path parameter
     *
     * @return the modified DynamicRequest object
     * @throws IllegalArgumentException if the parameterName is not found in the endpoint URL
     */
    public DynamicRequest setPathParam(String parameterName, String value) {
        if (!url.contains("{" + parameterName + "}")) {
            throw logger.logThrowableAsError(new IllegalArgumentException("no path param \"" + parameterName + "\""));
        }
        url = url.replace("{" + parameterName + "}", value);
        return this;
    }

    /**
     * Adds a query parameter to the request URL.
     * @param parameterName the name of the query parameter
     * @param value the value of the query parameter
     *
     * @return the modified DynamicRequest object
     */
    public DynamicRequest addQueryParam(String parameterName, String value) {
        queries.put(parameterName, value);
        return this;
    }

    private HttpRequest buildRequest() {
        if (url == null) {
            throw logger.logExceptionAsError(new NullPointerException("url cannot be null"));
        }
        if (httpMethod == null) {
            throw logger.logExceptionAsError(new NullPointerException("httpMethod cannot be null"));
        }
        if (!queries.isEmpty()) {
            url = url + (url.contains("?") ? "&" : "?");
            url = url + queries.keySet().stream().map(key -> key + "=" + queries.get(key)).collect(Collectors.joining("&"));
        }
        HttpRequest request = new HttpRequest(httpMethod, url);
        if (headers != null) {
            request = request.setHeaders(headers);
        }
        if (body != null && body.length != 0) {
            request = request.setBody(body);
        }
        return request;
    }

    /**
     * Sends the request through the HTTP pipeline synchronously.
     * @return the dynamic response received from the API
     */
    public DynamicResponse send() {
        return send(Context.NONE);
    }

    /**
     * Sends the request through the HTTP pipeline synchronously.
     * @param context the context to send with the request
     *
     * @return the dynamic response received from the API
     */
    public DynamicResponse send(Context context) {
        return sendAsync(context).block();
    }

    /**
     * Sends the request through the HTTP pipeline asynchronously.
     * @return the reactor publisher for the dynamic response to subscribe to
     */
    public Mono<DynamicResponse> sendAsync() {
        return FluxUtil.withContext(this::sendAsync);
    }

    private Mono<DynamicResponse> sendAsync(Context context) {
        return httpPipeline.send(buildRequest(), context)
            .flatMap(httpResponse -> BinaryData.fromFlux(httpResponse.getBody())
                .map(data -> new DynamicResponse(httpResponse, data)));
    }
}
