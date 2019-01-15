package com.microsoft.azure.keyvault.cryptography.algorithms;

public class Es256 extends Ecdsa {
    public final static String ALGORITHM_NAME = "ES256";
	
    @Override
    public int getDigestLength() {
        return 32;
    }

    @Override
    public int getCoordLength() {
        return 32;
    }
}
