// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.test.keyvault;

import com.azure.spring.test.AppRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.azure.spring.test.EnvironmentVariable.AZURE_KEYVAULT2_URI;
import static com.azure.spring.test.EnvironmentVariable.AZURE_KEYVAULT_URI;
import static com.azure.spring.test.EnvironmentVariable.KEY_VAULT1_COMMON_SECRET_VALUE;
import static com.azure.spring.test.EnvironmentVariable.KEY_VAULT1_SECRET_NAME;
import static com.azure.spring.test.EnvironmentVariable.KEY_VAULT1_SECRET_VALUE;
import static com.azure.spring.test.EnvironmentVariable.KEY_VAULT2_SECRET_NAME;
import static com.azure.spring.test.EnvironmentVariable.KEY_VAULT2_SECRET_VALUE;
import static com.azure.spring.test.EnvironmentVariable.KEY_VAULT_COMMON_SECRET_NAME;
import static com.azure.spring.test.EnvironmentVariable.SPRING_CLIENT_ID;
import static com.azure.spring.test.EnvironmentVariable.SPRING_CLIENT_SECRET;
import static com.azure.spring.test.EnvironmentVariable.SPRING_TENANT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MultipleKeyVaultsIT {

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
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].vault-url", AZURE_KEYVAULT_URI);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].name", KEY_VAULT_NAME_1);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].credential.client-id", SPRING_CLIENT_ID);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].credential.client-secret", SPRING_CLIENT_SECRET);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].profile.tenant-id", SPRING_TENANT_ID);
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
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].vault-url", AZURE_KEYVAULT2_URI);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].name", KEY_VAULT_NAME_2);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].credential.client-id", SPRING_CLIENT_ID);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].credential.client-secret", SPRING_CLIENT_SECRET);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].profile.tenant-id", SPRING_TENANT_ID);
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
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].vault-url", AZURE_KEYVAULT_URI);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].name", KEY_VAULT_NAME_1);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].credential.client-id", SPRING_CLIENT_ID);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].credential.client-secret", SPRING_CLIENT_SECRET);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].profile.tenant-id", SPRING_TENANT_ID);

            app.property("spring.cloud.azure.keyvault.secret.property-sources[1].vault-url", AZURE_KEYVAULT2_URI);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[1].name", KEY_VAULT_NAME_2);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[1].credential.client-id", SPRING_CLIENT_ID);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[1].credential.client-secret", SPRING_CLIENT_SECRET);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[1].profile.tenant-id", SPRING_TENANT_ID);

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
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].vault-url", AZURE_KEYVAULT_URI);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].name", KEY_VAULT_NAME_1);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].credential.client-id", SPRING_CLIENT_ID);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].credential.client-secret", SPRING_CLIENT_SECRET);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[0].profile.tenant-id", SPRING_TENANT_ID);

            app.property("spring.cloud.azure.keyvault.secret.property-sources[1].vault-url", AZURE_KEYVAULT2_URI);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[1].name", KEY_VAULT_NAME_2);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[1].credential.client-id", SPRING_CLIENT_ID);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[1].credential.client-secret", SPRING_CLIENT_SECRET);
            app.property("spring.cloud.azure.keyvault.secret.property-sources[1].profile.tenant-id", SPRING_TENANT_ID);
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
