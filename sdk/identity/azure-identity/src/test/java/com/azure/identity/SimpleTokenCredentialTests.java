package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import org.junit.Assert;
import org.junit.Test;

import java.time.OffsetDateTime;

public class SimpleTokenCredentialTests {

    @Test
    public void testValidStaticTokenString() {
        String token = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        // test
        SimpleTokenCredential credential =
            new SimpleTokenCredentialBuilder().accessToken(token).build();
        AccessToken accessToken = credential.getToken(request).block();
        Assert.assertEquals(token, accessToken.getToken());
        Assert.assertEquals(false, accessToken.isExpired());
    }

    @Test
    public void testValidStaticAccessToken() {
        AccessToken token = new AccessToken("token1", OffsetDateTime.MIN);
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        // test
        SimpleTokenCredential credential =
            new SimpleTokenCredentialBuilder().accessToken(token).build();
        AccessToken accessToken = credential.getToken(request).block();
        Assert.assertEquals(token.getToken(), accessToken.getToken());
        Assert.assertEquals(token.getExpiresAt(), accessToken.getExpiresAt());
    }

}
