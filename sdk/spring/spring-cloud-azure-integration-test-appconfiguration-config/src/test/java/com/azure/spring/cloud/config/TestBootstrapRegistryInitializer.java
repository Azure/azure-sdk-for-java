// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.config;

import com.azure.spring.cloud.appconfiguration.config.ConfigurationClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.SecretClientCustomizer;
import org.springframework.boot.bootstrap.BootstrapRegistry;
import org.springframework.boot.bootstrap.BootstrapRegistryInitializer;

class TestBootstrapRegistryInitializer implements BootstrapRegistryInitializer {

    @Override
    public void initialize(BootstrapRegistry registry) {
        registry.register(PaginationPagePolicy.class, context -> new PaginationPagePolicy());
        registry.register(CustomClient.class, context -> new CustomClient(context.get(PaginationPagePolicy.class)));
        registry.register(ConfigurationClientCustomizer.class, context -> context.get(CustomClient.class));
        registry.register(SecretClientCustomizer.class, context -> context.get(CustomClient.class));
        registry.addCloseListener(event -> event.getApplicationContext().getBeanFactory()
            .registerSingleton("paginationPagePolicy", event.getBootstrapContext().get(PaginationPagePolicy.class)));
    }
}
