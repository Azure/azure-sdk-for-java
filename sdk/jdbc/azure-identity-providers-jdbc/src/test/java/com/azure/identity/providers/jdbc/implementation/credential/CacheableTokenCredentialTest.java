package com.azure.identity.providers.jdbc.implementation.credential;


import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.providers.jdbc.implementation.cache.StaticAccessTokenCache;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CacheableTokenCredentialTest {

    @Test
    void testGetToken() {
        TokenCredential tokenCredential = mock(TokenCredential.class);
        when(tokenCredential.getToken(any()))
            .thenReturn(Mono.just(new AccessToken("fake-access-token-1", OffsetDateTime.now().plusHours(2))))
            .thenReturn(Mono.just(new AccessToken("fake-access-token-2", OffsetDateTime.now().plusHours(2))));
        TokenCredentialProviderOptions options = new TokenCredentialProviderOptions();

        TokenRequestContext requestContext = new TokenRequestContext();
        requestContext.addScopes("test-get-token");
        CacheableTokenCredential credential = new CacheableTokenCredential(StaticAccessTokenCache.getInstance(), tokenCredential, options);
        Mono<AccessToken> token01 = credential.getToken(requestContext);
        Mono<AccessToken> token02 = credential.getToken(requestContext);

        assertEquals(token01.block().getToken(), "fake-access-token-1");
        assertEquals(token02.block().getToken(), "fake-access-token-1");

    }
}
