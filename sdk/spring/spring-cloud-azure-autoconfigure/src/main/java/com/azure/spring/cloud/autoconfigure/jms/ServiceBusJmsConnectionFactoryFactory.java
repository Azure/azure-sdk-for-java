// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.spring.cloud.autoconfigure.jms.properties.ServiceBusJmsProperties;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * A factory for ServiceBusJmsConnectionFactory.
 */
public class ServiceBusJmsConnectionFactoryFactory {
    private final ServiceBusJmsProperties properties;

    ServiceBusJmsConnectionFactoryFactory(ServiceBusJmsProperties properties) {
        Assert.notNull(properties, "Properties must not be null");
        this.properties = properties;
    }

    <T extends ServiceBusJmsConnectionFactory> T createConnectionFactory(Class<T> factoryClass) throws IllegalStateException {
        try {
            T factory;
            String remoteUrl = this.properties.getRemoteUrl();
            String username = this.properties.getUsername();
            String password = this.properties.getPassword();

            if (StringUtils.hasLength(username) && StringUtils.hasLength(password)) {
                factory = factoryClass.getConstructor(String.class, String.class, String.class)
                                      .newInstance(username, password, remoteUrl);
            } else {
                factory = factoryClass.getConstructor(String.class).newInstance(remoteUrl);
            }
            if (StringUtils.hasText(this.properties.getTopicClientId())) {
                factory.setClientID(this.properties.getTopicClientId());
            }
            return factory;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new IllegalStateException("Unable to create JmsConnectionFactory", ex);
        }
    }
}
