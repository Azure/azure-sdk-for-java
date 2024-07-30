// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.policy;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.TestMode;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.models.NetworkCallRecord;
import com.azure.core.test.models.RecordedData;
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
    private static final class AlwaysRecordRecordNetworkCallPolicy extends RecordNetworkCallPolicy {
        @Override
        TestMode getTestMode() {
            return TestMode.RECORD;
        }

        AlwaysRecordRecordNetworkCallPolicy(RecordedData recordedData) {
            super(recordedData);
        }
    }

    @ParameterizedTest
    @MethodSource("sigValueIsRedactedSupplier")
    public void sigValueIsRedacted(String requestUrl, String expectedRedactedUrl) {
        RecordedData recordedData = new RecordedData();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new AlwaysRecordRecordNetworkCallPolicy(recordedData))
                .httpClient(request -> Mono.just(new MockHttpResponse(request, 200)))
                .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, requestUrl)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        NetworkCallRecord record = assertDoesNotThrow(() -> recordedData.findFirstAndRemoveNetworkCall(call -> true));
        assertEquals(expectedRedactedUrl, record.getUri());
    }

    private static Stream<Arguments> sigValueIsRedactedSupplier() {
        return Stream.of(Arguments.of("https://azure.com", "https://REDACTED.com"), // No sig should result in no sig
            Arguments.of("https://azure.com?sig", "https://REDACTED.com?sig=REDACTED"), // Empty sig should result in
            // redacted sig
            Arguments.of("https://azure.com?sig=", "https://REDACTED.com?sig=REDACTED"), // Empty sig should result in
            // redacted sig
            Arguments.of("https://azure.com?sig=fake", "https://REDACTED.com?sig=REDACTED") // sig should result in
                                                                                           // redacted sig
        );
    }
}
