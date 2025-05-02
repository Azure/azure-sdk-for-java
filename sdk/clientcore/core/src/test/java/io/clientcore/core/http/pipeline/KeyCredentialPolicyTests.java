// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.credentials.KeyCredential;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class KeyCredentialPolicyTests {
    @ParameterizedTest
    @MethodSource("setCredentialSupplier")
    public void setCredential(KeyCredentialPolicy policy, String expectedHeader) {
        HttpHeaders headers = new HttpHeaders();

        policy.setCredential(headers);

        assertEquals(expectedHeader, headers.getValue(HttpHeaderName.AUTHORIZATION));
    }

    @ParameterizedTest
    @MethodSource("validateSchemesSupplier")
    public void validateSchemes(String url, boolean shouldPass) {
        KeyCredential credential = new KeyCredential("fakeKeyPlaceholder");
        KeyCredentialPolicy policy = new KeyCredentialPolicy(HttpHeaderName.AUTHORIZATION.toString(), credential, null);
        HttpPipelinePolicy mockReturnPolicy = (HttpRequest request,
            HttpPipelineNextPolicy next) -> new Response<>(request, 200, null, BinaryData.empty());
        HttpClient client = (request) -> {
            return new Response<>(request, 200, null, BinaryData.empty());
        };
        HttpPipeline pipeline
            = new HttpPipelineBuilder().addPolicy(policy).addPolicy(mockReturnPolicy).httpClient(client).build();
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        try {
            pipeline.send(request);
        } catch (IllegalStateException e) {
            if (shouldPass) {
                fail("Expected request to pass but it failed with: " + e.getMessage());
            }
        }
    }

    private static Stream<Arguments> setCredentialSupplier() {
        String fakeKey = "fakeKeyPlaceholder";
        KeyCredential credential = new KeyCredential(fakeKey);

        return Stream.of(
            Arguments.of(new KeyCredentialPolicy(HttpHeaderName.AUTHORIZATION.toString(), credential, null), fakeKey),
            Arguments.of(new KeyCredentialPolicy(HttpHeaderName.AUTHORIZATION.toString(), credential, "Bearer"),
                "Bearer " + fakeKey),
            Arguments.of(new KeyCredentialPolicy(HttpHeaderName.AUTHORIZATION.toString(), credential, "Bearer "),
                "Bearer " + fakeKey));
    }

    private static Stream<Arguments> validateSchemesSupplier() {
        return Stream.of(Arguments.of("http://localhost", false), Arguments.of("https://localhost", true));
    }
}
