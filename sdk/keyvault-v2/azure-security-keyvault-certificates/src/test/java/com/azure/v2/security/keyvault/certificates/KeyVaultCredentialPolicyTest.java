// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.certificates;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.security.keyvault.certificates.implementation.KeyVaultCredentialPolicy;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class KeyVaultCredentialPolicyTest {
    private static final String VAULT_URI = "https://kvtest.vault.azure.net";
    private static final String ENTRA_TENANT_ID = "72f988bf-86f1-41af-91ab-2d7cd022db57";
    private static final String DSTS_TENANT_ID = "de763a21-49f7-4b08-a8e1-52c8fbc103b4";
    private static final String AUTHENTICATE_HEADER_ENTRA = "Bearer authorization=\"https://login.windows.net/"
        + ENTRA_TENANT_ID + "\", " + "resource=\"https://vault.azure.net\"";
    private static final String AUTHENTICATE_HEADER_DSTS_V2
        = "Bearer authorization=\"https://uswest2-passive-dsts.dsts.core.windows.net/dstsv2/" + DSTS_TENANT_ID
            + "\", resource=\"https://vault.azure.net\"";
    private static final String BEARER = "Bearer";

    @AfterEach
    public void cleanup() {
        KeyVaultCredentialPolicy.clearCache();
    }

    @Test
    public void onChallengeExtractsTenantIdFromEntraAuthorizationUri() {
        AtomicReference<String> requestedTenantId = new AtomicReference<>();
        List<String> sentAuthorizationHeaders = new ArrayList<>();
        HttpPipeline pipeline = createPipeline(AUTHENTICATE_HEADER_ENTRA, requestedTenantId, sentAuthorizationHeaders);

        try (Response<BinaryData> response = pipeline.send(createRequest())) {
            assertEquals(200, response.getStatusCode());
        }

        assertEquals(ENTRA_TENANT_ID, requestedTenantId.get());
        assertAuthorizedOnRetry(sentAuthorizationHeaders);
    }

    @Test
    public void onChallengeExtractsTenantIdFromDstsV2AuthorizationUri() {
        AtomicReference<String> requestedTenantId = new AtomicReference<>();
        List<String> sentAuthorizationHeaders = new ArrayList<>();
        HttpPipeline pipeline
            = createPipeline(AUTHENTICATE_HEADER_DSTS_V2, requestedTenantId, sentAuthorizationHeaders);

        try (Response<BinaryData> response = pipeline.send(createRequest())) {
            assertEquals(200, response.getStatusCode());
        }

        // The tenant ID follows the 'dstsv2' path segment rather than being the first path segment.
        assertEquals(DSTS_TENANT_ID, requestedTenantId.get());
        assertAuthorizedOnRetry(sentAuthorizationHeaders);
    }

    @Test
    public void onChallengeCachedForSubsequentRequests() {
        AtomicReference<String> requestedTenantId = new AtomicReference<>();
        List<String> sentAuthorizationHeaders = new ArrayList<>();
        HttpPipeline pipeline
            = createPipeline(AUTHENTICATE_HEADER_DSTS_V2, requestedTenantId, sentAuthorizationHeaders);

        try (Response<BinaryData> response = pipeline.send(createRequest())) {
            assertEquals(200, response.getStatusCode());
        }

        try (Response<BinaryData> response = pipeline.send(createRequest())) {
            assertEquals(200, response.getStatusCode());
        }

        // 401 -> 200 for the first request, then the cached challenge authorizes the second request up front.
        assertEquals(3, sentAuthorizationHeaders.size());
        assertNull(sentAuthorizationHeaders.get(0));
        assertTrue(sentAuthorizationHeaders.get(1).startsWith(BEARER));
        assertTrue(sentAuthorizationHeaders.get(2).startsWith(BEARER));
        assertEquals(DSTS_TENANT_ID, requestedTenantId.get());
    }

    @ParameterizedTest
    @MethodSource("authorizationUriTenantIds")
    public void onChallengeExtractsTenantIdFromAuthorizationUriVariants(String authorizationUri,
        String expectedTenantId) {
        AtomicReference<String> requestedTenantId = new AtomicReference<>();
        List<String> sentAuthorizationHeaders = new ArrayList<>();
        HttpPipeline pipeline
            = createPipeline("Bearer authorization=\"" + authorizationUri + "\", resource=\"https://vault.azure.net\"",
                requestedTenantId, sentAuthorizationHeaders);

        try (Response<BinaryData> response = pipeline.send(createRequest())) {
            assertEquals(200, response.getStatusCode());
        }

        assertEquals(expectedTenantId, requestedTenantId.get());
        assertAuthorizedOnRetry(sentAuthorizationHeaders);
    }

    private static HttpRequest createRequest() {
        return new HttpRequest().setMethod(HttpMethod.GET).setUri(VAULT_URI);
    }

    private static Stream<Arguments> authorizationUriTenantIds() {
        String dstsAuthority = "https://uswest2-passive-dsts.dsts.core.windows.net";
        String entraAuthority = "https://login.microsoftonline.com/" + ENTRA_TENANT_ID;

        return Stream.of(
            // The 'dstsv2' segment is matched case-insensitively.
            Arguments.of(dstsAuthority + "/DSTSv2/" + DSTS_TENANT_ID, DSTS_TENANT_ID),
            // A trailing slash after the tenant ID does not change it.
            Arguments.of(dstsAuthority + "/dstsv2/" + DSTS_TENANT_ID + "/", DSTS_TENANT_ID),
            // Neither do further path segments after the tenant ID.
            Arguments.of(dstsAuthority + "/dstsv2/" + DSTS_TENANT_ID + "/oauth2/token", DSTS_TENANT_ID),
            // Without a segment after 'dstsv2', the first path segment is used, as before.
            Arguments.of(dstsAuthority + "/dstsv2", "dstsv2"),
            // The same applies when only a slash follows 'dstsv2'.
            Arguments.of(dstsAuthority + "/dstsv2/", "dstsv2"),
            // An empty segment after 'dstsv2' is not used as the tenant ID either.
            Arguments.of(dstsAuthority + "/dstsv2//" + DSTS_TENANT_ID, "dstsv2"),
            // Only a first path segment that is exactly 'dstsv2' denotes a DSTSv2 authority.
            Arguments.of(dstsAuthority + "/dstsv2x/" + DSTS_TENANT_ID, "dstsv2x"),
            // A 'dstsv2' segment later in an Entra ID authorization URI is ignored.
            Arguments.of(entraAuthority + "/dstsv2/" + DSTS_TENANT_ID, ENTRA_TENANT_ID));
    }

    private static void assertAuthorizedOnRetry(List<String> sentAuthorizationHeaders) {
        // The first request is unauthenticated, the retry after the challenge carries the token.
        assertEquals(2, sentAuthorizationHeaders.size());
        assertNull(sentAuthorizationHeaders.get(0));
        assertTrue(sentAuthorizationHeaders.get(1).startsWith(BEARER));
    }

    /**
     * Creates a pipeline whose HTTP client answers the first request with a 401 challenge and every later request
     * with a 200, while recording the tenant ID requested from the credential and the Authorization header sent.
     */
    private static HttpPipeline createPipeline(String authenticateHeader, AtomicReference<String> requestedTenantId,
        List<String> sentAuthorizationHeaders) {

        TokenCredential credential = tokenRequestContext -> {
            requestedTenantId.set(tokenRequestContext.getTenantId());

            return new AccessToken("token", OffsetDateTime.now().plusHours(1));
        };

        HttpClient httpClient = request -> {
            sentAuthorizationHeaders.add(request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));

            if (sentAuthorizationHeaders.size() == 1) {
                return new Response<>(request, 401,
                    new HttpHeaders().set(HttpHeaderName.WWW_AUTHENTICATE, authenticateHeader), null);
            }

            return new Response<>(request, 200, new HttpHeaders(), null);
        };

        return new HttpPipelineBuilder().addPolicy(new KeyVaultCredentialPolicy(credential, false))
            .httpClient(httpClient)
            .build();
    }
}
