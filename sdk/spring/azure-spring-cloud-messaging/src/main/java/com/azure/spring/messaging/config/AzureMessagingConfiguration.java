// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.config;

import com.azure.spring.integration.core.api.SubscribeByGroupOperation;
import com.azure.spring.messaging.container.DefaultAzureListenerContainerFactory;
import com.azure.spring.messaging.container.ListenerContainerFactory;
import com.azure.spring.messaging.container.MessageListenerContainer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

/**
 * @author Warren Zhu
 */
@Configuration
public class AzureMessagingConfiguration {

    /**
     * Bean for the {@link ListenerContainerFactory}.
     * @param subscribeByGroupOperation the {@link SubscribeByGroupOperation}.
     * @param errorHandler the {@link ErrorHandler}
     * @return the {@link ListenerContainerFactory} bean.
     */
    @ConditionalOnMissingBean
    @Bean(name = AzureListenerAnnotationBeanPostProcessor.DEFAULT_AZURE_LISTENER_CONTAINER_FACTORY_BEAN_NAME)
    public ListenerContainerFactory<? extends MessageListenerContainer> azureListenerContainerFactory(
        SubscribeByGroupOperation subscribeByGroupOperation, ObjectProvider<ErrorHandler> errorHandler) {
        DefaultAzureListenerContainerFactory azureListenerContainerFactory =
            new DefaultAzureListenerContainerFactory(subscribeByGroupOperation);
        azureListenerContainerFactory.setErrorHandler(errorHandler.getIfUnique());
        return azureListenerContainerFactory;
    }
}
