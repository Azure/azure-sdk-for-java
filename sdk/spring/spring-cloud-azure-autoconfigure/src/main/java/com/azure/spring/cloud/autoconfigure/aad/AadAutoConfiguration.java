// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad;


import com.azure.spring.cloud.autoconfigure.aad.configuration.AadOAuth2ClientConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.configuration.AadPropertiesConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.configuration.AadResourceServerConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.configuration.AadWebApplicationConfiguration;
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
    AadPropertiesConfiguration.class,
    AadWebApplicationConfiguration.class,
    AadResourceServerConfiguration.class,
    AadOAuth2ClientConfiguration.OAuth2ClientRepositoryConfiguration.class,
    AadOAuth2ClientConfiguration.WebApplicationWithoutResourceServerOAuth2AuthorizedClientManagerConfiguration.class,
    AadOAuth2ClientConfiguration.ResourceServerWithOBOOAuth2AuthorizedClientManagerConfiguration.class,
    AadOAuth2ClientConfiguration.WebApplicationAndResourceServiceOAuth2AuthorizedClientManagerConfiguration.class
})
public class AadAutoConfiguration {

}
