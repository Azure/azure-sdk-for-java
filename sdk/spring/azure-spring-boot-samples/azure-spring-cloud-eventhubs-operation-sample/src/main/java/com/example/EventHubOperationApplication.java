// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import com.microsoft.azure.spring.integration.eventhub.api.EventHubOperation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * {@link EventHubOperation} code sample.
 *
 * @author Warren Zhu
 */
@SpringBootApplication
public class EventHubOperationApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventHubOperationApplication.class, args);
    }
}
