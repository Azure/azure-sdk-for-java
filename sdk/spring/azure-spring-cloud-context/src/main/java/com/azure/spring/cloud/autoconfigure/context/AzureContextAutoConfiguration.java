// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import java.io.IOException;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.spring.cloud.context.core.api.CredentialsProvider;
import com.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.cloud.context.core.impl.AzureResourceManagerProvider;
import com.azure.spring.cloud.context.core.impl.DefaultCredentialsProvider;
import com.google.common.annotations.VisibleForTesting;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.utils.ProviderRegistrationInterceptor;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceManagerThrottlingInterceptor;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;

/**
 * Auto-config to provide default {@link CredentialsProvider} for all Azure
 * services
 *
 * @author Warren Zhu
 */
@Configuration
@EnableConfigurationProperties(AzureProperties.class)
@ConditionalOnClass(Azure.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure", value = { "resource-group" })
public class AzureContextAutoConfiguration {

    private static final String PROJECT_VERSION = AzureContextAutoConfiguration.class.getPackage()
            .getImplementationVersion();
    private static final String SPRING_CLOUD_USER_AGENT = "spring-cloud-azure/" + PROJECT_VERSION;

    @Bean
    @ConditionalOnMissingBean
    public ResourceManagerProvider resourceManagerProvider(Azure azure, AzureProperties azureProperties) {
        return new AzureResourceManagerProvider(azure, azureProperties);
    }

    /**
     * Create an {@link Azure} bean.
     * 
     * @param credentials     The credential to connect to Azure.
     * @param azureProperties The configured Azure properties.
     * @return An Azure object.
     * @throws IOException When IOException happens.
     */
    @Bean
    @ConditionalOnMissingBean
    public Azure azure(AzureTokenCredentials credentials, AzureProperties azureProperties) throws IOException {
        RestClient restClient = new RestClient.Builder()
                .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials).withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .withInterceptor(new ProviderRegistrationInterceptor(credentials))
                .withInterceptor(new ResourceManagerThrottlingInterceptor()).withUserAgent(SPRING_CLOUD_USER_AGENT)
                .build();

        String subscriptionId = Optional.ofNullable(azureProperties.getSubscriptionId())
                .orElseGet(credentials::defaultSubscriptionId);

        return authenticateToAzure(restClient, subscriptionId, credentials);
    }

    @VisibleForTesting
    protected Azure authenticateToAzure(RestClient restClient, String subscriptionId,
            AzureTokenCredentials credentials) {
        return Azure.authenticate(restClient, credentials.domain()).withSubscription(subscriptionId);
    }

    @Bean
    @ConditionalOnMissingBean
    public AzureTokenCredentials credentials(AzureProperties azureProperties) {
        CredentialsProvider credentialsProvider = new DefaultCredentialsProvider(azureProperties);
        return credentialsProvider.getCredentials();
    }

}
