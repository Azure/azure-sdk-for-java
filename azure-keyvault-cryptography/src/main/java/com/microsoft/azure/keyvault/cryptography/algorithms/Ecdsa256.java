package com.microsoft.azure.keyvault.cryptography.algorithms;

public class Ecdsa256 extends Ecdsa {
    public final static String ALGORITHM_NAME = "NONEwithECDSA";
	
    @Override
    public void checkDigestLength(byte[] digest) {
        if (digest.length != 32) {
            throw new IllegalArgumentException("Please check the hash of the digest.");
        }
    }
}
