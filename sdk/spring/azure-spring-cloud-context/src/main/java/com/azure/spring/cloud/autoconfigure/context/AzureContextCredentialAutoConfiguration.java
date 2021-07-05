// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.IntelliJCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.identity.VisualStudioCodeCredentialBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.spring.core.CredentialProperties;
import com.azure.spring.identity.SpringCredentialBuilderBase;
import com.azure.spring.identity.SpringEnvironmentCredentialBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Map;


@Configuration
@EnableConfigurationProperties(CredentialProperties.class)
public class AzureContextCredentialAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureContextCredentialAutoConfiguration.class);

    public static final int SPRING_ENV_CREDENTIAL_ORDER = 1;

    @Bean
    @ConditionalOnMissingBean
    public IdentityClientOptions identityClientOptions(CredentialProperties azureProperties) {
        // TODO (xiada) use properties defined in core properties to construct identityClientOptions
        return new IdentityClientOptions();
    }

    @Bean
    @Order(SPRING_ENV_CREDENTIAL_ORDER)
    public SpringEnvironmentCredentialBuilder springEnvironmentCredentialBuilder(Environment environment) {
        return new SpringEnvironmentCredentialBuilder(environment);
    }

    @Bean
    @Order(SPRING_ENV_CREDENTIAL_ORDER + 100)
    public ManagedIdentityCredentialBuilder managedIdentityCredentialBuilder(CredentialProperties azureProperties) {
        return new ManagedIdentityCredentialBuilder().clientId(
            azureProperties.getClientId()); // TODO (xiada) change to use managedIdentityClientId
    }

    @Bean
    @Order(SPRING_ENV_CREDENTIAL_ORDER + 200)
    public IntelliJCredentialBuilder intelliJCredentialBuilder(CredentialProperties azureProperties) {
        return new IntelliJCredentialBuilder().tenantId(azureProperties.getTenantId());
        // TODO (xiada) check whether the property has a default value from Azure SDK env
    }

    @Bean
    @Order(SPRING_ENV_CREDENTIAL_ORDER + 300)
    public VisualStudioCodeCredentialBuilder visualStudioCodeCredentialBuilder(CredentialProperties azureProperties) {
        return new VisualStudioCodeCredentialBuilder().tenantId(azureProperties.getTenantId());
        // TODO (xiada) check whether the property has a default value from Azure SDK env
    }

    @Bean
    @Order(SPRING_ENV_CREDENTIAL_ORDER + 400)
    public AzureCliCredentialBuilder azureCliCredentialBuilder() {
        return new AzureCliCredentialBuilder();
    }

    @Bean
    @Order(SPRING_ENV_CREDENTIAL_ORDER + 500)
    public AzurePowerShellCredentialBuilder azurePowerShellCredentialBuilder() {
        return new AzurePowerShellCredentialBuilder();
    }

    @Bean
    @ConditionalOnMissingBean // TODO (xiada) dedicated names for the azureTokenCredential
    public TokenCredential azureTokenCredential(List<SpringCredentialBuilderBase> credentialBuilders) {
        final ChainedTokenCredentialBuilder chainedTokenCredentialBuilder = new ChainedTokenCredentialBuilder();
        for (SpringCredentialBuilderBase builder : credentialBuilders) {
            chainedTokenCredentialBuilder.addLast(builder.build());
        }
        return chainedTokenCredentialBuilder.build();
    }

    static class CredentialBuilderPostProcessor implements BeanFactoryPostProcessor {

        @SuppressWarnings({"rawtypes"})
        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            final IdentityClientOptions identityClientOptions;
            try {
                identityClientOptions = beanFactory.getBean(IdentityClientOptions.class);
            } catch (NoSuchBeanDefinitionException e) {
                LOGGER.warn(
                    "No bean of type IdentityClientOptions found, will skip the CredentialBuilderPostProcessor");
                return;
            }

            final Map<String, SpringCredentialBuilderBase> credentialBuilders = beanFactory.getBeansOfType(
                SpringCredentialBuilderBase.class);
            for (Map.Entry<String, SpringCredentialBuilderBase> entry : credentialBuilders.entrySet()) {
                final SpringCredentialBuilderBase bean = (SpringCredentialBuilderBase) beanFactory.getBean(entry.getKey());

                bean.identityClientOptions(identityClientOptions);
            }
        }
    }


}
