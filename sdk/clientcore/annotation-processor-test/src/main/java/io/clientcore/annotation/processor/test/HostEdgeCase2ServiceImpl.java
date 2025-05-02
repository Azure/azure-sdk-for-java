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
import io.clientcore.annotation.processor.test.implementation.HostEdgeCase2Service;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonSerializer;
import io.clientcore.core.serialization.xml.XmlSerializer;
import io.clientcore.core.http.models.HttpResponseException;

/**
 * Initializes a new instance of the HostEdgeCase2ServiceImpl type.
 */
public class HostEdgeCase2ServiceImpl implements HostEdgeCase2Service {

    private static final ClientLogger LOGGER = new ClientLogger(HostEdgeCase2Service.class);

    private final HttpPipeline httpPipeline;

    private final JsonSerializer jsonSerializer;

    private final XmlSerializer xmlSerializer;

    private HostEdgeCase2ServiceImpl(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        this.jsonSerializer = JsonSerializer.getInstance();
        this.xmlSerializer = XmlSerializer.getInstance();
    }

    /**
     * Creates an instance of HostEdgeCase2Service that is capable of sending requests to the service.
     * @param httpPipeline The HTTP pipeline to use for sending requests.
     * @return An instance of `HostEdgeCase2Service`;
     */
    public static HostEdgeCase2Service getNewInstance(HttpPipeline httpPipeline) {
        return new HostEdgeCase2ServiceImpl(httpPipeline);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public byte[] getByteArray(String uri, int numberOfBytes) {
        String requestUri = uri + "/bytes/" + numberOfBytes;
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        BinaryData responseBody = networkResponse.getValue();
        byte[] responseBodyBytes = responseBody != null ? responseBody.toBytes() : null;
        return responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null;
    }
}
