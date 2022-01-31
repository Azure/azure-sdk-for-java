// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure;

import com.azure.core.util.ConfigurationBuilder;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.core.aware.ClientAware;
import com.azure.spring.core.aware.ProxyAware;
import com.azure.spring.core.aware.RetryAware;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.util.AzurePropertiesUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A configuration base class for all Azure SDK configuration.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
public abstract class AzureServiceConfigurationBase {

    protected AzureGlobalProperties azureGlobalProperties;
    protected com.azure.core.util.Configuration configuration;

    /**
     * Create an instance of {@link AzureServiceConfigurationBase}.
     * @param azureProperties the {@link AzureProperties} object.
     */
    public AzureServiceConfigurationBase(AzureGlobalProperties azureProperties, com.azure.core.util.Configuration configuration) {
        this.azureGlobalProperties = azureProperties;
        this.configuration = configuration;
    }

    /**
     * Load the default value to an Azure Service properties from the global Azure properties.
     *
     * @param source The global Azure properties.
     * @param target The properties of an Azure Service, such as Event Hubs properties. Some common components of the
     *               service's properties have default value as set to the global properties. For example, the proxy of
     *               the Event Hubs properties takes the proxy set to the global Azure properties as default.
     * @param <T> The type of the properties of an Azure Service.
     * @return The Azure Service's properties.
     */
    protected <T extends AzureProperties> T loadProperties(AzureGlobalProperties source, T target) {
        AzurePropertiesUtils.copyAzureCommonProperties(source, target);

        if (target.getClient() instanceof ClientAware.HttpClient) {
            BeanUtils.copyProperties(source.getClient().getHttp(), target.getClient());

            ClientAware.HttpClient targetClient = (ClientAware.HttpClient) target.getClient();
            BeanUtils.copyProperties(source.getClient().getHttp().getLogging(), targetClient.getLogging());
            targetClient.getLogging().getAllowedHeaderNames().addAll(source.getClient().getHttp().getLogging().getAllowedHeaderNames());
            targetClient.getLogging().getAllowedQueryParamNames().addAll(source.getClient().getHttp().getLogging().getAllowedQueryParamNames());
        }

        if (target.getClient() instanceof ClientAware.AmqpClient) {
            BeanUtils.copyProperties(source.getClient().getAmqp(), target.getClient());
        }

        if (target.getProxy() instanceof ProxyAware.HttpProxy) {
            BeanUtils.copyProperties(source.getProxy().getHttp(), target.getProxy());
        }

        if (target.getRetry() instanceof RetryAware.HttpRetry) {
            BeanUtils.copyProperties(source.getRetry().getHttp(), target.getRetry());
        }

        return target;
    }
}
