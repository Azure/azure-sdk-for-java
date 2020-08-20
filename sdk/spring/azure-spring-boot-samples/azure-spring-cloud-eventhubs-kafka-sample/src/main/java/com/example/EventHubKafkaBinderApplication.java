// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Warren Zhu
 */
@SpringBootApplication
public class EventHubKafkaBinderApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventHubKafkaBinderApplication.class, args);
    }
}
