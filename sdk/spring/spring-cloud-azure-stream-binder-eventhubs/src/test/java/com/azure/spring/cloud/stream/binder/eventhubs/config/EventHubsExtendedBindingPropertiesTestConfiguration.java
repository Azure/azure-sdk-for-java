// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.config;

import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsExtendedBindingProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(EventHubsExtendedBindingProperties.class)
public class EventHubsExtendedBindingPropertiesTestConfiguration {
}
