// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.azure.spring.integration.eventhub.api.EventHubOperation;
import com.azure.spring.integration.eventhub.factory.DefaultEventHubClientFactory;
import com.azure.spring.integration.eventhub.factory.EventHubSharedAuthenticationClientBuilder;
import com.azure.spring.integration.eventhub.factory.EventProcessorSharedAuthenticationClientBuilder;
import com.azure.spring.integration.eventhub.impl.EventHubTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * An auto-configuration for Event Hub, which provides {@link EventHubOperation}
 *
 * @author Warren Zhu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EventHubOperation.class)
@ConditionalOnBean(EventHubSharedAuthenticationClientBuilder.class)
@AzureEventHubAutoConfiguration.ConditionalOnEventHub
@AutoConfigureAfter(AzureEventHubAutoConfiguration.class)
@Import(AzureEventHubSharedCredentialClientConfiguration.class)
public class AzureEventHubOperationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EventHubClientFactory eventhubClientFactory(EventHubSharedAuthenticationClientBuilder eventHubServiceClientBuilder,
                                                       EventProcessorSharedAuthenticationClientBuilder eventProcessorServiceClientBuilder) {
        return new DefaultEventHubClientFactory(eventHubServiceClientBuilder, eventProcessorServiceClientBuilder);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubOperation eventHubOperation(EventHubClientFactory clientFactory) {
        return new EventHubTemplate(clientFactory);
    }

}
