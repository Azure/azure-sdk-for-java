// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.autoconfigure.implementation.resourcemanager.AzureServiceBusResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.jms.AzureServiceBusJmsConnectionFactoryCustomizer;
import com.azure.spring.cloud.core.implementation.util.AzurePasswordlessPropertiesUtils;
import com.azure.spring.cloud.core.implementation.util.ReflectionUtils;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import org.apache.qpid.jms.JmsConnectionExtensions;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jms.autoconfigure.JmsAutoConfiguration;
import org.springframework.boot.jms.autoconfigure.JndiConnectionFactoryAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.core.JmsTemplate;

import java.net.URI;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Service Bus JMS support.
 *
 * @since 4.0.0
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(JmsAutoConfiguration.class)
@AutoConfigureAfter({
    JndiConnectionFactoryAutoConfiguration.class,
    AzureServiceBusResourceManagerAutoConfiguration.class })
@ConditionalOnProperty(value = "spring.jms.servicebus.enabled", matchIfMissing = true)
@ConditionalOnClass({ ConnectionFactory.class, JmsConnectionFactory.class, JmsTemplate.class })
@EnableConfigurationProperties
@Import({
    ServiceBusJmsContainerConfiguration.class,
    ServiceBusJmsPropertiesConfiguration.class,
    ServiceBusJmsConnectionFactoryConfiguration.class
})
public class ServiceBusJmsAutoConfiguration {

    @Bean
    AzureServiceBusJmsProperties serviceBusJmsProperties(AzureGlobalProperties azureGlobalProperties) {
        AzureServiceBusJmsProperties properties = new AzureServiceBusJmsProperties();
        return mergeAzureProperties(azureGlobalProperties, properties);
    }

    /**
     * Standard tier does not support the property "com.microsoft:is-client-provider", so remove it.
     */
    @Bean
    @ConditionalOnExpression("'standard'.equalsIgnoreCase('${spring.jms.servicebus.pricing-tier}')")
    @SuppressWarnings("unchecked")
    AzureServiceBusJmsConnectionFactoryCustomizer amqpOpenPropertiesCustomizer() {
        return factory -> {
            JmsConnectionFactory jmsFactory = (JmsConnectionFactory) ReflectionUtils.getField(ServiceBusJmsConnectionFactory.class, "factory", factory);
            EnumMap<JmsConnectionExtensions, BiFunction<Connection, URI, Object>> extensionMap =
                (EnumMap) ReflectionUtils.getField(JmsConnectionFactory.class, "extensionMap", jmsFactory);

            if (extensionMap.containsKey(JmsConnectionExtensions.AMQP_OPEN_PROPERTIES)) {
                Map<String, Object> properties = (Map) extensionMap.get(JmsConnectionExtensions.AMQP_OPEN_PROPERTIES).apply(null, null);
                if (properties.containsKey("com.microsoft:is-client-provider")) {
                    jmsFactory.setExtension(JmsConnectionExtensions.AMQP_OPEN_PROPERTIES.toString(),
                        (connection, uri) -> {
                            properties.remove("com.microsoft:is-client-provider");
                            return properties;
                        });
                }
            }
        };
    }

    private AzureServiceBusJmsProperties mergeAzureProperties(AzureGlobalProperties azureGlobalProperties, AzureServiceBusJmsProperties azurePasswordlessProperties) {
        AzureServiceBusJmsProperties mergedProperties = new AzureServiceBusJmsProperties();
        AzurePasswordlessPropertiesUtils.mergeAzureCommonProperties(azureGlobalProperties, azurePasswordlessProperties, mergedProperties);
        return mergedProperties;
    }
}
