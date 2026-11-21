// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

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
import com.azure.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
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
    private static final String ENDPOINT = "https://paging.vault.azure.net";
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
                KeyClientBuilder builder = builder(customPipeline, disableVerification);
                KeyClient client = builder.buildClient();
                KeyAsyncClient asyncClient = builder.buildAsyncClient();
                List<PagedIterable<?>> syncPages = Arrays.asList(client.listPropertiesOfKeys(),
                    client.listPropertiesOfKeyVersions("key"), client.listDeletedKeys());
                List<PagedFlux<?>> asyncPages = Arrays.asList(asyncClient.listPropertiesOfKeys(),
                    asyncClient.listPropertiesOfKeyVersions("key"), asyncClient.listDeletedKeys());

                for (int i = 0; i < syncPages.size(); i++) {
                    PagedIterable<?> sync = syncPages.get(i);
                    PagedFlux<?> async = asyncPages.get(i);
                    assertRejectedContinuations(sync, async);
                    assertEquals(1, readPages(sync, async, null));
                    assertRejectedContinuations(sync, async);
                    assertEquals(1, readPages(sync, async, "HTTPS://PAGING.VAULT.AZURE.NET:443/keys?skip=1"));
                }

                assertRejectedContinuations(builder.buildClient().listPropertiesOfKeys(),
                    builder.buildAsyncClient().listPropertiesOfKeys());
            }
        }
        assertTrue(tokenRequests.get() > 0);
        assertTrue(authorizedSends.get() > 0);
    }

    @SyncAsyncTest
    public void serviceProvidedContinuationIsValidated() {
        KeyClientBuilder builder = builder(false, false);
        KeyClient client = builder.buildClient();
        KeyAsyncClient asyncClient = builder.buildAsyncClient();
        responseNextLink.set(ENDPOINT + "/keys?skip=1");
        assertEquals(2, readPages(client.listPropertiesOfKeys(), asyncClient.listPropertiesOfKeys(), null));

        int previousTokens = tokenRequests.get();
        int previousSends = sends.get();
        responseNextLink.set("https://other.example/keys");
        assertThrows(IllegalArgumentException.class,
            () -> readPages(client.listPropertiesOfKeys(), asyncClient.listPropertiesOfKeys(), null));
        assertEquals(previousTokens, tokenRequests.get());
        assertEquals(previousSends + 1, sends.get());
    }

    private void assertRejectedContinuations(PagedIterable<?> sync, PagedFlux<?> async) {
        for (String nextLink : Arrays.asList("https://other.example/keys", "https://other.vault.azure.net/keys",
            "http://paging.vault.azure.net/keys", ENDPOINT + ":8443/keys", "https://user@paging.vault.azure.net/keys",
            ENDPOINT + ":invalid/keys", "/keys")) {
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

    private KeyClientBuilder builder(boolean customPipeline, boolean disableVerification) {
        TokenCredential credential = context -> {
            tokenRequests.incrementAndGet();
            return Mono.just(new AccessToken("fake-key-vault-token", OffsetDateTime.now().plusHours(1)));
        };
        HttpClient httpClient = request -> Mono.fromSupplier(() -> {
            sends.incrementAndGet();
            String authorization = request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            if (authorization == null) {
                return new MockHttpResponse(request, 401, new HttpHeaders().set(HttpHeaderName.WWW_AUTHENTICATE,
                    "Bearer authorization=\"https://login.example/tenant\", resource=\"https://vault.azure.net\""));
            }
            assertEquals("Bearer fake-key-vault-token", authorization);
            authorizedSends.incrementAndGet();
            String nextLink = responseNextLink.getAndSet(null);
            String body = nextLink == null ? "{\"value\":[]}" : "{\"value\":[],\"nextLink\":\"" + nextLink + "\"}";
            return new MockHttpResponse(request, 200,
                new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                body.getBytes(StandardCharsets.UTF_8));
        });
        KeyClientBuilder builder
            = new KeyClientBuilder().vaultUrl(ENDPOINT).credential(credential).httpClient(httpClient);
        if (disableVerification) {
            builder.disableChallengeResourceVerification();
        }
        if (customPipeline) {
            builder.pipeline(new HttpPipelineBuilder().httpClient(httpClient).policies((context, next) -> {
                customPolicyCalls.incrementAndGet();
                return next.process();
            }, new BearerTokenAuthenticationPolicy(credential, "https://vault.azure.net/.default")).build());
        }
        return builder;
    }
}
