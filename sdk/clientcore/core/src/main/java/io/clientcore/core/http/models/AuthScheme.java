// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.utils.ExpandableEnum;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents auth schemes.
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class AuthScheme implements ExpandableEnum<String> {
    private static final Map<String, AuthScheme> VALUES = new ConcurrentHashMap<>();
    private final String name;

    private AuthScheme(String name) {
        this.name = name;
    }

    @Override
    public String getValue() {
        return name;
    }

    /**
     * Creates or finds a {@link AuthScheme} for the passed {@code name}.
     *
     * <p>{@code null} will be returned if {@code name} is {@code null}.</p>
     *
     * @param name A name to look for.
     *
     * @return The corresponding {@link AuthScheme} of the provided name, or {@code null} if {@code name} was
     * {@code null}.
     */
    public static AuthScheme fromString(String name) {
        if (name == null) {
            return null;
        }

        return VALUES.computeIfAbsent(name, AuthScheme::new);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AuthScheme)) {
            return false;
        }

        AuthScheme other = (AuthScheme) obj;

        return Objects.equals(name, other.name);
    }

    /**
     * {@code OAuth2}
     */
    public static final AuthScheme OAUTH2 = fromString("oauth2");

    /**
     * {@code apiKey}
     */
    public static final AuthScheme API_KEY = fromString("apiKey");

    /**
     * {@code noAuth}
     */
    public static final AuthScheme NO_AUTH = fromString("noAuth");
}
