// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.credentials.oauth;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * The {@link OAuthTokenRequestProperties} is a class used to provide additional information and context when requesting an
 * access token from an authentication source. It allows you to customize the token request and specify additional
 * parameters, such as scopes, claims, or authentication options.
 * </p>
 *
 * <p>
 * Here's a high-level overview of how you can use the {@link OAuthTokenRequestProperties}:
 * </p>
 *
 * <ol>
 * <li>Create an instance of the {@link OAuthTokenRequestProperties} class and configure the required properties.
 * The {@link OAuthTokenRequestProperties} class allows you to specify the scopes or resources for which you want to request
 * an access token, as well as any additional claims.</li>
 *
 * <li>Pass the OAuthTokenRequestProperties instance to the appropriate authentication client or mechanism when
 * requesting an access token. The specific method or API to do this will depend on the authentication mechanism
 * you are using.</li>
 *
 * <li>The authentication client or mechanism will handle the token request and return an access token that can
 * be used to authenticate.</li>
 * </ol>
 *
 * @see io.clientcore.core.credentials
 */
public class OAuthTokenRequestProperties {
    private final List<String> scopes;
    private Map<String, Object> params;

    /**
     * Creates a token request instance.
     */
    public OAuthTokenRequestProperties() {
        this.scopes = new ArrayList<>();
        this.params = new HashMap<>();
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
     * @return the OAuthTokenRequestProperties itself
     */
    public OAuthTokenRequestProperties setScopes(List<String> scopes) {
        Objects.requireNonNull(scopes, "'scopes' cannot be null.");
        this.scopes.clear();
        this.scopes.addAll(scopes);
        return this;
    }

    /**
     * Adds one or more scopes to the request scopes.
     * @param scopes one or more scopes to add
     * @return the OAuthTokenRequestProperties itself
     */
    public OAuthTokenRequestProperties addScopes(String... scopes) {
        this.scopes.addAll(Arrays.asList(scopes));
        return this;
    }

    /**
     * Gets the additional parameters required for the token.
     * @return the parameters required for the token
     */
    public Map<String, Object> getParams() {
        return this.params;
    }

    /**
     * Sets the additional parameters required for the token.
     *
     * @param params the additional parameters
     * @return the OAuthTokenRequestProperties itself
     */
    public OAuthTokenRequestProperties setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    /**
     * Adds the additional parameter required for the token.
     *
     * @param key the key
     * @param value the value
     * @return the OAuthTokenRequestProperties itself
     */
    public OAuthTokenRequestProperties addParams(String key, String value) {
        this.params.put(key, value);
        return this;
    }
}
