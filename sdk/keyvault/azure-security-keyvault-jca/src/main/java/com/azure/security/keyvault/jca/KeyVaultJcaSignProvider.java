// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.util.Collections;

/**
 * The Azure Key Vault security provider.
 */
public class KeyVaultJcaSignProvider extends Provider {

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
    private static final String INFO = "Azure Key Vault JCA Sign Provider";

    /**
     * Stores the version.
     */
    private static final Double VERSION = 1.0;

    /**
     * Stores the name.
     */
    private static final String NAME = "AzureKeyVaultSignFactory";

    /**
     * Constructor.
     */
    public KeyVaultJcaSignProvider() {
        super(NAME, VERSION, INFO);
        initialize();
    }

    /**
     * Initialize the provider.
     */
    private void initialize() {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            putService(
                new Service(
                    this,
                    "Signature",
                    "RSASSA-PSS",
                    KeyVaultRsaSignature.class.getName(),
                    Collections.singletonList(KeyVaultKeyStore.ALGORITHM_NAME),
                    null
                )
            );
            //TODO: support EC256K & ECP-521
            putService(
                new Service(
                    this,
                    "Signature",
                    "SHA256withECDSA",
                    KeyVaultECSignature.KeyVaultSHA256.class.getName(),
                    Collections.singletonList(KeyVaultKeyStore.ALGORITHM_NAME),
                    null
                )
            );
            putService(
                new Service(
                    this,
                    "Signature",
                    "SHA384withECDSA",
                    KeyVaultECSignature.KeyVaultSHA384.class.getName(),
                    Collections.singletonList(KeyVaultKeyStore.ALGORITHM_NAME),
                    null
                )
            );
            return null;
        });
    }
}
