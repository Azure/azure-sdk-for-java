// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure;

import com.azure.spring.cloud.autoconfigure.properties.AzureConfigurationProperties;
import com.azure.spring.core.properties.AzureProperties;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * A configuration base class for all Azure SDK configuration.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnBean(AzureConfigurationProperties.class)
public class AzureServiceConfigurationBase {

    protected AzureConfigurationProperties azureProperties;

    public AzureServiceConfigurationBase(AzureConfigurationProperties azureProperties) {
        this.azureProperties = azureProperties;
    }

    protected <T extends AzureProperties> T copyProperties(AzureConfigurationProperties source, T target) {
        BeanUtils.copyProperties(source.getClient(), target.getClient());
        BeanUtils.copyProperties(source.getProxy(), target.getProxy());
        BeanUtils.copyProperties(source.getRetry(), target.getRetry());
        BeanUtils.copyProperties(source.getProfile(), target.getProfile());
        BeanUtils.copyProperties(source.getCredential(), target.getCredential());
        return target;
    }
}
