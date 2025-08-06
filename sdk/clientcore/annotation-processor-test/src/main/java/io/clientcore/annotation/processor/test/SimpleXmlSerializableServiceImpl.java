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
import io.clientcore.core.utils.GeneratedCodeUtils;
import java.lang.reflect.ParameterizedType;

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
        if (simpleXmlSerializable != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml");
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
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
            networkResponse.close();
        }
        networkResponse.close();
    }

    @SuppressWarnings("cast")
    @Override
    public void sendTextXml(SimpleXmlSerializable simpleXmlSerializable) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri("http://localhost/sendTextXml");
        if (simpleXmlSerializable != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/xml");
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
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
            networkResponse.close();
        }
        networkResponse.close();
    }

    @SuppressWarnings("cast")
    @Override
    public SimpleXmlSerializable getXml(String contentType) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost/getXml");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
            networkResponse.close();
        }
        SimpleXmlSerializable deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(SimpleXmlSerializable.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(networkResponse.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw LOGGER.throwableAtError().addKeyValue("serializationFormat", serializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public SimpleXmlSerializable getInvalidXml(String contentType) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost/getInvalidXml");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
            networkResponse.close();
        }
        SimpleXmlSerializable deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(SimpleXmlSerializable.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(networkResponse.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw LOGGER.throwableAtError().addKeyValue("serializationFormat", serializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
        }
        return deserializedResult;
    }
}
