package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.util.LoggingUtil;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class OnBehalfOfCredential implements TokenCredential {
    private String clientId;
    private String tenantId;
    private String clientSecret;
    private IdentityClientOptions options;
    private volatile HashMap<UserAssertionScope, IdentityClient> userAssertionClientMap;
    private final ClientLogger logger = new ClientLogger(OnBehalfOfCredential.class);


    public OnBehalfOfCredential(String clientId, String tenantId, String clientSecret,
                                IdentityClientOptions options) {
        this.clientId = clientId;
        this.tenantId = tenantId;
        this.clientSecret = clientSecret;
        this.options = options;
        this.userAssertionClientMap = new HashMap<>();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.deferContextual(ctx -> {
            UserAssertionScope scope = ctx.get(UserAssertionScope.USER_ASSERTION_SCOPE_KEY);
            IdentityClient client = userAssertionClientMap.containsKey(scope) ? userAssertionClientMap.get(scope)
                : userAssertionClientMap.put(scope, new IdentityClientBuilder()
                    .tenantId(tenantId)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .identityClientOptions(options)
                    .build());

            return client.authenticateWithConfidentialClientCache(request)
                .onErrorResume(t -> Mono.empty())
                .switchIfEmpty(Mono.defer(() -> client.authenticateWithOBO(request, scope.UserAssertion)))
                .doOnNext(token -> LoggingUtil.logTokenSuccess(logger, request))
                .doOnError(error -> LoggingUtil.logTokenError(logger, request, error));
        });
    }
}
