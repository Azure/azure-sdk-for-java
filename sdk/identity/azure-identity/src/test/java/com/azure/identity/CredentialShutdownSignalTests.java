// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentitySyncClient;
import com.azure.identity.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A shutdown signal raised while a credential is acquiring a token is a cancellation by the caller, not a cache miss
 * and not an authentication failure. These tests drive the behaviour through the public {@code getTokenSync} surface.
 */
public class CredentialShutdownSignalTests {
    private static final String TENANT_ID = "contoso.com";
    private static final String CLIENT_ID = "11111111-1111-1111-1111-111111111111";
    private static final TokenRequestContext REQUEST
        = new TokenRequestContext().addScopes("https://management.azure.com");

    private static RuntimeException interruption() {
        return new RuntimeException("The token request was interrupted before it completed.",
            new InterruptedException());
    }

    @Test
    public void cancellationDuringCacheLookupIsNotTreatedAsCacheMiss() {
        RuntimeException interruption = interruption();

        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class);
            MockedConstruction<IdentitySyncClient> syncClientMock
                = mockConstruction(IdentitySyncClient.class, (syncClient, context) -> {
                    when(syncClient.authenticateWithConfidentialClientCache(any())).thenThrow(interruption);
                })) {

            ClientSecretCredential credential = new ClientSecretCredentialBuilder().tenantId(TENANT_ID)
                .clientId(CLIENT_ID)
                .clientSecret("fakeSecretPlaceholder")
                .build();

            RuntimeException thrown = assertThrows(RuntimeException.class, () -> credential.getTokenSync(REQUEST));

            // Surfaced unchanged rather than swallowed as a cache miss ...
            assertSame(interruption, thrown);
            // ... and the full acquisition, which would have been interrupted as well, was never attempted.
            verify(syncClientMock.constructed().get(0), never()).authenticateWithConfidentialClient(any());
            assertEquals(1, identityClientMock.constructed().size());
        }
    }

    @Test
    public void ordinaryCacheFailureStillFallsThroughToFullAcquisition() {
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class);
            MockedConstruction<IdentitySyncClient> syncClientMock
                = mockConstruction(IdentitySyncClient.class, (syncClient, context) -> {
                    when(syncClient.authenticateWithConfidentialClientCache(any()))
                        .thenThrow(new IllegalStateException("Token not found in the cache"));
                    when(syncClient.authenticateWithConfidentialClient(any()))
                        .thenReturn(TestUtils.getMockAccessTokenSync("token", expiresAt));
                })) {

            ClientSecretCredential credential = new ClientSecretCredentialBuilder().tenantId(TENANT_ID)
                .clientId(CLIENT_ID)
                .clientSecret("fakeSecretPlaceholder")
                .build();

            AccessToken token = credential.getTokenSync(REQUEST);

            assertEquals("token", token.getToken());
            assertEquals(1, identityClientMock.constructed().size());
            assertEquals(1, syncClientMock.constructed().size());
        }
    }

    @Test
    public void cancellationIsNotRewrappedAndLoggedAsAnAuthenticationError() {
        RuntimeException interruption = interruption();

        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class);
            MockedConstruction<IdentitySyncClient> syncClientMock
                = mockConstruction(IdentitySyncClient.class, (syncClient, context) -> {
                    when(syncClient.authenticateWithConfidentialClientCache(any())).thenReturn(null);
                    when(syncClient.authenticateWithConfidentialClient(any())).thenThrow(interruption);
                })) {

            ClientAssertionCredential credential = new ClientAssertionCredentialBuilder().tenantId(TENANT_ID)
                .clientId(CLIENT_ID)
                .clientAssertion(() -> "fakeAssertionPlaceholder")
                .build();

            RuntimeException thrown = assertThrows(RuntimeException.class, () -> credential.getTokenSync(REQUEST));

            // ClientAssertionCredential wraps failures in a RuntimeException logged at error level. A cancellation
            // must reach the caller as it is, without that second error-level log.
            assertSame(interruption, thrown);
            assertEquals(1, identityClientMock.constructed().size());
            assertEquals(1, syncClientMock.constructed().size());
        }
    }

    @Test
    public void cancellationDoesNotPromptTheUserAfterACacheLookup() {
        RuntimeException interruption = interruption();
        String recordJson = "{\"authority\":\"authority\",\"homeAccountId\":\"homeAccountId\","
            + "\"tenantId\":\"tenantId\",\"username\":\"username\",\"clientId\":\"clientId\"}";
        AuthenticationRecord record
            = AuthenticationRecord.deserialize(new ByteArrayInputStream(recordJson.getBytes(StandardCharsets.UTF_8)));

        try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class);
            MockedConstruction<IdentitySyncClient> syncClientMock
                = mockConstruction(IdentitySyncClient.class, (syncClient, context) -> {
                    when(syncClient.authenticateWithPublicClientCache(any(), any())).thenThrow(interruption);
                })) {

            InteractiveBrowserCredential credential
                = new InteractiveBrowserCredentialBuilder().clientId(CLIENT_ID).authenticationRecord(record).build();

            RuntimeException thrown = assertThrows(RuntimeException.class, () -> credential.getTokenSync(REQUEST));

            assertSame(interruption, thrown);
            // The caller cancelled, so the browser must not be opened for the request they gave up on.
            verify(syncClientMock.constructed().get(0), never()).authenticateWithBrowserInteraction(any(), any(), any(),
                any());
            assertEquals(1, identityClientMock.constructed().size());
        }
    }
}
