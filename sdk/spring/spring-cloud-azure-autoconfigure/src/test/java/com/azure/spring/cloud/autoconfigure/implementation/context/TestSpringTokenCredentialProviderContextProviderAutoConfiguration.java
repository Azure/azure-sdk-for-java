// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.context;

import com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.SpringTokenCredentialProviderContextProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(AzureAuthenticationTemplate.class)
public class TestSpringTokenCredentialProviderContextProviderAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    SpringTokenCredentialProviderContextProvider springTokenCredentialProviderContextProvider() {
        return new SpringTokenCredentialProviderContextProvider();
    }
}
