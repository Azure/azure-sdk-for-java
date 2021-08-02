package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import reactor.core.publisher.Mono;

public class OnBehalfOfCredential implements TokenCredential {
    private String clientId;
    private String tenantId;
    private String clientSecret;
    private IdentityClientOptions options;

    public OnBehalfOfCredential(String clientId, String tenantId, String clientSecret,
                                IdentityClientOptions options) {
        this.clientId = clientId;
        this.tenantId = tenantId;
        this.clientSecret = clientSecret;
        this.options = options;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.defer(() -> {
            UserAssertionScope.currentScope.client = new IdentityClientBuilder()
                .clientId(clientId)
                .tenantId(tenantId)
                .clientSecret(clientSecret)
                .identityClientOptions(options)
                .build();


//                _client ?? new MsalConfidentialClient(_pipeline, tenantId, _clientId, _clientSecret, UserAssertionScope.Current.CacheOptions, default);

             return UserAssertionScope.currentScope.client.authenticateWithOBO(request, UserAssertionScope.currentScope.UserAssertion);

//                 UserAssertionScope.Current.Client
//                    .AcquireTokenOnBehalfOf(requestContext.Scopes, tenantId, UserAssertionScope.Current.UserAssertion, async, cancellationToken)
//                    .ConfigureAwait(false);
//
//                return new AccessToken(result.AccessToken, result.ExpiresOn);
        });
    }
}
