// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.integration.tests.keyvault.secret;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.integration.tests.util.TestCredentialUtils;
import org.springframework.boot.bootstrap.BootstrapRegistry;
import org.springframework.boot.bootstrap.BootstrapRegistryInitializer;

class TestBootstrapRegistryInitializer implements BootstrapRegistryInitializer {

    @Override
    public void initialize(BootstrapRegistry registry) {
        registry.register(TokenCredential.class, context -> TestCredentialUtils.getIntegrationTestTokenCredential());
    }
}
