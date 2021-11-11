// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.factory;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.core.aware.AzureProfileAware;
import com.azure.spring.core.aware.ClientAware;
import com.azure.spring.core.aware.authentication.ConnectionStringAware;
import com.azure.spring.core.connectionstring.ConnectionStringProvider;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.provider.AzureCredentialProvider;
import com.azure.spring.core.credential.resolver.AzureCredentialResolver;
import com.azure.spring.core.credential.resolver.AzureCredentialResolvers;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.core.customizer.NoOpAzureServiceClientBuilderCustomizer;
import com.azure.spring.core.properties.AzureProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    private static final TokenCredential DEFAULT_TOKEN_CREDENTIAL = new DefaultAzureCredentialBuilder().build();
    protected abstract T createBuilderInstance();

    protected abstract AzureProperties getAzureProperties();

    protected abstract List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(T builder);

    protected abstract void configureProxy(T builder);

    protected abstract void configureRetry(T builder);

    protected abstract void configureService(T builder);

    protected abstract BiConsumer<T, String> consumeApplicationId();

    protected abstract BiConsumer<T, Configuration> consumeConfiguration();

    protected abstract BiConsumer<T, TokenCredential> consumeDefaultTokenCredential();

    protected abstract BiConsumer<T, String> consumeConnectionString();

    protected TokenCredential defaultTokenCredential = DEFAULT_TOKEN_CREDENTIAL;
    private String applicationId; // end-user
    private String springIdentifier;
    private ConnectionStringProvider<?> connectionStringProvider;
    private boolean credentialConfigured = false;

    /**
     * <ol>
     *  <li>Create a builder instance.</li>
     *  <li>Configure Azure core level configuration.</li>
     *  <li>Configure service level configuration.</li>
     *  <li>Customize builder.</li>
     * </ol>
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

    /**
     * The application id provided to sdk should be a concatenation of customer-application-id and
     * azure-spring-identifier.
     *
     * @param builder the service client builder
     */
    protected void configureApplicationId(T builder) {
        String applicationId = getApplicationId() + this.springIdentifier;
        consumeApplicationId().accept(builder, applicationId);
    }

    protected void configureAzureEnvironment(T builder) {
        AzureProfileAware.Profile profile = getAzureProperties().getProfile();

        Configuration configuration = new Configuration();
        configuration.put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, profile.getEnvironment().getActiveDirectoryEndpoint());

        consumeConfiguration().accept(builder, configuration);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void configureCredential(T builder) {
        List<AuthenticationDescriptor<?>> descriptors = getAuthenticationDescriptors(builder);
        AzureCredentialProvider<?> azureCredentialProvider = resolveAzureCredential(getAzureProperties(), descriptors);
        if (azureCredentialProvider == null) {
            LOGGER.debug("No authentication credential configured for class {}.", builder.getClass().getSimpleName());
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
            LOGGER.debug("Connection string configured for class {}.", builder.getClass().getSimpleName());
        } else {
            AzureProperties azureProperties = getAzureProperties();
            if (azureProperties instanceof ConnectionStringAware) {
                String connectionString = ((ConnectionStringAware) azureProperties).getConnectionString();
                if (StringUtils.hasText(connectionString)) {
                    consumeConnectionString().accept(builder, connectionString);
                    credentialConfigured = true;
                    LOGGER.debug("Connection string configured for class {}.", builder.getClass().getSimpleName());
                }
            }
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

    private String getApplicationId() {
        final ClientAware.Client client = getAzureProperties().getClient();
        return Optional.ofNullable(client)
                       .map(ClientAware.Client::getApplicationId)
                       .orElse("");
    }

    public void setSpringIdentifier(String springIdentifier) {
        if (!StringUtils.hasText(springIdentifier)) {
            LOGGER.warn("SpringIdentifier is null or empty.");
            return;
        }
        this.springIdentifier = springIdentifier;
    }

    public void setDefaultTokenCredential(TokenCredential defaultTokenCredential) {
        this.defaultTokenCredential = defaultTokenCredential;
    }

    public void setConnectionStringProvider(ConnectionStringProvider<?> connectionStringProvider) {
        this.connectionStringProvider = connectionStringProvider;
    }
}
