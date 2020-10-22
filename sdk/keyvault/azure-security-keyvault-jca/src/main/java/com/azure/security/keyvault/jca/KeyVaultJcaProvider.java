// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.util.Arrays;
import java.util.Collections;

/**
 * The Azure KeyVault security provider.
 */
public class KeyVaultJcaProvider extends Provider {

    /**
     * Stores the serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Stores the information.
     */
    private static final String INFO = "Azure KeyVault JCA Provider";

    /**
     * Stores the name.
     */
    private static final String NAME = "AzureKeyVault";

    /**
     * Stores the version.
     */
    private static final Double VERSION = 1.0;

    /**
     * Constructor.
     */
    public KeyVaultJcaProvider() {
        super(NAME, VERSION, INFO);
        initialize();
    }

    /**
     * Initialize the provider.
     */
    private void initialize() {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
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
                    "AzureKeyVault",
                    KeyVaultKeyStore.class.getName(),
                    Collections.singletonList("AzureKeyVault"),
                    null
                )
            );
            return null;
        });
    }
}
