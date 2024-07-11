// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@Import({
    AzureCosmosPropertiesWithConnectionDetailsClassWithConnectionDetailsBeanConfiguration.class,
    AzureCosmosPropertiesWithConnectionDetailsClassWithoutConnectionDetailsBeanConfiguration.class,
    AzureCosmosPropertiesWithoutConnectionDetailsClassConfiguration.class
})
@EnableConfigurationProperties
public class AzureCosmosPropertiesConfiguration {

}
