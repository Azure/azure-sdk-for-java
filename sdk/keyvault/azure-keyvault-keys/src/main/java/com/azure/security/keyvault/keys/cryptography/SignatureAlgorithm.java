package com.azure.security.keyvault.keys.cryptography;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for SignatureAlgorithm.
 */
public final class SignatureAlgorithm {

    /** Static value PS256 for SignatureAlgorithm. */
    public static final SignatureAlgorithm PS256 = new SignatureAlgorithm("PS256");

    /** Static value PS384 for SignatureAlgorithm. */
    public static final SignatureAlgorithm PS384 = new SignatureAlgorithm("PS384");

    /** Static value PS512 for SignatureAlgorithm. */
    public static final SignatureAlgorithm PS512 = new SignatureAlgorithm("PS512");

    /** Static value RS256 for SignatureAlgorithm. */
    public static final SignatureAlgorithm RS256 = new SignatureAlgorithm("RS256");

    /** Static value RS384 for SignatureAlgorithm. */
    public static final SignatureAlgorithm RS384 = new SignatureAlgorithm("RS384");

    /** Static value RS512 for SignatureAlgorithm. */
    public static final SignatureAlgorithm RS512 = new SignatureAlgorithm("RS512");

    /** Static value RSNULL for SignatureAlgorithm. */
    public static final SignatureAlgorithm RSNULL = new SignatureAlgorithm("RSNULL");
    /** Static value ES256 for SignatureAlgorithm. */
    public static final SignatureAlgorithm ES256 = new SignatureAlgorithm("ES256");
    /** Static value ES384 for SignatureAlgorithm. */
    public static final SignatureAlgorithm ES384 = new SignatureAlgorithm("ES384");
    /** Static value ES512 for SignatureAlgorithm. */
    public static final SignatureAlgorithm ES512 = new SignatureAlgorithm("ES512");
    /** Static value ECDSA256 for SignatureAlgorithm. */
    public static final SignatureAlgorithm ES256K = new SignatureAlgorithm("ES256K");

    private String value;

    /**
     * Creates a custom value for SignatureAlgorithm.
     *
     * @param value
     *            the custom value
     */
    public SignatureAlgorithm(String value) {
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
        if (!(obj instanceof SignatureAlgorithm)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        SignatureAlgorithm rhs = (SignatureAlgorithm) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }

    /**
     * All the JWK signature algorithms.
     */
    public static final List<SignatureAlgorithm> ALL_ALGORITHMS = Collections.unmodifiableList(
            Arrays.asList(RS256, RS384, RS512, RSNULL, PS256, PS384, PS512, ES256, ES384, ES512, ES256K));
}
