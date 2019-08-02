package com.azure.security.keyvault.keys.cryptography;

/**
 * Represents the details of sign operation result.
 */
public final class SignResult {

    public SignResult(byte[] signature, SignatureAlgorithm algorithm){
        this.signature = signature;
        this.algorithm = algorithm;
    }

    /**
     * The signature created from the digest.
     */
    private byte[] signature;

    /**
     * The algorithm used to create the signature.
     */
    private SignatureAlgorithm algorithm;

    /**
     * Get the signature created from the digest.
     * @return The signature.
     */
    public byte[] signature() {
        return signature;
    }

    public SignResult signature(byte[] signature) {
        this.signature = signature;
        return this;
    }

    /**
     * Get the signature algorithm used to create the signature.
     * @return The signature algorithm.
     */
    public SignatureAlgorithm algorithm() {
        return algorithm;
    }

    public SignResult algorithm(SignatureAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }
}
