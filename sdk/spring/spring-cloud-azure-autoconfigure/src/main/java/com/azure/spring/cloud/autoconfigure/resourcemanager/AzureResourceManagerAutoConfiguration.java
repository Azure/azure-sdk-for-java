// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Resource Manager support.
 *
 * @since 4.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(AzureResourceManager.class)
@ConditionalOnExpression("${spring.cloud.azure.resource-manager.enabled:true}")
@ConditionalOnProperty("spring.cloud.azure.profile.subscription-id")
public class AzureResourceManagerAutoConfiguration {

    private final AzureGlobalProperties globalProperties;

    /**
     * Create {@link AzureResourceManagerAutoConfiguration} instance
     * @param globalProperties the azure global properties
     */
    AzureResourceManagerAutoConfiguration(AzureGlobalProperties globalProperties) {
        this.globalProperties = globalProperties;
    }

    /**
     * Autoconfigure the {@link AzureResourceManager} instance.
     * @param tokenCredential the {@link TokenCredential} used to authenticate with the {@link AzureResourceManager}.
     * @param azureProfile the {@link AzureProfile} used by the {@link AzureResourceManager}.
     * @return the Azure resource manager.
     */
    @Bean
    @ConditionalOnMissingBean
    public AzureResourceManager azureResourceManager(TokenCredential tokenCredential, AzureProfile azureProfile) {
        // TODO (xiada) Do we need to pass our User-Agent to with the management sdk?
        // TODO (xiada) configure the http client of arm client
        return AzureResourceManager.configure().authenticate(tokenCredential, azureProfile).withDefaultSubscription();
    }

    /**
     * Autoconfigure the {@link AzureProfile} instance.
     * @return the azure profile.
     */
    @Bean
    @ConditionalOnMissingBean
    public AzureProfile azureProfile() {
        return new AzureProfile(this.globalProperties.getProfile().getTenantId(),
                                this.globalProperties.getProfile().getSubscriptionId(),
                                this.globalProperties.getProfile().getEnvironment().toAzureManagementEnvironment());
    }

}
