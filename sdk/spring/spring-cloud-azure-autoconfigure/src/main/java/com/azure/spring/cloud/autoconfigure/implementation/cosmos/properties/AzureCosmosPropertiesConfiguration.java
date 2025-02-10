// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@Import({
    ConfigurationWithConnectionDetailsBean.class,
    ConfigurationWithoutConnectionDetailsBean.class,
})
@EnableConfigurationProperties
public class AzureCosmosPropertiesConfiguration {

}
