package com.microsoft.azure.keyvault.cryptography.algorithms;

public class Es384 extends Ecdsa {

    public final static String ALGORITHM_NAME = "SHA384withECDSA";

    @Override
    public void checkDigestLength(byte[] digest) {
        if (digest.length != 48) {
            throw new IllegalArgumentException("Please check the hash of the digest.");
        }
    }
}
