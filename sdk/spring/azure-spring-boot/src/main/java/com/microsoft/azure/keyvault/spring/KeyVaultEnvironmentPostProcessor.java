// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.spring;

import static com.microsoft.azure.utils.Constants.AZURE_KEYVAULT_ENABLED;
import static com.microsoft.azure.utils.Constants.AZURE_KEYVAULT_ORDER;
import static com.microsoft.azure.utils.Constants.AZURE_KEYVAULT_PREFIX;
import static com.microsoft.azure.utils.Constants.AZURE_KEYVAULT_VAULT_URI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ClassUtils;

/**
 * Leverage {@link EnvironmentPostProcessor} to add Key Vault secrets as a property source.
 */
public class KeyVaultEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    public static final int DEFAULT_ORDER = ConfigFileApplicationListener.DEFAULT_ORDER + 1;
    private int order = DEFAULT_ORDER;

    /**
     * Post process the environment.
     *
     * <p>
     * Here we are going to process any key vault(s) and make them as available
     * PropertySource(s). Note this supports both the singular key vault setup,
     * as well as the multiple key vault setup.
     * </p>
     *
     * @param environment the environment.
     * @param application the application.
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        final KeyVaultEnvironmentPostProcessorHelper helper
                = new KeyVaultEnvironmentPostProcessorHelper(environment);
        if (isKeyVaultEnabled(environment, "")) {
            helper.addKeyVaultPropertySource("");
        }
        if (hasMultipleKeyVaultsEnabled(environment)) {
            final String property = environment.getProperty(AZURE_KEYVAULT_PREFIX + AZURE_KEYVAULT_ORDER, "");
            final String[] keyVaultNames = property.split(",");
            for (int i = keyVaultNames.length - 1; i >= 0; i--) {
                if (isKeyVaultEnabled(environment, keyVaultNames[i].trim() + ".")) {
                    helper.addKeyVaultPropertySource(keyVaultNames[i].trim() + ".");
                }
            }
        }
    }

    /**
     * Is the key vault enabled.
     *
     * <p>
     * If the (normalizedName+) AZURE_KEYVAULT_URI is not present then the user
     * does not want to enable the key vault at all.
     * </p>
     * </p>
     * If the (normalizedName+) AZURE_KEYVAULT_ENABLED is set to false the user
     * wants to disable the key vault, if it is set to true the key vault will
     * be enabled.
     * </p>
     * <p>
     * If the KeyVaultClient implementation is not available then key vault
     * support will not be enabled.
     * </p>
     *
     * @param environment the environment.
     * @param normalizedName the normalized name used to differentiate between
     * multiple key vaults.
     * @return true if the key vault is enabled, false otherwise.
     */
    private boolean isKeyVaultEnabled(ConfigurableEnvironment environment, String normalizedName) {
        return environment.getProperty(
            AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_ENABLED, Boolean.class, true)
            && environment.getProperty(AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_VAULT_URI) != null
            && isKeyVaultClientAvailable();
    }

    /**
     * Determine whether or not multiple key vaults are enabled.
     *
     * <p>
     * Look for the AZURE_KEYVAULT_ORDER property to determine if multiple key
     * vault support should be enabled.
     * </p>
     *
     * @param environment the environment.
     * @return true if enabled, false otherwise.
     */
    private boolean hasMultipleKeyVaultsEnabled(ConfigurableEnvironment environment) {
        boolean result = false;
        if (environment.getProperty(AZURE_KEYVAULT_PREFIX + AZURE_KEYVAULT_ORDER) != null) {
            result = true;
        }
        return result;
    }

    private boolean isKeyVaultClientAvailable() {
        return ClassUtils.isPresent("com.azure.security.keyvault.secrets.SecretClient",
            KeyVaultEnvironmentPostProcessor.class.getClassLoader());
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
