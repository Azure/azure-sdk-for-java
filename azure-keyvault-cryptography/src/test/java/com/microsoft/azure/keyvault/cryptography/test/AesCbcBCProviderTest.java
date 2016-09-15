package com.microsoft.azure.keyvault.cryptography.test;

import java.security.Provider;

import org.junit.Before;

public class AesCbcBCProviderTest extends AesCbcTest {

    @Before
    public void setUp() throws Exception {
        try {
            super.setProvider((Provider) Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider").newInstance());
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

}
