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
     * RSASSA-PSS using SHA-256 and MGF1 with SHA-256, as described in
     * <a href="https://tools.ietf.org/html/rfc7518">RFC7518</a>.
     */
    public static final SignatureAlgorithm PS256 = fromString("PS256");

    /**
     * RSASSA-PSS using SHA-384 and MGF1 with SHA-384, as described in
     * <a href="https://tools.ietf.org/html/rfc7518">RFC7518</a>.
     */
    public static final SignatureAlgorithm PS384 = fromString("PS384");

    /**
     * RSASSA-PSS using SHA-512 and MGF1 with SHA-512, as described in
     * <a href="https://tools.ietf.org/html/rfc7518">RFC7518</a>.
     */
    public static final SignatureAlgorithm PS512 = fromString("PS512");

    /**
     * RSASSA-PKCS1-v1_5 using SHA-256, as described in <a href="https://tools.ietf.org/html/rfc7518">RFC7518</a>.
     */
    public static final SignatureAlgorithm RS256 = fromString("RS256");

    /**
     * RSASSA-PKCS1-v1_5 using SHA-384, as described in <a href="https://tools.ietf.org/html/rfc7518">RFC7518</a>.
     */
    public static final SignatureAlgorithm RS384 = fromString("RS384");

    /**
     * RSASSA-PKCS1-v1_5 using SHA-512, as described in <a href="https://tools.ietf.org/html/rfc7518">RFC7518</a>.
     */
    public static final SignatureAlgorithm RS512 = fromString("RS512");

    /**
     * ECDSA using P-256 and SHA-256, as described in <a href="https://tools.ietf.org/html/rfc7518">RFC7518</a>.
     */
    public static final SignatureAlgorithm ES256 = fromString("ES256");

    /**
     * ECDSA using P-384 and SHA-384, as described in <a href="https://tools.ietf.org/html/rfc7518">RFC7518</a>.
     */
    public static final SignatureAlgorithm ES384 = fromString("ES384");

    /**
     * ECDSA using P-521 and SHA-512, as described in <a href="https://tools.ietf.org/html/rfc7518">RFC7518</a>.
     */
    public static final SignatureAlgorithm ES512 = fromString("ES512");

    /**
     * ECDSA using P-256K and SHA-256, as described in <a href="https://tools.ietf.org/html/rfc7518">RFC7518</a>.
     */
    public static final SignatureAlgorithm ES256K = fromString("ES256K");

    /**
     * HMAC using SHA-256, as described in <a href="https://tools.ietf.org/html/rfc7518">RFC7518</a>.
     */
    public static final SignatureAlgorithm HS256 = fromString("HS256");

    /**
     * HMAC using SHA-384, as described in <a href="https://tools.ietf.org/html/rfc7518">RFC7518</a>.
     */
    public static final SignatureAlgorithm HS384 = fromString("HS384");

    /**
     * HMAC using SHA-512, as described in <a href="https://tools.ietf.org/html/rfc7518">RFC7518</a>.
     */
    public static final SignatureAlgorithm HS512 = fromString("HS512");

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
