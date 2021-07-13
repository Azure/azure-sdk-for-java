// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.unity.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.spring.autoconfigure.unity.AzureProperties;
import com.azure.spring.identity.SpringAzureCliCredentialBuilder;
import com.azure.spring.identity.SpringAzurePowerShellCredentialBuilder;
import com.azure.spring.identity.SpringCredentialBuilderBase;
import com.azure.spring.identity.SpringEnvironmentCredentialBuilder;
import com.azure.spring.identity.SpringIntelliJCredentialBuilder;
import com.azure.spring.identity.SpringManagedIdentityCredentialBuilder;
import com.azure.spring.identity.SpringVisualStudioCodeCredentialBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * Auto-configuration for Azure Spring default token credential.
 */
@Configuration
public class AzureDefaultTokenCredentialAutoConfiguration {

    public static final int SPRING_ENV_CREDENTIAL_ORDER = 0;

    @Bean
    public AzureSpringConfiguration azureSpringConfiguration(AzureProperties azureProperties) {
        return new AzureSpringConfiguration(azureProperties);
    }

    @ConditionalOnMissingBean
    @Bean
    @Order(SPRING_ENV_CREDENTIAL_ORDER)
    public SpringEnvironmentCredentialBuilder springEnvironmentCredentialBuilder(AzureSpringConfiguration azureSpringConfiguration) {
        return new SpringEnvironmentCredentialBuilder().credentialPropertiesProvider(azureSpringConfiguration);
    }

    @ConditionalOnMissingBean
    @Bean
    @Order(SPRING_ENV_CREDENTIAL_ORDER + 100)
    public SpringManagedIdentityCredentialBuilder managedIdentityCredentialBuilder(AzureSpringConfiguration azureSpringConfiguration) {
        return new SpringManagedIdentityCredentialBuilder().clientId(azureSpringConfiguration.getClientId());
    }

    @ConditionalOnMissingBean
    @Bean
    @Order(SPRING_ENV_CREDENTIAL_ORDER + 200)
    public SpringIntelliJCredentialBuilder intelliJCredentialBuilder(AzureSpringConfiguration azureSpringConfiguration) {
        return new SpringIntelliJCredentialBuilder().tenantId(azureSpringConfiguration.getTenantId());
    }

    @ConditionalOnMissingBean
    @Bean
    @Order(SPRING_ENV_CREDENTIAL_ORDER + 300)
    public SpringVisualStudioCodeCredentialBuilder visualStudioCodeCredentialBuilder(AzureSpringConfiguration azureSpringConfiguration) {
        return new SpringVisualStudioCodeCredentialBuilder().tenantId(azureSpringConfiguration.getTenantId());
    }

    @ConditionalOnMissingBean
    @Bean
    @Order(SPRING_ENV_CREDENTIAL_ORDER + 400)
    public SpringAzureCliCredentialBuilder azureCliCredentialBuilder() {
        return new SpringAzureCliCredentialBuilder();
    }

    @ConditionalOnMissingBean
    @Bean
    @Order(SPRING_ENV_CREDENTIAL_ORDER + 500)
    public SpringAzurePowerShellCredentialBuilder azurePowerShellCredentialBuilder() {
        return new SpringAzurePowerShellCredentialBuilder();
    }

    @SuppressWarnings("rawtypes")
    @Bean
    @ConditionalOnMissingBean
    // TODO (xiada) dedicated names for the azureTokenCredential
    public TokenCredential azureTokenCredential(List<SpringCredentialBuilderBase> credentialBuilders) {
        final ChainedTokenCredentialBuilder chainedTokenCredentialBuilder = new ChainedTokenCredentialBuilder();
        for (SpringCredentialBuilderBase builder : credentialBuilders) {
            chainedTokenCredentialBuilder.addLast(builder.build());
        }
        return chainedTokenCredentialBuilder.build();
    }

    /*@Bean
    public CredentialBuilderPostProcessor credentialBuilderPostProcessor() {
        return new CredentialBuilderPostProcessor();
    }

    static class CredentialBuilderPostProcessor implements BeanFactoryPostProcessor {

        @SuppressWarnings({"rawtypes"})
        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            final Map<String, SpringCredentialBuilderBase> credentialBuilders = beanFactory.getBeansOfType(
                SpringCredentialBuilderBase.class);
            for (Map.Entry<String, SpringCredentialBuilderBase> entry : credentialBuilders.entrySet()) {
                final SpringCredentialBuilderBase bean = (SpringCredentialBuilderBase) beanFactory.getBean(entry.getKey());

//                bean.identityClientOptions(identityClientOptions);
            }
        }
    }*/


}
