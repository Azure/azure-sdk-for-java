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
import io.clientcore.annotation.processor.test.implementation.HostEdgeCase1Service;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonSerializer;
import io.clientcore.core.serialization.xml.XmlSerializer;
import io.clientcore.core.utils.GeneratedCodeUtils;
import java.util.Arrays;
import java.util.Base64;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.serialization.SerializationFormat;

/**
 * Initializes a new instance of the HostEdgeCase1ServiceImpl type.
 */
public class HostEdgeCase1ServiceImpl implements HostEdgeCase1Service {

    private static final ClientLogger LOGGER = new ClientLogger(HostEdgeCase1Service.class);

    private final HttpPipeline httpPipeline;

    private final JsonSerializer jsonSerializer;

    private final XmlSerializer xmlSerializer;

    private HostEdgeCase1ServiceImpl(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        this.jsonSerializer = JsonSerializer.getInstance();
        this.xmlSerializer = XmlSerializer.getInstance();
    }

    /**
     * Creates an instance of HostEdgeCase1Service that is capable of sending requests to the service.
     * @param httpPipeline The HTTP pipeline to use for sending requests.
     * @return An instance of `HostEdgeCase1Service`;
     */
    public static HostEdgeCase1Service getNewInstance(HttpPipeline httpPipeline) {
        return new HostEdgeCase1ServiceImpl(httpPipeline);
    }

    @Override
    public byte[] getByteArray(String url, int numberOfBytes) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri((url == null ? "" : url) + "/bytes/" + UriEscapers.PATH_ESCAPER.escape(String.valueOf(numberOfBytes)));
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
}
