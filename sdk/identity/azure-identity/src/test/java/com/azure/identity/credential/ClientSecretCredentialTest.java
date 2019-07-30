package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.identity.IdentityClient;
import com.microsoft.aad.msal4j.MsalServiceException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.azure.identity.*")
public class ClientSecretCredentialTest {

    private static final String tenantId = "contoso.com";
    private static final String clientId = UUID.randomUUID().toString();

    @Test
    public void testValidSecrets() throws Exception {
        // setup
        String secret = "secret";
        String token1 = "token1";
        String token2 = "token2";
        String[] scopes1 = new String[] { "https://management.azure.com" };
        String[] scopes2 = new String[] { "https://vault.azure.net" };
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithClientSecret(tenantId, clientId, secret, scopes1)).thenReturn(getMockAccessToken(token1, expiresOn));
        when(identityClient.authenticateWithClientSecret(tenantId, clientId, secret, scopes2)).thenReturn(getMockAccessToken(token2, expiresOn));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        ClientSecretCredential credential = new ClientSecretCredential().tenantId(tenantId).clientId(clientId).clientSecret(secret);
        AccessToken token = credential.getToken(scopes1).block();
        Assert.assertEquals(token1, token.token());
        Assert.assertEquals(expiresOn, token.expiresOn());
        token = credential.getToken(scopes2).block();
        Assert.assertEquals(token2, token.token());
        Assert.assertEquals(expiresOn, token.expiresOn());
    }

    @Test
    public void testInvalidSecrets() throws Exception {
        // setup
        String secret = "secret";
        String badSecret = "badsecret";
        String token1 = "token1";
        String[] scopes = new String[] { "https://management.azure.com" };
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithClientSecret(tenantId, clientId, secret, scopes)).thenReturn(getMockAccessToken(token1, expiresOn));
        when(identityClient.authenticateWithClientSecret(tenantId, clientId, badSecret, scopes)).thenThrow(new MsalServiceException("bad secret", "BadSecret"));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        ClientSecretCredential credential = new ClientSecretCredential().tenantId(tenantId).clientId(clientId).clientSecret(secret);
        AccessToken token = credential.getToken(scopes).block();
        Assert.assertEquals(token1, token.token());
        Assert.assertEquals(expiresOn, token.expiresOn());
        try {
            credential.clientSecret(badSecret);
            credential.getToken(scopes).block();
            fail();
        } catch (MsalServiceException e) {
            Assert.assertEquals("bad secret", e.getMessage());
        }
    }

    @Test
    public void testInvalidParameters() throws Exception {
        // setup
        String secret = "secret";
        String token1 = "token1";
        String[] scopes = new String[] { "https://management.azure.com" };
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithClientSecret(tenantId, clientId, secret, scopes)).thenReturn(getMockAccessToken(token1, expiresOn));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        try {
            ClientSecretCredential credential = new ClientSecretCredential().clientId(clientId).clientSecret(secret);
            credential.getToken(scopes).block();
            fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("tenantId"));
        }
        try {
            ClientSecretCredential credential = new ClientSecretCredential().tenantId(tenantId).clientSecret(secret);
            credential.getToken(scopes).block();
            fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("clientId"));
        }
        try {
            ClientSecretCredential credential = new ClientSecretCredential().tenantId(tenantId).clientId(clientId);
            credential.getToken(scopes).block();
            fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("clientSecret"));
        }
    }

    private static Mono<AccessToken> getMockAccessToken(String accessToken, OffsetDateTime expiresOn) {
        return Mono.just(new AccessToken(accessToken, expiresOn.plusMinutes(2)));
    }
}
