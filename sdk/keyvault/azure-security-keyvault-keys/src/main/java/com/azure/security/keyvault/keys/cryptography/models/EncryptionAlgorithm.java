// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for EncryptionAlgorithm.
 */
public final class EncryptionAlgorithm extends ExpandableStringEnum<EncryptionAlgorithm> {
    /**
     * [Not recommended] RSAES using Optimal Asymmetric Encryption Padding (OAEP), as described in
     * https://tools.ietf.org/html/rfc3447, with the default parameters specified by RFC 3447 in Section A.2.1. Those
     * default parameters are using a hash function of SHA-1 and a mask generation function of MGF1 with SHA-1.
     * Microsoft recommends using RSA_OAEP_256 or stronger algorithms for enhanced security. Microsoft does *not*
     * recommend RSA_OAEP, which is included solely for backwards compatibility. RSA_OAEP utilizes SHA1, which has known
     * collision problems.
     */
    public static final EncryptionAlgorithm RSA_OAEP = fromString("RSA-OAEP");

    /**
     * RSAES using Optimal Asymmetric Encryption Padding with a hash function of SHA-256 and a mask generation function
     * of MGF1 with SHA-256.
     */
    public static final EncryptionAlgorithm RSA_OAEP_256 = fromString("RSA-OAEP-256");

    /**
     * [Not recommended] RSAES-PKCS1-V1_5 key encryption, as described in https://tools.ietf.org/html/rfc3447. Microsoft
     * recommends using RSA_OAEP_256 or stronger algorithms for enhanced security. Microsoft does *not* recommend
     * RSA_1_5, which is included solely for backwards compatibility. Cryptographic standards no longer consider RSA
     * with the PKCS#1 v1.5 padding scheme secure for encryption.
     */
    public static final EncryptionAlgorithm RSA1_5 = fromString("RSA1_5");

    /**
     * 128-bit AES-CBC.
     */
    public static final EncryptionAlgorithm A128CBC = fromString("A128CBC");

    /**
     * 192-bit AES-CBC.
     */
    public static final EncryptionAlgorithm A192CBC = fromString("A192CBC");

    /**
     * 256-bit AES-CBC.
     */
    public static final EncryptionAlgorithm A256CBC = fromString("A256CBC");

    /**
     * 128-bit AES-CBC with PKCS padding.
     */
    public static final EncryptionAlgorithm A128CBCPAD = fromString("A128CBCPAD");

    /**
     * 192-bit AES-CBC with PKCS padding.
     */
    public static final EncryptionAlgorithm A192CBCPAD = fromString("A192CBCPAD");

    /**
     * 256-bit AES-CBC with PKCS padding.
     */
    public static final EncryptionAlgorithm A256CBCPAD = fromString("A256CBCPAD");

    /**
     * 128-bit AES-CBC with 256-bit HMAC.
     */
    public static final EncryptionAlgorithm A128CBC_HS256 = fromString("A128CBC-HS256");

    /**
     * 256-bit AES-CBC with 384-bit HMAC.
     */
    public static final EncryptionAlgorithm A192CBC_HS384 = fromString("A192CBC-HS384");

    /**
     * 256-bit AES-CBC with 512-bit HMAC.
     */
    public static final EncryptionAlgorithm A256CBC_HS512 = fromString("A256CBC-HS512");

    /**
     * 128-bit AES-GCM.
     */
    public static final EncryptionAlgorithm A128GCM = fromString("A128GCM");

    /**
     * 192-bit AES-GCM.
     */
    public static final EncryptionAlgorithm A192GCM = fromString("A192GCM");

    /**
     * 256-bit AES-GCM.
     */
    public static final EncryptionAlgorithm A256GCM = fromString("A256GCM");

    /**
     * 128-bit AES key wrap.
     *
     * @deprecated This value is not supported for encrypt/decrypt operations. For key wrapping/unwrapping, use
     * {@link KeyWrapAlgorithm#A128KW} in {@link KeyWrapAlgorithm} instead.
     */
    @Deprecated
    public static final EncryptionAlgorithm A128KW = fromString("A128KW");

    /**
     * 192-bit AES key wrap.
     *
     * @deprecated This value is not supported for encrypt/decrypt operations. For key wrapping/unwrapping, use
     * {@link KeyWrapAlgorithm#A192KW} in {@link KeyWrapAlgorithm} instead.
     */
    @Deprecated
    public static final EncryptionAlgorithm A192KW = fromString("A192KW");

    /**
     * 256-bit AES key wrap.
     *
     * @deprecated This value is not supported for encrypt/decrypt operations. For key wrapping/unwrapping, use
     * {@link KeyWrapAlgorithm#A256KW} in {@link KeyWrapAlgorithm} instead.
     */
    @Deprecated
    public static final EncryptionAlgorithm A256KW = fromString("A256KW");

    /**
     * Creates a new instance of {@link EncryptionAlgorithm} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link EncryptionAlgorithm} which doesn't
     * have a String enum value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public EncryptionAlgorithm() {
    }

    /**
     * Creates or finds a EncryptionAlgorithm from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding EncryptionAlgorithm.
     */
    public static EncryptionAlgorithm fromString(String name) {
        return fromString(name, EncryptionAlgorithm.class);
    }

    /**
     * Gets known EncryptionAlgorithm values.
     *
     * @return known EncryptionAlgorithm values.
     */
    public static Collection<EncryptionAlgorithm> values() {
        return values(EncryptionAlgorithm.class);
    }
}
