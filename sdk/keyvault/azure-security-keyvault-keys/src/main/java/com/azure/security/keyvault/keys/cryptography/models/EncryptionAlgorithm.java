// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * Defines values for EncryptionAlgorithm.
 */
public final class EncryptionAlgorithm extends ExpandableStringEnum<EncryptionAlgorithm> {

    /**
     * Static value RSA_OAEP for EncryptionAlgorithm.
     */
    public static final EncryptionAlgorithm RSA_OAEP = fromString("RSA-OAEP");

    /**
     * Static value RSA_OAEP_256 for EncryptionAlgorithm.
     */
    public static final EncryptionAlgorithm RSA_OAEP_256 = fromString("RSA-OAEP-256");

    /**
     * Static value RSA1_5 for EncryptionAlgorithm.
     */
    public static final EncryptionAlgorithm RSA1_5 = fromString("RSA1_5");

    /**
     * Static value A128CBC for EncryptionAlgorithm.
     */
    public static final EncryptionAlgorithm A128CBC = fromString("A128CBC");

    /**
     * Static value A192CBC for EncryptionAlgorithm.
     */
    public static final EncryptionAlgorithm A192CBC = fromString("A192CBC");

    /**
     * Static value A256CBC for EncryptionAlgorithm.
     */
    public static final EncryptionAlgorithm A256CBC = fromString("A256CBC");

    /**
     * Static value A128CBCPAD for EncryptionAlgorithm.
     */
    public static final EncryptionAlgorithm A128CBCPAD = fromString("A128CBCPAD");

    /**
     * Static value A192CBCPAD for EncryptionAlgorithm.
     */
    public static final EncryptionAlgorithm A192CBCPAD = fromString("A192CBCPAD");

    /**
     * Static value A256CBCPAD for EncryptionAlgorithm.
     */
    public static final EncryptionAlgorithm A256CBCPAD = fromString("A256CBCPAD");

    /**
     * Static value A128CBC_HS256 for EncryptionAlgorithm.
     */
    public static final EncryptionAlgorithm A128CBC_HS256 = fromString("A128CBC-HS256");

    /**
     * Static value A192CBC_HS384 for EncryptionAlgorithm.
     */
    public static final EncryptionAlgorithm A192CBC_HS384 = fromString("A192CBC-HS384");

    /**
     * Static value A256CBC_HS512 for EncryptionAlgorithm.
     */
    public static final EncryptionAlgorithm A256CBC_HS512 = fromString("A256CBC-HS512");

    /**
     * Static value A128GCM for EncryptionAlgorithm.
     */
    public static final EncryptionAlgorithm A128GCM = fromString("A128GCM");

    /**
     * Static value A192GCM for EncryptionAlgorithm.
     */
    public static final EncryptionAlgorithm A192GCM = fromString("A192GCM");

    /**
     * Static value A256GCM for EncryptionAlgorithm.
     */
    public static final EncryptionAlgorithm A256GCM = fromString("A256GCM");

    /**
     * Static value A128KW for EncryptionAlgorithm.
     */
    public static final EncryptionAlgorithm A128KW = fromString("A128KW");

    /**
     * Static value A192KW for EncryptionAlgorithm.
     */
    public static final EncryptionAlgorithm A192KW = fromString("A192KW");

    /**
     * Static value A256KW for EncryptionAlgorithm.
     */
    public static final EncryptionAlgorithm A256KW = fromString("A256KW");

    /**
     * Creates or finds a EncryptionAlgorithm from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding EncryptionAlgorithm.
     */
    @JsonCreator
    public static EncryptionAlgorithm fromString(String name) {
        return fromString(name, EncryptionAlgorithm.class);
    }

    /**
     * @return known EncryptionAlgorithm values.
     */
    public static Collection<EncryptionAlgorithm> values() {
        return values(EncryptionAlgorithm.class);
    }
}
