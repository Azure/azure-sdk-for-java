// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.core.exception.AzureException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.test.http.MockHttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobPollingTests {
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void resumesExistingJobsUsingGetOnly(boolean async) {
        AIProjectClientBuilder builder = builder("succeeded");
        if (async) {
            assertNotNull(builder.beta()
                .buildBetaDatasetsAsyncClient()
                .resumeGenerationJob("job")
                .setPollInterval(Duration.ofMillis(1))
                .last()
                .flatMap(response -> response.getFinalResult())
                .block(Duration.ofSeconds(5)));
            assertNotNull(builder.beta()
                .buildBetaEvaluatorsAsyncClient()
                .resumeEvaluatorGenerationJob("job")
                .setPollInterval(Duration.ofMillis(1))
                .last()
                .flatMap(response -> response.getFinalResult())
                .block(Duration.ofSeconds(5)));
            assertNotNull(builder.beta()
                .buildBetaAgentInsightMonitorsAsyncClient()
                .resumeAgentInsightRun("monitor", "run")
                .setPollInterval(Duration.ofMillis(1))
                .last()
                .flatMap(response -> response.getFinalResult())
                .block(Duration.ofSeconds(5)));
        } else {
            assertNotNull(builder.beta()
                .buildBetaDatasetsClient()
                .resumeGenerationJob("job")
                .setPollInterval(Duration.ofMillis(1))
                .getFinalResult(Duration.ofSeconds(5)));
            assertNotNull(builder.beta()
                .buildBetaEvaluatorsClient()
                .resumeEvaluatorGenerationJob("job")
                .setPollInterval(Duration.ofMillis(1))
                .getFinalResult(Duration.ofSeconds(5)));
            assertNotNull(builder.beta()
                .buildBetaAgentInsightMonitorsClient()
                .resumeAgentInsightRun("monitor", "run")
                .setPollInterval(Duration.ofMillis(1))
                .getFinalResult(Duration.ofSeconds(5)));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "failed", "cancelled" })
    void failedJobsDoNotReturnResults(String status) {
        AIProjectClientBuilder builder = builder(status);
        assertThrows(AzureException.class,
            () -> builder.beta()
                .buildBetaDatasetsClient()
                .resumeGenerationJob("job")
                .setPollInterval(Duration.ofMillis(1))
                .getFinalResult(Duration.ofSeconds(5)));
        assertThrows(AzureException.class,
            () -> builder.beta()
                .buildBetaDatasetsAsyncClient()
                .resumeGenerationJob("job")
                .setPollInterval(Duration.ofMillis(1))
                .last()
                .flatMap(response -> response.getFinalResult())
                .block(Duration.ofSeconds(5)));
    }

    private static AIProjectClientBuilder builder(String status) {
        return new AIProjectClientBuilder().endpoint("https://localhost/projects/test")
            .pipeline(new HttpPipelineBuilder().httpClient(request -> {
                assertEquals(HttpMethod.GET, request.getHttpMethod());
                String body = "{\"id\":\"job\",\"status\":\"" + status + "\",\"result\":{}}";
                return Mono.just(new MockHttpResponse(request, 200,
                    new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                    body.getBytes(StandardCharsets.UTF_8)));
            }).build());
    }
}
