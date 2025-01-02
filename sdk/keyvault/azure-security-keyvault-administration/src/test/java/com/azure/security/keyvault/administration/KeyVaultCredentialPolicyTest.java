// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

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
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Base64Util;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.security.keyvault.administration.implementation.KeyVaultCredentialPolicy;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
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
