// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.azure.identity.*")
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
public class EventHubSharedKeyCredentialTest {

    private static final String SHARED_ACCESS_POLICY = "<YOUR-EVENT-HUB-SHARED-ACCESS-POLICY>";

    private static final String SHARED_ACCESS_KEY = "<YOUR-EVENT-HUB-SHARED-ACCESS-KEY>";

    private static final String SHARED_ACCESS_SIGNATURE = "<YOUR-EVENT-HUB-ACCESS-SIGNATURE>";

    private static final TokenRequestContext REQUEST = new TokenRequestContext().addScopes("<YOUR-ACCESS-SCOPE>");

    @Test
    public void testValidShardAccessKey() throws Exception {

        // mock
        EventHubSharedKeyCredential eventHubSharedKeyCredential = PowerMockito.mock(EventHubSharedKeyCredential.class);
        when(eventHubSharedKeyCredential.getToken(any(TokenRequestContext.class)))
            .thenReturn(getMockAccessToken(SHARED_ACCESS_SIGNATURE, OffsetDateTime.now(ZoneOffset.UTC)));
        PowerMockito.whenNew(EventHubSharedKeyCredential.class).withAnyArguments().thenReturn(eventHubSharedKeyCredential);

        EventHubSharedKeyCredential credential
            = new EventHubSharedKeyCredentialBuilder()
            .sharedAccessKey(SHARED_ACCESS_KEY)
            .sharedAccessPolicy(SHARED_ACCESS_POLICY)
            .build();
        StepVerifier.create(credential.getToken(REQUEST))
            .expectNextMatches(accessToken -> SHARED_ACCESS_SIGNATURE.equals(accessToken.getToken()))
            .verifyComplete();
    }

    @Test
    public void testValidSharedAccessSignature() {
        EventHubSharedKeyCredential credential
            = new EventHubSharedKeyCredentialBuilder()
            .sharedAccessSignature(SHARED_ACCESS_SIGNATURE)
            .build();
        StepVerifier.create(credential.getToken(REQUEST))
            .expectNextMatches(accessToken -> SHARED_ACCESS_SIGNATURE.equals(accessToken.getToken()))
            .verifyComplete();
    }

    /**
     * Creates a mock {@link AccessToken} instance.
     * @param accessToken the access token to return
     * @param expiresOn the expiration time
     * @return a Mono publisher of the result
     */
    public static Mono<AccessToken> getMockAccessToken(String accessToken, OffsetDateTime expiresOn) {
        return Mono.just(new AccessToken(accessToken, expiresOn.plusMinutes(20)));
    }

}
