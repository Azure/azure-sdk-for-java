// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.context.properties.bind.BindableRuntimeHintsRegistrar;
import org.springframework.util.ClassUtils;

class KeyVaultEnvironmentRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    @SuppressWarnings("removal")
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        if (ClassUtils.isPresent("com.azure.security.keyvault.secrets.SecretClient", classLoader)) {
            // The environment post-processor binds these types directly, outside bean registration.
            BindableRuntimeHintsRegistrar.forTypes(AzureGlobalProperties.class, AzureKeyVaultSecretProperties.class)
                .registerHints(hints);
        }
    }
}
