// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.factory;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.core.connectionstring.ConnectionStringProvider;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.provider.AzureCredentialProvider;
import com.azure.spring.core.credential.resolver.AzureCredentialResolver;
import com.azure.spring.core.credential.resolver.AzureCredentialResolvers;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.core.customizer.NoOpAzureServiceClientBuilderCustomizer;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Abstract azure service client builder factory, it's the implementation of {@link AzureServiceClientBuilderFactory} to
 * provide the template methods to extend any service on the top of azure core.
 *
 * @param <T> Type of the service client builder
 */
public abstract class AbstractAzureServiceClientBuilderFactory<T> implements AzureServiceClientBuilderFactory<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAzureServiceClientBuilderFactory.class);
    protected abstract T createBuilderInstance();

    protected abstract AzureProperties getAzureProperties();

    protected abstract List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(T builder);

    protected abstract void configureApplicationId(T builder);

    protected abstract void configureProxy(T builder);

    protected abstract void configureRetry(T builder);

    protected abstract void configureService(T builder);

    protected abstract BiConsumer<T, Configuration> consumeConfiguration();

    protected abstract BiConsumer<T, TokenCredential> consumeDefaultTokenCredential();

    protected abstract BiConsumer<T, String> consumeConnectionString();

    protected TokenCredential defaultTokenCredential = new DefaultAzureCredentialBuilder().build();
    private AzureEnvironment azureEnvironment = AzureEnvironment.AZURE;
    private String applicationId; // end-user
    private String springIdentifier;
    private ConnectionStringProvider<?> connectionStringProvider;
    private boolean credentialConfigured = false;

    /**
     * 1. create a builder instance 2. configure builder 2.1 configure azure core level configuration 2.1.1 configure
     * http client getHttpClientInstance 2.2 configure service level configuration 3. customize builder 4. return
     * builder
     *
     * @return the service client builder
     */
    public T build() {
        T builder = createBuilderInstance();
        configureCore(builder);
        configureService(builder);
        customizeBuilder(builder);
        return builder;
    }

    protected void configureCore(T builder) {
        configureApplicationId(builder);
        configureAzureEnvironment(builder);
        configureRetry(builder);
        configureProxy(builder);
        configureCredential(builder);
        configureConnectionString(builder);
        configureDefaultCredential(builder);
    }

    protected void configureAzureEnvironment(T builder) {
        Configuration configuration = new Configuration();
        configuration.put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, this.azureEnvironment.getActiveDirectoryEndpoint());
        consumeConfiguration().accept(builder, configuration);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void configureCredential(T builder) {
        List<AuthenticationDescriptor<?>> descriptors = getAuthenticationDescriptors(builder);
        AzureCredentialProvider<?> azureCredentialProvider = resolveAzureCredential(getAzureProperties(), descriptors);
        if (azureCredentialProvider == null) {
            LOGGER.warn("No authentication credential configured for class {}.", builder.getClass());
            return;
        }

        final Consumer consumer = descriptors.stream()
                                             .filter(d -> d.azureCredentialType() == azureCredentialProvider.getType())
                                             .map(AuthenticationDescriptor::consumer)
                                             .findFirst()
                                             .orElseThrow(
                                                 () -> new IllegalArgumentException("Consumer should not be null"));


        consumer.accept(azureCredentialProvider);
        credentialConfigured = true;
    }

    protected void configureConnectionString(T builder) {
        if (this.connectionStringProvider != null
                && StringUtils.hasText(this.connectionStringProvider.getConnectionString())) {
            consumeConnectionString().accept(builder, this.connectionStringProvider.getConnectionString());
            credentialConfigured = true;
        }
    }

    protected void configureDefaultCredential(T builder) {
        if (!credentialConfigured) {
            LOGGER.info("Will configure the default credential for {}.", builder.getClass());
            consumeDefaultTokenCredential().accept(builder, this.defaultTokenCredential);
        }
    }

    protected List<AzureServiceClientBuilderCustomizer<T>> getBuilderCustomizers() {
        return Collections.singletonList(new NoOpAzureServiceClientBuilderCustomizer<>());
    }

    protected void customizeBuilder(T builder) {
        for (AzureServiceClientBuilderCustomizer<T> customizer : getBuilderCustomizers()) {
            customizer.customize(builder);
        }
    }

    private AzureCredentialProvider<?> resolveAzureCredential(AzureProperties azureProperties,
                                                              List<AuthenticationDescriptor<?>> descriptors) {
        List<AzureCredentialResolver<?>> resolvers = descriptors.stream()
                                                                .map(AuthenticationDescriptor::azureCredentialResolver)
                                                                .collect(Collectors.toList());
        AzureCredentialResolvers credentialResolvers = new AzureCredentialResolvers(resolvers);
        return credentialResolvers.resolve(azureProperties);
    }

    protected AzureEnvironment getAzureEnvironment() {
        return azureEnvironment;
    }

    public void setAzureEnvironment(AzureEnvironment azureEnvironment) {
        this.azureEnvironment = azureEnvironment;
    }

    protected String getApplicationId() {
        final ClientProperties clientProperties = getAzureProperties().getClient();
        return this.applicationId != null ? this.applicationId : (clientProperties != null
                                                                      ? clientProperties.getApplicationId() : null);
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public void setDefaultTokenCredential(TokenCredential defaultTokenCredential) {
        this.defaultTokenCredential = defaultTokenCredential;
    }

    public void setConnectionStringProvider(ConnectionStringProvider<?> connectionStringProvider) {
        this.connectionStringProvider = connectionStringProvider;
    }

    public void setSpringIdentifier(String springIdentifier) {
        this.springIdentifier = springIdentifier;
    }
}
