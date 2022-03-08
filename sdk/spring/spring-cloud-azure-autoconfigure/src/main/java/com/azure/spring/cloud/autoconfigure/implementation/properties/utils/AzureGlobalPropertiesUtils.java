// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.utils;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.core.provider.ClientOptionsProvider;
import com.azure.spring.cloud.core.provider.ProxyOptionsProvider;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.cloud.core.properties.AzureProperties;
import org.springframework.beans.BeanUtils;

/**
 * Util class for processor {@link AzureGlobalProperties}.
 */
public final class AzureGlobalPropertiesUtils {

    private AzureGlobalPropertiesUtils() {

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
    public static <T extends AzureProperties> T loadProperties(AzureGlobalProperties source, T target) {
        AzurePropertiesUtils.copyAzureCommonProperties(source, target);

        if (target.getClient() instanceof ClientOptionsProvider.HttpClientOptions) {
            BeanUtils.copyProperties(source.getClient().getHttp(), target.getClient());

            ClientOptionsProvider.HttpClientOptions targetClient = (ClientOptionsProvider.HttpClientOptions) target.getClient();

            targetClient.getHeaders().addAll(source.getClient().getHttp().getHeaders());

            BeanUtils.copyProperties(source.getClient().getHttp().getLogging(), targetClient.getLogging());
            targetClient.getLogging().getAllowedHeaderNames().addAll(source.getClient().getHttp().getLogging().getAllowedHeaderNames());
            targetClient.getLogging().getAllowedQueryParamNames().addAll(source.getClient().getHttp().getLogging().getAllowedQueryParamNames());
        } else if (target.getClient() instanceof ClientOptionsProvider.AmqpClientOptions) {
            BeanUtils.copyProperties(source.getClient().getAmqp(), target.getClient());
        }

        if (target.getProxy() instanceof ProxyOptionsProvider.HttpProxyOptions) {
            BeanUtils.copyProperties(source.getProxy().getHttp(), target.getProxy());
        } else if (target.getProxy() instanceof ProxyOptionsProvider.AmqpProxyOptions) {
            BeanUtils.copyProperties(source.getProxy().getAmqp(), target.getProxy());
        }

        if (target instanceof RetryOptionsProvider) {
            RetryOptionsProvider.RetryOptions retry = ((RetryOptionsProvider) target).getRetry();
            if (retry instanceof RetryOptionsProvider.AmqpRetryOptions) {
                BeanUtils.copyProperties(source.getRetry().getAmqp(), retry);
            }
        }

        return target;
    }

}
