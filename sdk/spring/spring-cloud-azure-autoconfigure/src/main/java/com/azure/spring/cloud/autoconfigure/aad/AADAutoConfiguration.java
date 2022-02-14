// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad;


import com.azure.spring.cloud.autoconfigure.aad.configuration.AADOAuth2ClientConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.configuration.AADPropertiesConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.configuration.AADResourceServerConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.configuration.AADWebApplicationConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * <p>
 * Auto configure beans required for AAD.
 * </p>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(value = "spring.cloud.azure.active-directory.enabled", havingValue = "true")
@Import({
    AADPropertiesConfiguration.class,
    AADWebApplicationConfiguration.class,
    AADResourceServerConfiguration.class,
    AADOAuth2ClientConfiguration.OAuth2ClientRepositoryConfiguration.class,
    AADOAuth2ClientConfiguration.WebApplicationOAuth2AuthorizedClientManagerConfiguration.class,
    AADOAuth2ClientConfiguration.ResourceServerWithOboOAuth2AuthorizedClientManagerConfiguration.class,
    AADOAuth2ClientConfiguration.WebApplicationAndResourceServiceOAuth2AuthorizedClientManagerConfiguration.class
})
public class AADAutoConfiguration {

}
