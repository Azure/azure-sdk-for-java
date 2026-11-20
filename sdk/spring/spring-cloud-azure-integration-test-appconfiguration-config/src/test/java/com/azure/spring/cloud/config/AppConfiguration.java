// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootConfiguration(proxyBeanMethods = false)
@EnableAutoConfiguration
@EnableConfigurationProperties({ MessageProperties.class, PaginationProperties.class, SelectionProperties.class })
class AppConfiguration {
}