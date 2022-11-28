// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;

class KafkaBinderConfigurationPropertiesBeanPostProcessorTest
    extends AbstractKafkaPropertiesBeanPostProcessorTest<KafkaBinderConfigurationPropertiesBeanPostProcessor> {

    KafkaBinderConfigurationPropertiesBeanPostProcessorTest() {
        super(new KafkaBinderConfigurationPropertiesBeanPostProcessor(new AzureGlobalProperties()));
    }
}
