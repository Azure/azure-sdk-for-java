// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.test.keyvault;

import com.microsoft.azure.test.management.ClientSecretAccess;
import com.microsoft.azure.test.utils.AppRunner;
import org.junit.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.Assert.assertEquals;

public class MultipleKeyVaultsIT {

    private static final String KEY_VAULT1_SECRET_VALUE = System.getenv("KEY_VAULT_SECRET_VALUE");
    private static final String KEY_VAULT1_SECRET_NAME = System.getenv("KEY_VAULT_SECRET_NAME");
    private static final String KEY_VAULT2_SECRET_VALUE = System.getenv("KEY_VAULT2_SECRET_VALUE");
    private static final String KEY_VAULT2_SECRET_NAME = System.getenv("KEY_VAULT2_SECRET_NAME");
    private static final String KEY_VAULT_COMMON_SECRET_NAME = System.getenv("KEY_VAULT_COMMON_SECRET_NAME");
    private static final String KEY_VAULT1_COMMON_SECRET_VALUE = System.getenv("KEY_VAULT1_COMMON_SECRET_VALUE");
    private static final String KEY_VAULT2_COMMON_SECRET_VALUE = System.getenv("KEY_VAULT2_COMMON_SECRET_VALUE");
    private static final String AZURE_KEYVAULT1_ENDPOINT = System.getenv("AZURE_KEYVAULT_ENDPOINT");
    private static final String AZURE_KEYVAULT2_ENDPOINT = System.getenv("AZURE_KEYVAULT2_ENDPOINT");
    private static final ClientSecretAccess CLIENT_SECRET_ACCESS = ClientSecretAccess.load();

    /**
     * Test getting value from 'keyvault1'.
     */
    @Test
    public void testGetValueFromKeyVault1() {
        try (AppRunner app = new AppRunner(TestApp.class)) {
            app.property("azure.keyvault.order", "keyvault1");
            app.property("azure.keyvault.keyvault1.uri", AZURE_KEYVAULT1_ENDPOINT);
            app.property("azure.keyvault.keyvault1.enabled", "true");
            app.property("azure.keyvault.keyvault1.client-id", CLIENT_SECRET_ACCESS.clientId());
            app.property("azure.keyvault.keyvault1.client-key", CLIENT_SECRET_ACCESS.clientSecret());
            app.property("azure.keyvault.keyvault1.tenant-id", CLIENT_SECRET_ACCESS.tenantId());
            app.start("dummy");
            assertEquals(KEY_VAULT1_SECRET_VALUE, app.getProperty(KEY_VAULT1_SECRET_NAME));
        }
    }

    /**
     * Test getting value from 'keyvault2'.
     */
    @Test
    public void testGetValueFromKeyVault2() {
        try (AppRunner app = new AppRunner(TestApp.class)) {
            app.property("azure.keyvault.order", "keyvault2");
            app.property("azure.keyvault.keyvault2.uri", AZURE_KEYVAULT2_ENDPOINT);
            app.property("azure.keyvault.keyvault2.enabled", "true");
            app.property("azure.keyvault.keyvault2.client-id", CLIENT_SECRET_ACCESS.clientId());
            app.property("azure.keyvault.keyvault2.client-key", CLIENT_SECRET_ACCESS.clientSecret());
            app.property("azure.keyvault.keyvault2.tenant-id", CLIENT_SECRET_ACCESS.tenantId());
            app.start("dummy");
            assertEquals(KEY_VAULT2_SECRET_VALUE, app.getProperty(KEY_VAULT2_SECRET_NAME));
        }
    }

    /**
     * Test getting value for a duplicate key which should resolve to the value
     * in 'keyvault1' as that is the first one of the configured key vaults.
     */
    @Test
    public void testGetValueForDuplicateKey() {
        try (AppRunner app = new AppRunner(TestApp.class)) {
            app.property("azure.keyvault.order", "keyvault1, keyvault2");
            app.property("azure.keyvault.keyvault1.uri", AZURE_KEYVAULT1_ENDPOINT);
            app.property("azure.keyvault.keyvault1.enabled", "true");
            app.property("azure.keyvault.keyvault1.client-id", CLIENT_SECRET_ACCESS.clientId());
            app.property("azure.keyvault.keyvault1.client-key", CLIENT_SECRET_ACCESS.clientSecret());
            app.property("azure.keyvault.keyvault1.tenant-id", CLIENT_SECRET_ACCESS.tenantId());
            app.property("azure.keyvault.keyvault2.uri", AZURE_KEYVAULT2_ENDPOINT);
            app.property("azure.keyvault.keyvault2.enabled", "true");
            app.property("azure.keyvault.keyvault2.client-id", CLIENT_SECRET_ACCESS.clientId());
            app.property("azure.keyvault.keyvault2.client-key", CLIENT_SECRET_ACCESS.clientSecret());
            app.property("azure.keyvault.keyvault2.tenant-id", CLIENT_SECRET_ACCESS.tenantId());
            app.start("dummy");
            assertEquals(KEY_VAULT1_COMMON_SECRET_VALUE, app.getProperty(KEY_VAULT_COMMON_SECRET_NAME));
        }
    }

    /**
     * Test getting value from a vault configured both with single and the
     * multiple vault support.
     */
    @Test
    public void testGetValueFromSingleVault() {
        try (AppRunner app = new AppRunner(TestApp.class)) {
            app.property("azure.keyvault.enabled", "true");
            app.property("azure.keyvault.uri", AZURE_KEYVAULT1_ENDPOINT);
            app.property("azure.keyvault.client-id", CLIENT_SECRET_ACCESS.clientId());
            app.property("azure.keyvault.client-key", CLIENT_SECRET_ACCESS.clientSecret());
            app.property("azure.keyvault.tenant-id", CLIENT_SECRET_ACCESS.tenantId());
            app.property("azure.keyvault.order", "keyvault2");
            app.property("azure.keyvault.keyvault2.enabled", "true");
            app.property("azure.keyvault.keyvault2.uri", AZURE_KEYVAULT2_ENDPOINT);
            app.property("azure.keyvault.keyvault2.client-id", CLIENT_SECRET_ACCESS.clientId());
            app.property("azure.keyvault.keyvault2.client-key", CLIENT_SECRET_ACCESS.clientSecret());
            app.property("azure.keyvault.keyvault2.tenant-id", CLIENT_SECRET_ACCESS.tenantId());
            app.start("dummy");
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
