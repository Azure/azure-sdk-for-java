// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.credentials.oauth;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.utils.ExpandableEnum;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents Access Token types.
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class AccessTokenType implements ExpandableEnum<String> {
    private static final Map<String, AccessTokenType> VALUES = new ConcurrentHashMap<>();
    private final String caseSensitive;
    private final String caseInsensitive;

    private AccessTokenType(String name) {
        this.caseSensitive = name;
        this.caseInsensitive = name.toLowerCase();
    }

    @Override
    public String getValue() {
        return caseSensitive;
    }

    /**
     * Gets the {@link AccessTokenType} based on the name passed into {@link #fromString(String)}.
     *
     * @return The token type based on the construction of this {@link AccessTokenType}.
     */
    public String getCaseSensitiveName() {
        return toString();
    }

    /**
     * Gets the {@link AccessTokenType} lower cased.
     *
     * @return The {@link AccessTokenType} lower cased.
     */
    public String getCaseInsensitiveName() {
        return caseInsensitive;
    }

    /**
     * Creates or finds a {@link AccessTokenType} for the passed {@code name}.
     *
     * <p>{@code null} will be returned if {@code name} is {@code null}.</p>
     *
     * @param name A name to look for.
     *
     * @return The corresponding {@link AccessTokenType} of the provided name, or {@code null} if {@code name} was
     * {@code null}.
     */
    public static AccessTokenType fromString(String name) {
        if (name == null) {
            return null;
        }

        AccessTokenType accessTokenType = VALUES.get(name);

        if (accessTokenType != null) {
            return accessTokenType;
        }

        return VALUES.computeIfAbsent(name, AccessTokenType::new);
    }

    @Override
    public String toString() {
        return caseSensitive;
    }

    @Override
    public int hashCode() {
        return caseInsensitive.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AccessTokenType)) {
            return false;
        }

        AccessTokenType other = (AccessTokenType) obj;
        return Objects.equals(caseInsensitive, other.caseInsensitive);
    }

    /**
     * The Bearer token type.
     */
    public static final AccessTokenType BEARER = fromString("Bearer");

    /**
     * The Pop token type.
     */
    public static final AccessTokenType POP = fromString("Pop");
}
