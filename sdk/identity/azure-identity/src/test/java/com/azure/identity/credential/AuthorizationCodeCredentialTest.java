// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credential.TokenRequest;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.util.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.azure.identity.*")
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
public class AuthorizationCodeCredentialTest {

    private final String clientId = UUID.randomUUID().toString();

    @Test
    public void testValidAuthorizationCode() throws Exception {
        // setup
        String token1 = "token1";
        String token2 = "token2";
        TokenRequest request1 = new TokenRequest().addScopes("https://management.azure.com");
        TokenRequest request2 = new TokenRequest().addScopes("https://vault.azure.net");
        String authCode1 = "authCode1";
        URI redirectUri = new URI("http://foo.com/bar");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithAuthorizationCode(eq(request1), eq(authCode1), eq(redirectUri)))
            .thenReturn(TestUtils.getMockMsalToken(token1, expiresOn));
        when(identityClient.authenticateWithUserRefreshToken(any(), any()))
            .thenAnswer(invocation -> {
                TokenRequest argument = (TokenRequest) invocation.getArguments()[0];
                if (argument.getScopes().size() == 1 && argument.getScopes().get(0).equals(request2.getScopes().get(0))) {
                    return TestUtils.getMockMsalToken(token2, expiresOn);
                } else if (argument.getScopes().size() == 1 && argument.getScopes().get(0).equals(request1.getScopes().get(0))) {
                    return Mono.error(new UnsupportedOperationException("nothing cached"));
                } else {
                    throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                }
            });
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        AuthorizationCodeCredential credential = new AuthorizationCodeCredentialBuilder()
                .clientId(clientId).authorizationCode(authCode1).redirectUri(redirectUri.toString()).build();
        StepVerifier.create(credential.getToken(request1))
            .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                && expiresOn.getSecond() == accessToken.getExpiresOn().getSecond())
            .verifyComplete();
        StepVerifier.create(credential.getToken(request2))
            .expectNextMatches(accessToken -> token2.equals(accessToken.getToken())
                && expiresOn.getSecond() == accessToken.getExpiresOn().getSecond())
            .verifyComplete();
    }
}
