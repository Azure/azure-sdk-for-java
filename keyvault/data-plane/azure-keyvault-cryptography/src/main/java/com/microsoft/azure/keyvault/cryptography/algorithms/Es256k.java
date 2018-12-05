package com.microsoft.azure.keyvault.cryptography.algorithms;

public class Es256k extends Ecdsa {
    public final static String ALGORITHM_NAME = "NONEwithECDSA";
	
    @Override
    public void checkDigestLength(byte[] digest) {
        if (digest.length != 32) {
            throw new IllegalArgumentException("Invalid digest length.");
        }
    }
}
