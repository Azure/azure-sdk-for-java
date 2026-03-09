// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.annotation.processor.test;

import io.clientcore.annotation.processor.test.implementation.EncodeDurationService;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.json.JsonSerializer;
import io.clientcore.core.serialization.xml.XmlSerializer;
import io.clientcore.core.utils.GeneratedCodeUtils;
import io.clientcore.core.utils.UriBuilder;
import java.time.Duration;

/**
 * Initializes a new instance of the EncodeDurationServiceImpl type.
 */
public class EncodeDurationServiceImpl implements EncodeDurationService {

    private static final HttpHeaderName DURATION = HttpHeaderName.fromString("duration");

    private static final ClientLogger LOGGER = new ClientLogger(EncodeDurationService.class);

    private final HttpPipeline httpPipeline;

    private final JsonSerializer jsonSerializer;

    private final XmlSerializer xmlSerializer;

    private EncodeDurationServiceImpl(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        this.jsonSerializer = JsonSerializer.getInstance();
        this.xmlSerializer = XmlSerializer.getInstance();
    }

    /**
     * Creates an instance of EncodeDurationService that is capable of sending requests to the service.
     *
     * @param httpPipeline The HTTP pipeline to use for sending requests.
     * @return An instance of {@code EncodeDurationService}.
     */
    public static EncodeDurationService getNewInstance(HttpPipeline httpPipeline) {
        return new EncodeDurationServiceImpl(httpPipeline);
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> queryDefault(String endpoint, Duration input, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder requestUri = UriBuilder.parse(endpoint + "/encode/duration/query/default");
        if (input != null) {
            GeneratedCodeUtils.addQueryParameter(requestUri, "input", true, input, true);
        }
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri.toString());
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 204;
            if (!expectedResponse) {
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer,
                    xmlSerializer, null, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> queryInt32Seconds(String endpoint, long input, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder requestUri = UriBuilder.parse(endpoint + "/encode/duration/query/int32-seconds");
        GeneratedCodeUtils.addQueryParameter(requestUri, "input", true, input, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri.toString());
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 204;
            if (!expectedResponse) {
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer,
                    xmlSerializer, null, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> queryInt32SecondsLargerUnit(String endpoint, long input, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder requestUri = UriBuilder.parse(endpoint + "/encode/duration/query/int32-seconds-larger-unit");
        GeneratedCodeUtils.addQueryParameter(requestUri, "input", true, input, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri.toString());
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 204;
            if (!expectedResponse) {
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer,
                    xmlSerializer, null, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> queryFloatSeconds(String endpoint, double input, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder requestUri = UriBuilder.parse(endpoint + "/encode/duration/query/float-seconds");
        GeneratedCodeUtils.addQueryParameter(requestUri, "input", true, input, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri.toString());
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 204;
            if (!expectedResponse) {
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer,
                    xmlSerializer, null, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> queryFloatSecondsLargerUnit(String endpoint, double input, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder requestUri = UriBuilder.parse(endpoint + "/encode/duration/query/float-seconds-larger-unit");
        GeneratedCodeUtils.addQueryParameter(requestUri, "input", true, input, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri.toString());
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 204;
            if (!expectedResponse) {
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer,
                    xmlSerializer, null, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> queryInt32Milliseconds(String endpoint, long input, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder requestUri = UriBuilder.parse(endpoint + "/encode/duration/query/int32-milliseconds");
        GeneratedCodeUtils.addQueryParameter(requestUri, "input", true, input, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri.toString());
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 204;
            if (!expectedResponse) {
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer,
                    xmlSerializer, null, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> queryInt32MillisecondsLargerUnit(String endpoint, long input, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder requestUri = UriBuilder.parse(endpoint + "/encode/duration/query/int32-milliseconds-larger-unit");
        GeneratedCodeUtils.addQueryParameter(requestUri, "input", true, input, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri.toString());
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 204;
            if (!expectedResponse) {
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer,
                    xmlSerializer, null, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> headerDefault(String endpoint, Duration duration, RequestContext requestContext) {
        // Create the HttpRequest.
        HttpRequest httpRequest
            = new HttpRequest().setMethod(HttpMethod.GET).setUri(endpoint + "/encode/duration/header/default");
        if (duration != null) {
            httpRequest.getHeaders().add(new HttpHeader(DURATION, String.valueOf(duration)));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 204;
            if (!expectedResponse) {
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer,
                    xmlSerializer, null, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> headerInt32Seconds(String endpoint, long duration, RequestContext requestContext) {
        // Create the HttpRequest.
        HttpRequest httpRequest
            = new HttpRequest().setMethod(HttpMethod.GET).setUri(endpoint + "/encode/duration/header/int32-seconds");
        httpRequest.getHeaders().add(new HttpHeader(DURATION, String.valueOf(duration)));
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 204;
            if (!expectedResponse) {
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer,
                    xmlSerializer, null, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> headerInt32SecondsLargerUnit(String endpoint, long duration, RequestContext requestContext) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET)
            .setUri(endpoint + "/encode/duration/header/int32-seconds-larger-unit");
        httpRequest.getHeaders().add(new HttpHeader(DURATION, String.valueOf(duration)));
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 204;
            if (!expectedResponse) {
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer,
                    xmlSerializer, null, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> headerFloatSeconds(String endpoint, double duration, RequestContext requestContext) {
        // Create the HttpRequest.
        HttpRequest httpRequest
            = new HttpRequest().setMethod(HttpMethod.GET).setUri(endpoint + "/encode/duration/header/float-seconds");
        httpRequest.getHeaders().add(new HttpHeader(DURATION, String.valueOf(duration)));
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 204;
            if (!expectedResponse) {
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer,
                    xmlSerializer, null, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> headerFloatSecondsLargerUnit(String endpoint, double duration, RequestContext requestContext) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET)
            .setUri(endpoint + "/encode/duration/header/float-seconds-larger-unit");
        httpRequest.getHeaders().add(new HttpHeader(DURATION, String.valueOf(duration)));
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 204;
            if (!expectedResponse) {
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer,
                    xmlSerializer, null, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> headerInt32Milliseconds(String endpoint, long duration, RequestContext requestContext) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET)
            .setUri(endpoint + "/encode/duration/header/int32-milliseconds");
        httpRequest.getHeaders().add(new HttpHeader(DURATION, String.valueOf(duration)));
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 204;
            if (!expectedResponse) {
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer,
                    xmlSerializer, null, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> headerInt32MillisecondsLargerUnit(String endpoint, long duration,
        RequestContext requestContext) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET)
            .setUri(endpoint + "/encode/duration/header/int32-milliseconds-larger-unit");
        httpRequest.getHeaders().add(new HttpHeader(DURATION, String.valueOf(duration)));
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 204;
            if (!expectedResponse) {
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer,
                    xmlSerializer, null, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }
}
