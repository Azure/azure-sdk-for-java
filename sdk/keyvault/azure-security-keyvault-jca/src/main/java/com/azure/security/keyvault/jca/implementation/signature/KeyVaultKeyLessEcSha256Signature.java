package com.azure.security.keyvault.jca.implementation.signature;

/**
 * key vault SHA256
 */
public final class KeyVaultKeyLessEcSha256Signature extends KeyVaultKeyLessECSignature {
    /**
     * support SHA-256
     */
    public KeyVaultKeyLessEcSha256Signature() {
        super("SHA-256", "ES256");
    }
}
