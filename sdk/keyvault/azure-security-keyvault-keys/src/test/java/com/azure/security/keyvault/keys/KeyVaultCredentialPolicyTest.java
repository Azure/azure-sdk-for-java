// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.credential.BasicAuthenticationCredential;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class KeyVaultCredentialPolicyTest {
    private static final String AUTHENTICATE_HEADER =
        "Bearer authorization=\"https://login.windows.net/72f988bf-86f1-41af-91ab-2d7cd022db57\", "
            + "resource=\"https://vault.azure.net\"";
    private static final String BEARER = "Bearer";
    private static final String BODY = "this is a sample body";
    private static final Flux<ByteBuffer> BODY_FLUX = Flux.defer(() ->
        Flux.fromStream(Stream.of(BODY.split("")).map(s -> ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)))));
    private BasicAuthenticationCredential credential;
    private HttpResponse unauthorizedHttpResponseWithHeader;
    private HttpResponse unauthorizedHttpResponseWithoutHeader;
    private HttpPipelineCallContext callContext;
    private HttpPipelineCallContext differentScopeContext;
    private HttpPipelineCallContext testContext;
    private HttpPipelineCallContext bodyContext;
    private HttpPipelineCallContext bodyFluxContext;

    private static HttpPipelineCallContext createCallContext(HttpRequest request, Context context) {
        AtomicReference<HttpPipelineCallContext> callContextReference = new AtomicReference<>();

        HttpPipeline callContextCreator = new HttpPipelineBuilder()
            .policies((callContext, next) -> {
                callContextReference.set(callContext);
                return next.process();
            })
            .httpClient(ignored -> Mono.empty())
            .build();

        callContextCreator.send(request, context).block();

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

        MockHttpResponse unauthorizedResponseWithHeader = new MockHttpResponse(
            new HttpRequest(HttpMethod.GET, "https://azure.com"), 500,
            new HttpHeaders().set(HttpHeaderName.WWW_AUTHENTICATE, AUTHENTICATE_HEADER));

        MockHttpResponse unauthorizedResponseWithoutHeader = new MockHttpResponse(
            new HttpRequest(HttpMethod.GET, "https://azure.com"), 500);

        this.unauthorizedHttpResponseWithHeader = unauthorizedResponseWithHeader;
        this.unauthorizedHttpResponseWithoutHeader = unauthorizedResponseWithoutHeader;
        this.callContext = createCallContext(request, Context.NONE);
        this.differentScopeContext = createCallContext(requestWithDifferentScope, Context.NONE);
        this.credential = new BasicAuthenticationCredential("user", "fakePasswordPlaceholder");
        this.testContext = createCallContext(request, Context.NONE);
        this.bodyContext = createCallContext(request, bodyContextContext);
        this.bodyFluxContext = createCallContext(request, bodyFluxContextContext);

    }

    @AfterEach
    public void cleanup() {
        KeyVaultCredentialPolicy.clearCache();
    }

    @SyncAsyncTest
    public void onChallengeCredentialPolicy() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        boolean onChallenge = SyncAsyncExtension.execute(
            () -> onChallengeSync(policy, this.callContext, this.unauthorizedHttpResponseWithHeader),
            () -> onChallenge(policy, this.callContext, this.unauthorizedHttpResponseWithHeader)
        );

        // Validate that the onChallengeSync ran successfully.
        assertTrue(onChallenge);

        String tokenValue = this.callContext.getHttpRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        assertFalse(tokenValue.isEmpty());
        assertTrue(tokenValue.startsWith(BEARER));
    }

    @Test
    public void onAuthorizeRequestChallengeCachePresent() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        // Challenge cache created
        onChallenge(policy, this.callContext, unauthorizedHttpResponseWithHeader).block();
        // Challenge cache used
        policy.authorizeRequest(this.testContext).block();

        String tokenValue = this.testContext.getHttpRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        assertFalse(tokenValue.isEmpty());
        assertTrue(tokenValue.startsWith(BEARER));
    }

    @SyncAsyncTest
    public void onAuthorizeRequestNoCache() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        // No challenge cache to use
        SyncAsyncExtension.execute(
            () -> policy.authorizeRequestSync(this.callContext),
            () -> policy.authorizeRequest(this.callContext)
        );

        assertNull(this.callContext.getHttpRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
    }

    @SyncAsyncTest
    public void testSetContentLengthHeader() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        boolean onChallenge = SyncAsyncExtension.execute(
            () -> onChallengeSync(policy, this.bodyContext, this.unauthorizedHttpResponseWithHeader),
            () -> onChallenge(policy, this.bodyFluxContext, this.unauthorizedHttpResponseWithHeader)
        );

        // Validate that the onChallengeSync ran successfully.
        assertTrue(onChallenge);

        HttpHeaders headers = this.bodyFluxContext.getHttpRequest().getHeaders();
        String tokenValue = headers.getValue(HttpHeaderName.AUTHORIZATION);
        assertFalse(tokenValue.isEmpty());
        assertTrue(tokenValue.startsWith(BEARER));
        assertEquals("21", headers.getValue(HttpHeaderName.CONTENT_LENGTH));

        HttpHeaders syncHeaders = this.bodyContext.getHttpRequest().getHeaders();
        String syncTokenValue = headers.getValue(HttpHeaderName.AUTHORIZATION);
        assertFalse(syncTokenValue.isEmpty());
        assertTrue(syncTokenValue.startsWith(BEARER));
        assertEquals("21", syncHeaders.getValue(HttpHeaderName.CONTENT_LENGTH));
    }

    @SyncAsyncTest
    public void onAuthorizeRequestNoScope() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        boolean onChallenge = SyncAsyncExtension.execute(
            () -> onChallengeSync(policy, this.callContext, this.unauthorizedHttpResponseWithoutHeader),
            () -> onChallenge(policy, this.callContext, this.unauthorizedHttpResponseWithoutHeader)
        );

        assertFalse(onChallenge);
    }

    @Test
    public void onAuthorizeRequestDifferentScope() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        assertThrows(RuntimeException.class,
            () -> onChallengeSync(policy, this.differentScopeContext, this.unauthorizedHttpResponseWithHeader));

        StepVerifier.create(onChallenge(policy, this.differentScopeContext, this.unauthorizedHttpResponseWithHeader))
            .verifyErrorMessage("The challenge resource 'https://vault.azure.net/.default' does not match the "
                + "requested domain. If you wish to disable this check for your client, pass 'true' to the "
                + "SecretClientBuilder.disableChallengeResourceVerification() method when building it. See "
                + "https://aka.ms/azsdk/blog/vault-uri for more information.");
    }

    @SyncAsyncTest
    public void onAuthorizeRequestDifferentScopeVerifyFalse() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, true);

        boolean onChallenge = SyncAsyncExtension.execute(
            () -> onChallengeSync(policy, this.differentScopeContext, this.unauthorizedHttpResponseWithHeader),
            () -> onChallenge(policy, this.differentScopeContext, this.unauthorizedHttpResponseWithHeader)
        );

        assertTrue(onChallenge);
    }

    @Test
    public void onAuthorizeRequestChallengeCachePresentSync() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential, false);

        // Challenge cache created
        onChallengeSync(policy, this.callContext, unauthorizedHttpResponseWithHeader);
        // Challenge cache used
        policy.authorizeRequestSync(this.testContext);

        String tokenValue = this.testContext.getHttpRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        assertFalse(tokenValue.isEmpty());
        assertTrue(tokenValue.startsWith(BEARER));
    }

    private Mono<Boolean> onChallenge(KeyVaultCredentialPolicy policy, HttpPipelineCallContext callContext,
                                      HttpResponse unauthorizedHttpResponse) {
        Mono<Boolean> onChallenge = policy.authorizeRequestOnChallenge(callContext, unauthorizedHttpResponse);
        KeyVaultCredentialPolicy.clearCache();
        return onChallenge;
    }

    private boolean onChallengeSync(KeyVaultCredentialPolicy policy, HttpPipelineCallContext callContext,
                                    HttpResponse unauthorizedHttpResponse) {
        boolean onChallengeSync = policy.authorizeRequestOnChallengeSync(callContext, unauthorizedHttpResponse);
        KeyVaultCredentialPolicy.clearCache();
        return onChallengeSync;
    }
}
