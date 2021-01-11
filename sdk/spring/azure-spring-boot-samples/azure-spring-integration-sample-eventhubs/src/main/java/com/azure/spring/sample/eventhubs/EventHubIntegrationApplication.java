// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.eventhubs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;

/**
 * Spring Integration Channel Adapters for Azure Event Hub code sample.
 *
 * @author Warren Zhu
 */
@SpringBootApplication
@EnableIntegration
public class EventHubIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventHubIntegrationApplication.class, args);
    }
}
