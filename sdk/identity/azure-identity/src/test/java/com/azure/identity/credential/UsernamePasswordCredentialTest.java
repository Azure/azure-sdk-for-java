// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.TokenRequest;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.util.TestUtils;
import com.microsoft.aad.msal4j.MsalServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.azure.identity.*")
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
public class UsernamePasswordCredentialTest {

    private final String clientId = UUID.randomUUID().toString();

    @Test
    public void testValidUserCredential() throws Exception {
        // setup
        String username = "testuser";
        String password = "P@ssw0rd";
        String token1 = "token1";
        String token2 = "token2";
        TokenRequest request1 = new TokenRequest().addScopes("https://management.azure.com");
        TokenRequest request2 = new TokenRequest().addScopes("https://vault.azure.net");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithUsernamePassword(request1, username, password)).thenReturn(TestUtils.getMockMsalToken(token1, expiresOn));
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
        UsernamePasswordCredential credential =
            new UsernamePasswordCredentialBuilder().clientId(clientId).username(username).password(password).build();
        StepVerifier.create(credential.getToken(request1))
            .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                && expiresOn.getSecond() == accessToken.getExpiresOn().getSecond())
            .verifyComplete();
        StepVerifier.create(credential.getToken(request2))
            .expectNextMatches(accessToken -> token2.equals(accessToken.getToken())
                && expiresOn.getSecond() == accessToken.getExpiresOn().getSecond())
            .verifyComplete();
    }

    @Test
    public void testInvalidUserCredential() throws Exception {
        // setup
        String username = "testuser";
        String badPassword = "Password";
        TokenRequest request = new TokenRequest().addScopes("https://management.azure.com");

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithUsernamePassword(request, username, badPassword)).thenThrow(new MsalServiceException("bad credential", "BadCredential"));
        when(identityClient.authenticateWithUserRefreshToken(any(), any()))
            .thenAnswer(invocation -> Mono.error(new UnsupportedOperationException("nothing cached")));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        UsernamePasswordCredential credential =
            new UsernamePasswordCredentialBuilder().clientId(clientId).username(username).password(badPassword).build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(t -> t instanceof MsalServiceException && "bad credential".equals(t.getMessage()))
            .verify();
    }

    @Test
    public void testInvalidParameters() throws Exception {
        // setup
        String username = "testuser";
        String password = "P@ssw0rd";
        String token1 = "token1";
        TokenRequest request = new TokenRequest().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithUsernamePassword(request, username, password)).thenReturn(TestUtils.getMockMsalToken(token1, expiresOn));
        when(identityClient.authenticateWithUserRefreshToken(any(), any()))
            .thenAnswer(invocation -> Mono.error(new UnsupportedOperationException("nothing cached")));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        UsernamePasswordCredential credential = new UsernamePasswordCredentialBuilder().username(username).password(password).build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof IllegalArgumentException && e.getMessage().contains("clientId"))
            .verify();

        credential =
            new UsernamePasswordCredentialBuilder().clientId(clientId).username(username).build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof IllegalArgumentException && e.getMessage().contains("password"))
            .verify();

        credential =
            new UsernamePasswordCredentialBuilder().clientId(clientId).password(password).build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(e -> e instanceof IllegalArgumentException && e.getMessage().contains("username"))
            .verify();
    }
}
