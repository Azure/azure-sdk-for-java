// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

public class KafkaPropertiesBeanPostProcessorTest
    extends AbstractKafkaPropertiesBeanPostProcessorTest<KafkaPropertiesBeanPostProcessor, KafkaProperties> {

    KafkaPropertiesBeanPostProcessorTest() {
        super(new KafkaPropertiesBeanPostProcessor(new AzureGlobalProperties()));
    }
}
