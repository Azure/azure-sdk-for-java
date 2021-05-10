// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.cloudfoundry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class AzureCloudFoundryServiceSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(AzureCloudFoundryServiceSampleApplication.class, args);
    }
}
