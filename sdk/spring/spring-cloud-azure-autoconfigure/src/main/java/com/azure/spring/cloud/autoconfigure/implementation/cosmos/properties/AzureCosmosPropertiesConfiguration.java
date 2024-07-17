// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties;

import com.azure.cosmos.CosmosClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@Import({
    ConfigurationWithClassWithBean.class,
    ConfigurationWithClassWithoutBean.class,
    ConfigurationWithoutClass.class
})
@ConditionalOnClass(CosmosClientBuilder.class)
@EnableConfigurationProperties
public class AzureCosmosPropertiesConfiguration {

}
