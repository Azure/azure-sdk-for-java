package com.azure.security.keyvault.jca.implementation.signature;

/**
 * key vault SHA384
 */
public final class KeyVaultKeyLessEcSha384Signature extends KeyVaultKeyLessECSignature {
    /**
     * support SHA-384
     */
    public KeyVaultKeyLessEcSha384Signature() {
        super("SHA-384", "ES384");
    }
}
