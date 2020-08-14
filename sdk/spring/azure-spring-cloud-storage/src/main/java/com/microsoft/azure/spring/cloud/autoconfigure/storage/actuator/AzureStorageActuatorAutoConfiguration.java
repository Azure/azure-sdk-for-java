// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.storage.actuator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.microsoft.azure.spring.cloud.autoconfigure.storage.AzureStorageAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.storage.AzureStorageQueueAutoConfiguration;

@Configuration
@AutoConfigureAfter({ AzureStorageAutoConfiguration.class, AzureStorageQueueAutoConfiguration.class })
@ConditionalOnClass(HealthIndicator.class)
public class AzureStorageActuatorAutoConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public BlobStorageHealthIndicator blobStorageHealthIndicator() {
        return new BlobStorageHealthIndicator(applicationContext);
    }

    @Bean
    public FileStorageHealthIndicator fileStorageHealthIndicator() {
        return new FileStorageHealthIndicator(applicationContext);
    }

}
