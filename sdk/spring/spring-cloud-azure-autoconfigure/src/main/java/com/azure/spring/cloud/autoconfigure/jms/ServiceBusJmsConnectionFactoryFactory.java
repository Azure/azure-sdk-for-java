// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.cloud.autoconfigure.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.core.implementation.util.ReflectionUtils;
import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactorySettings;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.policy.JmsDefaultPrefetchPolicy;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;

/**
 * A factory for ServiceBusJmsConnectionFactory.
 */
public class ServiceBusJmsConnectionFactoryFactory {
    private final AzureServiceBusJmsProperties properties;
    private final List<ServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers;

    private static final String AMQP_URI_FORMAT = "amqps://%s?amqp.idleTimeout=%d";

    ServiceBusJmsConnectionFactoryFactory(AzureServiceBusJmsProperties properties,
                                          List<ServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers) {
        Assert.notNull(properties, "Properties must not be null");
        this.properties = properties;
        this.factoryCustomizers = (factoryCustomizers != null) ? factoryCustomizers : Collections.emptyList();

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
        JmsConnectionFactory jmsFactory = (JmsConnectionFactory) ReflectionUtils.getField(ServiceBusJmsConnectionFactory.class, "factory", factory);
        JmsDefaultPrefetchPolicy prefetchPolicy = (JmsDefaultPrefetchPolicy) jmsFactory.getPrefetchPolicy();
//        JmsDefaultPrefetchPolicy prefetchPolicy = (JmsDefaultPrefetchPolicy) factory.getPrefetchPolicy();
//        Map<String, String> configurationOptions = factory.getSettings().getConfigurationOptions();
        prefetchPolicy.setDurableTopicPrefetch(prefetchProperties.getDurableTopicPrefetch());
//        configurationOptions.put("jms.prefetchPolicy.durableTopicPrefetch", String.valueOf(prefetchProperties.getDurableTopicPrefetch()));
        prefetchPolicy.setQueueBrowserPrefetch(prefetchProperties.getQueueBrowserPrefetch());
//        configurationOptions.put("jms.prefetchPolicy.queueBrowserPrefetch", String.valueOf(prefetchProperties.getQueueBrowserPrefetch()));
        prefetchPolicy.setQueuePrefetch(prefetchProperties.getQueuePrefetch());
//        configurationOptions.put("jms.prefetchPolicy.queuePrefetch", String.valueOf(prefetchProperties.getQueuePrefetch()));
        prefetchPolicy.setTopicPrefetch(prefetchProperties.getTopicPrefetch());
//        configurationOptions.put("jms.prefetchPolicy.topicPrefetch", String.valueOf(prefetchProperties.getTopicPrefetch()));
//        ServiceBusJmsConnectionFactorySettings settings = new ServiceBusJmsConnectionFactorySettings(configurationOptions);
        jmsFactory.setPrefetchPolicy(prefetchPolicy);
//        try {
//            Field factorySettings = factory.getClass().getDeclaredField("settings");
//            factorySettings.setAccessible(true);
//            factorySettings.set(factory, configurationOptions);
//        } catch (NoSuchFieldException | IllegalAccessException ex) {
//            throw new IllegalStateException("Unable to set PrefetchPolicy", ex);
//        }
    }

    private <T extends ServiceBusJmsConnectionFactory> T createConnectionFactoryInstance(Class<T> factoryClass) {
        try {
            T factory;
            if (properties.isPasswordlessEnabled()) {
                String hostName = properties.getNamespace() + "." + properties.getProfile().getEnvironment().getServiceBusDomainName();

                TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

                factory = factoryClass.getConstructor(TokenCredential.class, String.class, ServiceBusJmsConnectionFactorySettings.class)
                                      .newInstance(tokenCredential, hostName, new ServiceBusJmsConnectionFactorySettings());

            } else {
                factory = factoryClass.getConstructor(String.class, ServiceBusJmsConnectionFactorySettings.class).newInstance(properties.getConnectionString(), new ServiceBusJmsConnectionFactorySettings());
            }
            return factory;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new IllegalStateException("Unable to create JmsConnectionFactory", ex);
        }
    }

    private void customize(ServiceBusJmsConnectionFactory connectionFactory) {
        for (ServiceBusJmsConnectionFactoryCustomizer factoryCustomizer : this.factoryCustomizers) {
            factoryCustomizer.customize(connectionFactory);
        }
    }
}
