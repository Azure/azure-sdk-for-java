// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure;

import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.core.aware.ClientAware;
import com.azure.spring.core.aware.ProxyAware;
import com.azure.spring.core.aware.RetryAware;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.AzurePropertiesUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * A configuration base class for all Azure SDK configuration.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
public abstract class AzureServiceConfigurationBase {

    protected AzureGlobalProperties azureGlobalProperties;

    public AzureServiceConfigurationBase(AzureGlobalProperties azureProperties) {
        this.azureGlobalProperties = azureProperties;
    }

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
