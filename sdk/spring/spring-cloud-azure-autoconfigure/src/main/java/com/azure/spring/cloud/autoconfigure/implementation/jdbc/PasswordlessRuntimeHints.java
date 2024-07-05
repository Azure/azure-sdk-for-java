// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.context.properties.bind.BindableRuntimeHintsRegistrar;

class PasswordlessRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        BindableRuntimeHintsRegistrar.forTypes(AzurePasswordlessProperties.class)
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
    }

}
