// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.security.keyvault.administration.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class KeyVaultPaginationTest {
    private static final String ENDPOINT = "https://paging.managedhsm.azure.net";
    private final AtomicInteger tokenRequests = new AtomicInteger();
    private final AtomicInteger sends = new AtomicInteger();
    private final AtomicInteger authorizedSends = new AtomicInteger();
    private final AtomicInteger customPolicyCalls = new AtomicInteger();
    private final AtomicReference<String> responseNextLink = new AtomicReference<>();

    @AfterEach
    public void cleanup() {
        KeyVaultCredentialPolicy.clearCache();
    }

    @SyncAsyncTest
    public void allContinuationOperationsValidateBeforePipeline() {
        for (boolean customPipeline : new boolean[] { false, true }) {
            for (boolean disableVerification : new boolean[] { false, true }) {
                KeyVaultCredentialPolicy.clearCache();
                KeyVaultAccessControlClientBuilder builder = builder(customPipeline, disableVerification);
                KeyVaultAccessControlClient client = builder.buildClient();
                KeyVaultAccessControlAsyncClient asyncClient = builder.buildAsyncClient();
                List<PagedIterable<?>> syncPages = Arrays.asList(client.listRoleDefinitions(KeyVaultRoleScope.GLOBAL),
                    client.listRoleAssignments(KeyVaultRoleScope.GLOBAL));
                List<PagedFlux<?>> asyncPages = Arrays.asList(asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL),
                    asyncClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL));

                for (int i = 0; i < syncPages.size(); i++) {
                    PagedIterable<?> sync = syncPages.get(i);
                    PagedFlux<?> async = asyncPages.get(i);
                    assertRejectedContinuations(sync, async);
                    assertEquals(1, readPages(sync, async, null));
                    assertRejectedContinuations(sync, async);
                    assertEquals(1, readPages(sync, async, "HTTPS://PAGING.MANAGEDHSM.AZURE.NET:443/roles?skip=1"));
                }

                assertRejectedContinuations(builder.buildClient().listRoleDefinitions(KeyVaultRoleScope.GLOBAL),
                    builder.buildAsyncClient().listRoleDefinitions(KeyVaultRoleScope.GLOBAL));
            }
        }
        assertTrue(tokenRequests.get() > 0);
        assertTrue(authorizedSends.get() > 0);
    }

    @SyncAsyncTest
    public void serviceProvidedContinuationIsValidated() {
        KeyVaultAccessControlClientBuilder builder = builder(false, false);
        KeyVaultAccessControlClient client = builder.buildClient();
        KeyVaultAccessControlAsyncClient asyncClient = builder.buildAsyncClient();
        responseNextLink.set(ENDPOINT + "/roles?skip=1");
        assertEquals(2, readPages(client.listRoleDefinitions(KeyVaultRoleScope.GLOBAL),
            asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL), null));

        int previousTokens = tokenRequests.get();
        int previousSends = sends.get();
        responseNextLink.set("https://other.example/roles");
        assertThrows(IllegalArgumentException.class,
            () -> readPages(client.listRoleDefinitions(KeyVaultRoleScope.GLOBAL),
                asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL), null));
        assertEquals(previousTokens, tokenRequests.get());
        assertEquals(previousSends + 1, sends.get());
    }

    private void assertRejectedContinuations(PagedIterable<?> sync, PagedFlux<?> async) {
        for (String nextLink : Arrays.asList("https://other.example/roles", "https://other.managedhsm.azure.net/roles",
            "http://paging.managedhsm.azure.net/roles", ENDPOINT + ":8443/roles",
            "https://user@paging.managedhsm.azure.net/roles", ENDPOINT + ":invalid/roles", "/roles")) {
            int previousTokens = tokenRequests.get();
            int previousSends = sends.get();
            int previousAuthorizedSends = authorizedSends.get();
            int previousPolicyCalls = customPolicyCalls.get();
            assertThrows(IllegalArgumentException.class, () -> readPages(sync, async, nextLink), nextLink);
            assertEquals(previousTokens, tokenRequests.get());
            assertEquals(previousSends, sends.get());
            assertEquals(previousAuthorizedSends, authorizedSends.get());
            assertEquals(previousPolicyCalls, customPolicyCalls.get());
        }
    }

    @SyncAsyncTest
    public void validCloudAndCustomChallengesWorkWithVerificationEnabled() {
        // DNS routing does not change the logical URL origin.
        String[][] endpoints = {
            { "https://paging.managedhsm.azure.net", "https://managedhsm.azure.net" },
            { "https://paging.region.managedhsm.azure.net", "https://managedhsm.azure.net" },
            { "https://paging.managedhsm.azure.cn", "https://managedhsm.azure.cn" },
            { "https://paging.managedhsm.usgovcloudapi.net", "https://managedhsm.usgovcloudapi.net" },
            { "https://hsm.arbitrary.example:8443", "https://arbitrary.example" },
            { "HTTPS://HSM.XN--BCHER-KVA.EXAMPLE:443", "https://xn--bcher-kva.example" } };
        for (boolean disableVerification : new boolean[] { false, true }) {
            for (String[] endpoint : endpoints) {
                KeyVaultCredentialPolicy.clearCache();
                KeyVaultAccessControlClientBuilder builder
                    = builder(endpoint[0], endpoint[1], false, disableVerification);
                assertEquals(1,
                    readPages(builder.buildClient().listRoleDefinitions(KeyVaultRoleScope.GLOBAL),
                        builder.buildAsyncClient().listRoleDefinitions(KeyVaultRoleScope.GLOBAL),
                        endpoint[0] + "/roles?skip=1"));
            }
        }
    }

    @SyncAsyncTest
    public void defaultResourceVerificationDoesNotAuthorizeForeignContinuation() {
        assertValidChallengeCannotAuthorizeForeignContinuation(false);
    }

    @SyncAsyncTest
    public void verificationOptOutDoesNotAuthorizeForeignContinuation() {
        assertValidChallengeCannotAuthorizeForeignContinuation(true);
    }

    private void assertValidChallengeCannotAuthorizeForeignContinuation(boolean disableVerification) {
        KeyVaultAccessControlClientBuilder original = builder(false, disableVerification);
        KeyVaultAccessControlClient client = original.buildClient();
        KeyVaultAccessControlAsyncClient asyncClient = original.buildAsyncClient();
        assertEquals(1, readPages(client.listRoleDefinitions(KeyVaultRoleScope.GLOBAL),
            asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL), null));
        String otherOrigin = "https://other.managedhsm.azure.net";
        KeyVaultAccessControlClientBuilder other
            = builder(otherOrigin, "https://managedhsm.azure.net", false, disableVerification);
        assertEquals(1, readPages(other.buildClient().listRoleDefinitions(KeyVaultRoleScope.GLOBAL),
            other.buildAsyncClient().listRoleDefinitions(KeyVaultRoleScope.GLOBAL), null));
        int previousTokens = tokenRequests.get();
        int previousSends = sends.get();
        assertThrows(IllegalArgumentException.class,
            () -> readPages(client.listRoleDefinitions(KeyVaultRoleScope.GLOBAL),
                asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL), otherOrigin + "/roles?skip=1"));
        assertEquals(previousTokens, tokenRequests.get());
        assertEquals(previousSends, sends.get());
    }

    @SyncAsyncTest
    public void verificationOptOutChangesOnlyChallengeResourceValidation() {
        String endpoint = "https://hsm.example:8443";
        String resource = "https://managedhsm.azure.net";
        KeyVaultAccessControlClientBuilder verified = builder(endpoint, resource, false, false);
        RuntimeException rejected = assertThrows(RuntimeException.class,
            () -> readPages(verified.buildClient().listRoleDefinitions(KeyVaultRoleScope.GLOBAL),
                verified.buildAsyncClient().listRoleDefinitions(KeyVaultRoleScope.GLOBAL), endpoint + "/roles"));
        assertTrue(rejected.getMessage().contains("challenge resource"));
        assertEquals(0, tokenRequests.get());
        assertEquals(1, sends.get());

        KeyVaultAccessControlClientBuilder optedOut = builder(endpoint, resource, false, true);
        KeyVaultAccessControlClient client = optedOut.buildClient();
        KeyVaultAccessControlAsyncClient asyncClient = optedOut.buildAsyncClient();
        assertEquals(1, readPages(client.listRoleDefinitions(KeyVaultRoleScope.GLOBAL),
            asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL), endpoint + "/roles"));
        assertEquals(1, tokenRequests.get());
        int previousSends = sends.get();
        for (boolean disableVerification : new boolean[] { false, true }) {
            String insecureEndpoint = "http://hsm.example:8443";
            KeyVaultAccessControlClientBuilder insecure
                = builder(insecureEndpoint, resource, false, disableVerification);
            RuntimeException insecureRejected = assertThrows(RuntimeException.class,
                () -> readPages(insecure.buildClient().listRoleDefinitions(KeyVaultRoleScope.GLOBAL),
                    insecure.buildAsyncClient().listRoleDefinitions(KeyVaultRoleScope.GLOBAL),
                    insecureEndpoint + "/roles"));
            assertTrue(insecureRejected.getMessage().contains("HTTPS"));
            assertEquals(1, tokenRequests.get());
            assertEquals(previousSends, sends.get());
        }
        assertThrows(IllegalArgumentException.class,
            () -> readPages(client.listRoleDefinitions(KeyVaultRoleScope.GLOBAL),
                asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL), "https://other.example:8443/roles"));
        assertEquals(1, tokenRequests.get());
        assertEquals(previousSends, sends.get());
    }

    private static int readPages(PagedIterable<?> sync, PagedFlux<?> async, String nextLink) {
        AtomicInteger pages = new AtomicInteger();
        SyncAsyncExtension.execute(() -> sync.iterableByPage(nextLink).forEach(page -> pages.incrementAndGet()),
            () -> (nextLink == null ? async.byPage() : async.byPage(nextLink)).doOnNext(page -> pages.incrementAndGet())
                .then());
        return pages.get();
    }

    private KeyVaultAccessControlClientBuilder builder(boolean customPipeline, boolean disableVerification) {
        return builder(ENDPOINT, "https://managedhsm.azure.net", customPipeline, disableVerification);
    }

    private KeyVaultAccessControlClientBuilder builder(String endpoint, String resource, boolean customPipeline,
        boolean disableVerification) {
        TokenCredential credential = context -> {
            tokenRequests.incrementAndGet();
            return Mono.just(new AccessToken("fake-key-vault-token", OffsetDateTime.now().plusHours(1)));
        };
        HttpClient httpClient = request -> Mono.fromSupplier(() -> {
            sends.incrementAndGet();
            String authorization = request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            if (authorization == null) {
                return new MockHttpResponse(request, 401, new HttpHeaders().set(HttpHeaderName.WWW_AUTHENTICATE,
                    "Bearer authorization=\"https://login.example/tenant\", resource=\"" + resource + "\""));
            }
            assertEquals("Bearer fake-key-vault-token", authorization);
            authorizedSends.incrementAndGet();
            String nextLink = responseNextLink.getAndSet(null);
            String body = nextLink == null ? "{\"value\":[]}" : "{\"value\":[],\"nextLink\":\"" + nextLink + "\"}";
            return new MockHttpResponse(request, 200,
                new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                body.getBytes(StandardCharsets.UTF_8));
        });
        KeyVaultAccessControlClientBuilder builder = new KeyVaultAccessControlClientBuilder().vaultUrl(endpoint)
            .credential(credential)
            .httpClient(httpClient)
            .retryOptions(new RetryOptions(new FixedDelayOptions(0, Duration.ofMillis(1))));
        if (disableVerification) {
            builder.disableChallengeResourceVerification();
        }
        if (customPipeline) {
            builder.pipeline(new HttpPipelineBuilder().httpClient(httpClient).policies((context, next) -> {
                customPolicyCalls.incrementAndGet();
                return next.process();
            }, new BearerTokenAuthenticationPolicy(credential, "https://managedhsm.azure.net/.default")).build());
        }
        return builder;
    }
}
