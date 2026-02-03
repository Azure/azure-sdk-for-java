// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

public class RequestIdPolicyTests {
    private static final HttpHeaderName REQUEST_ID_HEADER = HttpHeaderName.fromString("request-id");

    @Test
    public void newRequestIdForEachCall() throws IOException {
        AtomicReference<String> previousRequestId = new AtomicReference<>();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().addPolicy(new RequestIdPolicy(REQUEST_ID_HEADER)).httpClient(request -> {
                String lastRequestId = previousRequestId.get();
                if (lastRequestId != null) {
                    String newRequestId = request.getHeaders().getValue(REQUEST_ID_HEADER);
                    Assertions.assertNotNull(newRequestId);
                    Assertions.assertNotEquals(newRequestId, lastRequestId);
                }

                String newRequestId = request.getHeaders().getValue(REQUEST_ID_HEADER);
                previousRequestId.set(newRequestId);
                if (newRequestId == null) {
                    Assertions.fail();
                }

                return new Response<>(request, 500, new HttpHeaders(), BinaryData.empty());
            }).build();

        try (Response<?> response
            = pipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri("https://test.com"))) {
            Assertions.assertEquals(500, response.getStatusCode());
        }

        try (Response<?> response
            = pipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri("https://test.com"))) {
            Assertions.assertEquals(500, response.getStatusCode());
        }
    }

    @Test
    public void sameRequestIdForRetry() throws IOException {
        AtomicReference<String> previousRequestId = new AtomicReference<>();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().addPolicy(new HttpRetryPolicy(new HttpRetryOptions(3, Duration.ofSeconds(0))))
                .addPolicy(new RequestIdPolicy(REQUEST_ID_HEADER))
                .httpClient(request -> {
                    String lastRequestId = previousRequestId.get();
                    if (lastRequestId != null) {
                        String newRequestId = request.getHeaders().getValue(REQUEST_ID_HEADER);
                        Assertions.assertNotNull(newRequestId);
                        Assertions.assertEquals(newRequestId, lastRequestId);
                    }

                    String newRequestId = request.getHeaders().getValue(REQUEST_ID_HEADER);
                    previousRequestId.set(newRequestId);
                    if (newRequestId == null) {
                        Assertions.fail();
                    }

                    return new Response<>(request, 500, new HttpHeaders(), BinaryData.empty());
                })
                .build();

        try (Response<?> response
            = pipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri("https://test.com"))) {
            Assertions.assertEquals(500, response.getStatusCode());
        }
    }
}
