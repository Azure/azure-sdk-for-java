// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.test;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.security.keyvault.jca.KeyVaultKeyStore;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;

public class PropertyConvertorUtils {

    public static void putEnvironmentPropertyToSystemPropertyForKeyVaultJca() {
        KEYVAULT_JCA_SYSTEM_PROPERTIES.forEach(
            environmentPropertyKey -> {
                String value = System.getenv(environmentPropertyKey);
                String systemPropertyKey = environmentPropertyKey.toLowerCase().replaceFirst("azure_keyvault_",
                    "azure.keyvault.").replaceAll("_", "-");
                System.getProperties().put(systemPropertyKey, value);
            }
        );
    }

    public static final List<String> KEYVAULT_JCA_SYSTEM_PROPERTIES = Arrays.asList("AZURE_KEYVAULT_ENDPOINT",
        "AZURE_KEYVAULT_TENANT_ID",
        "AZURE_KEYVAULT_CLIENT_ID",
        "AZURE_KEYVAULT_CLIENT_SECRET",
        "AZURE_KEYVAULT_URI");

    public static KeyStore getKeyVaultKeyStore() throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
        return KeyVaultKeyStore.getKeyVaultKeyStoreBySystemProperty();
    }

    public static void addKeyVaultJcaProvider() {
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.addProvider(provider);
    }

}
