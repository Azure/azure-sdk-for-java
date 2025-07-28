// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.annotation.processor.test;

import io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.implementation.utils.UriEscapers;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.annotation.processor.test.implementation.ParameterizedMultipleHostService;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonSerializer;
import io.clientcore.core.serialization.xml.XmlSerializer;
import io.clientcore.core.utils.GeneratedCodeUtils;
import io.clientcore.core.utils.CoreUtils;
import java.lang.reflect.ParameterizedType;
import io.clientcore.core.serialization.SerializationFormat;

/**
 * Initializes a new instance of the ParameterizedMultipleHostServiceImpl type.
 */
public class ParameterizedMultipleHostServiceImpl implements ParameterizedMultipleHostService {

    private static final ClientLogger LOGGER = new ClientLogger(ParameterizedMultipleHostService.class);

    private final HttpPipeline httpPipeline;

    private final JsonSerializer jsonSerializer;

    private final XmlSerializer xmlSerializer;

    private ParameterizedMultipleHostServiceImpl(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        this.jsonSerializer = JsonSerializer.getInstance();
        this.xmlSerializer = XmlSerializer.getInstance();
    }

    /**
     * Creates an instance of ParameterizedMultipleHostService that is capable of sending requests to the service.
     * @param httpPipeline The HTTP pipeline to use for sending requests.
     * @return An instance of `ParameterizedMultipleHostService`;
     */
    public static ParameterizedMultipleHostService getNewInstance(HttpPipeline httpPipeline) {
        return new ParameterizedMultipleHostServiceImpl(httpPipeline);
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON get(String scheme, String hostPart1, String hostPart2) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(scheme + "://" + hostPart1 + hostPart2 + "/get");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
            networkResponse.close();
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
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
