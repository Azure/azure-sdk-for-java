// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.BasicAuthenticationCredential;
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
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Base64Util;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.azure.core.http.HttpHeaderName.AUTHORIZATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class KeyVaultCredentialPolicyTest {
    private static final String AUTHENTICATE_HEADER =
        "Bearer authorization=\"https://login.windows.net/72f988bf-86f1-41af-91ab-2d7cd022db57\", "
            + "resource=\"https://vault.azure.net\"";
    private static final String AUTHENTICATE_HEADER_WITH_CLAIMS =
        "Bearer realm=\"\", authorization_uri=\"https://login.microsoftonline.com/common/oauth2/authorize\", "
            + "error=\"insufficient_claims\", "
            + "claims=\"eyJhY2Nlc3NfdG9rZW4iOnsiYWNycyI6eyJlc3NlbnRpYWwiOnRydWUsInZhbHVlIjoiY3AxIn19fQ==\"";
    private static final String BEARER = "Bearer";
    private static final String BODY = "this is a sample body";
    private static final Flux<ByteBuffer> BODY_FLUX = Flux.defer(() ->
        Flux.fromStream(Stream.of(BODY.split("")).map(s -> ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)))));

    private HttpResponse unauthorizedHttpResponseWithWrongStatusCode;
    private HttpResponse unauthorizedHttpResponseWithHeader;
    private HttpResponse unauthorizedHttpResponseWithoutHeader;
    private HttpResponse unauthorizedHttpResponseWithHeaderAndClaims;
    private HttpPipelineCallContext callContext;
    private HttpPipelineCallContext differentScopeContext;
    private HttpPipelineCallContext testContext;
    private HttpPipelineCallContext bodyContext;
    private HttpPipelineCallContext bodyFluxContext;
    private BasicAuthenticationCredential credential;

    private static HttpPipelineCallContext createCallContext(HttpRequest request, Context context) {
        AtomicReference<HttpPipelineCallContext> callContextReference = new AtomicReference<>();

        HttpPipeline callContextCreator = new HttpPipelineBuilder()
            .policies((callContext, next) -> {
                callContextReference.set(callContext);

                return next.process();
            })
            .httpClient(ignored -> Mono.empty())
            .build();

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

        MockHttpResponse unauthorizedResponseWithWrongStatusCode = new MockHttpResponse(
            new HttpRequest(HttpMethod.GET, "https://azure.com"), 500);

        MockHttpResponse unauthorizedResponseWithoutHeader = new MockHttpResponse(
            new HttpRequest(HttpMethod.GET, "https://azure.com"), 401);

        MockHttpResponse unauthorizedResponseWithHeader = new MockHttpResponse(
            new HttpRequest(HttpMethod.GET, "https://azure.com"), 401,
            new HttpHeaders().set(HttpHeaderName.WWW_AUTHENTICATE, AUTHENTICATE_HEADER));

        MockHttpResponse unauthorizedResponseWithHeaderAndClaims = new MockHttpResponse(
            new HttpRequest(HttpMethod.GET, "https://azure.com"), 401,
            new HttpHeaders().set(HttpHeaderName.WWW_AUTHENTICATE, AUTHENTICATE_HEADER_WITH_CLAIMS));

        this.unauthorizedHttpResponseWithWrongStatusCode = unauthorizedResponseWithWrongStatusCode;
        this.unauthorizedHttpResponseWithHeader = unauthorizedResponseWithHeader;
        this.unauthorizedHttpResponseWithoutHeader = unauthorizedResponseWithoutHeader;
        this.unauthorizedHttpResponseWithHeaderAndClaims = unauthorizedResponseWithHeaderAndClaims;
        this.callContext = createCallContext(request, Context.NONE);
        this.differentScopeContext = createCallContext(requestWithDifferentScope, Context.NONE);
        this.testContext = createCallContext(request, Context.NONE);
        this.bodyContext = createCallContext(request, bodyContextContext);
        this.bodyFluxContext = createCallContext(request, bodyFluxContextContext);
        this.credential = new BasicAuthenticationCredential("user", "fakePasswordPlaceholder");
    }

    @AfterEach
    public void cleanup() {
        KeyVaultCredentialPolicy.clearCache();
    }

    @SyncAsyncTest
    public void onNon401ErrorResponse() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policy)
            .httpClient(ignored -> Mono.just(unauthorizedHttpResponseWithWrongStatusCode))
            .build();

        SyncAsyncExtension.execute(
            () -> pipeline.sendSync(this.callContext.getHttpRequest(), this.callContext.getContext()),
            () -> pipeline.send(this.callContext.getHttpRequest(), this.callContext.getContext())
        );

        assertNull(this.callContext.getHttpRequest().getHeaders().get(AUTHORIZATION));

        KeyVaultCredentialPolicy.clearCache();
    }

    @SyncAsyncTest
    public void on401UnauthorizedResponseWithHeader() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policy)
            .httpClient(ignored -> Mono.just(unauthorizedHttpResponseWithHeader))
            .build();

        SyncAsyncExtension.execute(
            () -> pipeline.sendSync(this.callContext.getHttpRequest(), this.callContext.getContext()),
            () -> pipeline.send(this.callContext.getHttpRequest(), this.callContext.getContext())
        );

        assertNotNull(this.callContext.getHttpRequest().getHeaders().get(AUTHORIZATION));

        KeyVaultCredentialPolicy.clearCache();
    }

    @SyncAsyncTest
    public void onChallengeCredentialPolicy() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        boolean onChallenge = SyncAsyncExtension.execute(
            () -> onChallengeAndClearCacheSync(policy, this.callContext, this.unauthorizedHttpResponseWithHeader),
            () -> onChallengeAndClearCache(policy, this.callContext, this.unauthorizedHttpResponseWithHeader)
        );

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
        MutableTestCredential testCredential = new MutableTestCredential();
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(testCredential, false);

        StepVerifier.create(policy.authorizeRequestOnChallenge(this.callContext, // Challenge cache created
                    this.unauthorizedHttpResponseWithHeader)
                .flatMap(authorized -> {
                    if (authorized) {
                        String firstToken = this.testContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);

                        assertFalse(firstToken.isEmpty());
                        assertTrue(firstToken.startsWith(BEARER));

                        return policy.authorizeRequestOnChallenge(this.callContext, // Challenge with claims received
                                this.unauthorizedHttpResponseWithHeaderAndClaims)
                            .map(ignored -> firstToken);
                    } else {
                        return Mono.just("");
                    }
                }))
            .assertNext(firstToken -> {
                String newToken = this.testContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);

                assertFalse(newToken.isEmpty());
                assertTrue(newToken.startsWith(BEARER));

                assertNotEquals(firstToken, newToken);
            })
            .verifyComplete();

        KeyVaultCredentialPolicy.clearCache();
    }

    @Test
    public void onAuthorizeRequestChallengeNoCachePresentWithClaims() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        StepVerifier.create(policy.authorizeRequestOnChallenge(this.callContext, // Challenge cache created
                this.unauthorizedHttpResponseWithHeaderAndClaims))
            .assertNext(result -> {
                assertFalse(result);
                assertNull(this.testContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION));
            })
            .verifyComplete();

        KeyVaultCredentialPolicy.clearCache();
    }

    @Test
    public void onAuthorizeRequestChallengeCachePresentWithClaimsSync() {
        MutableTestCredential testCredential = new MutableTestCredential();
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(testCredential, false);

        // Challenge cache created
        assertTrue(policy.authorizeRequestOnChallengeSync(this.callContext, this.unauthorizedHttpResponseWithHeader));

        String firstToken = this.testContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);

        assertFalse(firstToken.isEmpty());
        assertTrue(firstToken.startsWith(BEARER));

        // Challenge with claims received
        assertTrue(policy.authorizeRequestOnChallengeSync(this.callContext,
            this.unauthorizedHttpResponseWithHeaderAndClaims));

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
        assertFalse(policy.authorizeRequestOnChallengeSync(this.callContext,
            this.unauthorizedHttpResponseWithHeaderAndClaims));
        assertNull(this.testContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION));

        KeyVaultCredentialPolicy.clearCache();
    }

    @SyncAsyncTest
    public void onAuthorizeRequestNoCache() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        // No challenge cache to use
        SyncAsyncExtension.execute(
            () -> policy.authorizeRequestSync(this.callContext),
            () -> policy.authorizeRequest(this.callContext)
        );

        assertNull(this.callContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION));
    }

    @SyncAsyncTest
    public void testSetContentLengthHeader() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        boolean onChallenge = SyncAsyncExtension.execute(
            () -> onChallengeAndClearCacheSync(policy, this.bodyContext, this.unauthorizedHttpResponseWithHeader),
            () -> onChallengeAndClearCache(policy, this.bodyFluxContext, this.unauthorizedHttpResponseWithHeader)
        );

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
            () -> onChallengeAndClearCache(policy, this.callContext, this.unauthorizedHttpResponseWithoutHeader)
        );

        assertFalse(onChallenge);
    }

    @Test
    public void onAuthorizeRequestDifferentScope() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        assertThrows(RuntimeException.class,
            () -> onChallengeAndClearCacheSync(policy, this.differentScopeContext,
                this.unauthorizedHttpResponseWithHeader));

        StepVerifier.create(onChallengeAndClearCache(policy, this.differentScopeContext,
                this.unauthorizedHttpResponseWithHeader))
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
                this.unauthorizedHttpResponseWithHeader)
        );

        assertTrue(onChallenge);
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

    private static class MutableTestCredential implements TokenCredential {
        private String credential;

        public MutableTestCredential() {
            this.credential = new Random().toString();
        }

        /**
         * @throws RuntimeException If the UTF-8 encoding isn't supported.
         */
        @Override
        public Mono<AccessToken> getToken(TokenRequestContext request) {
            if (request.isCaeEnabled() && request.getClaims() != null) {
                credential = new Random().toString();
            }

            String encodedCredential = Base64Util.encodeToString(credential.getBytes(StandardCharsets.UTF_8));

            return Mono.fromCallable(() -> new AccessToken(encodedCredential, OffsetDateTime.MAX));
        }
    }
}
