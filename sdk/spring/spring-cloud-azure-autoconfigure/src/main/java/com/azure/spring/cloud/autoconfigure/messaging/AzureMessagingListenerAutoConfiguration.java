// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.messaging;

import com.azure.spring.cloud.autoconfigure.eventhubs.AzureEventHubOperationAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusTopicMessagingAutoConfiguration;
import com.azure.spring.messaging.annotation.EnableAzureMessaging;
import com.azure.spring.messaging.config.AzureListenerAnnotationBeanPostProcessor;
import com.azure.spring.messaging.core.SubscribeByGroupOperation;
import com.azure.spring.messaging.container.DefaultAzureListenerContainerFactory;
import com.azure.spring.messaging.container.ListenerContainerFactory;
import com.azure.spring.messaging.container.MessageListenerContainer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Warren Zhu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EnableAzureMessaging.class)
@AutoConfigureAfter(value = { AzureEventHubOperationAutoConfiguration.class,
    AzureServiceBusTopicMessagingAutoConfiguration.class })
@ConditionalOnBean(SubscribeByGroupOperation.class)
public class AzureMessagingListenerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = AzureListenerAnnotationBeanPostProcessor.DEFAULT_AZURE_LISTENER_CONTAINER_FACTORY_BEAN_NAME)
    public ListenerContainerFactory<? extends MessageListenerContainer> azureListenerContainerFactory(
        SubscribeByGroupOperation subscribeByGroupOperation) {
        return new DefaultAzureListenerContainerFactory(subscribeByGroupOperation);
    }
}
