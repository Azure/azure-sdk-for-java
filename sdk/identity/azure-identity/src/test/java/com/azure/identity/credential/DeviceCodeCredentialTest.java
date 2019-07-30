package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.identity.DeviceCodeChallenge;
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
import java.util.function.Consumer;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.azure.identity.*")
public class DeviceCodeCredentialTest {

    private static final String tenantId = "contoso.com";
    private static final String clientId = UUID.randomUUID().toString();

    @Test
    public void testValidDeviceCode() throws Exception {
        // setup
        Consumer<DeviceCodeChallenge> consumer = deviceCodeChallenge -> { /* do nothing */ };
        String token1 = "token1";
        String token2 = "token2";
        String[] scopes1 = new String[] { "https://management.azure.com" };
        String[] scopes2 = new String[] { "https://vault.azure.net" };
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithDeviceCode(clientId, scopes1, consumer)).thenReturn(getMockAccessToken(token1, expiresOn));
        when(identityClient.authenticateWithCurrentlyLoggedInAccount(scopes1)).thenReturn(Mono.error(new UnsupportedOperationException("nothing cached")));
        when(identityClient.authenticateWithCurrentlyLoggedInAccount(scopes2)).thenReturn(getMockAccessToken(token2, expiresOn));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        DeviceCodeCredential credential = new DeviceCodeCredential(consumer).clientId(clientId);
        AccessToken token = credential.getToken(scopes1).block();
        Assert.assertEquals(token1, token.token());
        Assert.assertEquals(expiresOn, token.expiresOn());
        token = credential.getToken(scopes2).block();
        Assert.assertEquals(token2, token.token());
        Assert.assertEquals(expiresOn, token.expiresOn());

    }

    private static Mono<AccessToken> getMockAccessToken(String accessToken, OffsetDateTime expiresOn) {
        return Mono.just(new AccessToken(accessToken, expiresOn.plusMinutes(2)));
    }
}
