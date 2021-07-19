// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.context.core.api.AzureResourceMetadata;
import com.azure.spring.identity.DefaultSpringCredentialBuilder;
import com.azure.spring.cloud.context.core.impl.AzureManager;
import com.azure.spring.cloud.context.core.impl.ResourceGroupManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 *
 *
 * @author Warren Zhu
 */
@Configuration
@EnableConfigurationProperties(AzureContextProperties.class)
@ConditionalOnClass(AzureManager.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure", value = { "resource-group" })
public class AzureResourceManagerAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public AzureEnvironment azureEnvironment(AzureContextProperties azureContextProperties) {
        return parseAzureEnvironment(azureContextProperties.getEnvironment());
    }

    @Bean
    @ConditionalOnMissingBean
    public AzureProfile azureProfile(AzureContextProperties azureContextProperties, AzureEnvironment azureEnvironment) {
        return new AzureProfile(azureContextProperties.getTenantId(),
                                azureContextProperties.getSubscriptionId(),
                                azureEnvironment);
    }

    /**
     * Create an {@link AzureResourceManager} bean.
     *
     * @param credential The credential to connect to Azure.
     * @param profile The azure profile.
     * @return An AzureResourceManager object.
     */
    @Bean
    @ConditionalOnMissingBean
    public AzureResourceManager azureResourceManager(TokenCredential credential, AzureProfile profile) {
        // TODO (xiada) Do we need to pass our User-Agent to with the management sdk?
        return AzureResourceManager.configure()
                                   .authenticate(credential, profile)
                                   .withDefaultSubscription();
    }

    @Bean
    @ConditionalOnMissingBean
    public AzureResourceMetadata azureResourceMetadata(AzureContextProperties azureContextProperties) {
        AzureResourceMetadata azureResourceMetadata = new AzureResourceMetadata();
        azureResourceMetadata.setAutoCreateResources(azureContextProperties.isAutoCreateResources());
        azureResourceMetadata.setRegion(azureContextProperties.getRegion());
        azureResourceMetadata.setResourceGroup(azureContextProperties.getResourceGroup());

        return azureResourceMetadata;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(AzureResourceManager.class)
    public ResourceGroupManager resourceGroupManager(AzureResourceManager azureResourceManager,
                                                     AzureResourceMetadata azureResourceMetadata) {
        ResourceGroupManager resourceGroupManager = new ResourceGroupManager(azureResourceManager, azureResourceMetadata);
        if (azureResourceMetadata.isAutoCreateResources()
                && !resourceGroupManager.exists(azureResourceMetadata.getResourceGroup())) {
            resourceGroupManager.create(azureResourceMetadata.getResourceGroup());
        }
        return resourceGroupManager;
    }


    private AzureEnvironment parseAzureEnvironment(String environment) {
        AzureEnvironment azureEnvironment = AzureEnvironment.AZURE;

        if (!StringUtils.hasText(environment)) {
            return azureEnvironment;
        }

        switch (environment.toUpperCase(Locale.ROOT)) {
            case "AZURE_CHINA":
                azureEnvironment = AzureEnvironment.AZURE_CHINA;
                break;
            case "AZURE_US_GOVERNMENT":
                azureEnvironment = AzureEnvironment.AZURE_US_GOVERNMENT;
                break;
            case "AZURE_GERMANY":
                azureEnvironment = AzureEnvironment.AZURE_GERMANY;
                break;
            default:
                azureEnvironment = AzureEnvironment.AZURE;
                break;
        }

        return azureEnvironment;
    }

    // TODO (xiada) shouldn't be here
    @Bean
    @ConditionalOnMissingBean
    public TokenCredential credential(Environment environment) {
        return new DefaultSpringCredentialBuilder().environment(environment)
                                                   .alternativePrefix(AzureContextProperties.PREFIX)
                                                   .build();
    }


}
