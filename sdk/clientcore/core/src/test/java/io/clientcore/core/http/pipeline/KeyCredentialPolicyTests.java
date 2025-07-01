// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.credentials.KeyCredential;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static io.clientcore.core.http.pipeline.PipelineTestHelpers.sendRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@ParameterizedClass(name = "isAsync={0}")
@ValueSource(booleans = { false, true })
public class KeyCredentialPolicyTests {
    private final boolean isAsync;

    public KeyCredentialPolicyTests(boolean isAsync) {
        this.isAsync = isAsync;
    }

    @ParameterizedTest
    @MethodSource("setCredentialSupplier")
    public void setCredential(KeyCredentialPolicy policy, String expectedHeader) {
        HttpHeaders headers = new HttpHeaders();

        policy.setCredential(headers);

        assertEquals(expectedHeader, headers.getValue(HttpHeaderName.AUTHORIZATION));
    }

    @SuppressWarnings("try")
    @ParameterizedTest
    @MethodSource("validateSchemesSupplier")
    public void validateSchemes(String url, boolean shouldPass) {
        KeyCredential credential = new KeyCredential("fakeKeyPlaceholder");
        KeyCredentialPolicy policy = new KeyCredentialPolicy(HttpHeaderName.AUTHORIZATION.toString(), credential, null);
        HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(policy)
            .addPolicy((request, ignored) -> new Response<>(request, 200, null, BinaryData.empty()))
            .httpClient(request -> new Response<>(request, 200, null, BinaryData.empty()))
            .build();
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);

        try (Response<BinaryData> ignored = sendRequest(pipeline, request, isAsync)) {
            // ignored
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
