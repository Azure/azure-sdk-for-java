// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.stream.binder.kafka.config.KafkaBinderConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * {@link org.springframework.boot.autoconfigure.AutoConfiguration} of {@link BindingServicePropertiesBeanPostProcessor}.
 * To trigger the {@link BindingServicePropertiesBeanPostProcessor} when kafka binder is used.
 *
 * @since 4.4.0
 */
@ConditionalOnClass(KafkaBinderConfiguration.class)
public class BindingServicePropertiesBeanPostProcessorAutoConfiguration {

    @Bean
    static BeanPostProcessor bindingServicePropertiesBeanPostProcessor() {
        return new BindingServicePropertiesBeanPostProcessor();
    }
}
