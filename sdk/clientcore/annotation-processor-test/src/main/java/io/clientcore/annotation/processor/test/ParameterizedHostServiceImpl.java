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
import io.clientcore.annotation.processor.test.implementation.ParameterizedHostService;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonSerializer;
import io.clientcore.core.serialization.xml.XmlSerializer;

/**
 * Initializes a new instance of the ParameterizedHostServiceImpl type.
 */
public class ParameterizedHostServiceImpl implements ParameterizedHostService {

    private static final ClientLogger LOGGER = new ClientLogger(ParameterizedHostService.class);

    private final HttpPipeline httpPipeline;

    private final JsonSerializer jsonSerializer;

    private final XmlSerializer xmlSerializer;

    private ParameterizedHostServiceImpl(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        this.jsonSerializer = JsonSerializer.getInstance();
        this.xmlSerializer = XmlSerializer.getInstance();
    }

    /**
     * Creates an instance of ParameterizedHostService that is capable of sending requests to the service.
     * @param httpPipeline The HTTP pipeline to use for sending requests.
     * @return An instance of `ParameterizedHostService`;
     */
    public static ParameterizedHostService getNewInstance(HttpPipeline httpPipeline) {
        return new ParameterizedHostServiceImpl(httpPipeline);
    }

    @SuppressWarnings("cast")
    @Override
    public byte[] getByteArray(String scheme, String host, int numberOfBytes) {
        String uri = scheme + "://" + host + "/bytes/" + numberOfBytes;
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        BinaryData responseBody = networkResponse.getValue();
        byte[] responseBodyBytes = responseBody != null ? responseBody.toBytes() : null;
        // Close the network response as the body should be consumed.
        networkResponse.close();
        return (responseBodyBytes != null && responseBodyBytes.length == 0) ? null : responseBodyBytes;
    }
}
