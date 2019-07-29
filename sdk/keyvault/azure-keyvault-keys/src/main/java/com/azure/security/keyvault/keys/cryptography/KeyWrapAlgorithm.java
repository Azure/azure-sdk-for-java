package com.azure.security.keyvault.keys.cryptography;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Defines values for EncryptionAlgorithm.
 */
public final class KeyWrapAlgorithm {

    /** Static value RSA-OAEP for EncryptionAlgorithm. */
    public static final KeyWrapAlgorithm RSA_OAEP = new KeyWrapAlgorithm("RSA-OAEP");

    /** Static value RSA-OAEP-256 for EncryptionAlgorithm. */
    public static final KeyWrapAlgorithm RSA_OAEP_256 = new KeyWrapAlgorithm("RSA-OAEP-256");

    /** Static value RSA1_5 for EncryptionAlgorithm. */
    public static final KeyWrapAlgorithm RSA1_5 = new KeyWrapAlgorithm("RSA1_5");

    private String value;

    /**
     * Creates a custom value for EncryptionAlgorithm.
     *
     * @param value
     *            the custom value
     */
    public KeyWrapAlgorithm(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof KeyWrapAlgorithm)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        KeyWrapAlgorithm rhs = (KeyWrapAlgorithm) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }

    /**
     * All the JWK encryption algorithms.
     */
    public static final List<KeyWrapAlgorithm> ALL_ALGORITHMS = Collections
            .unmodifiableList(Arrays.asList(RSA_OAEP, RSA1_5, RSA_OAEP_256));
}
