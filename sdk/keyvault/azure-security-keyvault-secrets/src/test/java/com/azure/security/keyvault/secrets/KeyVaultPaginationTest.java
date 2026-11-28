// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

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
import com.azure.security.keyvault.secrets.implementation.KeyVaultCredentialPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
    private final AtomicReference<String> challengeResource = new AtomicReference<>("https://vault.azure.net");
    private final AtomicReference<URL> lastRequestUrl = new AtomicReference<>();

    @AfterEach
    public void cleanup() {
        KeyVaultCredentialPolicy.clearCache();
    }

    @Test
    public void playbackSanitizerOnlyRewritesRecordedOrigin() {
        TestProxySanitizer sanitizer = SecretClientTestBase.playbackNextLinkSanitizer("https://localhost:8080/base");
        Pattern pattern = Pattern.compile(sanitizer.getRegex());
        String suffix = "secrets/name%2Fpart?api-version=7.6&skiptoken=a%2Bb%3D";
        for (String origin : Arrays.asList("https://REDACTED:443/", "HTTPS://redacted/")) {
            assertEquals("https://localhost:8080/" + suffix,
                pattern.matcher(origin + suffix).replaceFirst(sanitizer.getRedactedValue()));
        }
        for (String origin : Arrays.asList("https://other.example/", "http://REDACTED/", "https://REDACTED:8443/",
            "https://REDACTED.other.example/", "https://user@REDACTED/", "https://REDACTED@other.example/")) {
            assertEquals(origin + suffix, pattern.matcher(origin + suffix).replaceFirst(sanitizer.getRedactedValue()));
        }
        assertEquals("$.nextLink", sanitizer.getKey());
    }

    @SyncAsyncTest
    public void allContinuationOperationsValidateBeforePipeline() {
        for (boolean customPipeline : new boolean[] { false, true }) {
            for (boolean disableVerification : new boolean[] { false, true }) {
                KeyVaultCredentialPolicy.clearCache();
                SecretClientBuilder builder = builder(ENDPOINT, customPipeline, disableVerification);
                SecretClient client = builder.buildClient();
                SecretAsyncClient asyncClient = builder.buildAsyncClient();
                List<PagedIterable<?>> syncPages = Arrays.asList(client.listPropertiesOfSecrets(),
                    client.listPropertiesOfSecretVersions("secret"), client.listDeletedSecrets());
                List<PagedFlux<?>> asyncPages = Arrays.asList(asyncClient.listPropertiesOfSecrets(),
                    asyncClient.listPropertiesOfSecretVersions("secret"), asyncClient.listDeletedSecrets());

                for (int i = 0; i < syncPages.size(); i++) {
                    PagedIterable<?> sync = syncPages.get(i);
                    PagedFlux<?> async = asyncPages.get(i);
                    assertRejectedContinuations(sync, async);
                    assertEquals(1, readPages(sync, async, null));
                    assertRejectedContinuations(sync, async);
                    assertEquals(1, readPages(sync, async, "HTTPS://PAGING.VAULT.AZURE.NET:443/secrets?skip=1"));
                }

                // A new client may reuse the static challenge cache, but must still validate its first continuation.
                assertRejectedContinuations(builder.buildClient().listPropertiesOfSecrets(),
                    builder.buildAsyncClient().listPropertiesOfSecrets());
            }
        }
        assertTrue(tokenRequests.get() > 0);
        assertTrue(authorizedSends.get() > 0);
    }

    @SyncAsyncTest
    public void serviceProvidedContinuationIsValidated() {
        SecretClientBuilder builder = builder(ENDPOINT, false, false);
        SecretClient client = builder.buildClient();
        SecretAsyncClient asyncClient = builder.buildAsyncClient();
        responseNextLink.set(ENDPOINT + "/secrets?skip=1");
        assertEquals(2, readPages(client.listPropertiesOfSecrets(), asyncClient.listPropertiesOfSecrets(), null));

        int previousTokens = tokenRequests.get();
        int previousSends = sends.get();
        responseNextLink.set("https://other.example/secrets");
        assertThrows(IllegalArgumentException.class,
            () -> readPages(client.listPropertiesOfSecrets(), asyncClient.listPropertiesOfSecrets(), null));
        assertEquals(previousTokens, tokenRequests.get());
        assertEquals(previousSends + 1, sends.get());
    }

    @SyncAsyncTest
    public void configuredCloudAndCustomOriginsAreSupported() {
        for (String endpoint : Arrays.asList("https://paging.vault.usgovcloudapi.net", "https://paging.vault.azure.cn",
            "https://vault.example:8443", "https://localhost:443", "HTTPS://PAGING.VAULT.AZURE.NET:443",
            "https://vault.example.:8443", "https://vault.xn--bcher-kva.example:8443")) {
            SecretClientBuilder builder = builder(endpoint, false, true);
            assertEquals(1, readPages(builder.buildClient().listPropertiesOfSecrets(),
                builder.buildAsyncClient().listPropertiesOfSecrets(), endpoint + "/secrets?skip=1"));
        }
    }

    @SyncAsyncTest
    public void validCloudAndCustomChallengesWorkWithVerificationEnabled() {
        String[][] endpoints = {
            { "https://paging.vault.azure.net", "https://vault.azure.net" },
            { "https://paging.vault.usgovcloudapi.net", "https://vault.usgovcloudapi.net" },
            { "https://paging.vault.azure.cn", "https://vault.azure.cn" },
            { "https://paging.vault.microsoftazure.de", "https://vault.microsoftazure.de" },
            { "https://vault.arbitrary.example:8443", "https://arbitrary.example" } };
        for (boolean disableVerification : new boolean[] { false, true }) {
            for (String[] endpoint : endpoints) {
                KeyVaultCredentialPolicy.clearCache();
                challengeResource.set(endpoint[1]);
                SecretClientBuilder builder = builder(endpoint[0], false, disableVerification);
                assertEquals(1, readPages(builder.buildClient().listPropertiesOfSecrets(),
                    builder.buildAsyncClient().listPropertiesOfSecrets(), endpoint[0] + "/secrets?skip=1"));
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
        SecretClientBuilder original = builder(ENDPOINT, false, disableVerification);
        SecretClient client = original.buildClient();
        SecretAsyncClient asyncClient = original.buildAsyncClient();
        assertEquals(1, readPages(client.listPropertiesOfSecrets(), asyncClient.listPropertiesOfSecrets(), null));

        String otherOrigin = "https://other.vault.azure.net";
        SecretClientBuilder other = builder(otherOrigin, false, disableVerification);
        // The existing challenge-resource rule accepts this other vault, but it is not our configured origin.
        assertEquals(1, readPages(other.buildClient().listPropertiesOfSecrets(),
            other.buildAsyncClient().listPropertiesOfSecrets(), null));
        int previousTokens = tokenRequests.get();
        int previousSends = sends.get();
        assertThrows(IllegalArgumentException.class, () -> readPages(client.listPropertiesOfSecrets(),
            asyncClient.listPropertiesOfSecrets(), otherOrigin + "/secrets?skip=1"));
        assertEquals(previousTokens, tokenRequests.get());
        assertEquals(previousSends, sends.get());
    }

    @SyncAsyncTest
    public void verificationOptOutChangesOnlyChallengeResourceValidation() {
        String endpoint = "https://vault.example:8443";
        SecretClientBuilder verified = builder(endpoint, false, false);
        RuntimeException rejected
            = assertThrows(RuntimeException.class, () -> readPages(verified.buildClient().listPropertiesOfSecrets(),
                verified.buildAsyncClient().listPropertiesOfSecrets(), endpoint + "/secrets"));
        assertTrue(rejected.getMessage().contains("challenge resource"));
        assertEquals(0, tokenRequests.get());
        assertEquals(1, sends.get());

        SecretClientBuilder optedOut = builder(endpoint, false, true);
        SecretClient client = optedOut.buildClient();
        SecretAsyncClient asyncClient = optedOut.buildAsyncClient();
        assertEquals(1,
            readPages(client.listPropertiesOfSecrets(), asyncClient.listPropertiesOfSecrets(), endpoint + "/secrets"));
        assertEquals(1, tokenRequests.get());
        int previousSends = sends.get();
        for (boolean disableVerification : new boolean[] { false, true }) {
            String insecureEndpoint = "http://vault.example:8443";
            SecretClientBuilder insecure = builder(insecureEndpoint, false, disableVerification);
            RuntimeException insecureRejected
                = assertThrows(RuntimeException.class, () -> readPages(insecure.buildClient().listPropertiesOfSecrets(),
                    insecure.buildAsyncClient().listPropertiesOfSecrets(), insecureEndpoint + "/secrets"));
            assertTrue(insecureRejected.getMessage().contains("HTTPS"));
            assertEquals(1, tokenRequests.get());
            assertEquals(previousSends, sends.get());
        }
        assertThrows(IllegalArgumentException.class, () -> readPages(client.listPropertiesOfSecrets(),
            asyncClient.listPropertiesOfSecrets(), "https://other.example:8443/secrets"));
        assertEquals(1, tokenRequests.get());
        assertEquals(previousSends, sends.get());
    }

    @SyncAsyncTest
    public void normalizedContinuationRetainsPathAndQuery() {
        SecretClientBuilder builder = builder("HTTPS://PAGING.VAULT.AZURE.NET:443", false, false);
        SecretClient client = builder.buildClient();
        SecretAsyncClient asyncClient = builder.buildAsyncClient();
        for (String continuation : Arrays.asList("https://paging.vault.azure.net/secrets?skip=1",
            "HTTPS://PAGING.VAULT.AZURE.NET:443/secrets?skip=1")) {
            assertEquals(1,
                readPages(client.listPropertiesOfSecrets(), asyncClient.listPropertiesOfSecrets(), continuation));
            assertEquals("/secrets", lastRequestUrl.get().getPath());
            assertEquals("skip=1", lastRequestUrl.get().getQuery());
            assertEquals("paging.vault.azure.net", lastRequestUrl.get().getHost().toLowerCase(Locale.ROOT));
        }
    }

    @SyncAsyncTest
    public void customUnauthenticatedPipelineSupportsSameOriginHttp() {
        SecretClientBuilder builder = new SecretClientBuilder().vaultUrl("http://localhost")
            .pipeline(new HttpPipelineBuilder().httpClient(request -> {
                sends.incrementAndGet();
                assertEquals("http", request.getUrl().getProtocol());
                return Mono.just(new MockHttpResponse(request, 200,
                    new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                    "{\"value\":[]}".getBytes(StandardCharsets.UTF_8)));
            }).build());
        SecretClient client = builder.buildClient();
        SecretAsyncClient asyncClient = builder.buildAsyncClient();
        assertEquals(1, readPages(client.listPropertiesOfSecrets(), asyncClient.listPropertiesOfSecrets(),
            "HTTP://LOCALHOST:80/secrets?skip=1"));
        assertThrows(IllegalArgumentException.class, () -> readPages(client.listPropertiesOfSecrets(),
            asyncClient.listPropertiesOfSecrets(), "https://localhost/secrets?skip=1"));
        assertEquals(1, sends.get());
    }

    private void assertRejectedContinuations(PagedIterable<?> sync, PagedFlux<?> async) {
        for (String nextLink : Arrays.asList("https://other.example/secrets", "https://other.vault.azure.net/secrets",
            "http://paging.vault.azure.net/secrets", ENDPOINT + ":8443/secrets",
            "https://paging.vault.azure.net.other.example/secrets", "https://user@paging.vault.azure.net/secrets",
            "https://paging.vault.azure.net@other.example/secrets", ENDPOINT + ":invalid/secrets",
            ENDPOINT + ":65536/secrets", ENDPOINT + "/bad path", ENDPOINT + "/secrets#fragment", "/secrets",
            "//other.example/secrets")) {
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

    private SecretClientBuilder builder(String endpoint, boolean customPipeline, boolean disableVerification) {
        TokenCredential credential = context -> {
            tokenRequests.incrementAndGet();
            return Mono.just(new AccessToken("fake-key-vault-token", OffsetDateTime.now().plusHours(1)));
        };
        HttpClient httpClient = request -> Mono.fromSupplier(() -> {
            lastRequestUrl.set(request.getUrl());
            sends.incrementAndGet();
            String authorization = request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            if (authorization == null) {
                return new MockHttpResponse(request, 401,
                    new HttpHeaders().set(HttpHeaderName.WWW_AUTHENTICATE,
                        "Bearer authorization=\"https://login.example/tenant\", resource=\"" + challengeResource.get()
                            + "\""));
            }
            assertEquals("Bearer fake-key-vault-token", authorization);
            authorizedSends.incrementAndGet();
            String nextLink = responseNextLink.getAndSet(null);
            String body = nextLink == null ? "{\"value\":[]}" : "{\"value\":[],\"nextLink\":\"" + nextLink + "\"}";
            return new MockHttpResponse(request, 200,
                new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                body.getBytes(StandardCharsets.UTF_8));
        });
        SecretClientBuilder builder = new SecretClientBuilder().vaultUrl(endpoint)
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
