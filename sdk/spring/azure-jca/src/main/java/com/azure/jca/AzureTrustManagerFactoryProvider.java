// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jca;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;

/**
 * The Azure Key Vault TrustManagerFactory provider.
 */
public class AzureTrustManagerFactoryProvider extends Provider {

    /**
     * Stores the serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Stores the information.
     */
    private static final String INFO = "Azure Key Vault TrustManagerFactory Provider";

    /**
     * Stores the name.
     */
    private static final String NAME = "AzureKeyVaultTrustManagerFactory";

    /**
     * Stores the version.
     */
    private static final Double VERSION = 1.0;

    /**
     * Constructor.
     */
    public AzureTrustManagerFactoryProvider() {
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
                    "TrustManagerFactory",
                    "PKIX",
                    AzureTrustManagerFactory.class.getName(),
                    null,
                    null
                )
            );
            return null;
        });
    }
}
