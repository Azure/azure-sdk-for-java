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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.azure.identity.*")
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
public class UserCredentialTest {

    private final String tenantId = "contoso.com";
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
        when(identityClient.authenticateWithUsernamePassword(scopes1, username, password)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
        when(identityClient.authenticateWithUsernamePassword(scopes2, username, password)).thenReturn(TestUtils.getMockAccessToken(token2, expiresOn));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        UserCredential credential = new UserCredentialBuilder().tenantId(tenantId).clientId(clientId).username(username).password(password).build();
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
        String password = "P@ssw0rd";
        String badPassword = "Password";
        String token1 = "token1";
        String[] scopes = new String[] { "https://management.azure.com" };
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithUsernamePassword(scopes, username, password)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
        when(identityClient.authenticateWithUsernamePassword(scopes, username, badPassword)).thenThrow(new MsalServiceException("bad credential", "BadCredential"));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        UserCredential credential = new UserCredentialBuilder().tenantId(tenantId).clientId(clientId).username(username).password(password).build();
        AccessToken token = credential.getToken(scopes).block();
        Assert.assertEquals(token1, token.token());
        Assert.assertEquals(expiresOn.getSecond(), token.expiresOn().getSecond());
        try {
            credential = new UserCredentialBuilder().tenantId(tenantId).clientId(clientId).username(username).password(badPassword).build();
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
        when(identityClient.authenticateWithUsernamePassword(scopes, username, password)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        try {
            UserCredential credential = new UserCredentialBuilder().clientId(clientId).username(username).password(password).build();
            credential.getToken(scopes).block();
            fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("tenantId"));
        }
        try {
            UserCredential credential = new UserCredentialBuilder().tenantId(tenantId).username(username).password(password).build();
            credential.getToken(scopes).block();
            fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("clientId"));
        }
        try {
            UserCredential credential = new UserCredentialBuilder().tenantId(tenantId).clientId(clientId).username(username).build();
            credential.getToken(scopes).block();
            fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("password"));
        }
        try {
            UserCredential credential = new UserCredentialBuilder().tenantId(tenantId).clientId(clientId).password(password).build();
            credential.getToken(scopes).block();
            fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("username"));
        }
    }
}
