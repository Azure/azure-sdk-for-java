// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad;


import com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2.AADOAuth2ClientConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.properties.AADAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.aad.properties.AADResourceServerProperties;
import com.azure.spring.cloud.autoconfigure.aad.webapi.AADResourceServerConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.webapp.AADWebApplicationConfiguration;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * <p>
 * Auto configure beans required for AAD.
 * </p>
 */
@Configuration
@ConditionalOnProperty(value = "spring.cloud.azure.active-directory.enabled", havingValue = "true")
@EnableConfigurationProperties({
    AzureGlobalProperties.class,
    AADAuthenticationProperties.class,
    AADResourceServerProperties.class
})
@Import({
    AADWebApplicationConfiguration.class,
    AADResourceServerConfiguration.class,
    AADOAuth2ClientConfiguration.class
})
public class AADAutoConfiguration {

    public AADAutoConfiguration(AzureGlobalProperties global, AADAuthenticationProperties aad) {
        if (aad.getCredential().getClientId() == null) {
            aad.setClientId(global.getCredential().getClientId());
        }
        if (aad.getCredential().getClientSecret() == null) {
            aad.setClientSecret(global.getCredential().getClientSecret());
        }
        if (aad.getProfile().getTenantId() == null) {
            aad.setTenantId(global.getProfile().getTenantId());
        }
        if (aad.getProfile().getCloud() == null) {
            aad.getProfile().setCloud(global.getProfile().getCloud());
        }
        if (aad.getProfile().getEnvironment().getActiveDirectoryEndpoint() == null) {
            aad.getProfile().getEnvironment().setActiveDirectoryEndpoint(
                global.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        }
        if (aad.getProfile().getEnvironment().getMicrosoftGraphEndpoint() == null) {
            aad.getProfile().getEnvironment().setMicrosoftGraphEndpoint(
                global.getProfile().getEnvironment().getMicrosoftGraphEndpoint());
        }
    }

}
