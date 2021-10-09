// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 *
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(AzureResourceManager.class)
@ConditionalOnExpression("${spring.cloud.azure.resource-manager.enabled:true}")
@ConditionalOnProperty({ "spring.cloud.azure.profile.tenant-id", "spring.cloud.azure.profile.subscription-id" })
public class AzureResourceManagerAutoConfiguration {

    private final AzureGlobalProperties globalProperties;

    public AzureResourceManagerAutoConfiguration(AzureGlobalProperties globalProperties) {
        this.globalProperties = globalProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public AzureResourceManager azureResourceManager(TokenCredential tokenCredential, AzureProfile azureProfile) {
        // TODO (xiada) Do we need to pass our User-Agent to with the management sdk?
        // TODO (xiada) configure the http client of arm client
        return AzureResourceManager.configure().authenticate(tokenCredential, azureProfile).withDefaultSubscription();
    }

    @Bean
    @ConditionalOnMissingBean
    public AzureProfile azureProfile() {
        return new AzureProfile(this.globalProperties.getProfile().getTenantId(),
                                this.globalProperties.getProfile().getSubscriptionId(),
                                new AzureEnvironment(this.globalProperties.getProfile()
                                                                          .getEnvironment()
                                                                          .exportEndpointsMap()));
    }

}
