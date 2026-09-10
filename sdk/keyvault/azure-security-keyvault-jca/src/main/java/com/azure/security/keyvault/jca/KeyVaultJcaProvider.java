// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import com.azure.security.keyvault.jca.implementation.signature.KeyVaultKeylessEcSha256Signature;
import com.azure.security.keyvault.jca.implementation.signature.KeyVaultKeylessEcSha384Signature;
import com.azure.security.keyvault.jca.implementation.signature.KeyVaultKeylessEcSha512Signature;
import com.azure.security.keyvault.jca.implementation.signature.KeyVaultKeylessRsa256Signature;
import com.azure.security.keyvault.jca.implementation.signature.KeyVaultKeylessRsa512Signature;
import com.azure.security.keyvault.jca.implementation.signature.KeyVaultKeylessRsaSsaPssSignature;

import java.security.PrivilegedAction;
import java.security.Provider;
import java.util.Arrays;
import java.util.Collections;

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
    @SuppressWarnings("deprecation")
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
            putService(new Provider.Service(this, "KeyManagerFactory", "SunX509",
                KeyVaultKeyManagerFactory.class.getName(), Arrays.asList("SunX509", "IbmX509"), null));

            /*
             * Note for Tomcat we needed to add "DKS" as an algorithm so it does
             * not use an in-memory key store and later on can wrap the
             * KeyManager using its JSSEKeyManager so the key alias is known.
             *
             * See SSLUtilBase.getKeyManagers and look for the
             * "DKS".equalsIgnoreCase(certificate.getCertificateKeystoreType()
             */
            putService(new Provider.Service(this, "KeyStore", "DKS", KeyVaultKeyStore.class.getName(),
                Collections.singletonList("DKS"), null));
            putService(new Provider.Service(this, "KeyStore", KeyVaultKeyStore.ALGORITHM_NAME,
                KeyVaultKeyStore.class.getName(), Collections.singletonList(KeyVaultKeyStore.ALGORITHM_NAME), null));

            putService(new Service(this, "Signature", KeyVaultKeylessRsaSsaPssSignature.ALGORITHM_NAME,
                KeyVaultKeylessRsaSsaPssSignature.class.getName(), null, null));
            putService(new Service(this, "Signature", KeyVaultKeylessRsa256Signature.ALGORITHM_NAME,
                KeyVaultKeylessRsa256Signature.class.getName(), null, null));
            putService(new Service(this, "Signature", KeyVaultKeylessRsa512Signature.ALGORITHM_NAME,
                KeyVaultKeylessRsa512Signature.class.getName(), null, null));
            putService(new Service(this, "Signature", KeyVaultKeylessEcSha256Signature.ALGORITHM_NAME,
                KeyVaultKeylessEcSha256Signature.class.getName(), null, null));
            putService(new Service(this, "Signature", KeyVaultKeylessEcSha384Signature.ALGORITHM_NAME,
                KeyVaultKeylessEcSha384Signature.class.getName(), null, null));
            putService(new Service(this, "Signature", KeyVaultKeylessEcSha512Signature.ALGORITHM_NAME,
                KeyVaultKeylessEcSha512Signature.class.getName(), null, null));
            return null;
        });
    }
}
