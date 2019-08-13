// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.util.TestUtils;
import com.microsoft.aad.msal4j.MsalServiceException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.Assert.fail;
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
        String[] scopes1 = new String[] { "https://management.azure.com" };
        String[] scopes2 = new String[] { "https://vault.azure.net" };
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithUsernamePassword(scopes1, username, password)).thenReturn(TestUtils.getMockMsalToken(token1, expiresOn));
        when(identityClient.authenticateWithUserRefreshToken(any(), any()))
            .thenAnswer(invocation -> {
                String[] argument = (String[]) invocation.getArguments()[0];
                if (argument.length == 1 && argument[0].equals(scopes2[0])) {
                    return TestUtils.getMockMsalToken(token2, expiresOn);
                } else if (argument.length == 1 && argument[0].equals(scopes1[0])) {
                    return Mono.error(new UnsupportedOperationException("nothing cached"));
                } else {
                    throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                }
            });
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        UsernamePasswordCredential credential = new UsernamePasswordCredentialBuilder().clientId(clientId).username(username).password(password).build();
        AccessToken token = credential.getToken(scopes1).block();
        Assert.assertEquals(token1, token.token());
        Assert.assertEquals(expiresOn.getSecond(), token.expiresOn().getSecond());
        token = credential.getToken(scopes2).block();
        Assert.assertEquals(token2, token.token());
        Assert.assertEquals(expiresOn.getSecond(), token.expiresOn().getSecond());
    }

    @Test
    public void testInvalidUserCredential() throws Exception {
        // setup
        String username = "testuser";
        String badPassword = "Password";
        String[] scopes = new String[] { "https://management.azure.com" };

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithUsernamePassword(scopes, username, badPassword)).thenThrow(new MsalServiceException("bad credential", "BadCredential"));
        when(identityClient.authenticateWithUserRefreshToken(any(), any()))
            .thenAnswer(invocation -> Mono.error(new UnsupportedOperationException("nothing cached")));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        try {
            UsernamePasswordCredential credential = new UsernamePasswordCredentialBuilder().clientId(clientId).username(username).password(badPassword).build();
            credential.getToken(scopes).block();
            fail();
        } catch (MsalServiceException e) {
            Assert.assertEquals("bad credential", e.getMessage());
        }
    }

    @Test
    public void testInvalidParameters() throws Exception {
        // setup
        String username = "testuser";
        String password = "P@ssw0rd";
        String token1 = "token1";
        String[] scopes = new String[] { "https://management.azure.com" };
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithUsernamePassword(scopes, username, password)).thenReturn(TestUtils.getMockMsalToken(token1, expiresOn));
        when(identityClient.authenticateWithUserRefreshToken(any(), any()))
            .thenAnswer(invocation -> Mono.error(new UnsupportedOperationException("nothing cached")));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        try {
            UsernamePasswordCredential credential = new UsernamePasswordCredentialBuilder().username(username).password(password).build();
            credential.getToken(scopes).block();
            fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("clientId"));
        }
        try {
            UsernamePasswordCredential credential = new UsernamePasswordCredentialBuilder().clientId(clientId).username(username).build();
            credential.getToken(scopes).block();
            fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("password"));
        }
        try {
            UsernamePasswordCredential credential = new UsernamePasswordCredentialBuilder().clientId(clientId).password(password).build();
            credential.getToken(scopes).block();
            fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("username"));
        }
    }
}
