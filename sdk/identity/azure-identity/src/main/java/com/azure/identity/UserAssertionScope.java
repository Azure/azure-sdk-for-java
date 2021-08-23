package com.azure.identity;

import com.azure.identity.implementation.IdentityClient;
import com.microsoft.aad.msal4j.UserAssertion;

public class UserAssertionScope {
    public static String USER_ASSERTION_SCOPE_KEY = "USER_ASSERTION_SCOPE_KEY";
    UserAssertion UserAssertion;

    /**
     * Initializes a new instance of {@link UserAssertionScope} using the supplied access token.
     * @param accessToken The access token that will be used by {@link OnBehalfOfCredential} as the user assertion when requesting On-Behalf-Of tokens.
     */
    public UserAssertionScope(String accessToken) {
        UserAssertion = new UserAssertion(accessToken);
    }
}
