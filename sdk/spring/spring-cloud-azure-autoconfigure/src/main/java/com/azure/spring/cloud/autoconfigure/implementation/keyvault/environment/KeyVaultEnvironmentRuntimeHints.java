// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment;

import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.context.properties.bind.BindableRuntimeHintsRegistrar;
import org.springframework.util.ClassUtils;

class KeyVaultEnvironmentRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        if (ClassUtils.isPresent("com.azure.security.keyvault.secrets.SecretClient", classLoader)) {
            // The environment post-processor binds this type directly, outside bean registration.
            BindableRuntimeHintsRegistrar.forTypes(AzureKeyVaultSecretProperties.class)
                .registerHints(hints);
            // Azure Identity instantiates this expandable enum reflectively.
            hints.reflection().registerTypeIfPresent(classLoader, "com.azure.identity.AzureIdentityEnvVars",
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
            // Azure Core detects and invokes these models' JSON streaming methods reflectively.
            hints.reflection().registerTypeIfPresent(classLoader,
                "com.azure.security.keyvault.secrets.implementation.models.SecretItem",
                MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerTypeIfPresent(classLoader,
                "com.azure.security.keyvault.secrets.implementation.models.SecretBundle",
                MemberCategory.INVOKE_PUBLIC_METHODS);
        }
    }
}
