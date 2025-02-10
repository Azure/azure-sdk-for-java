// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for SignatureAlgorithm.
 */
public final class SignatureAlgorithm extends ExpandableStringEnum<SignatureAlgorithm> {

    /**
     * Static value PS256 for SignatureAlgorithm.
     */
    public static final SignatureAlgorithm PS256 = fromString("PS256");

    /**
     * Static value PS384 for SignatureAlgorithm.
     */
    public static final SignatureAlgorithm PS384 = fromString("PS384");

    /**
     * Static value PS512 for SignatureAlgorithm.
     */
    public static final SignatureAlgorithm PS512 = fromString("PS512");

    /**
     * Static value RS256 for SignatureAlgorithm.
     */
    public static final SignatureAlgorithm RS256 = fromString("RS256");

    /**
     * Static value RS384 for SignatureAlgorithm.
     */
    public static final SignatureAlgorithm RS384 = fromString("RS384");

    /**
     * Static value RS512 for SignatureAlgorithm.
     */
    public static final SignatureAlgorithm RS512 = fromString("RS512");

    /**
     * Static value ES256 for SignatureAlgorithm.
     */
    public static final SignatureAlgorithm ES256 = fromString("ES256");

    /**
     * Static value ES384 for SignatureAlgorithm.
     */
    public static final SignatureAlgorithm ES384 = fromString("ES384");

    /**
     * Static value ES512 for SignatureAlgorithm.
     */
    public static final SignatureAlgorithm ES512 = fromString("ES512");

    /**
     * Static value ES256K for SignatureAlgorithm.
     */
    public static final SignatureAlgorithm ES256K = fromString("ES256K");

    /**
     * Creates a new instance of {@link SignatureAlgorithm} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link SignatureAlgorithm} which doesn't
     * have a String enum value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public SignatureAlgorithm() {
    }

    /**
     * Creates or finds a SignatureAlgorithm from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding SignatureAlgorithm.
     */
    public static SignatureAlgorithm fromString(String name) {
        return fromString(name, SignatureAlgorithm.class);
    }

    /**
     * Gets the known SignatureAlgorithm values.
     *
     * @return known SignatureAlgorithm values.
     */
    public static Collection<SignatureAlgorithm> values() {
        return values(SignatureAlgorithm.class);
    }
}
