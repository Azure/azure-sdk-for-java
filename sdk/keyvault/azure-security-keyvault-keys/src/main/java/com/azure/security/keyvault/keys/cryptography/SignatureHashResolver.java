// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class SignatureHashResolver {

    public static final SignatureHashResolver DEFAULT = new SignatureHashResolver();

    static {
        DEFAULT.put(SignatureAlgorithm.ES256, HashAlgorithm.SHA_256);
        DEFAULT.put(SignatureAlgorithm.ES256K, HashAlgorithm.SHA_256);
        DEFAULT.put(SignatureAlgorithm.ES384, HashAlgorithm.SHA_384);

        DEFAULT.put(SignatureAlgorithm.ES512, HashAlgorithm.SHA_512);
        DEFAULT.put(SignatureAlgorithm.RS256, HashAlgorithm.SHA_256);
        DEFAULT.put(SignatureAlgorithm.RS384, HashAlgorithm.SHA_384);
        DEFAULT.put(SignatureAlgorithm.RS512, HashAlgorithm.SHA_512);
        DEFAULT.put(SignatureAlgorithm.PS256, HashAlgorithm.SHA_256);
        DEFAULT.put(SignatureAlgorithm.PS512, HashAlgorithm.SHA_512);
        DEFAULT.put(SignatureAlgorithm.PS384, HashAlgorithm.SHA_384);
    }

    private final ConcurrentMap<SignatureAlgorithm, HashAlgorithm> algorithms = new ConcurrentHashMap<>();

    /**
     * Returns the hash algorithm used for signature algorithm.
     *
     * @param signatureAlgorithm The signature algorithm.
     * @return The hash algorithm or null.
     */
    public HashAlgorithm get(SignatureAlgorithm signatureAlgorithm) {
        return algorithms.get(signatureAlgorithm);
    }

    /**
     * Add/Update a named algorithm implementation.
     *
     * @param algorithm The signature algorithm.
     * @param hashAlgorithm The hash algorithm.
     */
    public void put(SignatureAlgorithm algorithm, HashAlgorithm hashAlgorithm) {
        algorithms.put(algorithm, hashAlgorithm);
    }

    /**
     * Remove a named algorithm implementation.
     *
     * @param algorithm The algorithm to be removed
     */
    public void remove(SignatureAlgorithm algorithm) {
        algorithms.remove(algorithm);
    }

}
