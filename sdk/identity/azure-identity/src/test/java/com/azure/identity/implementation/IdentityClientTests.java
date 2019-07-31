package com.azure.identity.implementation;

import com.azure.core.credentials.AccessToken;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.util.TestUtils;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.MsalServiceException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

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
        String[] scopes = new String[] { "https://management.azure.com" };
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        PowerMockito.stub(PowerMockito.method(ConfidentialClientApplication.class, "acquireToken", ClientCredentialParameters.class))
            .toReturn(TestUtils.getMockAuthenticationResult(accessToken, expiresOn));

        // test
        IdentityClient client = new IdentityClient(tenantId, clientId, null);
        AccessToken token = client.authenticateWithClientSecret(secret, scopes).block();
        Assert.assertEquals(accessToken, token.token());
        Assert.assertEquals(expiresOn, token.expiresOn());
    }

    @Test
    public void testInvalidSecret() throws Exception {
        // setup
        String secret = "secret";
        String[] scopes = new String[] { "https://management.azure.com" };

        // mock
        PowerMockito.stub(PowerMockito.method(ConfidentialClientApplication.class, "acquireToken", ClientCredentialParameters.class))
            .toThrow(new MsalServiceException("bad secret", "BadSecret"));

        // test
        try {
            IdentityClient client = new IdentityClient(tenantId, clientId, null);
            client.authenticateWithClientSecret(secret, scopes).block();
            fail();
        } catch (MsalServiceException e) {
            Assert.assertEquals("bad secret", e.getMessage());
        }
    }
}
