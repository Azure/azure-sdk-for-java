// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad;


import com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2.AADOAuth2ClientConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.properties.AADPropertiesConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.webapi.AADResourceServerConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.webapp.AADWebApplicationConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * <p>
 * Auto configure beans required for AAD.
 * </p>
 */
@Configuration
@ConditionalOnProperty(value = "spring.cloud.azure.active-directory.enabled", havingValue = "true")
@Import({
    AADPropertiesConfiguration.class,
    AADWebApplicationConfiguration.class,
    AADResourceServerConfiguration.class,
    AADOAuth2ClientConfiguration.class
})
public class AADAutoConfiguration {

}
