// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.policy;

import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.HttpPipeline;
import com.typespec.core.http.HttpPipelineBuilder;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.test.http.MockHttpResponse;
import com.typespec.core.test.implementation.TestingHelpers;
import com.typespec.core.test.models.NetworkCallRecord;
import com.typespec.core.test.models.RecordedData;
import com.typespec.core.util.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Isolated
public class RecordNetworkCallPolicyTests {
    private static String azureTestRecordMode;

    @SuppressWarnings("deprecation")
    @BeforeAll
    public static void beforeAll() {
        azureTestRecordMode = Configuration.getGlobalConfiguration().get(TestingHelpers.AZURE_TEST_MODE);
        Configuration.getGlobalConfiguration().put(TestingHelpers.AZURE_TEST_MODE, "RECORD");
    }

    @SuppressWarnings("deprecation")
    @AfterAll
    public static void afterAll() {
        if (azureTestRecordMode == null) {
            Configuration.getGlobalConfiguration().remove(TestingHelpers.AZURE_TEST_MODE);
        } else {
            Configuration.getGlobalConfiguration().put(TestingHelpers.AZURE_TEST_MODE, azureTestRecordMode);
        }
    }

    @ParameterizedTest
    @MethodSource("sigValueIsRedactedSupplier")
    public void sigValueIsRedacted(String requestUrl, String expectedRedactedUrl) {
        RecordedData recordedData = new RecordedData();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RecordNetworkCallPolicy(recordedData))
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 200)))
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, requestUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        NetworkCallRecord record = assertDoesNotThrow(() -> recordedData.findFirstAndRemoveNetworkCall(call -> true));
        assertEquals(expectedRedactedUrl, record.getUri());
    }

    private static Stream<Arguments> sigValueIsRedactedSupplier() {
        return Stream.of(
            Arguments.of("https://azure.com", "https://REDACTED.com"), // No sig should result in no sig
            Arguments.of("https://azure.com?sig", "https://REDACTED.com?sig=REDACTED"), // Empty sig should result in redacted sig
            Arguments.of("https://azure.com?sig=", "https://REDACTED.com?sig=REDACTED"), // Empty sig should result in redacted sig
            Arguments.of("https://azure.com?sig=fake", "https://REDACTED.com?sig=REDACTED") // sig should result in redacted sig
        );
    }
}
