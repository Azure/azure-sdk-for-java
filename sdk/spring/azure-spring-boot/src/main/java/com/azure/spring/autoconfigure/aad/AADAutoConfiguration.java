// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.aad;


import com.azure.spring.aad.AADOAuth2ClientConfiguration;
import com.azure.spring.aad.webapi.AADResourceServerConfiguration;
import com.azure.spring.aad.webapi.AADResourceServerProperties;
import com.azure.spring.aad.webapp.AADWebApplicationConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * <p>
 * Auto configure beans required for AAD.
 * </p>
 */
@Configuration
@ConditionalOnResource(resources = "classpath:aad.enable.config")
@EnableConfigurationProperties({
    AADAuthenticationProperties.class,
    AADResourceServerProperties.class
})
@Import({
    AADWebApplicationConfiguration.class,
    AADResourceServerConfiguration.class,
    AADOAuth2ClientConfiguration.class
})
public class AADAutoConfiguration {

}
