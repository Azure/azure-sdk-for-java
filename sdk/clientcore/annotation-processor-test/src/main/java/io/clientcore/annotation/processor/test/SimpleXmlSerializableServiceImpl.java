// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.annotation.processor.test;

import io.clientcore.annotation.processor.test.implementation.models.SimpleXmlSerializable;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.implementation.utils.UriEscapers;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.annotation.processor.test.implementation.SimpleXmlSerializableService;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonSerializer;
import io.clientcore.core.serialization.xml.XmlSerializer;
import io.clientcore.core.serialization.SerializationFormat;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.http.models.HttpResponseException;
import java.lang.reflect.ParameterizedType;
import io.clientcore.core.http.models.HttpHeader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Initializes a new instance of the SimpleXmlSerializableServiceImpl type.
 */
public class SimpleXmlSerializableServiceImpl implements SimpleXmlSerializableService {

    private static final ClientLogger LOGGER = new ClientLogger(SimpleXmlSerializableService.class);

    private final HttpPipeline httpPipeline;

    private final JsonSerializer jsonSerializer;

    private final XmlSerializer xmlSerializer;

    private SimpleXmlSerializableServiceImpl(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        this.jsonSerializer = JsonSerializer.getInstance();
        this.xmlSerializer = XmlSerializer.getInstance();
    }

    /**
     * Creates an instance of SimpleXmlSerializableService that is capable of sending requests to the service.
     * @param httpPipeline The HTTP pipeline to use for sending requests.
     * @return An instance of `SimpleXmlSerializableService`;
     */
    public static SimpleXmlSerializableService getNewInstance(HttpPipeline httpPipeline) {
        return new SimpleXmlSerializableServiceImpl(httpPipeline);
    }

    @SuppressWarnings("cast")
    @Override
    public void sendApplicationXml(SimpleXmlSerializable simpleXmlSerializable) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri("http://localhost/sendApplicationXml");
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml");
        if (simpleXmlSerializable != null) {
            SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
            if (xmlSerializer.supportsFormat(serializationFormat)) {
                httpRequest.setBody(BinaryData.fromObject(simpleXmlSerializable, xmlSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromObject(simpleXmlSerializable, jsonSerializer));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            BinaryData value = networkResponse.getValue();
            if (value == null || value.toBytes().length == 0) {
                networkResponse.close();
                throw instantiateUnexpectedException(responseCode, networkResponse, null, null);
            } else {
                ParameterizedType returnType = null;
                Object decoded = CoreUtils.decodeNetworkResponse(value, jsonSerializer, returnType);
                networkResponse.close();
                throw instantiateUnexpectedException(responseCode, networkResponse, value, decoded);
            }
        }
        networkResponse.close();
    }

    @SuppressWarnings("cast")
    @Override
    public void sendTextXml(SimpleXmlSerializable simpleXmlSerializable) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri("http://localhost/sendTextXml");
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/xml");
        if (simpleXmlSerializable != null) {
            SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
            if (xmlSerializer.supportsFormat(serializationFormat)) {
                httpRequest.setBody(BinaryData.fromObject(simpleXmlSerializable, xmlSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromObject(simpleXmlSerializable, jsonSerializer));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            BinaryData value = networkResponse.getValue();
            if (value == null || value.toBytes().length == 0) {
                networkResponse.close();
                throw instantiateUnexpectedException(responseCode, networkResponse, null, null);
            } else {
                ParameterizedType returnType = null;
                Object decoded = CoreUtils.decodeNetworkResponse(value, jsonSerializer, returnType);
                networkResponse.close();
                throw instantiateUnexpectedException(responseCode, networkResponse, value, decoded);
            }
        }
        networkResponse.close();
    }

    @SuppressWarnings("cast")
    @Override
    public SimpleXmlSerializable getXml(String contentType) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost/getXml");
        if (contentType != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_TYPE, contentType));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            BinaryData value = networkResponse.getValue();
            if (value == null || value.toBytes().length == 0) {
                networkResponse.close();
                throw instantiateUnexpectedException(responseCode, networkResponse, null, null);
            } else {
                ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.SimpleXmlSerializable.class);
                Object decoded = CoreUtils.decodeNetworkResponse(value, jsonSerializer, returnType);
                networkResponse.close();
                throw instantiateUnexpectedException(responseCode, networkResponse, value, decoded);
            }
        }
        SimpleXmlSerializable deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(SimpleXmlSerializable.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + ".");
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public SimpleXmlSerializable getInvalidXml(String contentType) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost/getInvalidXml");
        if (contentType != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_TYPE, contentType));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            BinaryData value = networkResponse.getValue();
            if (value == null || value.toBytes().length == 0) {
                networkResponse.close();
                throw instantiateUnexpectedException(responseCode, networkResponse, null, null);
            } else {
                ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.SimpleXmlSerializable.class);
                Object decoded = CoreUtils.decodeNetworkResponse(value, jsonSerializer, returnType);
                networkResponse.close();
                throw instantiateUnexpectedException(responseCode, networkResponse, value, decoded);
            }
        }
        SimpleXmlSerializable deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(SimpleXmlSerializable.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + ".");
        }
        return deserializedResult;
    }

    private static HttpResponseException instantiateUnexpectedException(int responseCode, Response<BinaryData> response, BinaryData data, Object decodedValue) {
        StringBuilder exceptionMessage = new StringBuilder("Status code ").append(responseCode).append(", ");
        String contentType = response.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE);
        if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            String contentLength = response.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH);
            exceptionMessage.append("(").append(contentLength).append("-byte body)");
        } else if (data == null || data.toBytes().length == 0) {
            exceptionMessage.append("(empty body)");
        } else {
            exceptionMessage.append('"').append(new String(data.toBytes(), StandardCharsets.UTF_8)).append('"');
        }
        if (decodedValue instanceof IOException || decodedValue instanceof IllegalStateException) {
            return new HttpResponseException(exceptionMessage.toString(), response, (Throwable) decodedValue);
        }
        return new HttpResponseException(exceptionMessage.toString(), response, decodedValue);
    }
}
