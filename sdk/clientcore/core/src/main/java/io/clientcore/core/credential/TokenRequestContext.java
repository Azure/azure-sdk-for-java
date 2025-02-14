// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.credential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * The {@link TokenRequestContext} is a class used to provide additional information and context when requesting an
 * access token from an authentication source. It allows you to customize the token request and specify additional
 * parameters, such as scopes, claims, or authentication options.
 * </p>
 *
 * <p>
 * Here's a high-level overview of how you can use the {@link TokenRequestContext}:
 * </p>
 *
 * <ol>
 * <li>Create an instance of the {@link TokenRequestContext} class and configure the required properties.
 * The {@link TokenRequestContext} class allows you to specify the scopes or resources for which you want to request
 * an access token, as well as any additional claims.</li>
 *
 * <li>Pass the TokenRequestContext instance to the appropriate authentication client or mechanism when
 * requesting an access token. The specific method or API to do this will depend on the authentication mechanism
 * you are using.</li>
 *
 * <li>The authentication client or mechanism will handle the token request and return an access token that can
 * be used to authenticate.</li>
 * </ol>
 *
 * @see io.clientcore.core.credential
 */
public class TokenRequestContext {
    private final List<String> scopes;
    private String claims;

    /**
     * Creates a token request instance.
     */
    public TokenRequestContext() {
        this.scopes = new ArrayList<>();
    }

    /**
     * Gets the scopes required for the token.
     * @return the scopes required for the token
     */
    public List<String> getScopes() {
        return scopes;
    }

    /**
     * Sets the scopes required for the token.
     * @param scopes the scopes required for the token
     * @return the TokenRequestContext itself
     */
    public TokenRequestContext setScopes(List<String> scopes) {
        Objects.requireNonNull(scopes, "'scopes' cannot be null.");
        this.scopes.clear();
        this.scopes.addAll(scopes);
        return this;
    }

    /**
     * Adds one or more scopes to the request scopes.
     * @param scopes one or more scopes to add
     * @return the TokenRequestContext itself
     */
    public TokenRequestContext addScopes(String... scopes) {
        this.scopes.addAll(Arrays.asList(scopes));
        return this;
    }

    /**
     * Set the additional claims to be included in the token.
     *
     * @see <a href="https://openid.net/specs/openid-connect-core-1_0-final.html#ClaimsParameter">
     *     https://openid.net/specs/openid-connect-core-1_0-final.html#ClaimsParameter</a>
     *
     * @param claims the additional claims to be included in the token.
     * @return the updated TokenRequestContext itself
     */
    public TokenRequestContext setClaims(String claims) {
        this.claims = claims;
        return this;
    }

    /**
     * Get the additional claims to be included in the token.
     *
     * @see <a href="https://openid.net/specs/openid-connect-core-1_0-final.html#ClaimsParameter">
     *     https://openid.net/specs/openid-connect-core-1_0-final.html#ClaimsParameter</a>
     *
     * @return the additional claims to be included in the token.
     */
    public String getClaims() {
        return this.claims;
    }
}
