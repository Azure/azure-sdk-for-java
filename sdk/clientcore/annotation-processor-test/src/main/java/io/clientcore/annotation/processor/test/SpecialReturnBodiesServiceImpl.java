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
import java.util.Arrays;
import java.util.Base64;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.serialization.SerializationFormat;
import java.lang.reflect.ParameterizedType;
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

    @Override
    public BinaryData getBinaryData(String url) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri((url == null ? "" : url) + "/bytes");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        return networkResponse.getValue();
    }

    @Override
    public Response<BinaryData> getBinaryDataWithResponse(String url) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri((url == null ? "" : url) + "/bytes");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        return networkResponse;
    }

    @Override
    public byte[] getByteArray(String url) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri((url == null ? "" : url) + "/bytes");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            BinaryData responseBody = networkResponseToClose.getValue();
            byte[] responseBytes = responseBody != null ? responseBody.toBytes() : null;
            boolean quotedBase64 = responseBytes != null && responseBytes.length >= 2 && responseBytes[0] == '"' && responseBytes[responseBytes.length - 1] == '"' && GeneratedCodeUtils.isJsonContentType(networkResponseToClose.getHeaders());
            return quotedBase64 ? Base64.getDecoder().decode(Arrays.copyOfRange(responseBytes, 1, responseBytes.length - 1)) : responseBytes;
        }
    }

    @Override
    public Response<byte[]> getByteArrayWithResponse(String url) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri((url == null ? "" : url) + "/bytes");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            BinaryData responseBody = networkResponseToClose.getValue();
            byte[] responseBytes = responseBody != null ? responseBody.toBytes() : null;
            boolean quotedBase64 = responseBytes != null && responseBytes.length >= 2 && responseBytes[0] == '"' && responseBytes[responseBytes.length - 1] == '"' && GeneratedCodeUtils.isJsonContentType(networkResponseToClose.getHeaders());
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), quotedBase64 ? Base64.getDecoder().decode(Arrays.copyOfRange(responseBytes, 1, responseBytes.length - 1)) : responseBytes);
        }
    }

    @Override
    public InputStream getInputStream(String url) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri((url == null ? "" : url) + "/bytes");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        return networkResponse.getValue().toStream();
    }

    @Override
    public Response<InputStream> getInputStreamWithResponse(String url) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri((url == null ? "" : url) + "/bytes");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), networkResponse.getValue().toStream());
    }

    @Override
    public Response<List<BinaryData>> getListOfBinaryData(String endpoint) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri((endpoint == null ? "" : endpoint) + "/type/array/unknown");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        List<BinaryData> deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.http.models.Response.class, CoreUtils.createParameterizedType(java.util.List.class, io.clientcore.core.models.binarydata.BinaryData.class));
        SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponse.getHeaders());
        if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
        }
        networkResponse.close();
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }

    @Override
    public Response<byte[]> base64url(String endpoint, String contentType, byte[] value) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri((endpoint == null ? "" : endpoint) + "/encode/bytes/body/response/base64url");
        if (value != null) {
            if (contentType != null) {
                httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, contentType);
            }
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(value, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromBytes(value));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            BinaryData responseBody = networkResponseToClose.getValue();
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), responseBody != null ? new Base64Uri(responseBody.toBytes()).decodedBytes() : null);
        }
    }

    @Override
    public Response<byte[]> base64(String endpoint) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri((endpoint == null ? "" : endpoint) + "/encode/bytes/body/response/base64");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            BinaryData responseBody = networkResponseToClose.getValue();
            byte[] responseBytes = responseBody != null ? responseBody.toBytes() : null;
            boolean quotedBase64 = responseBytes != null && responseBytes.length >= 2 && responseBytes[0] == '"' && responseBytes[responseBytes.length - 1] == '"' && GeneratedCodeUtils.isJsonContentType(networkResponseToClose.getHeaders());
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), quotedBase64 ? Base64.getDecoder().decode(Arrays.copyOfRange(responseBytes, 1, responseBytes.length - 1)) : responseBytes);
        }
    }

    @Override
    public Response<String> getText(String endpoint) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri((endpoint == null ? "" : endpoint) + "/text");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            String deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.http.models.Response.class, java.lang.String.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (responseSerializationFormat == SerializationFormat.TEXT) {
                BinaryData responseBody = networkResponseToClose.getValue();
                deserializedResult = responseBody == null ? null : responseBody.toString();
            } else if (jsonSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), deserializedResult);
        }
    }

    @Override
    public Response<Void> rfc3339(String endpoint, OffsetDateTime value) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri((endpoint == null ? "" : endpoint) + "/encode/datetime/header/rfc3339");
        if (value != null) {
            httpRequest.getHeaders().add(new HttpHeader(VALUE, value.format(DateTimeFormatter.ISO_INSTANT)));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 204;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), null);
        }
    }

    @Override
    public Response<Void> omit(String endpoint, Foo body) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri((endpoint == null ? "" : endpoint) + "/parameters/body-optionality/optional-explicit/omit");
        if (body != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
            SerializationFormat requestSerializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
            if (xmlSerializer.supportsFormat(requestSerializationFormat)) {
                httpRequest.setBody(BinaryData.fromObject(body, xmlSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromObject(body, jsonSerializer));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 204;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), null);
        }
    }
}
