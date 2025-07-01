// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.core.http.pipeline.PipelineTestHelpers.CUSTOM_REQUEST_ID;
import static io.clientcore.core.http.pipeline.PipelineTestHelpers.sendRequest;

@ParameterizedClass(name = "isAsync={0}")
@ValueSource(booleans = { false, true })
public class RequestIdPolicyTests {
    private final boolean isAsync;

    public RequestIdPolicyTests(boolean isAsync) {
        this.isAsync = isAsync;
    }

    @Test
    public void newRequestIdForEachCall() {
        AtomicReference<String> previousRequestId = new AtomicReference<>();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().addPolicy(new RequestIdPolicy(CUSTOM_REQUEST_ID)).httpClient(request -> {
                String lastRequestId = previousRequestId.get();
                if (lastRequestId != null) {
                    String newRequestId = request.getHeaders().getValue(CUSTOM_REQUEST_ID);
                    Assertions.assertNotNull(newRequestId);
                    Assertions.assertNotEquals(newRequestId, lastRequestId);
                }

                String newRequestId = request.getHeaders().getValue(CUSTOM_REQUEST_ID);
                previousRequestId.set(newRequestId);
                if (newRequestId == null) {
                    Assertions.fail();
                }

                return new Response<>(request, 500, new HttpHeaders(), BinaryData.empty());
            }).build();

        try (Response<BinaryData> response = sendRequest(pipeline, isAsync)) {
            Assertions.assertEquals(500, response.getStatusCode());
        }

        try (Response<?> response
            = pipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri("https://test.com"))) {
            Assertions.assertEquals(500, response.getStatusCode());
        }
    }

    @Test
    public void sameRequestIdForRetry() {
        AtomicReference<String> previousRequestId = new AtomicReference<>();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().addPolicy(new HttpRetryPolicy(new HttpRetryOptions(3, Duration.ofSeconds(0))))
                .addPolicy(new RequestIdPolicy(CUSTOM_REQUEST_ID))
                .httpClient(request -> {
                    String lastRequestId = previousRequestId.get();
                    if (lastRequestId != null) {
                        String newRequestId = request.getHeaders().getValue(CUSTOM_REQUEST_ID);
                        Assertions.assertNotNull(newRequestId);
                        Assertions.assertEquals(newRequestId, lastRequestId);
                    }

                    String newRequestId = request.getHeaders().getValue(CUSTOM_REQUEST_ID);
                    previousRequestId.set(newRequestId);
                    if (newRequestId == null) {
                        Assertions.fail();
                    }

                    return new Response<>(request, 500, new HttpHeaders(), BinaryData.empty());
                })
                .build();

        try (Response<BinaryData> response = sendRequest(pipeline, isAsync)) {
            Assertions.assertEquals(500, response.getStatusCode());
        }
    }
}
