// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.credentials.oauth;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.CoreUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * The {@link OAuthTokenRequestContext} is a class used to provide additional information and context when requesting an
 * access token from an authentication source. It allows you to customize the token request and specify additional
 * parameters, such as scopes, claims, or authentication options.
 * </p>
 *
 * <p>
 * Here's a high-level overview of how you can use the {@link OAuthTokenRequestContext}:
 * </p>
 *
 * <ol>
 * <li>Create an instance of the {@link OAuthTokenRequestContext} class and configure the required properties.
 * The {@link OAuthTokenRequestContext} class allows you to specify the scopes or resources for which you want to request
 * an access token, as well as any additional properties.</li>
 *
 * <li>The authentication client or mechanism will handle the token request options and return an access token that can
 * be used to authenticate.</li>
 * </ol>
 *
 * @see io.clientcore.core.credentials
 */
@Metadata(properties = MetadataProperties.FLUENT)
public class OAuthTokenRequestContext {
    private static final ClientLogger LOGGER = new ClientLogger(OAuthTokenRequestContext.class);

    private List<String> scopes;
    private Map<String, Object> params;

    /**
     * Creates a token request instance.
     */
    public OAuthTokenRequestContext() {
    }

    /**
     * Gets the scopes required for the token.
     * @return the scopes required for the token
     */
    public List<String> getScopes() {
        if (CoreUtils.isNullOrEmpty(scopes)) {
            return Collections.emptyList();
        }
        return scopes;
    }

    /**
     * Sets the scopes required for the token.
     * @param scopes the scopes required for the token
     * @return the OAuthTokenRequestContext itself
     */
    public OAuthTokenRequestContext setScopes(List<String> scopes) {
        Objects.requireNonNull(scopes, "'scopes' cannot be null.");
        this.scopes = scopes;
        return this;
    }

    /**
     * Adds one or more scopes to the request scopes.
     * @param scopes one or more scopes to add
     * @return the OAuthTokenRequestProperties itself
     * @throws NullPointerException If scopes is null.
     * @throws IllegalArgumentException if empty scopes list is provided.
     * @throws IllegalArgumentException If empty or null scopes are provided as part of the parameters.
     */
    public OAuthTokenRequestContext addScopes(String... scopes) {
        Objects.requireNonNull(scopes, "'scopes' cannot be null.");

        if (scopes.length == 0) {
            throw LOGGER.throwableAtError().log("At least one scope must be provided.", IllegalArgumentException::new);
        }

        for (String scope : scopes) {
            if (CoreUtils.isNullOrEmpty(scope)) {
                throw LOGGER.throwableAtError()
                    .log("Scopes cannot contain null or empty values.", IllegalArgumentException::new);
            }
        }

        if (this.scopes == null) {
            this.scopes = new ArrayList<>();
        }
        this.scopes.addAll(Arrays.asList(scopes));
        return this;
    }

    /**
     * Gets the additional parameters required for the token.
     * @return the parameters required for the token
     */
    public Map<String, Object> getParams() {
        if (CoreUtils.isNullOrEmpty(params)) {
            return Collections.emptyMap();
        }
        return this.params;
    }

    /**
     * Sets the additional parameters required for the token.
     *
     * @param params the additional parameters
     * @return the OAuthTokenRequestContext itself
     * @throws NullPointerException If params is null.
     */
    public OAuthTokenRequestContext setParams(Map<String, Object> params) {
        Objects.requireNonNull(params, "'params' cannot be null");
        this.params = params;
        return this;
    }

    /**
     * Sets the additional parameter required for the token.
     *
     * @param key the key
     * @param value the value
     * @return the OAuthTokenRequestContext itself
     * @throws IllegalArgumentException if key and/or value parameters are null.
     */
    public OAuthTokenRequestContext setParam(String key, String value) {
        if (CoreUtils.isNullOrEmpty(key)) {
            throw LOGGER.throwableAtError()
                .log("Parameter 'key' cannot be null or empty.", IllegalArgumentException::new);
        }

        if (CoreUtils.isNullOrEmpty(value)) {
            throw LOGGER.throwableAtError()
                .log("Parameter 'value' cannot be null or empty.", IllegalArgumentException::new);
        }

        if (this.params == null) {
            this.params = new HashMap<>();
        }
        this.params.put(key, value);
        return this;
    }
}
