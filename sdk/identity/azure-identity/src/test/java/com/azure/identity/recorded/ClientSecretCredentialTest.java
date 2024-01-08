package com.azure.identity.recorded;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ClientSecretCredentialTest extends IdentityTestBase {

    private ClientSecretCredential getCredential() {
        return new ClientSecretCredentialBuilder()
            .clientSecret("Client-Secret")
            .clientId("Client-Id")
            .tenantId("tenant-id")
            .build();
    }

    @Test
    public void create() {
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
            .clientSecret("Client-Secret")
            .clientId("Client-Id")
            .tenantId("tenant-id")
            .build();

        assertNotNull(credential);
    }

    @Test
    public void getTokenSync() {
        String expectedAccessTokenValue =
            "3ff503e0-15ef-4be9-bd99-29e6026d4bf6:M2ZmNTAzZTAtMTVlZi00YmU5LWJkOTktMjllNjAyNmQ0YmY2";
        OffsetDateTime expectedExpiration = OffsetDateTime.MAX;
        ClientSecretCredential credential = getCredential();

        AccessToken token = credential.getTokenSync(new TokenRequestContext().addScopes("https://vault.azure.net/.default"));

        assertNotNull(token);
        assertEquals(expectedAccessTokenValue, token.getToken());
        assertEquals(expectedExpiration, token.getExpiresAt());
    }

}
