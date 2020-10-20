// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;

/**
 * Spring Integration Channel Adapters for Azure Storage Queue code sample.
 *
 * @author Miao Cao
 */
@SpringBootApplication
@EnableIntegration
public class StorageQueueIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(StorageQueueIntegrationApplication.class, args);
    }
}
