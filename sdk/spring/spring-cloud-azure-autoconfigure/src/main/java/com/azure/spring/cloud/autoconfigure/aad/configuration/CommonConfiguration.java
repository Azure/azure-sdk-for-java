// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.configuration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestOperations;

/**
 * Common bean used for Azure AD and Azure AD B2C
 */
@Configuration(proxyBeanMethods = false)
public class CommonConfiguration {

    /**
     * Declare RestOperations bean used by various OAuth2AccessTokenResponseClient.
     *
     * @param builderObjectProvider the optional rest template builder bean.
     * @return RestOperations bean
     */
    @Bean
    @ConditionalOnMissingBean(RestOperations.class)
    public RestOperations aadAuthRestOperations(ObjectProvider<RestTemplateBuilder> builderObjectProvider) {
        RestTemplateBuilder builder = builderObjectProvider.getIfAvailable(RestTemplateBuilder::new);
        return builder.build();
    }

}
