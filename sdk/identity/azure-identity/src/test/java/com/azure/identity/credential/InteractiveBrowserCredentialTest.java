// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Random;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.azure.identity.*")
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
public class InteractiveBrowserCredentialTest {

    private final String tenantId = "contoso.com";
    private final String clientId = UUID.randomUUID().toString();

    @Test
    public void testValidInteractive() throws Exception {
        Random random = new Random();

        // setup
        String token1 = "token1";
        String token2 = "token2";
        String[] scopes1 = new String[] { "https://management.azure.com" };
        String[] scopes2 = new String[] { "https://vault.azure.net" };
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        int port = random.nextInt(10000) + 10000;

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithBrowserInteraction(eq(scopes1), eq(port))).thenReturn(TestUtils.getMockMsalToken(token1, expiresOn));
        when(identityClient.authenticateWithUserRefreshToken(any(), any()))
            .thenAnswer(invocation -> {
                String[] argument = (String[]) invocation.getArguments()[0];
                if (argument.length == 1 && argument[0].equals(scopes2[0])) {
                    return TestUtils.getMockMsalToken(token2, expiresOn);
                } else if (argument.length == 1 && argument[0].equals(scopes1[0])) {
                    return Mono.error(new UnsupportedOperationException("nothing cached"));
                } else {
                    throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                }
            });
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        InteractiveBrowserCredential credential = new InteractiveBrowserCredentialBuilder().port(port).clientId(clientId).build();
        AccessToken token = credential.getToken(scopes1).block();
        Assert.assertEquals(token1, token.token());
        Assert.assertEquals(expiresOn.getSecond(), token.expiresOn().getSecond());
        token = credential.getToken(scopes2).block();
        Assert.assertEquals(token2, token.token());
        Assert.assertEquals(expiresOn.getSecond(), token.expiresOn().getSecond());
    }
}
