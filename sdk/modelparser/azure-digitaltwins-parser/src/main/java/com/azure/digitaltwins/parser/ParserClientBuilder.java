// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;

import java.util.Map;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link ParserClient}
 * and {@link ParserAsyncClient}, call {@link #buildClient() buildClient} and {@link
 * #buildAsyncClient() buildAsyncClient} respectively to construct an instance of the desired client.
 */
@ServiceClientBuilder(serviceClients = {ParserClient.class, ParserAsyncClient.class})
public final class ParserClientBuilder {
    // This is the name of the properties file in this repo that contains the default properties
    private static final String DIGITALTWINS_MODEL_PARSER_PROPERTIES = "azure-digitaltwins-parser.properties";

    // optional/have default values
    private ParserServiceVersion serviceVersion;
    private ClientOptions clientOptions;

    private final Map<String, String> properties;

    private Configuration configuration;

    /**
     * The public constructor for ModelsRepositoryClientBuilder
     */
    public ParserClientBuilder() {
        properties = CoreUtils.getProperties(DIGITALTWINS_MODEL_PARSER_PROPERTIES);
    }

    /**
     * Create a {@link ParserClient} based on the builder settings.
     *
     * @return the created synchronous ModelsRepotioryClient
     */
    public ParserClient buildClient() {
        return new ParserClient(buildAsyncClient());
    }

    /**
     * Create a {@link ParserAsyncClient} based on the builder settings.
     *
     * @return the created asynchronous ModelsRepositoryAsyncClient
     */
    public ParserAsyncClient buildAsyncClient() {
        Configuration buildConfiguration = this.configuration;
        if (buildConfiguration == null) {
            buildConfiguration = Configuration.getGlobalConfiguration().clone();
        }

        // Set defaults for these fields if they were not set while building the client
        ParserServiceVersion serviceVersion = this.serviceVersion;
        if (serviceVersion == null) {
            serviceVersion = ParserServiceVersion.getLatest();
        }

        return new ParserAsyncClient(serviceVersion);
    }

    /**
     * Sets the {@link ParserServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version of the client library will have the result of potentially moving to a newer service version.
     * <p>
     * Targeting a specific service version may also mean that the service will return an error for newer APIs.
     *
     * @param serviceVersion The service API version to use.
     * @return the updated ModelsRepositoryClientBuilder instance for fluent building.
     */
    public ParserClientBuilder serviceVersion(ParserServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     * <p>
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated ModelsRepositoryClientBuilder object for fluent building.
     */
    public ParserClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link ClientOptions} which enables various options to be set on the client. For example setting an
     * {@code applicationId} using {@link ClientOptions#setApplicationId(String)} to configure
     * the {@link UserAgentPolicy} for telemetry/monitoring purposes.
     *
     * <p>More About <a href="https://azure.github.io/azure-sdk/general_azurecore.html#telemetry-policy">Azure Core: Telemetry policy</a>
     *
     * @param clientOptions the {@link ClientOptions} to be set on the client.
     * @return The updated KeyClientBuilder object.
     */
    public ParserClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }
}
