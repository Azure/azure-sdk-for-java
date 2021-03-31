// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.messaging.servicebus;

import com.azure.spring.messaging.annotation.EnableAzureMessaging;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAzureMessaging
public class ServiceBusMessagingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceBusMessagingApplication.class, args);
    }

}
