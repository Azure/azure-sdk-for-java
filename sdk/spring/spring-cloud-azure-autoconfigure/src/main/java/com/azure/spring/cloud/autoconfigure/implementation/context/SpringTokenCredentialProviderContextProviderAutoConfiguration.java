// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.context;

import com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.SpringTokenCredentialProviderContextProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.azure.spring.cloud.autoconfigure.implementation.context.AzureContextUtils.SPRING_TOKEN_CREDENTIAL_PROVIDER_CONTEXT_BEAN_NAME;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Cloud Azure {@link SpringTokenCredentialProviderContextProvider}.
 *
 * @since 5.17.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(AzureAuthenticationTemplate.class)
class SpringTokenCredentialProviderContextProviderAutoConfiguration {

    @Bean(name = SPRING_TOKEN_CREDENTIAL_PROVIDER_CONTEXT_BEAN_NAME)
    @ConditionalOnMissingBean
    SpringTokenCredentialProviderContextProvider springTokenCredentialProviderContextProvider() {
        return new SpringTokenCredentialProviderContextProvider();
    }
}
