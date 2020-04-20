package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.MsalToken;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

public class VsCodeCredential implements TokenCredential {
    private final IdentityClient identityClient;
    private final AtomicReference<MsalToken> cachedToken;

    /**
     * Creates a DeviceCodeCredential with the given identity client options.
     *
     * @param identityClientOptions the options for configuring the identity client
     */
    VsCodeCredential(IdentityClientOptions identityClientOptions) {
        identityClient = new IdentityClientBuilder()
                             .tenantId("common")
                             .identityClientOptions(identityClientOptions)
                             .build();
        this.cachedToken = new AtomicReference<>();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.defer(() -> {
            if (cachedToken.get() != null) {
                return identityClient.authenticateWithUserRefreshToken(request, cachedToken.get())
                           .onErrorResume(t -> Mono.empty());
            } else {
                return Mono.empty();
            }
        }).switchIfEmpty(
            Mono.defer(() -> identityClient.authenticateWithVsCodeCredential(request)))
                   .map(msalToken -> {
                       cachedToken.set(msalToken);
                       return msalToken;
                   });
    }
}
