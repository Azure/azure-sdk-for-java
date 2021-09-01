package com.azure.identity;

import com.azure.identity.implementation.IdentityClient;
import com.microsoft.aad.msal4j.UserAssertion;

public class UserAssertionScope {
    private UserAssertion UserAssertion;

    /**
     * Initializes a new instance of {@link UserAssertionScope} using the supplied access token.
     * @param accessToken The access token that will be used by {@link OnBehalfOfCredential} as the user assertion when requesting On-Behalf-Of tokens.
     */
    public UserAssertionScope(String accessToken) {
        UserAssertion = new UserAssertion(accessToken);
    }

    UserAssertion getUserAssertion() {
        return this.UserAssertion;
    }
}
