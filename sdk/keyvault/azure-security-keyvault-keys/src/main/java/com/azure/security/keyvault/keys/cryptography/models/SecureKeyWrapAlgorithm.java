// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for the algorithms used by the secure key wrap and secure key unwrap operations.
 * <p>
 * Secure key wrap and unwrap operations are only supported remotely and cannot be performed locally.
 */
public final class SecureKeyWrapAlgorithm extends ExpandableStringEnum<SecureKeyWrapAlgorithm> {
    /**
     * RSAES using Optimal Asymmetric Encryption Padding with a hash function of SHA-256 and a mask generation function
     * of MGF1 with SHA-256.
     */
    public static final SecureKeyWrapAlgorithm RSA_OAEP_256 = fromString("RSA-OAEP-256");

    /**
     * 128-bit AES key wrap.
     */
    public static final SecureKeyWrapAlgorithm A128KW = fromString("A128KW");

    /**
     * 192-bit AES key wrap.
     */
    public static final SecureKeyWrapAlgorithm A192KW = fromString("A192KW");

    /**
     * 256-bit AES key wrap.
     */
    public static final SecureKeyWrapAlgorithm A256KW = fromString("A256KW");

    /**
     * 128-bit AES key wrap with padding.
     */
    public static final SecureKeyWrapAlgorithm A128KWPAD = fromString("A128KWPAD");

    /**
     * 192-bit AES key wrap with padding.
     */
    public static final SecureKeyWrapAlgorithm A192KWPAD = fromString("A192KWPAD");

    /**
     * 256-bit AES key wrap with padding.
     */
    public static final SecureKeyWrapAlgorithm A256KWPAD = fromString("A256KWPAD");

    /**
     * CKM AES key wrap.
     */
    public static final SecureKeyWrapAlgorithm CKM_AES_KEY_WRAP = fromString("CKM_AES_KEY_WRAP");

    /**
     * CKM AES key wrap with padding.
     */
    public static final SecureKeyWrapAlgorithm CKM_AES_KEY_WRAP_PAD = fromString("CKM_AES_KEY_WRAP_PAD");

    /**
     * Creates a new instance of {@link SecureKeyWrapAlgorithm} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link SecureKeyWrapAlgorithm} which doesn't
     * have a String enum value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public SecureKeyWrapAlgorithm() {
    }

    /**
     * Creates or finds a SecureKeyWrapAlgorithm from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding SecureKeyWrapAlgorithm.
     */
    public static SecureKeyWrapAlgorithm fromString(String name) {
        return fromString(name, SecureKeyWrapAlgorithm.class);
    }

    /**
     * Gets known SecureKeyWrapAlgorithm values.
     *
     * @return known SecureKeyWrapAlgorithm values.
     */
    public static Collection<SecureKeyWrapAlgorithm> values() {
        return values(SecureKeyWrapAlgorithm.class);
    }
}
