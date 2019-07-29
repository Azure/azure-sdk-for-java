package com.azure.security.keyvault.keys.cryptography;


public class SignResult {

    private byte[] signature;

    private SignatureAlgorithm algorithm;

    public byte[] signature() {
        return signature;
    }

    public SignResult signature(byte[] signature) {
        this.signature = signature;
        return this;
    }

    public SignatureAlgorithm algorithm() {
        return algorithm;
    }

    public SignResult algorithm(SignatureAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }
}
