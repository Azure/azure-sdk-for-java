package com.microsoft.azure.keyvault.cryptography.algorithms;

public class Es512 extends Ecdsa {
    
    public final static String ALGORITHM_NAME = "SHA512withECDSA";

    @Override
    public void checkDigestLength(byte[] digest) {
        if (digest.length != 64) {
            throw new IllegalArgumentException("Please check the hash of the digest.");
        }
    }
}
