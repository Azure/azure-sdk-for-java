package com.azure.security.keyvault.jca.implementation.signature;

/**
 * key vault SHA512
 */
public final class KeyVaultKeyLessEcSha512Signature extends KeyVaultKeyLessECSignature {
    /**
     * support SHA-512
     */
    public KeyVaultKeyLessEcSha512Signature() {
        super("SHA-512", "ES512");
    }
}
