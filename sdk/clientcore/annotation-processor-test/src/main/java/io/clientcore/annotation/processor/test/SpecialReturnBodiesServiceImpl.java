// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.annotation.processor.test;

import io.clientcore.annotation.processor.test.implementation.models.Foo;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.implementation.utils.UriEscapers;
import io.clientcore.core.models.binarydata.BinaryData;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import io.clientcore.annotation.processor.test.implementation.SpecialReturnBodiesService;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonSerializer;
import io.clientcore.core.serialization.xml.XmlSerializer;
import io.clientcore.core.utils.GeneratedCodeUtils;
import io.clientcore.core.utils.CoreUtils;
import java.lang.reflect.ParameterizedType;
import io.clientcore.core.serialization.SerializationFormat;
import io.clientcore.core.utils.Base64Uri;
import io.clientcore.core.http.models.HttpHeader;
import java.time.format.DateTimeFormatter;

/**
 * Initializes a new instance of the SpecialReturnBodiesServiceImpl type.
 */
public class SpecialReturnBodiesServiceImpl implements SpecialReturnBodiesService {

    private static final HttpHeaderName VALUE = HttpHeaderName.fromString("value");

    private static final ClientLogger LOGGER = new ClientLogger(SpecialReturnBodiesService.class);

    private final HttpPipeline httpPipeline;

    private final JsonSerializer jsonSerializer;

    private final XmlSerializer xmlSerializer;

    private SpecialReturnBodiesServiceImpl(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        this.jsonSerializer = JsonSerializer.getInstance();
        this.xmlSerializer = XmlSerializer.getInstance();
    }

    /**
     * Creates an instance of SpecialReturnBodiesService that is capable of sending requests to the service.
     * @param httpPipeline The HTTP pipeline to use for sending requests.
     * @return An instance of `SpecialReturnBodiesService`;
     */
    public static SpecialReturnBodiesService getNewInstance(HttpPipeline httpPipeline) {
        return new SpecialReturnBodiesServiceImpl(httpPipeline);
    }

    @SuppressWarnings("cast")
    @Override
    public BinaryData getBinaryData(String url) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url + "/bytes");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
            networkResponse.close();
        }
        return networkResponse.getValue();
    }

    @SuppressWarnings("cast")
    @Override
    public Response<BinaryData> getBinaryDataWithResponse(String url) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url + "/bytes");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
            networkResponse.close();
        }
        return networkResponse;
    }

    @SuppressWarnings("cast")
    @Override
    public byte[] getByteArray(String url) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url + "/bytes");
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 200;
            if (!expectedResponse) {
                // Handle unexpected response
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
            }
            BinaryData responseBody = networkResponse.getValue();
            return responseBody != null ? responseBody.toBytes() : null;
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<byte[]> getByteArrayWithResponse(String url) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url + "/bytes");
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 200;
            if (!expectedResponse) {
                // Handle unexpected response
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
            }
            BinaryData responseBody = networkResponse.getValue();
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), responseBody != null ? responseBody.toBytes() : null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public InputStream getInputStream(String url) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url + "/bytes");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
            networkResponse.close();
        }
        return networkResponse.getValue().toStream();
    }

    @SuppressWarnings("cast")
    @Override
    public Response<InputStream> getInputStreamWithResponse(String url) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url + "/bytes");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
            networkResponse.close();
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), networkResponse.getValue().toStream());
    }

    @SuppressWarnings("cast")
    @Override
    public Response<List<BinaryData>> getListOfBinaryData(String endpoint) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(endpoint + "/type/array/unknown");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
            networkResponse.close();
        }
        List<BinaryData> deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(List.class, BinaryData.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(networkResponse.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw LOGGER.throwableAtError().addKeyValue("serializationFormat", serializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
        }
        networkResponse.close();
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }

    @SuppressWarnings("cast")
    @Override
    public Response<byte[]> base64url(String endpoint, String contentType, byte[] value) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(endpoint + "/encode/bytes/body/response/base64url");
        if (value != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, contentType);
            SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
            if (xmlSerializer.supportsFormat(serializationFormat)) {
                httpRequest.setBody(BinaryData.fromObject(value, xmlSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromObject(value, jsonSerializer));
            }
        }
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 200;
            if (!expectedResponse) {
                // Handle unexpected response
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
            }
            BinaryData responseBody = networkResponse.getValue();
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), responseBody != null ? new Base64Uri(responseBody.toBytes()).decodedBytes() : null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> rfc3339(String endpoint, OffsetDateTime value) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(endpoint + "/encode/datetime/header/rfc3339");
        if (value != null) {
            httpRequest.getHeaders().add(new HttpHeader(VALUE, value.format(DateTimeFormatter.ISO_INSTANT)));
        }
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 204;
            if (!expectedResponse) {
                // Handle unexpected response
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> omit(String endpoint, Foo body) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(endpoint + "/parameters/body-optionality/optional-explicit/omit");
        if (body != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
            SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
            if (xmlSerializer.supportsFormat(serializationFormat)) {
                httpRequest.setBody(BinaryData.fromObject(body, xmlSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromObject(body, jsonSerializer));
            }
        }
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 204;
            if (!expectedResponse) {
                // Handle unexpected response
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }
}
