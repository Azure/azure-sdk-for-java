package com.azure.identity;

import com.azure.core.credentials.AccessToken;
import com.azure.identity.credential.ClientSecretCredential;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.MsalServiceException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.fail;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ConfidentialClientApplication.class, IdentityClient.class })
public class IdentityClientTests {

    private static final String tenantId = "contoso.com";
    private static final String clientId = UUID.randomUUID().toString();@Test
    public void testValidSecret() throws Exception {
        // setup
        String secret = "secret";
        String accessToken = "token";
        String scope = "https://management.azure.com";
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        PowerMockito.stub(PowerMockito.method(ConfidentialClientApplication.class, "acquireToken", ClientCredentialParameters.class))
            .toReturn(getMockAuthenticationResult(accessToken, expiresOn));

        // test
        ClientSecretCredential credential = new ClientSecretCredential().tenantId(tenantId).clientId(clientId).clientSecret(secret);
        AccessToken token = credential.getToken(scope).block();
        Assert.assertEquals(accessToken, token.token());
        Assert.assertEquals(expiresOn, token.expiresOn());
    }

    @Test
    public void testInvalidSecret() throws Exception {
        // setup
        String secret = "secret";
        String scope = "https://management.azure.com";

        // mock
        PowerMockito.stub(PowerMockito.method(ConfidentialClientApplication.class, "acquireToken", ClientCredentialParameters.class))
            .toThrow(new MsalServiceException("bad secret", "BadSecret"));

        // test
        try {
            ClientSecretCredential credential = new ClientSecretCredential().tenantId(tenantId).clientId(clientId).clientSecret(secret);
            credential.getToken(scope).block();
            fail();
        } catch (MsalServiceException e) {
            Assert.assertEquals("bad secret", e.getMessage());
        }
    }

    private static CompletableFuture<IAuthenticationResult> getMockAuthenticationResult(String accessToken, OffsetDateTime expiresOn) {
        return CompletableFuture.completedFuture(new IAuthenticationResult() {
            @Override
            public String accessToken() {
                return accessToken;
            }

            @Override
            public String idToken() {
                return null;
            }

            @Override
            public IAccount account() {
                return null;
            }

            @Override
            public String environment() {
                return null;
            }

            @Override
            public String scopes() {
                return null;
            }

            @Override
            public Date expiresOnDate() {
                // Access token dials back 2 minutes
                return Date.from(expiresOn.plusMinutes(2).toInstant());
            }
        });
    }
}
