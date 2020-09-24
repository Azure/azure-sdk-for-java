// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.context;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.utils.ProviderRegistrationInterceptor;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceManagerThrottlingInterceptor;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.azure.spring.cloud.context.core.api.CredentialsProvider;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;
import com.microsoft.azure.spring.cloud.context.core.impl.AzureResourceManagerProvider;
import com.microsoft.azure.spring.cloud.context.core.impl.DefaultCredentialsProvider;
import com.microsoft.rest.RestClient;
import java.io.IOException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-config to provide default {@link CredentialsProvider} for all Azure services
 *
 * @author Warren Zhu
 */
@Configuration
@EnableConfigurationProperties(AzureProperties.class)
@ConditionalOnClass(Azure.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure", value = {"resource-group"})
public class AzureContextAutoConfiguration {

    private static final String PROJECT_VERSION =
        AzureContextAutoConfiguration.class.getPackage().getImplementationVersion();
    private static final String SPRING_CLOUD_USER_AGENT = "spring-cloud-azure/" + PROJECT_VERSION;

    @Bean
    @ConditionalOnMissingBean
    public ResourceManagerProvider resourceManagerProvider(Azure azure, AzureProperties azureProperties) {
        return new AzureResourceManagerProvider(azure, azureProperties);
    }

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

        if (azureProperties.getSubscriptionId() == null) {
            return Azure.authenticate(restClient, credentials.domain()).withDefaultSubscription();
        } else {
            return Azure.authenticate(restClient, credentials.domain()).withSubscription(azureProperties.getSubscriptionId());
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public AzureTokenCredentials credentials(AzureProperties azureProperties) {
        CredentialsProvider credentialsProvider = new DefaultCredentialsProvider(azureProperties);
        return credentialsProvider.getCredentials();
    }

}
