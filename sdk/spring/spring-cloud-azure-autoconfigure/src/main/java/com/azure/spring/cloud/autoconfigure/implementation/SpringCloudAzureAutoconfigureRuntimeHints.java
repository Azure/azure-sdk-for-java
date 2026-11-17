// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.context.properties.bind.BindableRuntimeHintsRegistrar;
import org.springframework.util.ClassUtils;

class SpringCloudAzureAutoconfigureRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    @SuppressWarnings("removal")
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Environment post-processors also bind global properties outside bean registration.
        BindableRuntimeHintsRegistrar.forTypes(AzureGlobalProperties.class, AzurePasswordlessProperties.class)
            .registerHints(hints);
        hints.reflection().registerTypeIfPresent(classLoader,
            "com.azure.identity.extensions.jdbc.postgresql.AzurePostgresqlAuthenticationPlugin",
            MemberCategory.DECLARED_CLASSES,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS);
        hints.reflection().registerTypeIfPresent(classLoader,
            "com.azure.identity.extensions.jdbc.mysql.AzureMysqlAuthenticationPlugin",
            MemberCategory.DECLARED_CLASSES,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS);
        hints.reflection().registerTypeIfPresent(classLoader,
            "com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider",
            MemberCategory.DECLARED_CLASSES,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS);

        if (ClassUtils.isPresent("com.azure.security.keyvault.secrets.SecretClient", classLoader)) {
            // The environment post-processor binds this type directly, outside bean registration.
            BindableRuntimeHintsRegistrar.forTypes(AzureKeyVaultSecretProperties.class).registerHints(hints);
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
