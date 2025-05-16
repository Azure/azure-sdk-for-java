// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.annotation.processor.test;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.implementation.utils.UriEscapers;
import io.clientcore.core.models.binarydata.BinaryData;
import java.io.InputStream;
import java.util.List;
import io.clientcore.annotation.processor.test.implementation.SpecialReturnBodiesService;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonSerializer;
import io.clientcore.core.serialization.xml.XmlSerializer;
import java.lang.reflect.ParameterizedType;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.serialization.SerializationFormat;

/**
 * Initializes a new instance of the SpecialReturnBodiesServiceImpl type.
 */
public class SpecialReturnBodiesServiceImpl implements SpecialReturnBodiesService {

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
            BinaryData networkResponseValue = networkResponse.getValue();
            StringBuilder exceptionMessage = new StringBuilder("Status code ").append(responseCode).append(", ");
            if ("application/octet-stream".equalsIgnoreCase(networkResponse.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE))) {
                exceptionMessage.append("(").append(networkResponse.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH)).append("-byte body)");
                networkResponse.close();
                throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, null);
            } else if (networkResponseValue == null || networkResponseValue.toBytes().length == 0) {
                exceptionMessage.append("(empty body)");
                networkResponse.close();
                throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, null);
            } else {
                exceptionMessage.append('"').append(new String(networkResponseValue.toBytes(), java.nio.charset.StandardCharsets.UTF_8)).append('"');
                ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.models.binarydata.BinaryData.class);
                Object decoded = CoreUtils.decodeNetworkResponse(networkResponseValue, jsonSerializer, returnType);
                networkResponse.close();
                throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, decoded);
            }
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
            BinaryData networkResponseValue = networkResponse.getValue();
            StringBuilder exceptionMessage = new StringBuilder("Status code ").append(responseCode).append(", ");
            if ("application/octet-stream".equalsIgnoreCase(networkResponse.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE))) {
                exceptionMessage.append("(").append(networkResponse.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH)).append("-byte body)");
                networkResponse.close();
                throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, null);
            } else if (networkResponseValue == null || networkResponseValue.toBytes().length == 0) {
                exceptionMessage.append("(empty body)");
                networkResponse.close();
                throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, null);
            } else {
                exceptionMessage.append('"').append(new String(networkResponseValue.toBytes(), java.nio.charset.StandardCharsets.UTF_8)).append('"');
                ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.http.models.Response.class, BinaryData.class);
                Object decoded = CoreUtils.decodeNetworkResponse(networkResponseValue, jsonSerializer, returnType);
                networkResponse.close();
                throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, decoded);
            }
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
                BinaryData networkResponseValue = networkResponse.getValue();
                StringBuilder exceptionMessage = new StringBuilder("Status code ").append(responseCode).append(", ");
                if ("application/octet-stream".equalsIgnoreCase(networkResponse.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE))) {
                    exceptionMessage.append("(").append(networkResponse.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH)).append("-byte body)");
                    throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, null);
                } else if (networkResponseValue == null || networkResponseValue.toBytes().length == 0) {
                    exceptionMessage.append("(empty body)");
                    throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, null);
                } else {
                    exceptionMessage.append('"').append(new String(networkResponseValue.toBytes(), java.nio.charset.StandardCharsets.UTF_8)).append('"');
                    ParameterizedType returnType = null;
                    Object decoded = CoreUtils.decodeNetworkResponse(networkResponseValue, jsonSerializer, returnType);
                    throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, decoded);
                }
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
                BinaryData networkResponseValue = networkResponse.getValue();
                StringBuilder exceptionMessage = new StringBuilder("Status code ").append(responseCode).append(", ");
                if ("application/octet-stream".equalsIgnoreCase(networkResponse.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE))) {
                    exceptionMessage.append("(").append(networkResponse.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH)).append("-byte body)");
                    throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, null);
                } else if (networkResponseValue == null || networkResponseValue.toBytes().length == 0) {
                    exceptionMessage.append("(empty body)");
                    throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, null);
                } else {
                    exceptionMessage.append('"').append(new String(networkResponseValue.toBytes(), java.nio.charset.StandardCharsets.UTF_8)).append('"');
                    ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.http.models.Response.class, byte[].class);
                    Object decoded = CoreUtils.decodeNetworkResponse(networkResponseValue, jsonSerializer, returnType);
                    throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, decoded);
                }
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
            BinaryData networkResponseValue = networkResponse.getValue();
            StringBuilder exceptionMessage = new StringBuilder("Status code ").append(responseCode).append(", ");
            if ("application/octet-stream".equalsIgnoreCase(networkResponse.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE))) {
                exceptionMessage.append("(").append(networkResponse.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH)).append("-byte body)");
                networkResponse.close();
                throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, null);
            } else if (networkResponseValue == null || networkResponseValue.toBytes().length == 0) {
                exceptionMessage.append("(empty body)");
                networkResponse.close();
                throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, null);
            } else {
                exceptionMessage.append('"').append(new String(networkResponseValue.toBytes(), java.nio.charset.StandardCharsets.UTF_8)).append('"');
                ParameterizedType returnType = CoreUtils.createParameterizedType(java.io.InputStream.class);
                Object decoded = CoreUtils.decodeNetworkResponse(networkResponseValue, jsonSerializer, returnType);
                networkResponse.close();
                throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, decoded);
            }
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
            BinaryData networkResponseValue = networkResponse.getValue();
            StringBuilder exceptionMessage = new StringBuilder("Status code ").append(responseCode).append(", ");
            if ("application/octet-stream".equalsIgnoreCase(networkResponse.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE))) {
                exceptionMessage.append("(").append(networkResponse.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH)).append("-byte body)");
                networkResponse.close();
                throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, null);
            } else if (networkResponseValue == null || networkResponseValue.toBytes().length == 0) {
                exceptionMessage.append("(empty body)");
                networkResponse.close();
                throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, null);
            } else {
                exceptionMessage.append('"').append(new String(networkResponseValue.toBytes(), java.nio.charset.StandardCharsets.UTF_8)).append('"');
                ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.http.models.Response.class, InputStream.class);
                Object decoded = CoreUtils.decodeNetworkResponse(networkResponseValue, jsonSerializer, returnType);
                networkResponse.close();
                throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, decoded);
            }
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
            BinaryData networkResponseValue = networkResponse.getValue();
            StringBuilder exceptionMessage = new StringBuilder("Status code ").append(responseCode).append(", ");
            if ("application/octet-stream".equalsIgnoreCase(networkResponse.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE))) {
                exceptionMessage.append("(").append(networkResponse.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH)).append("-byte body)");
                networkResponse.close();
                throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, null);
            } else if (networkResponseValue == null || networkResponseValue.toBytes().length == 0) {
                exceptionMessage.append("(empty body)");
                networkResponse.close();
                throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, null);
            } else {
                exceptionMessage.append('"').append(new String(networkResponseValue.toBytes(), java.nio.charset.StandardCharsets.UTF_8)).append('"');
                ParameterizedType returnType = CoreUtils.createParameterizedType(List.class, BinaryData.class);
                Object decoded = CoreUtils.decodeNetworkResponse(networkResponseValue, jsonSerializer, returnType);
                networkResponse.close();
                throw CoreUtils.instantiateUnexpectedException(exceptionMessage.toString(), networkResponse, decoded);
            }
        }
        List<BinaryData> deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(List.class, BinaryData.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + ".");
        }
        networkResponse.close();
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }
}
