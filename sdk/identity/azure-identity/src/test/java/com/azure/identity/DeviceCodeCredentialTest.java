// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.util.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.azure.identity.*")
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
public class DeviceCodeCredentialTest {

    private final String clientId = UUID.randomUUID().toString();

    @Test
    public void testValidDeviceCode() throws Exception {
        // setup
        Consumer<DeviceCodeInfo> consumer = deviceCodeInfo -> { /* do nothing */ };
        String token1 = "token1";
        String token2 = "token2";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://vault.azure.net");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithDeviceCode(eq(request1), eq(consumer))).thenReturn(TestUtils.getMockMsalToken(token1, expiresAt));
        when(identityClient.authenticateWithPublicClientCache(any(), any()))
            .thenAnswer(invocation -> {
                TokenRequestContext argument = (TokenRequestContext) invocation.getArguments()[0];
                if (argument.getScopes().size() == 1 && argument.getScopes().get(0).equals(request2.getScopes().get(0))) {
                    return TestUtils.getMockMsalToken(token2, expiresAt);
                } else if (argument.getScopes().size() == 1 && argument.getScopes().get(0).equals(request1.getScopes().get(0))) {
                    return Mono.error(new UnsupportedOperationException("nothing cached"));
                } else {
                    throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                }
            });
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        DeviceCodeCredential credential =
            new DeviceCodeCredentialBuilder().challengeConsumer(consumer).clientId(clientId).build();
        StepVerifier.create(credential.getToken(request1))
            .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
            .verifyComplete();
        StepVerifier.create(credential.getToken(request2))
            .expectNextMatches(accessToken -> token2.equals(accessToken.getToken())
                && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
            .verifyComplete();
    }


    @Test
    public void testValidAuthenticateDeviceCode() throws Exception {
        // setup
        Consumer<DeviceCodeInfo> consumer = deviceCodeInfo -> { /* do nothing */ };
        String token1 = "token1";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithDeviceCode(eq(request1), eq(consumer)))
                .thenReturn(TestUtils.getMockMsalToken(token1, expiresAt));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        DeviceCodeCredential credential =
            new DeviceCodeCredentialBuilder().challengeConsumer(consumer).clientId(clientId).build();
        StepVerifier.create(credential.authenticate(request1))
    .expectNextMatches(authenticationRecord -> authenticationRecord.getAuthority()
               .equals("http://login.microsoftonline.com")
               && authenticationRecord.getUsername().equals("testuser")
               && authenticationRecord.getHomeAccountId() != null)
            .verifyComplete();
    }

    @Test
    public void testMultiTenantAuthenticationEnabled() throws Exception {
        // setup
        Consumer<DeviceCodeInfo> consumer = deviceCodeInfo -> { /* do nothing */ };
        String token1 = "token1";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithDeviceCode(eq(request1), eq(consumer)))
            .thenReturn(TestUtils.getMockMsalToken(token1, expiresAt));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        DeviceCodeCredential credential =
            new DeviceCodeCredentialBuilder().challengeConsumer(consumer)
                .allowMultiTenantAuthentication()
                .clientId(clientId)
                .disableAutomaticAuthentication().build();
        StepVerifier.create(credential.authenticate(request1))
            .expectNextMatches(authenticationRecord -> authenticationRecord.getAuthority()
                .equals("http://login.microsoftonline.com")
                && authenticationRecord.getUsername().equals("testuser")
                && authenticationRecord.getHomeAccountId() != null)
            .verifyComplete();
    }
}
