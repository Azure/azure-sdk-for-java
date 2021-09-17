// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.config;

import com.azure.spring.messaging.core.SubscribeByGroupOperation;
import com.azure.spring.messaging.container.DefaultAzureListenerContainerFactory;
import com.azure.spring.messaging.container.ListenerContainerFactory;
import com.azure.spring.messaging.container.MessageListenerContainer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Warren Zhu
 */
@Configuration
public class AzureMessagingConfiguration {

    @ConditionalOnMissingBean
    @Bean(name = AzureListenerAnnotationBeanPostProcessor.DEFAULT_AZURE_LISTENER_CONTAINER_FACTORY_BEAN_NAME)
    public ListenerContainerFactory<? extends MessageListenerContainer> azureListenerContainerFactory(
        SubscribeByGroupOperation subscribeByGroupOperation) {
        return new DefaultAzureListenerContainerFactory(subscribeByGroupOperation);
    }
}
