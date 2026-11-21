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

    private static int readPages(PagedIterable<?> sync, PagedFlux<?> async, String nextLink) {
        AtomicInteger pages = new AtomicInteger();
        SyncAsyncExtension.execute(() -> sync.iterableByPage(nextLink).forEach(page -> pages.incrementAndGet()),
            () -> (nextLink == null ? async.byPage() : async.byPage(nextLink)).doOnNext(page -> pages.incrementAndGet())
                .then());
        return pages.get();
    }

    private KeyVaultAccessControlClientBuilder builder(boolean customPipeline, boolean disableVerification) {
        TokenCredential credential = context -> {
            tokenRequests.incrementAndGet();
            return Mono.just(new AccessToken("fake-key-vault-token", OffsetDateTime.now().plusHours(1)));
        };
        HttpClient httpClient = request -> Mono.fromSupplier(() -> {
            sends.incrementAndGet();
            String authorization = request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            if (authorization == null) {
                return new MockHttpResponse(request, 401, new HttpHeaders().set(HttpHeaderName.WWW_AUTHENTICATE,
                    "Bearer authorization=\"https://login.example/tenant\", resource=\"https://managedhsm.azure.net\""));
            }
            assertEquals("Bearer fake-key-vault-token", authorization);
            authorizedSends.incrementAndGet();
            String nextLink = responseNextLink.getAndSet(null);
            String body = nextLink == null ? "{\"value\":[]}" : "{\"value\":[],\"nextLink\":\"" + nextLink + "\"}";
            return new MockHttpResponse(request, 200,
                new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                body.getBytes(StandardCharsets.UTF_8));
        });
        KeyVaultAccessControlClientBuilder builder
            = new KeyVaultAccessControlClientBuilder().vaultUrl(ENDPOINT).credential(credential).httpClient(httpClient);
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
