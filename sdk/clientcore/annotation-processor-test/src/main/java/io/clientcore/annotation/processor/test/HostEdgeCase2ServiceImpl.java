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
import io.clientcore.core.utils.GeneratedCodeUtils;
import java.util.Arrays;
import java.util.Base64;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.serialization.SerializationFormat;

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

    @Override
    public byte[] getByteArray(String uri, int numberOfBytes) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri((uri == null ? "" : uri) + "/bytes/" + UriEscapers.PATH_ESCAPER.escape(String.valueOf(numberOfBytes)));
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
