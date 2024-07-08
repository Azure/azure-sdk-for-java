// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.credential;

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
 * The {@link TokenRequestContext} is typically used with authentication mechanisms that require more advanced
 * configurations or options, such as
 * <a href="https://learn.microsoft.com/azure/active-directory/fundamentals/">Azure Active Directory (Azure AD)</a>
 * authentication.
 * </p>
 *
 * <p>
 * Here's a high-level overview of how you can use the {@link TokenRequestContext}:
 * </p>
 *
 * <ol>
 * <li>Create an instance of the {@link TokenRequestContext} class and configure the required properties.
 * The {@link TokenRequestContext} class allows you to specify the scopes or resources for which you want to request
 * an access token, as well as any additional claims or options.</li>
 *
 * <li>Pass the TokenRequestContext instance to the appropriate authentication client or mechanism when
 * requesting an access token. The specific method or API to do this will depend on the authentication mechanism
 * you are using. For example, if you are using Azure Identity for AAD authentication, you would pass the
 * TokenRequestContext instance to the getToken method of the {@link TokenCredential} implementation.</li>
 *
 * <li>The authentication client or mechanism will handle the token request and return an access token that can
 * be used to authenticate and authorize requests to Azure services.</li>
 * </ol>
 *
 * @see com.azure.core.credential
 * @see com.azure.core.v2.credential.TokenCredential
 */

public class TokenRequestContext {
    private final List<String> scopes;
    private String claims;
    private String tenantId;
    private boolean enableCae;

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

    /**
     * Set the tenant id to be used for the authentication request.
     *
     * @param tenantId the tenant to be used when requesting the token.
     * @return the updated TokenRequestContext itself
     */
    public TokenRequestContext setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Get the tenant id to be used for the authentication request.
     *
     * @return the configured tenant id.
     */
    public String getTenantId() {
        return this.tenantId;
    }

    /**
     * Indicates whether to enable Continuous Access Evaluation (CAE) for the requested token.
     *
     * <p> If a resource API implements CAE and your application declares it can handle CAE, your app receives
     * CAE tokens for that resource. For this reason, if you declare your app CAE ready, your application must handle
     * the CAE claim challenge for all resource APIs that accept Microsoft Identity access tokens. If you don't handle
     * CAE responses in these API calls, your app could end up in a loop retrying an API call with a token that is
     * still in the returned lifespan of the token but has been revoked due to CAE.</p>
     *
     * @param enableCae the flag indicating whether to enable Continuous Access Evaluation (CAE) for
     * the requested token.
     * @return the updated TokenRequestContext.
     */
    public TokenRequestContext setCaeEnabled(boolean enableCae) {
        this.enableCae = enableCae;
        return this;
    }

    /**
     * Get the status indicating whether Continuous Access Evaluation (CAE) is enabled for the requested token.
     *
     * @return the flag indicating whether CAE authentication should be used or not.
     */
    public boolean isCaeEnabled() {
        return this.enableCae;
    }
}
