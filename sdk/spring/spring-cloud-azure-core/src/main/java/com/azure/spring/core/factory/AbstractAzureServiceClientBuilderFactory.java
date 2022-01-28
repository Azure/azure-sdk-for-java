// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.factory;

import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.core.aware.ClientAware;
import com.azure.spring.core.aware.authentication.ConnectionStringAware;
import com.azure.spring.core.connectionstring.ConnectionStringProvider;
import com.azure.spring.core.credential.AzureCredentialResolver;
import com.azure.spring.core.credential.AzureCredentialResolvers;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.provider.AzureCredentialProvider;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.core.properties.AzureProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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

    /**
     * Create an instance of Azure sdk client builder.
     * @return The service client builder.
     */
    protected abstract T createBuilderInstance();

    /**
     * Get the {@link AzureProperties} object. The {@link AzureProperties} will tell the factory how to configure the
     * builder.
     * @return The Azure properties object.
     */
    protected abstract AzureProperties getAzureProperties();

    /**
     * Get a list of {@link AuthenticationDescriptor}, each represents an authentication method the Azure sdk client
     * supports.
     * @param builder The service client builder.
     * @return A list of {@link AuthenticationDescriptor}.
     */
    protected abstract List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(T builder);

    /**
     * Configure proxy to the builder.
     * @param builder The service client builder
     */
    protected abstract void configureProxy(T builder);

    /**
     * Configure retry to the builder.
     * @param builder The service client builder
     */
    protected abstract void configureRetry(T builder);

    /**
     * Configure service specific properties to the builder.
     * @param builder The service client builder
     */
    protected abstract void configureService(T builder);

    /**
     * Return a {@link BiConsumer} of how the {@link T} builder consume the application id.
     * @return The consumer of how the {@link T} builder consume the application id.
     */
    protected abstract BiConsumer<T, String> consumeApplicationId();

    /**
     * Return a {@link BiConsumer} of how the {@link T} builder consume a {@link Configuration}.
     * @return The consumer of how the {@link T} builder consume a {@link Configuration}.
     */
    protected abstract BiConsumer<T, Configuration> consumeConfiguration();

    /**
     * Return a {@link BiConsumer} of how the {@link T} builder consume a default {@link TokenCredential}.
     * @return The consumer of how the {@link T} builder consume a default {@link TokenCredential}.
     */
    protected abstract BiConsumer<T, TokenCredential> consumeDefaultTokenCredential();

    /**
     * Return a {@link BiConsumer} of how the {@link T} builder consume a connection string.
     * @return The consumer of how the {@link T} builder consume a connection string.
     */
    protected abstract BiConsumer<T, String> consumeConnectionString();

    private String springIdentifier;
    private ConnectionStringProvider<?> connectionStringProvider;
    private boolean credentialConfigured = false;
    private final List<AzureServiceClientBuilderCustomizer<T>> customizers = new ArrayList<>();
    protected final Configuration configuration = new Configuration();
    protected TokenCredential defaultTokenCredential = DEFAULT_TOKEN_CREDENTIAL;

    /**
     * Build the service client builder. The build consists of following steps:
     * <ol>
     *  <li>Create a builder instance.</li>
     *  <li>Configure Azure core level configuration.</li>
     *  <li>Configure service level configuration.</li>
     *  <li>Customize builder.</li>
     * </ol>
     *
     * @return the service client builder.
     */
    public T build() {
        T builder = createBuilderInstance();
        configureCore(builder);
        configureService(builder);
        customizeBuilder(builder);
        return builder;
    }

    /**
     * Configure Azure core level configurations. The core configuration consists of following steps:
     * <ol>
     *   <li>Configure Application Id.</li>
     *   <li>Configure Azure environment.</li>
     *   <li>Configure {@link Configuration}.</li>
     *   <li>Configure retry.</li>
     *   <li>Configure proxy.</li>
     *   <li>Configure credential.</li>
     *   <li>Configure connection string.</li>
     *   <li>Configure default credential.</li>
     * </ol>
     * @param builder The service client builder.
     */
    protected void configureCore(T builder) {
        configureApplicationId(builder);
        configureAzureEnvironment(builder);
        configureConfiguration(builder);
        configureRetry(builder);
        configureProxy(builder);
        configureCredential(builder);
        configureConnectionString(builder);
        configureDefaultCredential(builder);
    }

    /**
     * Configure application id to the builder.The application id provided to sdk should be a concatenation of
     * customer-application-id and azure-spring-identifier.
     *
     * @param builder The service client builder.
     */
    protected void configureApplicationId(T builder) {
        String applicationId = getApplicationId() + (this.springIdentifier == null ? "" : this.springIdentifier);
        consumeApplicationId().accept(builder, applicationId);
    }

    /**
     * Configure Azure environment, such as Azure Global or Azure China, to the builder.
     * @param builder The service client builder.
     */
    protected void configureAzureEnvironment(T builder) {
        configuration.put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST,
            getAzureProperties().getProfile().getEnvironment().getActiveDirectoryEndpoint());
    }

    /**
     * Configure {@link Configuration} to the builder. The {@link Configuration} is a container for predefined Azure sdk
     * environment variables.
     *
     * @param builder The service client builder.
     */
    protected void configureConfiguration(T builder) {
        consumeConfiguration().accept(builder, configuration);
    }

    /**
     * Configure credential to the builder. It will try to resolve the credential first. The authentication types a
     * service client supports is defined in {@link #getAuthenticationDescriptors(Object)}. If a credential is resolved
     * successfully, the {@link #credentialConfigured} flag will be set to {@code true}.
     *
     * @param builder The service client builder.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void configureCredential(T builder) {
        List<AuthenticationDescriptor<?>> descriptors = getAuthenticationDescriptors(builder);
        AzureCredentialProvider<?> azureCredentialProvider = resolveAzureCredential(getAzureProperties(), descriptors);
        if (azureCredentialProvider == null) {
            LOGGER.debug("No authentication credential configured for class {}.", builder.getClass().getSimpleName());
            return;
        }

        final Consumer consumer = descriptors.stream()
                                             .filter(d -> d.getAzureCredentialType() == azureCredentialProvider.getType())
                                             .map(AuthenticationDescriptor::getConsumer)
                                             .findFirst()
                                             .orElseThrow(
                                                 () -> new IllegalArgumentException("Consumer should not be null"));


        consumer.accept(azureCredentialProvider);
        credentialConfigured = true;
    }

    /**
     * Configure the connection string to the builder. It will try to resolve a connection string from the
     * {@link AzureProperties}, if it is a {@link ConnectionStringAware} instance. If no connection string found from
     * the {@link AzureProperties}, it will check if any {@link ConnectionStringProvider} is provided and get the
     * connection string from the provider if set. If a connection string is resolved successfully, the
     * {@link #credentialConfigured} flag will be set to {@code true}.
     *
     * @param builder The service client builder.
     */
    protected void configureConnectionString(T builder) {
        AzureProperties azureProperties = getAzureProperties();

        // connection string set to properties will advantage the one from connection string provider
        if (azureProperties instanceof ConnectionStringAware) {
            String connectionString = ((ConnectionStringAware) azureProperties).getConnectionString();

            if (StringUtils.hasText(connectionString)) {
                consumeConnectionString().accept(builder, connectionString);
                credentialConfigured = true;
                LOGGER.debug("Connection string configured for class {}.", builder.getClass().getSimpleName());
                return;
            }
        }

        if (this.connectionStringProvider != null
                && StringUtils.hasText(this.connectionStringProvider.getConnectionString())) {
            consumeConnectionString().accept(builder, this.connectionStringProvider.getConnectionString());
            credentialConfigured = true;
            LOGGER.debug("Connection string configured for class {}.", builder.getClass().getSimpleName());
        }
    }

    /**
     * Configure the default token credential to the builder. The default credential will be set if and only if the
     * {@link #credentialConfigured} is false.
     *
     * @param builder The service client builder.
     */
    protected void configureDefaultCredential(T builder) {
        if (!credentialConfigured && builder instanceof TokenCredentialTrait) {
            LOGGER.info("Will configure the default credential for {}.", builder.getClass());
            // TODO kasobol-msft how do we keep track if default credential has been set?
            // Should builder or configuration do it implicitly if nothing was configured?
            // Perhaps there could be some default/global flag driving such behavior - enabled by default for Spring.
            ((TokenCredentialTrait<?>) builder).credential(this.defaultTokenCredential);
        }
    }

    /**
     * Add a {@link AzureServiceClientBuilderCustomizer} to the factory. The factory provides a template of how to
     * configure the service client builder, but the caller of the factory call customize the configuration by adding
     * one or many {@link AzureServiceClientBuilderCustomizer}. The customizers will be called after all default
     * configuration defined by the factory applied to the service client builder.
     *
     * @param customizer An implementation of {@link AzureServiceClientBuilderCustomizer}.
     */
    public void addBuilderCustomizer(AzureServiceClientBuilderCustomizer<T> customizer) {
        this.customizers.add(customizer);
    }

    /**
     * Get the list of builder customizers.
     * @return The list of builder customizers.
     */
    protected List<AzureServiceClientBuilderCustomizer<T>> getBuilderCustomizers() {
        return this.customizers;
    }

    /**
     * Call the list of {@link AzureServiceClientBuilderCustomizer} one by one to apply the customization.
     * @param builder The service client builder.
     */
    protected void customizeBuilder(T builder) {
        for (AzureServiceClientBuilderCustomizer<T> customizer : getBuilderCustomizers()) {
            customizer.customize(builder);
        }
    }

    private AzureCredentialProvider<?> resolveAzureCredential(AzureProperties azureProperties,
                                                              List<AuthenticationDescriptor<?>> descriptors) {
        List<AzureCredentialResolver<?>> resolvers = descriptors.stream()
                                                                .map(AuthenticationDescriptor::getAzureCredentialResolver)
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

    /**
     * Set the Spring Cloud Azure library identifier to the factory. The identifier will be set to the application id,
     * and the application id will be sent with requests made by the service client.
     * @param springIdentifier The Spring Cloud Azure library identifier.
     * @see com.azure.spring.core.AzureSpringIdentifier
     */
    public void setSpringIdentifier(String springIdentifier) {
        if (!StringUtils.hasText(springIdentifier)) {
            LOGGER.warn("SpringIdentifier is null or empty.");
            return;
        }
        this.springIdentifier = springIdentifier;
    }

    /**
     * Set the default token credential.
     * @param defaultTokenCredential The default token credential.
     */
    public void setDefaultTokenCredential(TokenCredential defaultTokenCredential) {
        this.defaultTokenCredential = defaultTokenCredential;
    }

    /**
     * Set the connection string provider.
     * @param connectionStringProvider The connection string provider.
     */
    public void setConnectionStringProvider(ConnectionStringProvider<?> connectionStringProvider) {
        this.connectionStringProvider = connectionStringProvider;
    }
}
