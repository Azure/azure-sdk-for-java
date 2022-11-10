// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.config;

import com.azure.core.util.ClientOptions;
import com.azure.core.util.MetricsOptions;
import com.azure.messaging.servicebus.stress.util.ScenarioOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientOptionsConfig {
    @Autowired
    private ScenarioOptions options;

    @Bean
    public ClientOptions clientOptions() {
        ClientOptions clientOptions = new ClientOptions();
        MetricsOptions metricsOptions = new MetricsOptions();
        metricsOptions.setEnabled(options.isClientMetricsEnabled());
        clientOptions.setMetricsOptions(metricsOptions);
        return clientOptions;
    }
}
