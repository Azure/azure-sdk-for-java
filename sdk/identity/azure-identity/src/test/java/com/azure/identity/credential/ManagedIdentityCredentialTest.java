package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
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
public class ManagedIdentityCredentialTest {

    private static final String tenantId = "contoso.com";
    private static final String clientId = UUID.randomUUID().toString();

    @Test
    public void testMSIEndpoint() throws Exception {
        Configuration configuration = ConfigurationManager.getConfiguration();

        try {
            // setup
            String endpoint = "http://localhost";
            String secret = "secret";
            String token1 = "token1";
            String[] scopes1 = new String[]{"https://management.azure.com"};
            OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
            configuration.put("MSI_ENDPOINT", endpoint);
            configuration.put("MSI_SECRET", secret);

            // mock
            IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
            when(identityClient.authenticateToManagedIdentityEndpoint(endpoint, secret, clientId, scopes1)).thenReturn(getMockAccessToken(token1, expiresOn));
            PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

            // test
            ManagedIdentityCredential credential = new ManagedIdentityCredential().clientId(clientId);
            AccessToken token = credential.getToken(scopes1).block();
            Assert.assertEquals(token1, token.token());
            Assert.assertEquals(expiresOn, token.expiresOn());
        } finally {
            // clean up
            configuration.remove("MSI_ENDPOINT");
            configuration.remove("MSI_SECRET");
        }
    }

    @Test
    public void testIMDS() throws Exception {
        // setup
        String token1 = "token1";
        String[] scopes = new String[] { "https://management.azure.com" };
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateToIMDSEndpoint(clientId, scopes)).thenReturn(getMockAccessToken(token1, expiresOn));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        ManagedIdentityCredential credential = new ManagedIdentityCredential().clientId(clientId);
        AccessToken token = credential.getToken(scopes).block();
        Assert.assertEquals(token1, token.token());
        Assert.assertEquals(expiresOn, token.expiresOn());
    }

    private static Mono<AccessToken> getMockAccessToken(String accessToken, OffsetDateTime expiresOn) {
        return Mono.just(new AccessToken(accessToken, expiresOn.plusMinutes(2)));
    }
}
