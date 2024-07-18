// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@Import({
    ConfigurationWithConnectionDetailsBean.class,
    ConfigurationWithoutConnectionDetailsBean.class,
})
@ConditionalOnClass(CosmosClientBuilder.class)
@EnableConfigurationProperties({AzureCosmosProperties.class, AzureGlobalProperties.class})
public class AzureCosmosPropertiesConfiguration {

}
