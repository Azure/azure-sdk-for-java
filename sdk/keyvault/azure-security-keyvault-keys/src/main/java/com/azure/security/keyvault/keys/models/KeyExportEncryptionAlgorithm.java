// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/** Defines values for KeyExportEncryptionAlgorithm. */
public final class KeyExportEncryptionAlgorithm extends ExpandableStringEnum<KeyExportEncryptionAlgorithm> {
    /** Static value CKM_RSA_AES_KEY_WRAP for KeyExportEncryptionAlgorithm. */
    public static final KeyExportEncryptionAlgorithm CKM_RSA_AES_KEY_WRAP = fromString("CKM_RSA_AES_KEY_WRAP");

    /** Static value RSA_AES_KEY_WRAP_256 for KeyExportEncryptionAlgorithm. */
    public static final KeyExportEncryptionAlgorithm RSA_AES_KEY_WRAP_256 = fromString("RSA_AES_KEY_WRAP_256");

    /** Static value RSA_AES_KEY_WRAP_384 for KeyExportEncryptionAlgorithm. */
    public static final KeyExportEncryptionAlgorithm RSA_AES_KEY_WRAP_384 = fromString("RSA_AES_KEY_WRAP_384");

    /**
     * Creates or finds a {@link KeyExportEncryptionAlgorithm} from its string representation.
     *
     * @param name A name to look for.
     *
     * @return The corresponding {@link KeyExportEncryptionAlgorithm}.
     */
    @JsonCreator
    public static KeyExportEncryptionAlgorithm fromString(String name) {
        return fromString(name, KeyExportEncryptionAlgorithm.class);
    }

    /**
     * Get a collection of all known {@link KeyExportEncryptionAlgorithm} values
     *
     * @return All known {@link KeyExportEncryptionAlgorithm} values.
     */
    public static Collection<KeyExportEncryptionAlgorithm> values() {
        return values(KeyExportEncryptionAlgorithm.class);
    }
}
