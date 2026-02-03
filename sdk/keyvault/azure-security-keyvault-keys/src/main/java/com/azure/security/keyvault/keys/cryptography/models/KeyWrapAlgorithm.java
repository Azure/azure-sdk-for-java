// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for KeyWrapAlgorithm.
 */
public final class KeyWrapAlgorithm extends ExpandableStringEnum<KeyWrapAlgorithm> {
    /**
     * [Not recommended] RSAES using Optimal Asymmetric Encryption Padding (OAEP), as described in
     * https://tools.ietf.org/html/rfc3447, with the default parameters specified by RFC 3447 in Section A.2.1. Those
     * default parameters are using a hash function of SHA-1 and a mask generation function of MGF1 with SHA-1.
     * Microsoft recommends using RSA_OAEP_256 or stronger algorithms for enhanced security. Microsoft does *not*
     * recommend RSA_OAEP, which is included solely for backwards compatibility. RSA_OAEP utilizes SHA1, which has known
     * collision problems.
     */
    public static final KeyWrapAlgorithm RSA_OAEP = fromString("RSA-OAEP");

    /**
     * RSAES using Optimal Asymmetric Encryption Padding with a hash function of SHA-256 and a mask generation function
     * of MGF1 with SHA-256.
     */
    public static final KeyWrapAlgorithm RSA_OAEP_256 = fromString("RSA-OAEP-256");

    /**
     * [Not recommended] RSAES-PKCS1-V1_5 key encryption, as described in https://tools.ietf.org/html/rfc3447. Microsoft
     * recommends using RSA_OAEP_256 or stronger algorithms for enhanced security. Microsoft does *not* recommend
     * RSA_1_5, which is included solely for backwards compatibility. Cryptographic standards no longer consider RSA
     * with the PKCS#1 v1.5 padding scheme secure for encryption.
     */
    public static final KeyWrapAlgorithm RSA1_5 = fromString("RSA1_5");

    /**
     * 128-bit AES key wrap.
     */
    public static final KeyWrapAlgorithm A128KW = fromString("A128KW");

    /**
     * 192-bit AES key wrap.
     */
    public static final KeyWrapAlgorithm A192KW = fromString("A192KW");

    /**
     * 256-bit AES key wrap.
     */
    public static final KeyWrapAlgorithm A256KW = fromString("A256KW");

    /**
     * CKM AES key wrap.
     */
    public static final KeyWrapAlgorithm CKM_AES_KEY_WRAP = fromString("CKM_AES_KEY_WRAP");

    /**
     * CKM AES key wrap with padding.
     */
    public static final KeyWrapAlgorithm CKM_AES_KEY_WRAP_PAD = fromString("CKM_AES_KEY_WRAP_PAD");

    /**
     * Creates a new instance of {@link KeyWrapAlgorithm} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link KeyWrapAlgorithm} which doesn't
     * have a String enum value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public KeyWrapAlgorithm() {
    }

    /**
     * Creates or finds a KeyWrapAlgorithm from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding KeyWrapAlgorithm.
     */
    public static KeyWrapAlgorithm fromString(String name) {
        return fromString(name, KeyWrapAlgorithm.class);
    }

    /**
     * Gets known KeyWrapAlgorithm values.
     *
     * @return known KeyWrapAlgorithm values.
     */
    public static Collection<KeyWrapAlgorithm> values() {
        return values(KeyWrapAlgorithm.class);
    }
}
