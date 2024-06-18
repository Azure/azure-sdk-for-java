package com.azure.data.schemaregistry;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.test.InterceptorManager;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public class TestUtil {

    public static TokenCredential getTestTokenCredential(InterceptorManager interceptorManager) {
        if (interceptorManager.isLiveMode()) {
            return new AzurePowerShellCredentialBuilder().build();
        } else if (interceptorManager.isRecordMode()) {
            return new DefaultAzureCredentialBuilder().build();
        } else {
            return getPlaybackTokenCredential();
        }
    }

    public static TokenCredential getPlaybackTokenCredential() {
        return new TokenCredential() {
            @Override
            public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
                return Mono.fromCallable(() -> new AccessToken("foo", OffsetDateTime.now().plusMinutes(20)));
            }

            @Override
            public AccessToken getTokenSync(TokenRequestContext request) {
                return new AccessToken("foo", OffsetDateTime.now().plusMinutes(20));
            }
        };
    }
}
