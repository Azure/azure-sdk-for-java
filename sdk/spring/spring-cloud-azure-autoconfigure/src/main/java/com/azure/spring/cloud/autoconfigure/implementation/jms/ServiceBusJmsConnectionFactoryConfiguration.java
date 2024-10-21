// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;
import com.azure.identity.extensions.implementation.credential.provider.TokenCredentialProvider;
import com.azure.identity.extensions.implementation.enums.AuthProperty;
import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.azure.spring.cloud.autoconfigure.implementation.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.autoconfigure.jms.AzureServiceBusJmsConnectionFactoryCustomizer;
import com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider;
import jakarta.jms.ConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.PooledObject;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.JmsPoolConnectionFactoryFactory;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.util.StringUtils;

import java.util.Properties;
import java.util.stream.Collectors;

import static com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider.PASSWORDLESS_TOKEN_CREDENTIAL_BEAN_NAME;

@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(ConnectionFactory.class)
class ServiceBusJmsConnectionFactoryConfiguration  {

    private static final Log LOGGER = LogFactory.getLog(ServiceBusJmsConnectionFactoryConfiguration.class);
    private final GenericApplicationContext applicationContext;

    ServiceBusJmsConnectionFactoryConfiguration(GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private ServiceBusJmsConnectionFactory createJmsConnectionFactory(AzureServiceBusJmsProperties jmsProperties,
                                                                             ObjectProvider<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers) {
        TokenCredentialProvider tokenCredentialProvider = getPasswordlessTokenCredentialProvider(jmsProperties);
        return new ServiceBusJmsConnectionFactoryFactory(tokenCredentialProvider, jmsProperties,
            factoryCustomizers.orderedStream().collect(Collectors.toList()))
            .createConnectionFactory(ServiceBusJmsConnectionFactory.class);
    }

    private TokenCredentialProvider getPasswordlessTokenCredentialProvider(AzureServiceBusJmsProperties serviceBusJmsProperties) {
        if (!serviceBusJmsProperties.isPasswordlessEnabled()) {
            LOGGER.debug("Feature passwordless authentication is not enabled(" + AzureServiceBusJmsProperties.PREFIX + ".passwordless-enabled=false), "
                + "skip enhancing Service Bus JMS properties.");
            return null;
        }

        Properties properties = serviceBusJmsProperties.toPasswordlessProperties();
        String tokenCredentialBeanName = serviceBusJmsProperties.getCredential().getTokenCredentialBeanName();
        if (StringUtils.hasText(tokenCredentialBeanName)) {
            AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.setProperty(properties, tokenCredentialBeanName);
        } else {
            TokenCredentialProvider tokenCredentialProvider = TokenCredentialProvider.createDefault(new TokenCredentialProviderOptions(properties));
            TokenCredential tokenCredential = tokenCredentialProvider.get();

            tokenCredentialBeanName = PASSWORDLESS_TOKEN_CREDENTIAL_BEAN_NAME + "." + AzureServiceBusJmsProperties.PREFIX;
            AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.setProperty(properties, tokenCredentialBeanName);
            applicationContext.registerBean(tokenCredentialBeanName, TokenCredential.class, () -> tokenCredential);
        }

        AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.setProperty(properties, SpringTokenCredentialProvider.class.getName());
        AuthProperty.AUTHORITY_HOST.setProperty(properties, serviceBusJmsProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint());

        return TokenCredentialProvider.createDefault(new TokenCredentialProviderOptions(properties));
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "spring.jms.servicebus.pool", name = "enabled", havingValue = "false",
        matchIfMissing = true)
    class SimpleConnectionFactoryConfiguration {


        @Bean
        @ConditionalOnProperty(prefix = "spring.jms.cache", name = "enabled", havingValue = "false")
        @DependsOn("springTokenCredentialProviderContextProvider")
        ServiceBusJmsConnectionFactory jmsConnectionFactory(AzureServiceBusJmsProperties properties,
                                                            ObjectProvider<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers) {
            return createJmsConnectionFactory(properties, factoryCustomizers);
        }

        @Configuration(proxyBeanMethods = false)
        @ConditionalOnClass(CachingConnectionFactory.class)
        @ConditionalOnProperty(prefix = "spring.jms.cache", name = "enabled", havingValue = "true",
            matchIfMissing = true)
        class CachingConnectionFactoryConfiguration {

            @Bean
            @DependsOn("springTokenCredentialProviderContextProvider")
            CachingConnectionFactory jmsConnectionFactory(JmsProperties jmsProperties,
                                                          AzureServiceBusJmsProperties properties,
                                                          ObjectProvider<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers) {
                ServiceBusJmsConnectionFactory factory = createJmsConnectionFactory(properties, factoryCustomizers);
                CachingConnectionFactory connectionFactory = new CachingConnectionFactory(factory);
                JmsProperties.Cache cacheProperties = jmsProperties.getCache();
                connectionFactory.setCacheConsumers(cacheProperties.isConsumers());
                connectionFactory.setCacheProducers(cacheProperties.isProducers());
                connectionFactory.setSessionCacheSize(cacheProperties.getSessionCacheSize());
                return connectionFactory;
            }
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({ JmsPoolConnectionFactory.class, PooledObject.class })
    class PooledConnectionFactoryConfiguration {

        @Bean(destroyMethod = "stop")
        @ConditionalOnProperty(prefix = "spring.jms.servicebus.pool", name = "enabled", havingValue = "true")
        @DependsOn("springTokenCredentialProviderContextProvider")
        JmsPoolConnectionFactory jmsPoolConnectionFactory(AzureServiceBusJmsProperties properties,
                                                          ObjectProvider<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers) {
            ServiceBusJmsConnectionFactory factory = createJmsConnectionFactory(properties, factoryCustomizers);
            return new JmsPoolConnectionFactoryFactory(properties.getPool())
                .createPooledConnectionFactory(factory);
        }
    }
}
