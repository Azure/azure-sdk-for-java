package com.azure.identity;

import com.azure.identity.implementation.IdentityClient;
import com.microsoft.aad.msal4j.UserAssertion;

public class UserAssertionScope implements AutoCloseable {

    static volatile UserAssertionScope currentScope;
    UserAssertion UserAssertion;
    IdentityClient client;
//    internal ITokenCacheOptions CacheOptions { get; }

    /**
     * Initializes a new instance of {@link UserAssertionScope} using the supplied access token.
     * @param accessToken The access token that will be used by {@link OnBehalfOfCredential} as the user assertion when requesting On-Behalf-Of tokens.
     * @param options The {@link UserAssertionScopeOptions}to configure this instance.
     */
    public UserAssertionScope(String accessToken, UserAssertionScopeOptions options)
    {
        UserAssertion = new UserAssertion(accessToken);
        currentScope = this;
//        CacheOptions = options ? new UserAssertionCacheOptions(options);
    }
    @Override
    public void close() throws Exception {

    }
}
