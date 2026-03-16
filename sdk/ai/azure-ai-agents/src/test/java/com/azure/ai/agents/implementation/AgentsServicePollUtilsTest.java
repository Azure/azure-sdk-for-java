// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingStrategyOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class AgentsServicePollUtilsTest {

    static Stream<Arguments> remapStatusCases() {
        return Stream.of(
            // Custom statuses that need remapping
            Arguments.of("completed", LongRunningOperationStatus.SUCCESSFULLY_COMPLETED),
            Arguments.of("Completed", LongRunningOperationStatus.SUCCESSFULLY_COMPLETED),
            Arguments.of("COMPLETED", LongRunningOperationStatus.SUCCESSFULLY_COMPLETED),
            Arguments.of("superseded", LongRunningOperationStatus.USER_CANCELLED),
            Arguments.of("Superseded", LongRunningOperationStatus.USER_CANCELLED));
    }

    @ParameterizedTest
    @MethodSource("remapStatusCases")
    void remapStatusMapsCustomStatuses(String statusName, LongRunningOperationStatus expected) {
        // The parent's PollResult.setStatus(String) calls fromString(name, false) for unknown statuses
        LongRunningOperationStatus customStatus = LongRunningOperationStatus.fromString(statusName, false);
        PollResponse<String> original = new PollResponse<>(customStatus, "value");

        PollResponse<String> remapped = AgentsServicePollUtils.remapStatus(original);

        assertEquals(expected, remapped.getStatus());
        assertEquals("value", remapped.getValue());
    }

    static Stream<Arguments> standardStatusCases() {
        return Stream.of(Arguments.of(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED),
            Arguments.of(LongRunningOperationStatus.FAILED), Arguments.of(LongRunningOperationStatus.USER_CANCELLED),
            Arguments.of(LongRunningOperationStatus.IN_PROGRESS), Arguments.of(LongRunningOperationStatus.NOT_STARTED));
    }

    @ParameterizedTest
    @MethodSource("standardStatusCases")
    void remapStatusPassesThroughStandardStatuses(LongRunningOperationStatus status) {
        PollResponse<String> original = new PollResponse<>(status, "value");

        PollResponse<String> result = AgentsServicePollUtils.remapStatus(original);

        assertSame(original, result, "Standard status should return the same PollResponse instance");
    }

    @Test
    void remapStatusPreservesRetryAfter() {
        LongRunningOperationStatus completed = LongRunningOperationStatus.fromString("completed", false);
        java.time.Duration retryAfter = java.time.Duration.ofSeconds(5);
        PollResponse<String> original = new PollResponse<>(completed, "value", retryAfter);

        PollResponse<String> remapped = AgentsServicePollUtils.remapStatus(original);

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, remapped.getStatus());
        assertEquals(retryAfter, remapped.getRetryAfter());
    }

    @Test
    void withFoundryFeaturesAddsHeaderToContext() {
        PollingStrategyOptions options = new PollingStrategyOptions(new HttpPipelineBuilder().build());

        PollingStrategyOptions result = AgentsServicePollUtils.withFoundryFeatures(options);

        Context context = result.getContext();
        assertNotNull(context);
        Object headerObj = context.getData(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY).orElse(null);
        assertNotNull(headerObj, "Context should contain HTTP headers under the AddHeadersFromContextPolicy key");
        HttpHeaders headers = (HttpHeaders) headerObj;
        assertEquals("MemoryStores=V1Preview",
            headers.getValue(com.azure.core.http.HttpHeaderName.fromString("Foundry-Features")));
    }

    @Test
    void withFoundryFeaturesPreservesExistingContext() {
        PollingStrategyOptions options
            = new PollingStrategyOptions(new HttpPipelineBuilder().build()).setContext(new Context("myKey", "myValue"));

        PollingStrategyOptions result = AgentsServicePollUtils.withFoundryFeatures(options);

        Context context = result.getContext();
        assertEquals("myValue", context.getData("myKey").orElse(null), "Existing context data should be preserved");
        assertNotNull(context.getData(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY).orElse(null),
            "Foundry-Features header should also be present");
    }

    @Test
    void withFoundryFeaturesMergesWithExistingHeaders() {
        HttpHeaders existingHeaders = new HttpHeaders();
        existingHeaders.set(com.azure.core.http.HttpHeaderName.fromString("X-Custom"), "custom-value");
        Context contextWithHeaders
            = new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, existingHeaders);
        PollingStrategyOptions options
            = new PollingStrategyOptions(new HttpPipelineBuilder().build()).setContext(contextWithHeaders);

        PollingStrategyOptions result = AgentsServicePollUtils.withFoundryFeatures(options);

        HttpHeaders merged = (HttpHeaders) result.getContext()
            .getData(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY)
            .orElse(null);
        assertNotNull(merged);
        assertEquals("custom-value", merged.getValue(com.azure.core.http.HttpHeaderName.fromString("X-Custom")),
            "Pre-existing header should be preserved");
        assertEquals("MemoryStores=V1Preview",
            merged.getValue(com.azure.core.http.HttpHeaderName.fromString("Foundry-Features")),
            "Foundry-Features header should be added");
    }
}
