// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Base64Util;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.security.keyvault.secrets.implementation.KeyVaultCredentialPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.azure.core.http.HttpHeaderName.AUTHORIZATION;
import static com.azure.core.util.CoreUtils.isNullOrEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class KeyVaultCredentialPolicyTest {
    private static final String AUTHENTICATE_HEADER
        = "Bearer authorization=\"https://login.windows.net/72f988bf-86f1-41af-91ab-2d7cd022db57\", "
            + "resource=\"https://vault.azure.net\"";
    private static final String AUTHENTICATE_HEADER_WITH_CLAIMS
        = "Bearer realm=\"\", authorization_uri=\"https://login.microsoftonline.com/common/oauth2/authorize\", "
            + "error=\"insufficient_claims\", "
            + "claims=\"eyJhY2Nlc3NfdG9rZW4iOnsiYWNycyI6eyJlc3NlbnRpYWwiOnRydWUsInZhbHVlIjoiY3AxIn19fQ==\"";
    private static final String DECODED_CLAIMS = "{\"access_token\":{\"acrs\":{\"essential\":true,\"value\":\"cp1\"}}}";
    private static final String BEARER = "Bearer";
    private static final String BODY = "this is a sample body";
    private static final Flux<ByteBuffer> BODY_FLUX = Flux.defer(
        () -> Flux.fromStream(Stream.of(BODY.split("")).map(s -> ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)))));
    private static final String FAKE_ENCODED_CREDENTIAL
        = Base64Util.encodeToString("user:fakePasswordPlaceholder".getBytes(StandardCharsets.UTF_8));
    private static final List<Function<TokenRequestContext, Boolean>> BASE_ASSERTIONS = Arrays.asList(
        tokenRequestContext -> !tokenRequestContext.getScopes().isEmpty(),
        tokenRequestContext -> !isNullOrEmpty(tokenRequestContext.getTenantId()), TokenRequestContext::isCaeEnabled);

    private HttpResponse simpleResponse;
    private HttpResponse unauthorizedHttpResponseWithWrongStatusCode;
    private HttpResponse unauthorizedHttpResponseWithHeader;
    private HttpResponse unauthorizedHttpResponseWithoutHeader;
    private HttpResponse unauthorizedHttpResponseWithHeaderAndClaims;
    private HttpPipelineCallContext callContext;
    private HttpPipelineCallContext differentScopeContext;
    private HttpPipelineCallContext testContext;
    private HttpPipelineCallContext bodyContext;
    private HttpPipelineCallContext bodyFluxContext;
    private TokenCredential credential;

    private static HttpPipelineCallContext createCallContext(HttpRequest request, Context context) {
        AtomicReference<HttpPipelineCallContext> callContextReference = new AtomicReference<>();

        HttpPipeline callContextCreator = new HttpPipelineBuilder().policies((callContext, next) -> {
            callContextReference.set(callContext);

            return next.process();
        }).httpClient(ignored -> Mono.empty()).build();

        callContextCreator.sendSync(request, context);

        return callContextReference.get();
    }

    @BeforeEach
    public void setup() {
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://kvtest.vault.azure.net");
        HttpRequest requestWithDifferentScope = new HttpRequest(HttpMethod.GET, "https://mytest.azurecr.io");

        Context bodyContextContext = new Context("KeyVaultCredentialPolicyStashedBody", BinaryData.fromString(BODY))
            .addData("KeyVaultCredentialPolicyStashedContentLength", "21");

        Context bodyFluxContextContext = new Context("KeyVaultCredentialPolicyStashedBody", BODY_FLUX)
            .addData("KeyVaultCredentialPolicyStashedContentLength", "21");

        MockHttpResponse simpleResponse
            = new MockHttpResponse(new HttpRequest(HttpMethod.GET, "https://azure.com"), 200);

        MockHttpResponse unauthorizedResponseWithWrongStatusCode
            = new MockHttpResponse(new HttpRequest(HttpMethod.GET, "https://azure.com"), 500);

        MockHttpResponse unauthorizedResponseWithoutHeader
            = new MockHttpResponse(new HttpRequest(HttpMethod.GET, "https://azure.com"), 401);

        MockHttpResponse unauthorizedResponseWithHeader
            = new MockHttpResponse(new HttpRequest(HttpMethod.GET, "https://azure.com"), 401,
                new HttpHeaders().set(HttpHeaderName.WWW_AUTHENTICATE, AUTHENTICATE_HEADER));

        MockHttpResponse unauthorizedResponseWithHeaderAndClaims
            = new MockHttpResponse(new HttpRequest(HttpMethod.GET, "https://azure.com"), 401,
                new HttpHeaders().set(HttpHeaderName.WWW_AUTHENTICATE, AUTHENTICATE_HEADER_WITH_CLAIMS));

        this.simpleResponse = simpleResponse;
        this.unauthorizedHttpResponseWithWrongStatusCode = unauthorizedResponseWithWrongStatusCode;
        this.unauthorizedHttpResponseWithHeader = unauthorizedResponseWithHeader;
        this.unauthorizedHttpResponseWithoutHeader = unauthorizedResponseWithoutHeader;
        this.unauthorizedHttpResponseWithHeaderAndClaims = unauthorizedResponseWithHeaderAndClaims;
        this.callContext = createCallContext(request, Context.NONE);
        this.differentScopeContext = createCallContext(requestWithDifferentScope, Context.NONE);
        this.testContext = createCallContext(request, Context.NONE);
        this.bodyContext = createCallContext(request, bodyContextContext);
        this.bodyFluxContext = createCallContext(request, bodyFluxContextContext);
        // Can't use BasicAuthenticationCredential until the following PR is merged:
        // https://github.com/Azure/azure-sdk-for-java/pull/42238
        this.credential = tokenRequestContext -> Mono
            .fromCallable(() -> new AccessToken(FAKE_ENCODED_CREDENTIAL, OffsetDateTime.MAX.minusYears(1)));
    }

    @AfterEach
    public void cleanup() {
        KeyVaultCredentialPolicy.clearCache();
    }

    @SyncAsyncTest
    public void failedAcquisitionCannotReuseAnotherOriginsToken() {
        for (boolean sameTenant : new boolean[] { false, true }) {
            for (boolean sameScope : new boolean[] { false, true }) {
                KeyVaultCredentialPolicy.clearCache();
                AtomicBoolean failAcquisition = new AtomicBoolean();
                AtomicInteger tokenRequests = new AtomicInteger();
                AtomicInteger foreignAuthorizedSends = new AtomicInteger();
                String secondTenant = sameTenant ? "first" : "second";
                String secondScope = sameScope ? "https://vault.azure.net/.default" : "https://azure.net/.default";
                KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(context -> Mono.fromSupplier(() -> {
                    tokenRequests.incrementAndGet();
                    if (failAcquisition.get()) {
                        assertEquals(secondTenant, context.getTenantId());
                        assertEquals(Arrays.asList(secondScope), context.getScopes());
                        throw new IllegalStateException("Token acquisition failed.");
                    }
                    return new AccessToken("first-origin-token", OffsetDateTime.now().plusHours(1));
                }), false);
                HttpPipeline pipeline = new HttpPipelineBuilder()
                    .policies(new RetryPolicy(new FixedDelay(2, Duration.ofMillis(1))), policy)
                    .httpClient(request -> {
                        boolean second = request.getUrl().getHost().equals("second.vault.azure.net");
                        String authorization = request.getHeaders().getValue(AUTHORIZATION);
                        if (authorization == null) {
                            return Mono.just(tokenCacheChallenge(request, second ? secondTenant : "first",
                                second ? secondScope : "https://vault.azure.net/.default", null));
                        }
                        if (second) {
                            foreignAuthorizedSends.incrementAndGet();
                        }
                        return Mono.just(new MockHttpResponse(request, 200));
                    })
                    .build();

                sendTokenCacheRequest(pipeline, "https://first.vault.azure.net");
                assertEquals(1, tokenRequests.get());
                failAcquisition.set(true);
                assertThrows(IllegalStateException.class,
                    () -> sendTokenCacheRequest(pipeline, "https://second.vault.azure.net"));
                assertEquals(0, foreignAuthorizedSends.get());
                assertEquals(4, tokenRequests.get());

                failAcquisition.set(false);
                sendTokenCacheRequest(pipeline, "HTTPS://FIRST.VAULT.AZURE.NET:443/secrets");
                assertEquals(4, tokenRequests.get(),
                    "A failed acquisition for another origin must not evict this token.");
            }
        }
    }

    @SyncAsyncTest
    public void failedContextChangeCannotReusePreviousToken() {
        for (String change : Arrays.asList("tenant", "scope", "claims")) {
            KeyVaultCredentialPolicy.clearCache();
            AtomicBoolean changedChallenge = new AtomicBoolean();
            AtomicInteger tokenRequests = new AtomicInteger();
            AtomicInteger staleAuthorizedSends = new AtomicInteger();
            String tenant = "tenant".equals(change) ? "second" : "first";
            String scope = "scope".equals(change) ? "https://azure.net/.default" : "https://vault.azure.net/.default";
            String claims = "claims".equals(change) ? DECODED_CLAIMS : null;
            KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(context -> Mono.fromSupplier(() -> {
                tokenRequests.incrementAndGet();
                if (changedChallenge.get()) {
                    assertEquals(tenant, context.getTenantId());
                    assertEquals(Arrays.asList(scope), context.getScopes());
                    assertEquals(claims, context.getClaims());
                    assertTrue(context.isCaeEnabled());
                    throw new IllegalStateException("Token acquisition failed.");
                }
                return new AccessToken("previous-context-token", OffsetDateTime.now().plusHours(1));
            }), false);
            HttpPipeline pipeline
                = new HttpPipelineBuilder().policies(new RetryPolicy(new FixedDelay(2, Duration.ofMillis(1))), policy)
                    .httpClient(request -> {
                        String authorization = request.getHeaders().getValue(AUTHORIZATION);
                        if (changedChallenge.get() && authorization != null) {
                            staleAuthorizedSends.incrementAndGet();
                            return Mono.just(new MockHttpResponse(request, 200));
                        }
                        if ("/changed".equals(request.getUrl().getPath())) {
                            changedChallenge.set(true);
                            return Mono.just(tokenCacheChallenge(request, tenant, scope, claims));
                        }
                        return Mono.just(authorization == null
                            ? tokenCacheChallenge(request, "first", "https://vault.azure.net/.default", null)
                            : new MockHttpResponse(request, 200));
                    })
                    .build();

            sendTokenCacheRequest(pipeline, "https://first.vault.azure.net");
            assertThrows(IllegalStateException.class,
                () -> sendTokenCacheRequest(pipeline, "https://first.vault.azure.net/changed"));
            assertEquals(0, staleAuthorizedSends.get());
            assertEquals(4, tokenRequests.get());
        }
    }

    private static HttpResponse sendTokenCacheRequest(HttpPipeline pipeline, String url) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        return SyncAsyncExtension.execute(() -> pipeline.sendSync(request, Context.NONE),
            () -> pipeline.send(request, Context.NONE));
    }

    @SyncAsyncTest
    public void challengedContextSurvivesFailedAcquisitionAndRetry() {
        for (boolean disableVerification : new boolean[] { false, true }) {
            for (boolean changeTenantAndScope : new boolean[] { false, true }) {
                KeyVaultCredentialPolicy.clearCache();
                AtomicBoolean challenged = new AtomicBoolean();
                AtomicBoolean independentRequest = new AtomicBoolean();
                AtomicInteger tokenRequests = new AtomicInteger();
                AtomicInteger challengedTokenRequests = new AtomicInteger();
                String tenant = changeTenantAndScope ? "second" : "first";
                String scope = changeTenantAndScope ? "https://azure.net/.default" : "https://vault.azure.net/.default";
                KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(context -> Mono.fromSupplier(() -> {
                    tokenRequests.incrementAndGet();
                    assertEquals(challenged.get() ? tenant : "first", context.getTenantId());
                    assertEquals(Arrays.asList(challenged.get() ? scope : "https://vault.azure.net/.default"),
                        context.getScopes());
                    assertTrue(context.isCaeEnabled());
                    assertEquals(challenged.get() && !independentRequest.get() ? DECODED_CLAIMS : null,
                        context.getClaims());
                    if (challenged.get() && !independentRequest.get()) {
                        if (challengedTokenRequests.incrementAndGet() == 1) {
                            context.getScopes().clear();
                            context.setTenantId("credential-mutated").setClaims("mutated").setCaeEnabled(false);
                            throw new IllegalStateException("Transient challenged token acquisition failure.");
                        }
                        return new AccessToken("claims-token", OffsetDateTime.now().plusHours(1));
                    }
                    return new AccessToken("ordinary-token", OffsetDateTime.now().plusHours(1));
                }), disableVerification);
                HttpPipeline pipeline = new HttpPipelineBuilder()
                    .policies(new RetryPolicy(new FixedDelay(2, Duration.ofMillis(1))), policy)
                    .httpClient(request -> {
                        String authorization = request.getHeaders().getValue(AUTHORIZATION);
                        if ("/changed".equals(request.getUrl().getPath())) {
                            if (challenged.compareAndSet(false, true)) {
                                assertEquals("Bearer ordinary-token", authorization);
                                return Mono.just(tokenCacheChallenge(request, tenant, scope, DECODED_CLAIMS));
                            }
                            assertEquals("Bearer claims-token", authorization);
                        } else if (authorization == null) {
                            return Mono
                                .just(tokenCacheChallenge(request, "first", "https://vault.azure.net/.default", null));
                        } else {
                            assertEquals("Bearer ordinary-token", authorization);
                        }
                        return Mono.just(new MockHttpResponse(request, 200));
                    })
                    .build();

                assertEquals(200, sendTokenCacheRequest(pipeline, "https://first.vault.azure.net").getStatusCode());
                assertEquals(200,
                    sendTokenCacheRequest(pipeline, "https://first.vault.azure.net/changed").getStatusCode());
                assertEquals(2, challengedTokenRequests.get());
                assertEquals(3, tokenRequests.get());

                independentRequest.set(true);
                assertEquals(200,
                    sendTokenCacheRequest(pipeline, "https://first.vault.azure.net/independent").getStatusCode());
                assertEquals(4, tokenRequests.get(), "Claims must not leak into a separate request.");
            }
        }
    }

    @SyncAsyncTest
    public void originScopedTokensSupportRetriesAndNormalization() {
        AtomicInteger firstTokenRequests = new AtomicInteger();
        AtomicInteger secondTokenRequests = new AtomicInteger();
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(context -> Mono.fromSupplier(() -> {
            if ("first".equals(context.getTenantId())) {
                firstTokenRequests.incrementAndGet();
                return new AccessToken("first-origin-token", OffsetDateTime.now().plusHours(1));
            }
            if (secondTokenRequests.incrementAndGet() == 1) {
                throw new IllegalStateException("Transient token acquisition failure.");
            }
            return new AccessToken("second-origin-token", OffsetDateTime.now().plusHours(1));
        }), false);
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new RetryPolicy(new FixedDelay(2, Duration.ofMillis(1))), policy)
                .httpClient(request -> {
                    String tenant
                        = request.getUrl().getHost().equalsIgnoreCase("first.vault.azure.net") ? "first" : "second";
                    String authorization = request.getHeaders().getValue(AUTHORIZATION);
                    if (authorization == null) {
                        return Mono
                            .just(tokenCacheChallenge(request, tenant, "https://vault.azure.net/.default", null));
                    }
                    assertEquals("Bearer " + tenant + "-origin-token", authorization);
                    return Mono.just(new MockHttpResponse(request, 200));
                })
                .build();

        for (String url : Arrays.asList("https://first.vault.azure.net", "https://second.vault.azure.net",
            "HTTPS://FIRST.VAULT.AZURE.NET:443/secrets", "HTTPS://SECOND.VAULT.AZURE.NET:443/secrets")) {
            assertEquals(200, sendTokenCacheRequest(pipeline, url).getStatusCode());
        }
        assertEquals(1, firstTokenRequests.get());
        assertEquals(2, secondTokenRequests.get());
    }

    @Test
    public void concurrentTokenAcquisitionsKeepIndependentCacheState() {
        for (boolean sameOrigin : new boolean[] { false, true }) {
            Sinks.One<AccessToken> firstToken = Sinks.one();
            Sinks.One<AccessToken> secondToken = Sinks.one();
            AtomicInteger firstTokenRequests = new AtomicInteger();
            AtomicInteger secondTokenRequests = new AtomicInteger();
            KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(context -> {
                if ("first".equals(context.getTenantId())) {
                    firstTokenRequests.incrementAndGet();
                    return firstToken.asMono();
                }
                if (secondTokenRequests.incrementAndGet() == 1) {
                    return Mono.error(new IllegalStateException("Transient token acquisition failure."));
                }
                return secondToken.asMono();
            }, false);
            HttpPipelineCallContext firstContext
                = createCallContext(new HttpRequest(HttpMethod.GET, "https://first.vault.azure.net"), Context.NONE);
            HttpPipelineCallContext secondContext = createCallContext(new HttpRequest(HttpMethod.GET,
                sameOrigin ? "https://first.vault.azure.net" : "https://second.vault.azure.net"), Context.NONE);
            TokenRequestContext firstRequest = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
                .setTenantId("first")
                .setCaeEnabled(true);
            TokenRequestContext secondRequest = new TokenRequestContext().addScopes("https://vault.azure.net/.default")
                .setTenantId("second")
                .setCaeEnabled(true);

            StepVerifier.create(Mono.when(policy.setAuthorizationHeader(firstContext, firstRequest),
                policy.setAuthorizationHeader(secondContext, secondRequest).retry(1))).then(() -> {
                    assertEquals(1, firstTokenRequests.get());
                    assertEquals(2, secondTokenRequests.get());
                    secondToken.tryEmitValue(new AccessToken("second-token", OffsetDateTime.now().plusHours(1)));
                    firstToken.tryEmitValue(new AccessToken("first-token", OffsetDateTime.now().plusHours(1)));
                }).expectComplete().verify(Duration.ofSeconds(5));
            assertEquals("Bearer first-token", firstContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION));
            assertEquals("Bearer second-token", secondContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION));
            StepVerifier.create(policy.setAuthorizationHeader(secondContext, secondRequest)).verifyComplete();
            assertEquals(2, secondTokenRequests.get());
        }
    }

    private static HttpResponse tokenCacheChallenge(HttpRequest request, String tenant, String scope, String claims) {
        String challenge = "Bearer authorization=\"https://login.example/" + tenant + "\", scope=\"" + scope + "\"";
        if (claims != null) {
            challenge += ", error=\"insufficient_claims\", claims=\""
                + Base64Util.encodeToString(claims.getBytes(StandardCharsets.UTF_8)) + "\"";
        }
        return new MockHttpResponse(request, 401, new HttpHeaders().set(HttpHeaderName.WWW_AUTHENTICATE, challenge));
    }

    @Test
    public void concurrentChallengeCannotReplaceRequestLocalRetryContext() {
        Sinks.One<AccessToken> pendingToken = Sinks.one();
        AtomicInteger firstAcquisitions = new AtomicInteger();
        AtomicInteger secondAcquisitions = new AtomicInteger();
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(context -> {
            boolean first = "first".equals(context.getTenantId());
            assertEquals(first ? "first" : "second", context.getTenantId());
            assertEquals(Arrays.asList(first ? "https://vault.azure.net/.default" : "https://azure.net/.default"),
                context.getScopes());
            assertEquals(first ? DECODED_CLAIMS : "{}", context.getClaims());
            assertTrue(context.isCaeEnabled());
            if (first && firstAcquisitions.incrementAndGet() == 1) {
                return pendingToken.asMono();
            }
            if (!first) {
                secondAcquisitions.incrementAndGet();
            }
            return Mono
                .just(new AccessToken(first ? "first-token" : "second-token", OffsetDateTime.now().plusHours(1)));
        }, false);
        HttpPipelineCallContext first
            = createCallContext(new HttpRequest(HttpMethod.GET, "https://same.vault.azure.net/first"), Context.NONE);
        HttpPipelineCallContext second
            = createCallContext(new HttpRequest(HttpMethod.GET, "https://same.vault.azure.net/second"), Context.NONE);
        HttpResponse firstChallenge
            = tokenCacheChallenge(first.getHttpRequest(), "first", "https://vault.azure.net/.default", DECODED_CLAIMS);
        HttpResponse secondChallenge
            = tokenCacheChallenge(second.getHttpRequest(), "second", "https://azure.net/.default", "{}");

        StepVerifier
            .create(policy.authorizeRequestOnChallenge(first, firstChallenge)
                .onErrorResume(IllegalStateException.class, error -> policy.authorizeRequest(first).thenReturn(true)))
            .then(() -> {
                assertTrue(policy.authorizeRequestOnChallengeSync(second, secondChallenge));
                pendingToken.tryEmitError(new IllegalStateException("Transient acquisition failure."));
            })
            .expectNext(true)
            .expectComplete()
            .verify(Duration.ofSeconds(5));
        assertEquals(2, firstAcquisitions.get());
        assertEquals(1, secondAcquisitions.get());
        assertEquals("Bearer first-token", first.getHttpRequest().getHeaders().getValue(AUTHORIZATION));
        assertEquals("Bearer second-token", second.getHttpRequest().getHeaders().getValue(AUTHORIZATION));
    }

    @SyncAsyncTest
    public void cachedChallengeIsBoundToRequestOrigin() {
        AtomicInteger tokenRequests = new AtomicInteger();
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(context -> {
            tokenRequests.incrementAndGet();
            return credential.getToken(context);
        }, false);
        SyncAsyncExtension.execute(
            () -> policy.authorizeRequestOnChallengeSync(callContext, unauthorizedHttpResponseWithHeader),
            () -> policy.authorizeRequestOnChallenge(callContext, unauthorizedHttpResponseWithHeader));
        assertEquals(1, tokenRequests.get());

        for (KeyVaultCredentialPolicy cachedPolicy : Arrays.asList(policy, new KeyVaultCredentialPolicy(context -> {
            tokenRequests.incrementAndGet();
            return credential.getToken(context);
        }, false))) {
            for (String endpoint : Arrays.asList("https://other.vault.azure.net", "https://kvtest.vault.azure.net:8443",
                "http://kvtest.vault.azure.net")) {
                HttpPipelineCallContext foreignContext
                    = createCallContext(new HttpRequest(HttpMethod.GET, endpoint), Context.NONE);
                int previousTokenRequests = tokenRequests.get();
                SyncAsyncExtension.execute(() -> cachedPolicy.authorizeRequestSync(foreignContext),
                    () -> cachedPolicy.authorizeRequest(foreignContext));
                assertNull(foreignContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION));
                assertEquals(previousTokenRequests, tokenRequests.get());
            }

            HttpPipelineCallContext normalizedContext = createCallContext(
                new HttpRequest(HttpMethod.GET, "HTTPS://KVTEST.VAULT.AZURE.NET:443/secrets"), Context.NONE);
            SyncAsyncExtension.execute(() -> cachedPolicy.authorizeRequestSync(normalizedContext),
                () -> cachedPolicy.authorizeRequest(normalizedContext));
            assertNotNull(normalizedContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION));
        }
    }

    @SyncAsyncTest
    public void foreignContinuationIsRejectedBeforeFirstRequest() {
        AtomicInteger tokenRequests = new AtomicInteger();
        AtomicInteger sends = new AtomicInteger();
        SecretClientBuilder builder
            = new SecretClientBuilder().vaultUrl("https://kvtest.vault.azure.net").credential(context -> {
                tokenRequests.incrementAndGet();
                return credential.getToken(context);
            }).httpClient(request -> {
                sends.incrementAndGet();
                return Mono.just(new MockHttpResponse(request, 200,
                    new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                    "{\"value\":[]}".getBytes(StandardCharsets.UTF_8)));
            });

        assertThrows(IllegalArgumentException.class,
            () -> SyncAsyncExtension.execute(
                () -> builder.buildClient()
                    .listPropertiesOfSecrets()
                    .iterableByPage("https://other.vault.azure.net/secrets")
                    .iterator()
                    .next(),
                () -> builder.buildAsyncClient()
                    .listPropertiesOfSecrets()
                    .byPage("https://other.vault.azure.net/secrets")
                    .next()));
        assertEquals(0, tokenRequests.get());
        assertEquals(0, sends.get());
    }

    @SyncAsyncTest
    public void interleavedRequestsUseTheirOwnChallenge() {
        // Inspect the authorization contexts independently of the single access-token cache.
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(credential, false) {
            @Override
            public Mono<Void> setAuthorizationHeader(HttpPipelineCallContext context,
                TokenRequestContext tokenRequestContext) {
                return Mono.fromRunnable(() -> setAuthorizationHeaderSync(context, tokenRequestContext));
            }

            @Override
            public void setAuthorizationHeaderSync(HttpPipelineCallContext context,
                TokenRequestContext tokenRequestContext) {
                context.getHttpRequest().setHeader(AUTHORIZATION, "Bearer " + tokenRequestContext.getTenantId());
            }
        };
        List<HttpPipelineCallContext> contexts = new ArrayList<>();
        for (String tenant : Arrays.asList("first", "second")) {
            String endpoint = "https://" + tenant + ".vault.azure.net";
            HttpPipelineCallContext context
                = createCallContext(new HttpRequest(HttpMethod.GET, endpoint), Context.NONE);
            HttpResponse response = new MockHttpResponse(context.getHttpRequest(), 401, new HttpHeaders().set(
                HttpHeaderName.WWW_AUTHENTICATE,
                "Bearer authorization=\"https://login.example/" + tenant + "\", resource=\"https://vault.azure.net\""));
            SyncAsyncExtension.execute(() -> policy.authorizeRequestOnChallengeSync(context, response),
                () -> policy.authorizeRequestOnChallenge(context, response));
        }
        for (int i = 0; i < 20; i++) {
            contexts.add(createCallContext(
                new HttpRequest(HttpMethod.GET, "https://" + (i % 2 == 0 ? "first" : "second") + ".vault.azure.net"),
                Context.NONE));
        }
        SyncAsyncExtension.execute(() -> contexts.forEach(policy::authorizeRequestSync),
            () -> Flux.fromIterable(contexts)
                .flatMap(context -> policy.authorizeRequest(context).subscribeOn(Schedulers.parallel()), 2)
                .then());
        for (int i = 0; i < contexts.size(); i++) {
            assertEquals("Bearer " + (i % 2 == 0 ? "first" : "second"),
                contexts.get(i).getHttpRequest().getHeaders().getValue(AUTHORIZATION));
        }
    }

    @SyncAsyncTest
    public void requestOriginIsRevalidatedDuringTokenAcquisition() {
        for (boolean disableVerification : new boolean[] { false, true }) {
            for (boolean cachedChallenge : new boolean[] { false, true }) {
                for (String destination : Arrays.asList("https://other.example/secrets",
                    "https://other.vault.azure.net/secrets", "https://kvtest.vault.azure.net:8443/secrets",
                    "http://kvtest.vault.azure.net/secrets", "https://user@kvtest.vault.azure.net/secrets")) {
                    KeyVaultCredentialPolicy.clearCache();
                    if (cachedChallenge) {
                        KeyVaultCredentialPolicy seed = new KeyVaultCredentialPolicy(credential, false);
                        SyncAsyncExtension.execute(
                            () -> seed.authorizeRequestOnChallengeSync(callContext, unauthorizedHttpResponseWithHeader),
                            () -> seed.authorizeRequestOnChallenge(callContext, unauthorizedHttpResponseWithHeader));
                    }
                    HttpRequest request = new HttpRequest(HttpMethod.GET, "https://kvtest.vault.azure.net/secrets");
                    AtomicInteger tokenRequests = new AtomicInteger();
                    AtomicInteger sends = new AtomicInteger();
                    TokenCredential mutatingCredential = context -> Mono.fromSupplier(() -> {
                        tokenRequests.incrementAndGet();
                        request.setUrl(destination);
                        return new AccessToken("fake-token", OffsetDateTime.now().plusHours(1));
                    });
                    HttpPipeline pipeline = new HttpPipelineBuilder()
                        .policies(new KeyVaultCredentialPolicy(mutatingCredential, disableVerification))
                        .httpClient(ignored -> {
                            sends.incrementAndGet();
                            return Mono.just(request.getHeaders().getValue(AUTHORIZATION) == null
                                ? unauthorizedHttpResponseWithHeader
                                : simpleResponse);
                        })
                        .build();

                    assertThrows(IllegalArgumentException.class,
                        () -> SyncAsyncExtension.execute(() -> pipeline.sendSync(request, Context.NONE),
                            () -> pipeline.send(request, Context.NONE)));
                    assertEquals(1, tokenRequests.get());
                    assertEquals(cachedChallenge ? 0 : 1, sends.get());
                    assertNull(request.getHeaders().getValue(AUTHORIZATION));
                }
            }
        }
    }

    @SyncAsyncTest
    public void requestOriginIsRevalidatedBeforeCaeTokenAcquisition() {
        for (boolean disableVerification : new boolean[] { false, true }) {
            KeyVaultCredentialPolicy.clearCache();
            HttpRequest request = new HttpRequest(HttpMethod.GET, "https://kvtest.vault.azure.net/secrets");
            AtomicInteger tokenRequests = new AtomicInteger();
            AtomicInteger sends = new AtomicInteger();
            HttpPipeline pipeline = new HttpPipelineBuilder().policies(new KeyVaultCredentialPolicy(context -> {
                tokenRequests.incrementAndGet();
                return credential.getToken(context);
            }, disableVerification)).httpClient(ignored -> {
                if (sends.incrementAndGet() == 1) {
                    return Mono.just(unauthorizedHttpResponseWithHeader);
                }
                request.setUrl("https://other.vault.azure.net/secrets");
                return Mono.just(unauthorizedHttpResponseWithHeaderAndClaims);
            }).build();

            assertThrows(IllegalArgumentException.class, () -> SyncAsyncExtension
                .execute(() -> pipeline.sendSync(request, Context.NONE), () -> pipeline.send(request, Context.NONE)));
            assertEquals(1, tokenRequests.get());
            assertEquals(2, sends.get());
            assertNull(request.getHeaders().getValue(AUTHORIZATION));
        }
    }

    @SyncAsyncTest
    public void sameOriginChangeDuringTokenAcquisitionIsAllowed() {
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://kvtest.vault.azure.net/secrets");
        AtomicInteger sends = new AtomicInteger();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new KeyVaultCredentialPolicy(context -> Mono.fromSupplier(() -> {
                request.setUrl("HTTPS://KVTEST.VAULT.AZURE.NET:443/secrets?skip=1");
                return new AccessToken("fake-token", OffsetDateTime.now().plusHours(1));
            }), false)).httpClient(ignored -> {
                sends.incrementAndGet();
                return Mono.just(request.getHeaders().getValue(AUTHORIZATION) == null
                    ? unauthorizedHttpResponseWithHeader
                    : simpleResponse);
            }).build();

        HttpResponse response = SyncAsyncExtension.execute(() -> pipeline.sendSync(request, Context.NONE),
            () -> pipeline.send(request, Context.NONE));
        assertEquals(200, response.getStatusCode());
        assertEquals(2, sends.get());
        assertEquals("Bearer fake-token", request.getHeaders().getValue(AUTHORIZATION));
    }

    @SyncAsyncTest
    public void requestReplacementDuringTokenAcquisitionIsRejected() {
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://kvtest.vault.azure.net/secrets");
        HttpRequest replacement = new HttpRequest(HttpMethod.GET, "https://other.example/secrets");
        AtomicReference<HttpPipelineCallContext> activeContext = new AtomicReference<>();
        AtomicInteger sends = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies((context, next) -> {
            activeContext.set(context);
            return next.process();
        }, new KeyVaultCredentialPolicy(context -> Mono.fromSupplier(() -> {
            activeContext.get().setHttpRequest(replacement);
            return new AccessToken("fake-token", OffsetDateTime.now().plusHours(1));
        }), true)).httpClient(ignored -> {
            sends.incrementAndGet();
            return Mono.just(unauthorizedHttpResponseWithHeader);
        }).build();

        assertThrows(IllegalArgumentException.class, () -> SyncAsyncExtension
            .execute(() -> pipeline.sendSync(request, Context.NONE), () -> pipeline.send(request, Context.NONE)));
        assertEquals(1, sends.get());
        assertNull(request.getHeaders().getValue(AUTHORIZATION));
        assertNull(replacement.getHeaders().getValue(AUTHORIZATION));
    }

    @SyncAsyncTest
    public void onNon401ErrorResponse() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        HttpPipeline pipeline = new HttpPipelineBuilder().policies(policy)
            .httpClient(ignored -> Mono.just(unauthorizedHttpResponseWithWrongStatusCode))
            .build();

        SyncAsyncExtension.execute(
            () -> pipeline.sendSync(this.callContext.getHttpRequest(), this.callContext.getContext()),
            () -> pipeline.send(this.callContext.getHttpRequest(), this.callContext.getContext()));

        assertNull(this.callContext.getHttpRequest().getHeaders().get(AUTHORIZATION));

        KeyVaultCredentialPolicy.clearCache();
    }

    @SyncAsyncTest
    public void on401UnauthorizedResponseWithHeader() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        HttpPipeline pipeline = new HttpPipelineBuilder().policies(policy)
            .httpClient(ignored -> Mono.just(unauthorizedHttpResponseWithHeader))
            .build();

        SyncAsyncExtension.execute(
            () -> pipeline.sendSync(this.callContext.getHttpRequest(), this.callContext.getContext()),
            () -> pipeline.send(this.callContext.getHttpRequest(), this.callContext.getContext()));

        assertNotNull(this.callContext.getHttpRequest().getHeaders().get(AUTHORIZATION));

        KeyVaultCredentialPolicy.clearCache();
    }

    @SyncAsyncTest
    public void onChallengeCredentialPolicy() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        boolean onChallenge = SyncAsyncExtension.execute(
            () -> onChallengeAndClearCacheSync(policy, this.callContext, this.unauthorizedHttpResponseWithHeader),
            () -> onChallengeAndClearCache(policy, this.callContext, this.unauthorizedHttpResponseWithHeader));

        // Validate that the onChallengeSync ran successfully.
        assertTrue(onChallenge);

        String tokenValue = this.callContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);
        assertFalse(tokenValue.isEmpty());
        assertTrue(tokenValue.startsWith(BEARER));
    }

    @Test
    public void onAuthorizeRequestChallengeCachePresent() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        StepVerifier.create(onChallengeAndClearCache(policy, this.callContext, unauthorizedHttpResponseWithHeader) // Challenge cache created
            .then(policy.authorizeRequest(this.testContext))) // Challenge cache used
            .verifyComplete();

        String tokenValue = this.testContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);
        assertFalse(tokenValue.isEmpty());
        assertTrue(tokenValue.startsWith(BEARER));
    }

    @Test
    public void onAuthorizeRequestChallengeCachePresentSync() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        // Challenge cache created
        onChallengeAndClearCacheSync(policy, this.callContext, unauthorizedHttpResponseWithHeader);
        // Challenge cache used
        policy.authorizeRequestSync(this.testContext);

        String tokenValue = this.testContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);
        assertFalse(tokenValue.isEmpty());
        assertTrue(tokenValue.startsWith(BEARER));
    }

    @Test
    public void onAuthorizeRequestChallengeCachePresentWithClaims() {
        MutableTestCredential testCredential = new MutableTestCredential(new ArrayList<>(BASE_ASSERTIONS))
            .addAssertion(tokenRequestContext -> tokenRequestContext.getClaims() == null);
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(testCredential, false);

        StepVerifier.create(policy.authorizeRequestOnChallenge(this.callContext, // Challenge cache created
            this.unauthorizedHttpResponseWithHeader).flatMap(authorized -> {
                if (authorized) {
                    String firstToken = this.testContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);

                    assertFalse(firstToken.isEmpty());
                    assertTrue(firstToken.startsWith(BEARER));

                    testCredential.replaceAssertion(
                        tokenRequestContext -> DECODED_CLAIMS.equals(tokenRequestContext.getClaims()), 3);

                    return policy.authorizeRequestOnChallenge(this.callContext, // Challenge with claims received
                        this.unauthorizedHttpResponseWithHeaderAndClaims).map(ignored -> firstToken);
                } else {
                    return Mono.just("");
                }
            })).assertNext(firstToken -> {
                String newToken = this.testContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);

                assertFalse(newToken.isEmpty());
                assertTrue(newToken.startsWith(BEARER));

                assertNotEquals(firstToken, newToken);
            }).verifyComplete();

        KeyVaultCredentialPolicy.clearCache();
    }

    @Test
    public void onAuthorizeRequestChallengeNoCachePresentWithClaims() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        StepVerifier.create(policy.authorizeRequestOnChallenge(this.callContext, // Challenge cache created
            this.unauthorizedHttpResponseWithHeaderAndClaims)).assertNext(result -> {
                assertFalse(result);
                assertNull(this.testContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION));
            }).verifyComplete();

        KeyVaultCredentialPolicy.clearCache();
    }

    @Test
    public void onAuthorizeRequestChallengeCachePresentWithClaimsSync() {
        MutableTestCredential testCredential = new MutableTestCredential(new ArrayList<>(BASE_ASSERTIONS))
            .addAssertion(tokenRequestContext -> tokenRequestContext.getClaims() == null);
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(testCredential, false);

        // Challenge cache created
        assertTrue(policy.authorizeRequestOnChallengeSync(this.callContext, this.unauthorizedHttpResponseWithHeader));

        String firstToken = this.testContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);

        assertFalse(firstToken.isEmpty());
        assertTrue(firstToken.startsWith(BEARER));

        testCredential.replaceAssertion(tokenRequestContext -> DECODED_CLAIMS.equals(tokenRequestContext.getClaims()),
            3);

        // Challenge with claims received
        assertTrue(
            policy.authorizeRequestOnChallengeSync(this.callContext, this.unauthorizedHttpResponseWithHeaderAndClaims));

        String newToken = this.testContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);

        assertFalse(newToken.isEmpty());
        assertTrue(newToken.startsWith(BEARER));

        assertNotEquals(firstToken, newToken);

        KeyVaultCredentialPolicy.clearCache();
    }

    @Test
    public void onAuthorizeRequestChallengeNoCachePresentWithClaimsSync() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        // Challenge with claims received
        assertFalse(
            policy.authorizeRequestOnChallengeSync(this.callContext, this.unauthorizedHttpResponseWithHeaderAndClaims));
        assertNull(this.testContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION));

        KeyVaultCredentialPolicy.clearCache();
    }

    @SyncAsyncTest
    public void onAuthorizeRequestNoCache() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        // No challenge cache to use
        SyncAsyncExtension.execute(() -> policy.authorizeRequestSync(this.callContext),
            () -> policy.authorizeRequest(this.callContext));

        assertNull(this.callContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION));
    }

    @SyncAsyncTest
    public void testSetContentLengthHeader() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        boolean onChallenge = SyncAsyncExtension.execute(
            () -> onChallengeAndClearCacheSync(policy, this.bodyContext, this.unauthorizedHttpResponseWithHeader),
            () -> onChallengeAndClearCache(policy, this.bodyFluxContext, this.unauthorizedHttpResponseWithHeader));

        // Validate that the onChallengeSync ran successfully.
        assertTrue(onChallenge);

        HttpHeaders headers = this.bodyFluxContext.getHttpRequest().getHeaders();
        String tokenValue = headers.getValue(AUTHORIZATION);
        assertFalse(tokenValue.isEmpty());
        assertTrue(tokenValue.startsWith(BEARER));
        assertEquals("21", headers.getValue(HttpHeaderName.CONTENT_LENGTH));

        HttpHeaders syncHeaders = this.bodyContext.getHttpRequest().getHeaders();
        String syncTokenValue = headers.getValue(AUTHORIZATION);
        assertFalse(syncTokenValue.isEmpty());
        assertTrue(syncTokenValue.startsWith(BEARER));
        assertEquals("21", syncHeaders.getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @SyncAsyncTest
    public void onAuthorizeRequestNoScope() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        boolean onChallenge = SyncAsyncExtension.execute(
            () -> onChallengeAndClearCacheSync(policy, this.callContext, this.unauthorizedHttpResponseWithoutHeader),
            () -> onChallengeAndClearCache(policy, this.callContext, this.unauthorizedHttpResponseWithoutHeader));

        assertFalse(onChallenge);
    }

    @Test
    public void onAuthorizeRequestDifferentScope() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        assertThrows(RuntimeException.class, () -> onChallengeAndClearCacheSync(policy, this.differentScopeContext,
            this.unauthorizedHttpResponseWithHeader));

        StepVerifier
            .create(
                onChallengeAndClearCache(policy, this.differentScopeContext, this.unauthorizedHttpResponseWithHeader))
            .verifyErrorMessage("The challenge resource 'https://vault.azure.net/.default' does not match the "
                + "requested domain. If you wish to disable this check for your client, pass 'true' to the "
                + "SecretClientBuilder.disableChallengeResourceVerification() method when building it. See "
                + "https://aka.ms/azsdk/blog/vault-uri for more information.");
    }

    @SyncAsyncTest
    public void onAuthorizeRequestDifferentScopeVerifyFalse() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, true);

        boolean onChallenge = SyncAsyncExtension.execute(
            () -> onChallengeAndClearCacheSync(policy, this.differentScopeContext,
                this.unauthorizedHttpResponseWithHeader),
            () -> onChallengeAndClearCache(policy, this.differentScopeContext,
                this.unauthorizedHttpResponseWithHeader));

        assertTrue(onChallenge);
    }

    // Normal flow: 401 Unauthorized -> 200 OK -> 401 Unauthorized with claims -> 200 OK
    @SyncAsyncTest
    public void processMultipleResponses() {
        MutableTestCredential testCredential = new MutableTestCredential(new ArrayList<>(BASE_ASSERTIONS))
            .addAssertion(tokenRequestContext -> tokenRequestContext.getClaims() == null);
        HttpResponse[] responses = new HttpResponse[] {
            unauthorizedHttpResponseWithHeader,
            simpleResponse,
            unauthorizedHttpResponseWithHeaderAndClaims,
            simpleResponse };
        AtomicInteger currentResponse = new AtomicInteger();
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(testCredential, false);

        HttpPipeline pipeline = new HttpPipelineBuilder().policies(policy)
            .httpClient(ignored -> Mono.just(responses[currentResponse.getAndIncrement()]))
            .build();

        // The first request to a Key Vault endpoint without an access token will always return a 401 Unauthorized
        // response with a WWW-Authenticate header containing an authentication challenge.

        HttpResponse firstResponse = SyncAsyncExtension.execute(
            () -> pipeline.sendSync(this.callContext.getHttpRequest(), this.callContext.getContext()),
            () -> pipeline.send(this.callContext.getHttpRequest(), this.callContext.getContext()));

        String firstToken = this.callContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);

        // The first response was unauthorized and a token was set on the request.
        assertNotNull(firstToken);
        // On a second attempt, a successful response was received.
        assertEquals(simpleResponse, firstResponse);

        testCredential.replaceAssertion(tokenRequestContext -> DECODED_CLAIMS.equals(tokenRequestContext.getClaims()),
            3);

        // On receiving an unauthorized response with claims, the token should be updated and a new attempt to make the
        // original request should be made.

        HttpResponse newResponse = SyncAsyncExtension.execute(
            () -> pipeline.sendSync(this.callContext.getHttpRequest(), this.callContext.getContext()),
            () -> pipeline.send(this.callContext.getHttpRequest(), this.callContext.getContext()));

        String newToken = this.callContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);

        // A new token was fetched using the response with claims and set on the request.
        assertNotNull(newToken);
        // The token was updated.
        assertNotEquals(firstToken, newToken);
        // A subsequent request was successful.
        assertEquals(simpleResponse, newResponse);

        KeyVaultCredentialPolicy.clearCache();
    }

    // Edge case: 401 Unauthorized -> 200 OK -> 401 Unauthorized with claims -> 401 Unauthorized with claims
    @SyncAsyncTest
    public void processConsecutiveResponsesWithClaims() {
        MutableTestCredential testCredential = new MutableTestCredential(new ArrayList<>(BASE_ASSERTIONS))
            .addAssertion(tokenRequestContext -> tokenRequestContext.getClaims() == null);
        HttpResponse[] responses = new HttpResponse[] {
            unauthorizedHttpResponseWithHeader,
            simpleResponse,
            unauthorizedHttpResponseWithHeaderAndClaims,
            // If a second consecutive unauthorized response with claims is received, it shall be returned as is.
            unauthorizedHttpResponseWithHeaderAndClaims, };
        AtomicInteger currentResponse = new AtomicInteger();
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(testCredential, false);

        HttpPipeline pipeline = new HttpPipelineBuilder().policies(policy)
            .httpClient(ignored -> Mono.just(responses[currentResponse.getAndIncrement()]))
            .build();

        // The first request to a Key Vault endpoint without an access token will always return a 401 Unauthorized
        // response with a WWW-Authenticate header containing an authentication challenge.

        HttpResponse firstResponse = SyncAsyncExtension.execute(
            () -> pipeline.sendSync(this.callContext.getHttpRequest(), this.callContext.getContext()),
            () -> pipeline.send(this.callContext.getHttpRequest(), this.callContext.getContext()));

        String firstToken = this.callContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);

        // The first response was unauthorized and a token was set on the request
        assertNotNull(firstToken);
        // On a second attempt, a successful response was received.
        assertEquals(simpleResponse, firstResponse);

        testCredential.replaceAssertion(tokenRequestContext -> DECODED_CLAIMS.equals(tokenRequestContext.getClaims()),
            3);

        HttpResponse newResponse = SyncAsyncExtension.execute(
            () -> pipeline.sendSync(this.callContext.getHttpRequest(), this.callContext.getContext()),
            () -> pipeline.send(this.callContext.getHttpRequest(), this.callContext.getContext()));

        String newToken = this.callContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);

        // A new token was fetched using the first response with claims and set on the request
        assertNotEquals(firstToken, newToken);
        // A subsequent request was unsuccessful.
        assertEquals(unauthorizedHttpResponseWithHeaderAndClaims, newResponse);

        KeyVaultCredentialPolicy.clearCache();
    }

    // Edge case: 401 Unauthorized -> 200 OK -> 401 Unauthorized with claims -> 401 Unauthorized
    @SyncAsyncTest
    public void process401WithoutClaimsAfter401WithClaims() {
        MutableTestCredential testCredential = new MutableTestCredential(new ArrayList<>(BASE_ASSERTIONS))
            .addAssertion(tokenRequestContext -> tokenRequestContext.getClaims() == null);
        HttpResponse[] responses = new HttpResponse[] {
            unauthorizedHttpResponseWithHeader,
            simpleResponse,
            unauthorizedHttpResponseWithHeaderAndClaims,
            // If a second consecutive unauthorized response is received, it shall be returned as is.
            unauthorizedHttpResponseWithHeader };
        AtomicInteger currentResponse = new AtomicInteger();
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(testCredential, false);

        HttpPipeline pipeline = new HttpPipelineBuilder().policies(policy)
            .httpClient(ignored -> Mono.just(responses[currentResponse.getAndIncrement()]))
            .build();

        // The first request to a Key Vault endpoint without an access token will always return a 401 Unauthorized
        // response with a WWW-Authenticate header containing an authentication challenge.

        HttpResponse firstResponse = SyncAsyncExtension.execute(
            () -> pipeline.sendSync(this.callContext.getHttpRequest(), this.callContext.getContext()),
            () -> pipeline.send(this.callContext.getHttpRequest(), this.callContext.getContext()));

        String firstToken = this.callContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);

        // The first response was unauthorized and a token was set on the request
        assertNotNull(firstToken);
        // On a second attempt, a successful response was received.
        assertEquals(simpleResponse, firstResponse);

        testCredential.replaceAssertion(tokenRequestContext -> DECODED_CLAIMS.equals(tokenRequestContext.getClaims()),
            3);

        HttpResponse newResponse = SyncAsyncExtension.execute(
            () -> pipeline.sendSync(this.callContext.getHttpRequest(), this.callContext.getContext()),
            () -> pipeline.send(this.callContext.getHttpRequest(), this.callContext.getContext()));

        String newToken = this.callContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);

        // A new token was fetched using the first response with claims and set on the request
        assertNotEquals(firstToken, newToken);
        // A subsequent request was unsuccessful.
        assertEquals(unauthorizedHttpResponseWithHeader, newResponse);

        KeyVaultCredentialPolicy.clearCache();
    }

    // Edge case: 401 Unauthorized -> 401 Unauthorized with claims -> 200 OK
    @SyncAsyncTest
    public void process401WithClaimsAfter401WithoutClaims() {
        MutableTestCredential testCredential = new MutableTestCredential(new ArrayList<>(BASE_ASSERTIONS));
        final String[] firstToken = new String[1];

        testCredential.addAssertion(tokenRequestContext -> {
            // This will ensure that that the first request does not contains claims, but the second does after
            // receiving a 401 response with a challenge with claims.
            testCredential.replaceAssertion(
                anotherTokenRequestContext -> DECODED_CLAIMS.equals(anotherTokenRequestContext.getClaims()), 3);

            // We will also store the value of the first credential before it changes on a second call
            firstToken[0] = Base64Util.encodeToString(testCredential.getCredential().getBytes(StandardCharsets.UTF_8));

            assertNotNull(firstToken[0]);

            return tokenRequestContext.getClaims() == null;
        });

        HttpResponse[] responses = new HttpResponse[] {
            unauthorizedHttpResponseWithHeader,
            unauthorizedHttpResponseWithHeaderAndClaims,
            simpleResponse };
        AtomicInteger currentResponse = new AtomicInteger();
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(testCredential, false);

        HttpPipeline pipeline = new HttpPipelineBuilder().policies(policy)
            .httpClient(ignored -> Mono.just(responses[currentResponse.getAndIncrement()]))
            .build();

        // The first request to a Key Vault endpoint without an access token will always return a 401 Unauthorized
        // response with a WWW-Authenticate header containing an authentication challenge.

        HttpResponse firstResponse = SyncAsyncExtension.execute(
            () -> pipeline.sendSync(this.callContext.getHttpRequest(), this.callContext.getContext()),
            () -> pipeline.send(this.callContext.getHttpRequest(), this.callContext.getContext()));

        String newToken = this.callContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);

        // The first unauthorized response caused a token to be set on the request, then the token was updated on a
        // subsequent unauthorized response with claims.
        assertNotEquals(firstToken[0], newToken);
        // Finally, a successful response was received.
        assertEquals(simpleResponse, firstResponse);

        KeyVaultCredentialPolicy.clearCache();
    }

    private Mono<Boolean> onChallengeAndClearCache(KeyVaultCredentialPolicy policy, HttpPipelineCallContext callContext,
        HttpResponse unauthorizedHttpResponse) {
        Mono<Boolean> onChallenge = policy.authorizeRequestOnChallenge(callContext, unauthorizedHttpResponse);

        KeyVaultCredentialPolicy.clearCache();

        return onChallenge;
    }

    private boolean onChallengeAndClearCacheSync(KeyVaultCredentialPolicy policy, HttpPipelineCallContext callContext,
        HttpResponse unauthorizedHttpResponse) {
        boolean onChallengeSync = policy.authorizeRequestOnChallengeSync(callContext, unauthorizedHttpResponse);

        KeyVaultCredentialPolicy.clearCache();

        return onChallengeSync;
    }

    private static final class MutableTestCredential implements TokenCredential {
        private String credential;
        private List<Function<TokenRequestContext, Boolean>> assertions;

        private MutableTestCredential(List<Function<TokenRequestContext, Boolean>> assertions) {
            this.credential = new Random().toString();
            this.assertions = assertions;
        }

        /**
         * @throws RuntimeException if any of the assertions fail.
         */
        @Override
        public Mono<AccessToken> getToken(TokenRequestContext requestContext) {
            if (requestContext.isCaeEnabled() && requestContext.getClaims() != null) {
                credential = new Random().toString();
            }

            String encodedCredential = Base64Util.encodeToString(credential.getBytes(StandardCharsets.UTF_8));

            for (int i = 0; i < assertions.size(); i++) {
                if (!assertions.get(i).apply(requestContext)) {
                    return Mono.error(new RuntimeException(String.format("Assertion number %d failed", i)));
                }
            }

            return Mono.fromCallable(() -> new AccessToken(encodedCredential, OffsetDateTime.MAX.minusYears(1)));
        }

        private MutableTestCredential setAssertions(List<Function<TokenRequestContext, Boolean>> assertions) {
            this.assertions = assertions;

            return this;
        }

        private MutableTestCredential addAssertion(Function<TokenRequestContext, Boolean> assertion) {
            assertions.add(assertion);

            return this;
        }

        private MutableTestCredential replaceAssertion(Function<TokenRequestContext, Boolean> assertion, int index) {
            assertions.set(index, assertion);

            return this;
        }

        private String getCredential() {
            return this.credential;
        }
    }
}
