package com.azure.security.keyvault.administration;

import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.security.keyvault.administration.implementation.KeyVaultCredentialPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Execution(ExecutionMode.SAME_THREAD)
public class KeyVaultCredentialPolicyTest {
    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String AUTHENTICATE_HEADER = "Bearer authorization=\"https://login.windows.net/72f988bf-86f1-41af-91ab-2d7cd022db57\", resource=\"https://kvtest.azure.net\"";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer";
    private static final String BODY = "this is a sample body";
    private static final Flux<ByteBuffer> BODY_FLUX = Flux.defer(() ->
        Flux.fromStream(
            Stream.of(BODY.split(""))
                .map(s -> ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)))
        ));
    private BasicAuthenticationCredential credential;
    private HttpResponse unauthorizedHttpResponseWithHeader;
    private HttpResponse unauthorizedHttpResponseWithoutHeader;
    private HttpPipelineCallContext callContext;
    private HttpPipelineCallContext testContext;
    private HttpPipelineCallContext bodyContext;
    private HttpPipelineCallContext bodyFluxContext;

    @BeforeEach
    public void setup() {
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://mytest.azurecr.io");

        HttpPipelineCallContext plainContext = mock(HttpPipelineCallContext.class);
        when(plainContext.getHttpRequest()).thenReturn(request);

        HttpPipelineCallContext testContext = mock(HttpPipelineCallContext.class);
        when(testContext.getHttpRequest()).thenReturn(request);

        HttpPipelineCallContext bodyContext = mock(HttpPipelineCallContext.class);
        when(bodyContext.getHttpRequest()).thenReturn(request);
        when(bodyContext.getData("KeyVaultCredentialPolicyStashedBody")).thenReturn(Optional.of(BODY));
        when(bodyContext.getData("KeyVaultCredentialPolicyStashedContentLength")).thenReturn(Optional.of("21"));

        HttpPipelineCallContext bodyFluxContext = mock(HttpPipelineCallContext.class);
        when(bodyFluxContext.getHttpRequest()).thenReturn(request);
        when(bodyFluxContext.getData("KeyVaultCredentialPolicyStashedBody")).thenReturn(Optional.of(BODY_FLUX));
        when(bodyFluxContext.getData("KeyVaultCredentialPolicyStashedContentLength")).thenReturn(Optional.of("21"));

        MockHttpResponse unauthorizedResponseWithHeader = new MockHttpResponse(
            mock(HttpRequest.class),
            500,
            new HttpHeaders().set(WWW_AUTHENTICATE, AUTHENTICATE_HEADER)
        );

        MockHttpResponse unauthorizedResponseWithoutHeader = new MockHttpResponse(
            mock(HttpRequest.class),
            500);

        this.unauthorizedHttpResponseWithHeader = unauthorizedResponseWithHeader;
        this.unauthorizedHttpResponseWithoutHeader = unauthorizedResponseWithoutHeader;
        this.callContext = plainContext;
        this.credential = new BasicAuthenticationCredential("user", "pass");
        this.testContext = testContext;
        this.bodyContext = bodyContext;
        this.bodyFluxContext = bodyFluxContext;
    }

    @AfterEach
    public void cleanup() {
        KeyVaultCredentialPolicy.clearCache();
    }

    @Test
    public void onChallengeCredentialPolicy() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential);
        boolean onChallenge = policy.authorizeRequestOnChallenge(this.callContext, this.unauthorizedHttpResponseWithHeader).block();

        // Validate that the onChallenge ran successfully.
        assertTrue(onChallenge);

        String tokenValue = this.callContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);
        assertFalse(tokenValue.isEmpty());
        assertTrue(tokenValue.startsWith(BEARER));
    }

    @Test
    public void onAuthorizeRequestChallengeCachePresent() throws MalformedURLException {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential);

        // Challenge cache created
        policy.authorizeRequestOnChallenge(this.callContext, unauthorizedHttpResponseWithHeader).block();
        // Challenge cache used
        policy.authorizeRequest(this.testContext).block();

        String tokenValue = this.testContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);
        assertFalse(tokenValue.isEmpty());
        assertTrue(tokenValue.startsWith(BEARER));
    }

    @Test
    public void onAuthorizeRequestNoCache() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential);

        // No challenge cache to use
        policy.authorizeRequest(this.callContext).block();

        assertNull(this.callContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION));
    }

    @Test
    public void testSetContentLengthHeader() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential);

        boolean onChallenge = policy.authorizeRequestOnChallenge(this.bodyFluxContext, this.unauthorizedHttpResponseWithHeader).block();

        // Validate that the onChallenge ran successfully.
        assertTrue(onChallenge);

        HttpHeaders headers = this.bodyFluxContext.getHttpRequest().getHeaders();
        String tokenValue = headers.getValue(AUTHORIZATION);
        assertFalse(tokenValue.isEmpty());
        assertTrue(tokenValue.startsWith(BEARER));
        assertEquals("21", headers.getValue("content-length"));
    }

    @Test
    public void onAuthorizeRequestNoScope() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential);
        boolean onChallenge = policy.authorizeRequestOnChallenge(this.callContext, this.unauthorizedHttpResponseWithoutHeader).block();
        assertFalse(onChallenge);
    }

    @Test
    public void onChallengeCredentialPolicyTestSync() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential);
        boolean onChallenge = policy.authorizeRequestOnChallengeSync(this.callContext, this.unauthorizedHttpResponseWithHeader);

        // Validate that the onChallenge ran successfully.
        assertTrue(onChallenge);

        String tokenValue = this.callContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);
        assertFalse(tokenValue.isEmpty());
        assertTrue(tokenValue.startsWith(BEARER));
    }

    @Test
    public void onAuthorizeRequestChallengeCachePresentSync() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential);

        // Challenge cache created
        policy.authorizeRequestOnChallengeSync(this.callContext, unauthorizedHttpResponseWithHeader);
        // Challenge cache used
        policy.authorizeRequestSync(this.testContext);

        String tokenValue = this.testContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION);
        assertFalse(tokenValue.isEmpty());
        assertTrue(tokenValue.startsWith(BEARER));
    }

    @Test
    public void onAuthorizeRequestNoCacheSync() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential);

        // No challenge cache to use
        policy.authorizeRequestSync(this.callContext);

        assertNull(this.callContext.getHttpRequest().getHeaders().getValue(AUTHORIZATION));
    }

    @Test
    public void testSetContentLengthHeaderSync() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential);

        boolean onChallenge = policy.authorizeRequestOnChallengeSync(this.bodyContext, this.unauthorizedHttpResponseWithHeader);

        // Validate that the onChallenge ran successfully.
        assertTrue(onChallenge);

        HttpHeaders headers = this.bodyContext.getHttpRequest().getHeaders();
        String tokenValue = headers.getValue(AUTHORIZATION);
        assertFalse(tokenValue.isEmpty());
        assertTrue(tokenValue.startsWith(BEARER));
        assertEquals("21", headers.getValue("content-length"));
    }

    @Test
    public void onAuthorizeRequestNoScopeNoCacheSync() {
        KeyVaultCredentialPolicy policy = new KeyVaultCredentialPolicy(this.credential);
        boolean onChallenge = policy.authorizeRequestOnChallengeSync(this.callContext, this.unauthorizedHttpResponseWithoutHeader);
        assertFalse(onChallenge);
    }
}
