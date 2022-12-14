// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaBinderConfigurationProperties;

class KafkaBinderConfigurationPropertiesBeanPostProcessorTest
    extends AbstractKafkaPropertiesBeanPostProcessorTest<KafkaBinderConfigurationPropertiesBeanPostProcessor, KafkaBinderConfigurationProperties> {

    KafkaBinderConfigurationPropertiesBeanPostProcessorTest() {
        super(new KafkaBinderConfigurationPropertiesBeanPostProcessor(new AzureGlobalProperties()));
    }
}
