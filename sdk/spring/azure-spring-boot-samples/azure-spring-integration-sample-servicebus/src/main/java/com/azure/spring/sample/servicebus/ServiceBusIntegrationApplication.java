// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.servicebus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;

/**
 * Spring Integration Channel Adapters for Azure Service Bus code sample.
 *
 * @author Warren Zhu
 */
@SpringBootApplication
@EnableIntegration
public class ServiceBusIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceBusIntegrationApplication.class, args);
    }
}
