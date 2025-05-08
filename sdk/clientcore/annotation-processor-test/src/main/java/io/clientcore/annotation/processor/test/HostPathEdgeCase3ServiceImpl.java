// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.annotation.processor.test;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.implementation.utils.UriEscapers;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.annotation.processor.test.implementation.HostPathEdgeCase3Service;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonSerializer;
import io.clientcore.core.serialization.xml.XmlSerializer;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.utils.CoreUtils;
import java.lang.reflect.ParameterizedType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Initializes a new instance of the HostPathEdgeCase3ServiceImpl type.
 */
public class HostPathEdgeCase3ServiceImpl implements HostPathEdgeCase3Service {

    private static final ClientLogger LOGGER = new ClientLogger(HostPathEdgeCase3Service.class);

    private final HttpPipeline httpPipeline;

    private final JsonSerializer jsonSerializer;

    private final XmlSerializer xmlSerializer;

    private HostPathEdgeCase3ServiceImpl(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        this.jsonSerializer = JsonSerializer.getInstance();
        this.xmlSerializer = XmlSerializer.getInstance();
    }

    /**
     * Creates an instance of HostPathEdgeCase3Service that is capable of sending requests to the service.
     * @param httpPipeline The HTTP pipeline to use for sending requests.
     * @return An instance of `HostPathEdgeCase3Service`;
     */
    public static HostPathEdgeCase3Service getNewInstance(HttpPipeline httpPipeline) {
        return new HostPathEdgeCase3ServiceImpl(httpPipeline);
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> noOperationParams(String endpoint, String apiVersion, RequestContext requestContext) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(endpoint + "/server/path/multiple/" + apiVersion + "/");
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 204;
            if (!expectedResponse) {
                BinaryData value = networkResponse.getValue();
                if (value == null || value.toBytes().length == 0) {
                    throw instantiateUnexpectedException(responseCode, networkResponse, null, null);
                } else {
                    ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.http.models.Response.class, Void.class);
                    Object decoded = CoreUtils.decodeNetworkResponse(value, jsonSerializer, returnType);
                    throw instantiateUnexpectedException(responseCode, networkResponse, value, decoded);
                }
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
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
