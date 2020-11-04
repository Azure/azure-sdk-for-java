// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.azure.identity.spring.SpringEnvironmentTokenBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Auto-configure the new {@link AzureResourceManager}.
 */
@Configuration
@EnableConfigurationProperties(AzureProperties.class)
@ConditionalOnClass(AzureResourceManager.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure", value = { "resource-group" })
public class AzureResourceManager20AutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureResourceManager20AutoConfiguration.class);

    @Autowired(required = false)
    private AzureTokenCredentials credentials;

    @Bean
    @ConditionalOnMissingBean
    public AzureResourceManager.Authenticated azure20(TokenCredential tokenCredential,
                                                      AzureProperties azureProperties) {
        AzureEnvironment legacyEnvironment = azureProperties.getEnvironment();
        com.azure.core.management.AzureEnvironment azureEnvironment = com.azure.core.management.AzureEnvironment
            .knownEnvironments().stream()
            .filter(env -> env.getManagementEndpoint().equals(legacyEnvironment.managementEndpoint()))
            .findFirst()
            .get();
        return AzureResourceManager.authenticate(tokenCredential, new AzureProfile(azureEnvironment));
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenCredential tokenCredential(Environment environment, AzureProperties azureProperties) {
        SpringEnvironmentTokenBuilder builder = new SpringEnvironmentTokenBuilder().fromEnvironment(environment);
        if (!StringUtils.isBlank(azureProperties.getCredentialFilePath())) {
            if (credentials == null) {
                LOGGER.error("Legacy azure credentials not initialized though credential-file-path was provided");
            }
            builder.overrideNamedCredential("", new LegacyTokenCredentialAdapter(credentials));
        }
        return builder.build();
    }
}
