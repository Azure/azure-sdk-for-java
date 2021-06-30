// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.CoreUtils;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.azure.spring.integration.servicebus.factory.ServiceBusConnectionStringProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.regex.Pattern;

/**
 * An auto-configuration for Service Bus
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnClass(ServiceBusReceivedMessage.class)
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureServiceBusProperties.class)
public class AzureServiceBusAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusAutoConfiguration.class);

    private static final Pattern HOST_PORT_PATTERN = Pattern.compile("^[^:]+:\\d+");

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ AzureResourceManager.class, AzureProperties.class })
    public ServiceBusNamespaceManager serviceBusNamespaceManager(AzureResourceManager azureResourceManager,
                                                                 AzureProperties azureProperties) {
        return new ServiceBusNamespaceManager(azureResourceManager, azureProperties);
    }

    /**
     * Create a {@link ServiceBusConnectionStringProvider} bean. The bean will hold the connection string to the service
     * bus. If connection-string property is configured in the property files, it will use it. Otherwise, it will try to
     * construct the connection-string using the resource manager.
     *
     * @param namespaceManager The resource manager for Service Bus namespaces.
     * @param properties The Service Bus properties.
     * @return The {@link ServiceBusConnectionStringProvider} bean.
     * @throws IllegalArgumentException If connection string is empty.
     */
    @Bean
    @ConditionalOnMissingBean
    public ServiceBusConnectionStringProvider serviceBusConnectionStringProvider(
        @Autowired(required = false) ServiceBusNamespaceManager namespaceManager,
        AzureServiceBusProperties properties) {

        final String namespace = properties.getNamespace();
        final String connectionString = properties.getConnectionString();

        if (StringUtils.hasText(connectionString)) {
            return new ServiceBusConnectionStringProvider(connectionString);
        } else if (namespaceManager != null && namespace != null) {
            LOGGER.info("'spring.cloud.azure.servicebus.connection-string' auto configured");

            return new ServiceBusConnectionStringProvider(namespaceManager.getOrCreate(namespace));
        }

        LOGGER.warn("Can't construct the ServiceBusConnectionStringProvider, namespace: {}, connectionString: {}",
            namespace, connectionString);

        return null;
    }

    @Bean
    @ConditionalOnMissingBean
    public com.azure.core.util.Configuration configuration() {
        return com.azure.core.util.Configuration.getGlobalConfiguration().clone();
    }

    @Bean
    @ConditionalOnMissingBean
    public ProxyOptions proxyOptions(com.azure.core.util.Configuration configuration) {

        ProxyAuthenticationType authentication = ProxyAuthenticationType.NONE;

        String proxyAddress = configuration.get(com.azure.core.util.Configuration.PROPERTY_HTTP_PROXY);

        if (CoreUtils.isNullOrEmpty(proxyAddress)) {
            return ProxyOptions.SYSTEM_DEFAULTS;
        }

        return getProxyOptions(authentication, proxyAddress, configuration);
    }

    private ProxyOptions getProxyOptions(ProxyAuthenticationType authentication, String proxyAddress, com.azure.core.util.Configuration configuration) {
        String host;
        int port;
        if (HOST_PORT_PATTERN.matcher(proxyAddress.trim()).find()) {
            final String[] hostPort = proxyAddress.split(":");
            host = hostPort[0];
            port = Integer.parseInt(hostPort[1]);
            final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            final String username = configuration.get(ProxyOptions.PROXY_USERNAME);
            final String password = configuration.get(ProxyOptions.PROXY_PASSWORD);
            return new ProxyOptions(authentication, proxy, username, password);
        } else {
            com.azure.core.http.ProxyOptions coreProxyOptions = com.azure.core.http.ProxyOptions
                .fromConfiguration(configuration);
            return new ProxyOptions(authentication, new Proxy(coreProxyOptions.getType().toProxyType(),
                coreProxyOptions.getAddress()), coreProxyOptions.getUsername(), coreProxyOptions.getPassword());
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientOptions clientOptions() {
        return new ClientOptions();
    }
}
