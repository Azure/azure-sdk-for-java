// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.rest.BatchResult;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.storage.blob.models.ServicesSubmitBatchResponse;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

/**
 *
 */
public class BlobBatchResult extends BatchResult {
    private final Map<Integer, Response<?>> responses;

    /**
     *
     * @param batchResponse the response for the batch request from the service.
     */
    public BlobBatchResult(ServicesSubmitBatchResponse batchResponse) {
        responses = new HashMap<>();

        String batchBoundary = batchResponse.getHeaders().getValue("Content-Type").split("=")[1];
        byte[] body = FluxUtil.collectBytesInByteBufferStream(batchResponse.getValue()).block();
        String bodyAsString = new String(body, StandardCharsets.UTF_8);

        String[] responses = bodyAsString.split("--" + batchBoundary);

        for (int i = 1; i < responses.length - 1; i++) {
            String response = responses[i];
        }
    }

    @Override
    public Stream<Response<?>> getRawOperationResponses() {
        return responses.values().stream();
    }

    @Override
    public Iterator<Response<?>> iterator() {
        return responses.values().iterator();
    }
}
