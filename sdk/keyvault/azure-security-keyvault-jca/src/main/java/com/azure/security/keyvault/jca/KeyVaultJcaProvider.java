// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import com.azure.security.keyvault.jca.implementation.signature.KeyVaultKeyLessRsaSignature;
import com.azure.security.keyvault.jca.implementation.signature.KeyVaultKeyLessEcSha384Signature;
import com.azure.security.keyvault.jca.implementation.signature.KeyVaultKeyLessEcSha512Signature;
import com.azure.security.keyvault.jca.implementation.signature.KeyVaultKeyLessEcSha256Signature;
import com.azure.security.keyvault.jca.implementation.signature.AbstractKeyVaultKeyLessSignature;

import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * The Azure Key Vault security provider.
 *
 * @see Provider
 */
public final class KeyVaultJcaProvider extends Provider {

    /**
     * Stores the name.
     */
    public static final String PROVIDER_NAME = KeyVaultKeyStore.KEY_STORE_TYPE;

    /**
     * Stores the serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Stores the information.
     */
    private static final String INFO = "Azure Key Vault JCA Provider";

    /**
     * Stores the version.
     */
    private static final Double VERSION = 1.0;

    /**
     * Constructor.
     */
    public KeyVaultJcaProvider() {
        super(PROVIDER_NAME, VERSION, INFO);
        initialize();
    }

    /**
     * Initialize the provider.
     */
    @SuppressWarnings("removal")
    private void initialize() {
        java.security.AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            putService(
                new Provider.Service(
                    this,
                    "KeyManagerFactory",
                    "SunX509",
                    KeyVaultKeyManagerFactory.class.getName(),
                    Arrays.asList("SunX509", "IbmX509"),
                    null
                )
            );

            /*
             * Note for Tomcat we needed to add "DKS" as an algorithm so it does
             * not use an in-memory key store and later on can wrap the
             * KeyManager using its JSSEKeyManager so the key alias is known.
             *
             * See SSLUtilBase.getKeyManagers and look for the
             * "DKS".equalsIgnoreCase(certificate.getCertificateKeystoreType()
             */
            putService(
                new Provider.Service(
                    this,
                    "KeyStore",
                    "DKS",
                    KeyVaultKeyStore.class.getName(),
                    Collections.singletonList("DKS"),
                    null
                )
            );
            putService(
                new Provider.Service(
                    this,
                    "KeyStore",
                    KeyVaultKeyStore.ALGORITHM_NAME,
                    KeyVaultKeyStore.class.getName(),
                    Collections.singletonList(KeyVaultKeyStore.ALGORITHM_NAME),
                    null
                )
            );
            Stream.of(
                KeyVaultKeyLessRsaSignature.class,
                KeyVaultKeyLessEcSha256Signature.class,
                KeyVaultKeyLessEcSha384Signature.class,
                KeyVaultKeyLessEcSha512Signature.class)
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


    private String getAlgorithmName(Class<? extends AbstractKeyVaultKeyLessSignature> c) {
        try {
            return c.getDeclaredConstructor().newInstance().getAlgorithmName();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return "";
        }
    }
}
