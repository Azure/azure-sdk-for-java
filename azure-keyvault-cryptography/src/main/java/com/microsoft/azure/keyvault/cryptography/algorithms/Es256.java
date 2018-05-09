package com.microsoft.azure.keyvault.cryptography.algorithms;

public class Es256 extends Ecdsa {
    public final static String ALGORITHM_NAME = "SHA256withECDSA";
	
    @Override
    public void checkDigestLength(byte[] digest) {
        if (digest.length != 32) {
            throw new IllegalArgumentException("Please check the hash of the digest.");
        }
    }
}
