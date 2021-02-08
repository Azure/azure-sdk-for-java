// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.keyvault;

import static org.junit.Assert.assertEquals;

import com.azure.spring.test.management.ClientSecretAccess;
import com.azure.spring.test.AppRunner;
import org.junit.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

public class MultipleKeyVaultsIT {

    private static final String KEY_VAULT1_SECRET_VALUE = System.getenv("KEY_VAULT_SECRET_VALUE");
    private static final String KEY_VAULT1_SECRET_NAME = System.getenv("KEY_VAULT_SECRET_NAME");
    private static final String KEY_VAULT2_SECRET_VALUE = System.getenv("KEY_VAULT2_SECRET_VALUE");
    private static final String KEY_VAULT2_SECRET_NAME = System.getenv("KEY_VAULT2_SECRET_NAME");
    private static final String KEY_VAULT_COMMON_SECRET_NAME = System.getenv("KEY_VAULT_COMMON_SECRET_NAME");
    private static final String KEY_VAULT1_COMMON_SECRET_VALUE = System.getenv("KEY_VAULT1_COMMON_SECRET_VALUE");
    private static final String KEY_VAULT2_COMMON_SECRET_VALUE = System.getenv("KEY_VAULT2_COMMON_SECRET_VALUE");
    private static final String AZURE_KEYVAULT_URI = System.getenv("AZURE_KEYVAULT_URI");
    private static final String AZURE_KEYVAULT2_URI = System.getenv("AZURE_KEYVAULT2_URI");
    private static final ClientSecretAccess CLIENT_SECRET_ACCESS = ClientSecretAccess.load();
    private static final String KEY_VAULT_NAME_1 = getKeyVaultName(AZURE_KEYVAULT_URI);
    private static final String KEY_VAULT_NAME_2 = getKeyVaultName(AZURE_KEYVAULT2_URI);

    private static String getKeyVaultName(String keyVaultUri) {
        int beginIndex = keyVaultUri.indexOf("//") + 2;
        int endIndex = keyVaultUri.indexOf(".vault.azure.net");
        return keyVaultUri.substring(beginIndex, endIndex);
    }

    /**
     * Test getting value from 'keyvault1'.
     */
    @Test
    public void testGetValueFromKeyVault1() {
        try (AppRunner app = new AppRunner(TestApp.class)) {
            app.property("azure.keyvault.order", KEY_VAULT_NAME_1);
            app.property("azure.keyvault." + KEY_VAULT_NAME_1 + ".enabled", "true");
            app.property("azure.keyvault." + KEY_VAULT_NAME_1 + ".uri", AZURE_KEYVAULT_URI);
            app.property("azure.keyvault." + KEY_VAULT_NAME_1 + ".client-id", CLIENT_SECRET_ACCESS.clientId());
            app.property("azure.keyvault." + KEY_VAULT_NAME_1 + ".client-key", CLIENT_SECRET_ACCESS.clientSecret());
            app.property("azure.keyvault." + KEY_VAULT_NAME_1 + ".tenant-id", CLIENT_SECRET_ACCESS.tenantId());
            app.start();
            assertEquals(KEY_VAULT1_SECRET_VALUE, app.getProperty(KEY_VAULT1_SECRET_NAME));
        }
    }

    /**
     * Test getting value from 'keyvault2'.
     */
    @Test
    public void testGetValueFromKeyVault2() {
        try (AppRunner app = new AppRunner(TestApp.class)) {
            app.property("azure.keyvault.order", KEY_VAULT_NAME_2);
            app.property("azure.keyvault." + KEY_VAULT_NAME_2 + ".enabled", "true");
            app.property("azure.keyvault." + KEY_VAULT_NAME_2 + ".uri", AZURE_KEYVAULT2_URI);
            app.property("azure.keyvault." + KEY_VAULT_NAME_2 + ".client-id", CLIENT_SECRET_ACCESS.clientId());
            app.property("azure.keyvault." + KEY_VAULT_NAME_2 + ".client-key", CLIENT_SECRET_ACCESS.clientSecret());
            app.property("azure.keyvault." + KEY_VAULT_NAME_2 + ".tenant-id", CLIENT_SECRET_ACCESS.tenantId());
            app.start();
            assertEquals(KEY_VAULT2_SECRET_VALUE, app.getProperty(KEY_VAULT2_SECRET_NAME));
        }
    }

    /**
     * Test getting value for a duplicate key which should resolve to the value in 'keyvault1' as that is the first one
     * of the configured key vaults.
     */
    @Test
    public void testGetValueForDuplicateKey() {
        try (AppRunner app = new AppRunner(TestApp.class)) {
            app.property("azure.keyvault.order", String.join(",", KEY_VAULT_NAME_1, KEY_VAULT_NAME_2));
            app.property("azure.keyvault." + KEY_VAULT_NAME_1 + ".enabled", "true");
            app.property("azure.keyvault." + KEY_VAULT_NAME_1 + ".uri", AZURE_KEYVAULT_URI);
            app.property("azure.keyvault." + KEY_VAULT_NAME_1 + ".client-id", CLIENT_SECRET_ACCESS.clientId());
            app.property("azure.keyvault." + KEY_VAULT_NAME_1 + ".client-key", CLIENT_SECRET_ACCESS.clientSecret());
            app.property("azure.keyvault." + KEY_VAULT_NAME_1 + ".tenant-id", CLIENT_SECRET_ACCESS.tenantId());
            app.property("azure.keyvault." + KEY_VAULT_NAME_2 + ".enabled", "true");
            app.property("azure.keyvault." + KEY_VAULT_NAME_2 + ".uri", AZURE_KEYVAULT2_URI);
            app.property("azure.keyvault." + KEY_VAULT_NAME_2 + ".client-id", CLIENT_SECRET_ACCESS.clientId());
            app.property("azure.keyvault." + KEY_VAULT_NAME_2 + ".client-key", CLIENT_SECRET_ACCESS.clientSecret());
            app.property("azure.keyvault." + KEY_VAULT_NAME_2 + ".tenant-id", CLIENT_SECRET_ACCESS.tenantId());
            app.start();
            assertEquals(KEY_VAULT1_COMMON_SECRET_VALUE, app.getProperty(KEY_VAULT_COMMON_SECRET_NAME));
        }
    }

    /**
     * Test getting value from a vault configured both with single and the multiple vault support.
     */
    @Test
    public void testGetValueFromSingleVault() {
        try (AppRunner app = new AppRunner(TestApp.class)) {
            app.property("azure.keyvault.order", String.join(",", KEY_VAULT_NAME_1, KEY_VAULT_NAME_2));
            app.property("azure.keyvault." + KEY_VAULT_NAME_1 + ".enabled", "true");
            app.property("azure.keyvault." + KEY_VAULT_NAME_1 + ".uri", AZURE_KEYVAULT_URI);
            app.property("azure.keyvault." + KEY_VAULT_NAME_1 + ".client-id", CLIENT_SECRET_ACCESS.clientId());
            app.property("azure.keyvault." + KEY_VAULT_NAME_1 + ".client-key", CLIENT_SECRET_ACCESS.clientSecret());
            app.property("azure.keyvault." + KEY_VAULT_NAME_1 + ".tenant-id", CLIENT_SECRET_ACCESS.tenantId());
            app.property("azure.keyvault." + KEY_VAULT_NAME_2 + ".enabled", "true");
            app.property("azure.keyvault." + KEY_VAULT_NAME_2 + ".uri", AZURE_KEYVAULT2_URI);
            app.property("azure.keyvault." + KEY_VAULT_NAME_2 + ".client-id", CLIENT_SECRET_ACCESS.clientId());
            app.property("azure.keyvault." + KEY_VAULT_NAME_2 + ".client-key", CLIENT_SECRET_ACCESS.clientSecret());
            app.property("azure.keyvault." + KEY_VAULT_NAME_2 + ".tenant-id", CLIENT_SECRET_ACCESS.tenantId());
            app.start();
            assertEquals(KEY_VAULT1_SECRET_VALUE, app.getProperty(KEY_VAULT1_SECRET_NAME));
            assertEquals(KEY_VAULT2_SECRET_VALUE, app.getProperty(KEY_VAULT2_SECRET_NAME));
        }
    }

    /**
     * Defines the Spring Boot test application.
     */
    @SpringBootApplication
    public static class TestApp {

    }
}
