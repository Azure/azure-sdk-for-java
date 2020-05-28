// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.spring;

import com.microsoft.azure.utils.Constants;
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

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (isKeyVaultEnabled(environment)) {
            final KeyVaultEnvironmentPostProcessorHelper helper =
                new KeyVaultEnvironmentPostProcessorHelper(environment);
            helper.addKeyVaultPropertySource();
        }
    }

    private boolean isKeyVaultEnabled(ConfigurableEnvironment environment) {
        if (environment.getProperty(Constants.AZURE_KEYVAULT_VAULT_URI) == null) {
            // User doesn't want to enable Key Vault property initializer.
            return false;
        }
        return environment.getProperty(Constants.AZURE_KEYVAULT_ENABLED, Boolean.class, true)
            && isKeyVaultClientAvailable();
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
