// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.utils;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.Collections;
import java.util.Map;

/**
 * An authenticate challenge.
 * <p>
 * This challenge can be from any source, but will primarily be from parsing {@link HttpHeaderName#WWW_AUTHENTICATE} or
 * {@link HttpHeaderName#PROXY_AUTHENTICATE} headers using {@link AuthUtils#parseAuthenticateHeader(String)}.
 * <p>
 * Some challenge information may be optional, meaning the getters may return null or an empty collection.
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class AuthenticateChallenge {
    private static final ClientLogger LOGGER = new ClientLogger(AuthenticateChallenge.class);

    private final String scheme;
    private final Map<String, String> parameters;
    private final String token68;

    /**
     * Creates an instance of the AuthenticateChallenge.
     *
     * @param scheme The scheme of the challenge.
     * @throws IllegalArgumentException If the scheme is null or empty.
     */
    public AuthenticateChallenge(String scheme) {
        this(scheme, Collections.emptyMap(), null);
    }

    /**
     * Creates an instance of the AuthenticateChallenge.
     *
     * @param scheme The scheme of the challenge.
     * @param token68 The token68 of the challenge.
     * @throws IllegalArgumentException If the scheme is null or empty.
     */
    public AuthenticateChallenge(String scheme, String token68) {
        this(scheme, Collections.emptyMap(), token68);
    }

    /**
     * Creates an instance of the AuthenticateChallenge.
     *
     * @param scheme The scheme of the challenge.
     * @param parameters The parameters of the challenge.
     * @throws IllegalArgumentException If the scheme is null or empty.
     */
    public AuthenticateChallenge(String scheme, Map<String, String> parameters) {
        this(scheme, parameters, null);
    }

    AuthenticateChallenge(String scheme, Map<String, String> parameters, String token68) {
        if (CoreUtils.isNullOrEmpty(scheme)) {
            throw LOGGER.throwableAtError().log("scheme cannot be null or empty.", IllegalArgumentException::new);
        }

        this.scheme = scheme;
        this.parameters = Collections.unmodifiableMap(parameters);
        this.token68 = token68;
    }

    /**
     * Gets the scheme of the challenge.
     *
     * @return The scheme of the challenge.
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Gets the parameters of the challenge as a read-only map.
     * <p>
     * This map will be empty if the challenge does not have any parameters.
     *
     * @return The parameters of the challenge.
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Gets the token68 of the challenge.
     * <p>
     * This may be null if the challenge does not have a token68.
     *
     * @return The token68 of the challenge, or null if the challenge does not have a token68.
     */
    public String getToken68() {
        return token68;
    }
}
