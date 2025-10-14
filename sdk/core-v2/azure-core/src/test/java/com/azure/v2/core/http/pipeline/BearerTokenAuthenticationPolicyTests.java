// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.http.pipeline;

import com.azure.v2.core.credentials.TokenCredential;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BearerTokenAuthenticationPolicyTests {

    @ParameterizedTest
    @MethodSource("caeTestArguments")
    public void testDefaultCae(String challenge, int expectedStatusCode, String expectedClaims, String encodedClaims) {
        AtomicReference<String> claims = new AtomicReference<>();
        AtomicInteger callCount = new AtomicInteger();

        TokenCredential credential = getCaeTokenCredential(claims, callCount);
        BearerTokenAuthenticationPolicy policy = new BearerTokenAuthenticationPolicy(credential, "scope");
        HttpClient client = getCaeHttpClient(challenge, callCount);
        HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(policy).httpClient(client).build();

        try (Response<BinaryData> response
            = pipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri("https://localhost"))) {
            assertEquals(expectedStatusCode, response.getStatusCode());
        }
        assertEquals(expectedClaims, claims.get());

        if (expectedClaims != null) {
            String actualEncodedClaims = BearerTokenAuthenticationPolicy.getChallengeParameterFromResponse(
                new Response<>(null, 401, new HttpHeaders().add(HttpHeaderName.WWW_AUTHENTICATE, challenge), null),
                "Bearer", "claims");
            assertEquals(encodedClaims, actualEncodedClaims);
        }
    }

    // A fake token credential that lets us keep track of what got parsed out of a CAE claim for assertion.
    private static TokenCredential getCaeTokenCredential(AtomicReference<String> claims, AtomicInteger callCount) {
        return request -> {
            claims.set(request.getClaims());
            assertTrue(request.isCaeEnabled());
            callCount.incrementAndGet();
            return new AccessToken("token", OffsetDateTime.now().plusHours(2));
        };
    }

    // This http client is effectively a state sentinel for how we progressed through the challenge.
    // If we had a challenge, and it is invalid, the policy stops and returns 401 all the way out.
    // If the CAE challenge parses properly we will end complete the policy normally and get 200.
    private static HttpClient getCaeHttpClient(String challenge, AtomicInteger callCount) {
        return request -> {
            if (callCount.get() <= 1) {
                if (challenge == null) {
                    return new Response<>(request, 200, new HttpHeaders(), null);
                }
                return new Response<>(request, 401, new HttpHeaders().add(HttpHeaderName.WWW_AUTHENTICATE, challenge),
                    null);
            }
            return new Response<>(request, 200, new HttpHeaders(), null);
        };
    }

    private static Stream<Arguments> caeTestArguments() {
        return Stream.of(Arguments.of(null, 200, null, null), // no challenge
            Arguments.of(
                "Bearer authorization_uri=\"https://login.windows.net/\", error=\"invalid_token\", claims=\"ey==\"",
                401, null, "ey=="), // unexpected error value
            Arguments.of("Bearer claims=\"not base64\", error=\"insufficient_claims\"", 401, null, "not base64"), // parsing error
            Arguments.of(
                "Bearer realm=\"\", authorization_uri=\"http://localhost\", client_id=\"00000003-0000-0000-c000-000000000000\", error=\"insufficient_claims\", claims=\"ey==\"",
                200, "{", "ey=="), // more parameters in a different order
            Arguments.of(
                "Bearer realm=\"\", authorization_uri=\"https://login.microsoftonline.com/common/oauth2/authorize\", error=\"insufficient_claims\", claims=\"eyJhY2Nlc3NfdG9rZW4iOnsibmJmIjp7ImVzc2VudGlhbCI6dHJ1ZSwidmFsdWUiOiIxNzI2MDc3NTk1In0sInhtc19jYWVlcnJvciI6eyJ2YWx1ZSI6IjEwMDEyIn19fQ==\"",
                200,
                "{\"access_token\":{\"nbf\":{\"essential\":true,\"value\":\"1726077595\"},\"xms_caeerror\":{\"value\":\"10012\"}}}",
                "eyJhY2Nlc3NfdG9rZW4iOnsibmJmIjp7ImVzc2VudGlhbCI6dHJ1ZSwidmFsdWUiOiIxNzI2MDc3NTk1In0sInhtc19jYWVlcnJvciI6eyJ2YWx1ZSI6IjEwMDEyIn19fQ=="), // standard request
            Arguments.of(
                "PoP realm=\"\", authorization_uri=\"https://login.microsoftonline.com/common/oauth2/authorize\", client_id=\"00000003-0000-0000-c000-000000000000\", nonce=\"ey==\", Bearer realm=\"\", authorization_uri=\"https://login.microsoftonline.com/common/oauth2/authorize\", client_id=\"00000003-0000-0000-c000-000000000000\", error_description=\"Continuous access evaluation resulted in challenge with result: InteractionRequired and code: TokenIssuedBeforeRevocationTimestamp\", error=\"insufficient_claims\", claims=\"eyJhY2Nlc3NfdG9rZW4iOnsibmJmIjp7ImVzc2VudGlhbCI6dHJ1ZSwgInZhbHVlIjoiMTcyNjI1ODEyMiJ9fX0=\"",
                200, "{\"access_token\":{\"nbf\":{\"essential\":true, \"value\":\"1726258122\"}}}",
                "eyJhY2Nlc3NfdG9rZW4iOnsibmJmIjp7ImVzc2VudGlhbCI6dHJ1ZSwgInZhbHVlIjoiMTcyNjI1ODEyMiJ9fX0="), // multiple challenges
            Arguments.of("Bearer claims=\"\", error=\"insufficient_claims\"", 401, null, ""), // empty claims
            Arguments.of("Bearer error=\"insufficient_claims\"", 401, null, "") // missing claims
        );
    }
}
