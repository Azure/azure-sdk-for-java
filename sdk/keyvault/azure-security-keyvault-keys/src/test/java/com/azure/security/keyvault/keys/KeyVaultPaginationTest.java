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
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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
import java.util.regex.Pattern;

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

    @Test
    public void playbackSanitizerOnlyRewritesRecordedOrigin() {
        for (boolean managedHsm : new boolean[] { false, true }) {
            TestProxySanitizer sanitizer
                = KeyClientTestBase.playbackNextLinkSanitizer("https://localhost:8080/base", managedHsm);
            Pattern pattern = Pattern.compile(sanitizer.getRegex());
            String host = managedHsm ? "externalkeysdkhsm.managedhsm.azure.net" : "externalkeysdk.vault.azure.net";
            String suffix = "keys/name%2Fpart?api-version=7.6&skiptoken=a%2Bb%3D";
            for (String origin : Arrays.asList("https://" + host + ":443/", "HTTPS://" + host + "/")) {
                assertEquals("https://localhost:8080/" + suffix,
                    pattern.matcher(origin + suffix).replaceFirst(sanitizer.getRedactedValue()));
            }
            for (String origin : Arrays.asList("https://other.example/", "http://" + host + "/",
                "https://" + host + ":8443/", "https://" + host + ".other.example/", "https://user@" + host + "/",
                "https://" + host + "@other.example/")) {
                assertEquals(origin + suffix,
                    pattern.matcher(origin + suffix).replaceFirst(sanitizer.getRedactedValue()));
            }
            assertEquals("$.nextLink", sanitizer.getKey());
        }
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

    @SyncAsyncTest
    public void validCloudAndCustomChallengesWorkWithVerificationEnabled() {
        // Service suffix examples from https://aka.ms/azsdk/blog/vault-uri, plus custom origins.
        String[][] endpoints = {
            { "https://paging.vault.azure.net", "https://vault.azure.net" },
            { "https://paging.vault.azure.cn", "https://vault.azure.cn" },
            { "https://paging.vault.usgovcloudapi.net", "https://vault.usgovcloudapi.net" },
            { "https://paging.region.managedhsm.azure.net", "https://managedhsm.azure.net" },
            { "https://paging.managedhsm.azure.cn", "https://managedhsm.azure.cn" },
            { "https://paging.managedhsm.usgovcloudapi.net", "https://managedhsm.usgovcloudapi.net" },
            { "https://keys.arbitrary.example:8443", "https://arbitrary.example" },
            { "HTTPS://KEYS.XN--BCHER-KVA.EXAMPLE:443", "https://xn--bcher-kva.example" } };
        for (boolean disableVerification : new boolean[] { false, true }) {
            for (String[] endpoint : endpoints) {
                KeyVaultCredentialPolicy.clearCache();
                KeyClientBuilder builder = builder(endpoint[0], endpoint[1], false, disableVerification);
                assertEquals(1, readPages(builder.buildClient().listPropertiesOfKeys(),
                    builder.buildAsyncClient().listPropertiesOfKeys(), endpoint[0] + "/keys?skip=1"));
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
        KeyClientBuilder original = builder(false, disableVerification);
        KeyClient client = original.buildClient();
        KeyAsyncClient asyncClient = original.buildAsyncClient();
        assertEquals(1, readPages(client.listPropertiesOfKeys(), asyncClient.listPropertiesOfKeys(), null));
        String otherOrigin = "https://other.vault.azure.net";
        KeyClientBuilder other = builder(otherOrigin, "https://vault.azure.net", false, disableVerification);
        assertEquals(1, readPages(other.buildClient().listPropertiesOfKeys(),
            other.buildAsyncClient().listPropertiesOfKeys(), null));
        int previousTokens = tokenRequests.get();
        int previousSends = sends.get();
        assertThrows(IllegalArgumentException.class, () -> readPages(client.listPropertiesOfKeys(),
            asyncClient.listPropertiesOfKeys(), otherOrigin + "/keys?skip=1"));
        assertEquals(previousTokens, tokenRequests.get());
        assertEquals(previousSends, sends.get());
    }

    @SyncAsyncTest
    public void verificationOptOutChangesOnlyChallengeResourceValidation() {
        String endpoint = "https://keys.example:8443";
        String resource = "https://vault.azure.net";
        KeyClientBuilder verified = builder(endpoint, resource, false, false);
        RuntimeException rejected
            = assertThrows(RuntimeException.class, () -> readPages(verified.buildClient().listPropertiesOfKeys(),
                verified.buildAsyncClient().listPropertiesOfKeys(), endpoint + "/keys"));
        assertTrue(rejected.getMessage().contains("challenge resource"));
        assertEquals(0, tokenRequests.get());
        assertEquals(1, sends.get());

        KeyClientBuilder optedOut = builder(endpoint, resource, false, true);
        KeyClient client = optedOut.buildClient();
        KeyAsyncClient asyncClient = optedOut.buildAsyncClient();
        assertEquals(1,
            readPages(client.listPropertiesOfKeys(), asyncClient.listPropertiesOfKeys(), endpoint + "/keys"));
        assertEquals(1, tokenRequests.get());
        int previousSends = sends.get();
        for (boolean disableVerification : new boolean[] { false, true }) {
            String insecureEndpoint = "http://keys.example:8443";
            KeyClientBuilder insecure = builder(insecureEndpoint, resource, false, disableVerification);
            RuntimeException insecureRejected
                = assertThrows(RuntimeException.class, () -> readPages(insecure.buildClient().listPropertiesOfKeys(),
                    insecure.buildAsyncClient().listPropertiesOfKeys(), insecureEndpoint + "/keys"));
            assertTrue(insecureRejected.getMessage().contains("HTTPS"));
            assertEquals(1, tokenRequests.get());
            assertEquals(previousSends, sends.get());
        }
        assertThrows(IllegalArgumentException.class, () -> readPages(client.listPropertiesOfKeys(),
            asyncClient.listPropertiesOfKeys(), "https://other.example:8443/keys"));
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

    private KeyClientBuilder builder(boolean customPipeline, boolean disableVerification) {
        return builder(ENDPOINT, "https://vault.azure.net", customPipeline, disableVerification);
    }

    private KeyClientBuilder builder(String endpoint, String resource, boolean customPipeline,
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
        KeyClientBuilder builder = new KeyClientBuilder().vaultUrl(endpoint)
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
            }, new BearerTokenAuthenticationPolicy(credential, "https://vault.azure.net/.default")).build());
        }
        return builder;
    }
}
