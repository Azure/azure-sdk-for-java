// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import com.azure.security.keyvault.jca.implementation.signature.AbstractKeyVaultKeylessSignature;
import com.azure.security.keyvault.jca.implementation.signature.KeyVaultKeylessEcSha256Signature;
import com.azure.security.keyvault.jca.implementation.signature.KeyVaultKeylessEcSha384Signature;
import com.azure.security.keyvault.jca.implementation.signature.KeyVaultKeylessEcSha512Signature;
import com.azure.security.keyvault.jca.implementation.signature.KeyVaultKeylessRsa512Signature;
import com.azure.security.keyvault.jca.implementation.signature.KeyVaultKeylessRsaSignature;

import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.util.stream.Stream;

/**
 * The Azure Key Vault security provider.
 *
 * @see Provider
 */
public final class KeyVaultSigProvider extends Provider {

    /**
     * Stores the name.
     */
    public static final String PROVIDER_NAME = "KeyVaultSig";

    /**
     * Stores the serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Stores the information.
     */
    private static final String INFO = "Azure Key Vault Sig Provider";

    /**
     * Stores the version.
     */
    private static final Double VERSION = 1.0;

    /**
     * Constructor.
     */
    public KeyVaultSigProvider() {
        super(PROVIDER_NAME, VERSION, INFO);
        initialize();
    }

    /**
     * Initialize the provider.
     */
    @SuppressWarnings("removal")
    private void initialize() {
        java.security.AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            Stream.of(
                KeyVaultKeylessRsaSignature.class,
                KeyVaultKeylessRsa512Signature.class,
                KeyVaultKeylessEcSha256Signature.class,
                KeyVaultKeylessEcSha384Signature.class,
                KeyVaultKeylessEcSha512Signature.class)
                .forEach(c -> putService(
                    new Service(
                        this,
                        "Signature",
                        getAlgorithmName(c),
                        c.getName(),
                        null,
                        null
                    )
                ));
            return null;
        });
    }


    private String getAlgorithmName(Class<? extends AbstractKeyVaultKeylessSignature> c) {
        try {
            return c.getDeclaredConstructor().newInstance().getAlgorithmName();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return "";
        }
    }
}
