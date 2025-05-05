// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

import com.azure.v2.security.keyvault.keys.cryptography.models.SignatureAlgorithm;

import java.util.HashMap;
import java.util.Map;

final class SignatureHashResolver {
    public static final SignatureHashResolver DEFAULT;

    static {
        Map<SignatureAlgorithm, HashAlgorithm> defaultAlgorithms = new HashMap<>();

        defaultAlgorithms.put(SignatureAlgorithm.ES256, HashAlgorithm.SHA_256);
        defaultAlgorithms.put(SignatureAlgorithm.ES256K, HashAlgorithm.SHA_256);
        defaultAlgorithms.put(SignatureAlgorithm.ES384, HashAlgorithm.SHA_384);
        defaultAlgorithms.put(SignatureAlgorithm.ES512, HashAlgorithm.SHA_512);
        defaultAlgorithms.put(SignatureAlgorithm.RS256, HashAlgorithm.SHA_256);
        defaultAlgorithms.put(SignatureAlgorithm.RS384, HashAlgorithm.SHA_384);
        defaultAlgorithms.put(SignatureAlgorithm.RS512, HashAlgorithm.SHA_512);
        defaultAlgorithms.put(SignatureAlgorithm.PS256, HashAlgorithm.SHA_256);
        defaultAlgorithms.put(SignatureAlgorithm.PS512, HashAlgorithm.SHA_512);
        defaultAlgorithms.put(SignatureAlgorithm.PS384, HashAlgorithm.SHA_384);

        DEFAULT = new SignatureHashResolver(defaultAlgorithms);
    }

    private final Map<SignatureAlgorithm, HashAlgorithm> algorithms;

    private SignatureHashResolver(Map<SignatureAlgorithm, HashAlgorithm> algorithms) {
        this.algorithms = algorithms;
    }

    /**
     * Returns the hash algorithm used for signature algorithm.
     *
     * @param signatureAlgorithm The signature algorithm.
     * @return The hash algorithm or {@code null}.
     */
    public HashAlgorithm get(SignatureAlgorithm signatureAlgorithm) {
        return algorithms.get(signatureAlgorithm);
    }
}
