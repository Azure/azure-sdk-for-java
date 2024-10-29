// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;
import com.azure.identity.extensions.implementation.credential.provider.TokenCredentialProvider;
import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.azure.servicebus.jms.ServiceBusJmsConnectionFactorySettings;
import com.azure.spring.cloud.autoconfigure.implementation.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.autoconfigure.jms.AzureServiceBusJmsConnectionFactoryCustomizer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static com.azure.spring.cloud.autoconfigure.implementation.util.SpringPasswordlessPropertiesUtils.enhancePasswordlessProperties;

class ServiceBusJmsConnectionFactoryFactory {
    private final AzureServiceBusJmsProperties properties;
    private final List<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers;
    private final TokenCredentialProvider tokenCredentialProvider;

    ServiceBusJmsConnectionFactoryFactory(AzureServiceBusJmsProperties properties,
                                          List<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers) {
        Assert.notNull(properties, "Properties must not be null");
        this.properties = properties;
        this.factoryCustomizers = (factoryCustomizers != null) ? factoryCustomizers : Collections.emptyList();
        if (properties.isPasswordlessEnabled()) {
            Properties passwordlessProperties = properties.toPasswordlessProperties();
            enhancePasswordlessProperties(AzureServiceBusJmsProperties.PREFIX, properties, passwordlessProperties);
            this.tokenCredentialProvider = TokenCredentialProvider.createDefault(new TokenCredentialProviderOptions(passwordlessProperties));
        } else {
            this.tokenCredentialProvider = null;
        }
    }

    <T extends ServiceBusJmsConnectionFactory> T createConnectionFactory(Class<T> factoryClass) {
        T factory = createConnectionFactoryInstance(factoryClass);
        setClientId(factory);
        setPrefetchPolicy(factory);
        customize(factory);
        return factory;
    }

    private <T extends ServiceBusJmsConnectionFactory> void setClientId(T factory) {
        if (StringUtils.hasText(this.properties.getTopicClientId())) {
            factory.setClientId(this.properties.getTopicClientId());
        }
    }

    private <T extends ServiceBusJmsConnectionFactory> void setPrefetchPolicy(T factory) {
        AzureServiceBusJmsProperties.PrefetchPolicy prefetchProperties = this.properties.getPrefetchPolicy();
        factory.getSettings().getConfigurationOptions().put("jms.prefetchPolicy.durableTopicPrefetch",
            String.valueOf(prefetchProperties.getDurableTopicPrefetch()));
        factory.getSettings().getConfigurationOptions().put("jms.prefetchPolicy.queueBrowserPrefetch",
            String.valueOf(prefetchProperties.getQueueBrowserPrefetch()));
        factory.getSettings().getConfigurationOptions().put("jms.prefetchPolicy.queuePrefetch",
            String.valueOf(prefetchProperties.getQueuePrefetch()));
        factory.getSettings().getConfigurationOptions().put("jms.prefetchPolicy.topicPrefetch",
            String.valueOf(prefetchProperties.getTopicPrefetch()));
    }

    private <T extends ServiceBusJmsConnectionFactory> T createConnectionFactoryInstance(Class<T> factoryClass) {
        try {
            T factory;
            if (properties.isPasswordlessEnabled()) {
                String hostName =
                    properties.getNamespace() + "." + properties.getProfile().getEnvironment().getServiceBusDomainName();
                TokenCredential tokenCredential = tokenCredentialProvider.get();
                factory = factoryClass.getConstructor(TokenCredential.class, String.class,
                                          ServiceBusJmsConnectionFactorySettings.class)
                                      .newInstance(tokenCredential, hostName,
                                          new ServiceBusJmsConnectionFactorySettings());
            } else {
                factory = factoryClass.getConstructor(String.class, ServiceBusJmsConnectionFactorySettings.class)
                                      .newInstance(properties.getConnectionString(),
                                          new ServiceBusJmsConnectionFactorySettings());
            }
            return factory;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new IllegalStateException("Unable to create JmsConnectionFactory", ex);
        }
    }

    private void customize(ServiceBusJmsConnectionFactory connectionFactory) {
        for (AzureServiceBusJmsConnectionFactoryCustomizer factoryCustomizer : this.factoryCustomizers) {
            factoryCustomizer.customize(connectionFactory);
        }
    }
}
