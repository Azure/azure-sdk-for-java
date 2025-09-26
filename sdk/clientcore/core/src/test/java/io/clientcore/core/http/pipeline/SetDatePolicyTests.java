// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.core.http.pipeline.PipelineTestHelpers.sendRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Unit tests for {@link SetDatePolicy}.
 */
@ParameterizedClass(name = "isAsync={0}")
@ValueSource(booleans = { false, true })
public class SetDatePolicyTests {
    private final boolean isAsync;

    public SetDatePolicyTests(boolean isAsync) {
        this.isAsync = isAsync;
    }

    @Test
    public void dateIsRefreshedOnRetry() {
        try (Response<?> response = sendRequest(getPipeline(), isAsync)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    private static HttpPipeline getPipeline() {
        AtomicReference<String> firstDate = new AtomicReference<>();

        return new HttpPipelineBuilder().addPolicy(new HttpRetryPolicy(new HttpRetryOptions(1, Duration.ofSeconds(2))))
            .addPolicy(new SetDatePolicy())
            .httpClient(request -> {
                String date = request.getHeaders().getValue(HttpHeaderName.DATE);
                if (!firstDate.compareAndSet(null, date)) {
                    assertNotEquals(firstDate.get(), date);
                    return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
                } else {
                    return new Response<>(request, 429, new HttpHeaders(), BinaryData.empty());
                }
            })
            .build();
    }
}
