// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.azure.security.jca;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.util.Arrays;
import java.util.Collections;

/**
 * The Azure Key Vault security provider.
 */
public class AzureKeyManagerFactoryProvider extends Provider {

    /**
     * Stores the name.
     */
    public static final String PROVIDER_NAME = AzureKeyStore.KEY_STORE_TYPE;

    /**
     * Stores the serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Stores the information.
     */
    private static final String INFO = "Azure JCA Provider";

    /**
     * Stores the version.
     */
    private static final Double VERSION = 1.0;

    /**
     * Constructor.
     */
    public AzureKeyManagerFactoryProvider() {
        super(PROVIDER_NAME, VERSION, INFO);
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
                    AzureKeyManagerFactory.class.getName(),
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
                    AzureKeyStore.class.getName(),
                    Collections.singletonList("DKS"),
                    null
                )
            );
            putService(
                new Provider.Service(
                    this,
                    "KeyStore",
                    AzureKeyStore.ALGORITHM_NAME,
                    AzureKeyStore.class.getName(),
                    Collections.singletonList(AzureKeyStore.ALGORITHM_NAME),
                    null
                )
            );
            return null;
        });
    }
}
