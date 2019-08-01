package com.azure.security.keyvault.keys.cryptography;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class SignatureHashResolver {

    public static final SignatureHashResolver Default = new SignatureHashResolver();

    static {
        Default.put(SignatureAlgorithm.ES256, HashAlgorithm.SHA_256);
        Default.put(SignatureAlgorithm.ES256K,HashAlgorithm.SHA_256);
        Default.put(SignatureAlgorithm.ES384, HashAlgorithm.SHA_384);

        Default.put(SignatureAlgorithm.ES512, HashAlgorithm.SHA_512);
        Default.put(SignatureAlgorithm.RS256, HashAlgorithm.SHA_256);
        Default.put(SignatureAlgorithm.RS384, HashAlgorithm.SHA_384);
        Default.put(SignatureAlgorithm.RS512, HashAlgorithm.SHA_512);
        Default.put(SignatureAlgorithm.PS256, HashAlgorithm.SHA_256);
        Default.put(SignatureAlgorithm.PS512, HashAlgorithm.SHA_512);
        Default.put(SignatureAlgorithm.PS384, HashAlgorithm.SHA_384);
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
     * @param algorithmName The algorithm name
     */
    public void remove(String algorithmName) {
        algorithms.remove(algorithmName);
    }

}
