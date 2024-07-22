// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.administration;

import com.azure.ai.personalizer.models.PersonalizerAudience;
import com.azure.ai.personalizer.PersonalizerServiceVersion;
import com.azure.ai.personalizer.implementation.PersonalizerClientV1Preview3Impl;
import com.azure.ai.personalizer.implementation.PersonalizerClientV1Preview3ImplBuilder;
import com.azure.ai.personalizer.implementation.util.Utility;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.AzureKeyCredentialTrait;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * A builder for creating a new instance of PersonalizerAdministrationAsyncClient and PersonalizerAdministrationClient.
 */
@ServiceClientBuilder(serviceClients = {PersonalizerAdministrationAsyncClient.class, PersonalizerAdministrationClient.class})
public final class PersonalizerAdministrationClientBuilder implements
    AzureKeyCredentialTrait<PersonalizerAdministrationClientBuilder>,
    ConfigurationTrait<PersonalizerAdministrationClientBuilder>,
    EndpointTrait<PersonalizerAdministrationClientBuilder>,
    HttpTrait<PersonalizerAdministrationClientBuilder>,
    TokenCredentialTrait<PersonalizerAdministrationClientBuilder> {
    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();
    private final ClientLogger logger = new ClientLogger(PersonalizerAdministrationClientBuilder.class);
    private ClientOptions clientOptions;
    private String endpoint;
    private AzureKeyCredential azureKeyCredential;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline httpPipeline;
    private Configuration configuration;
    private RetryPolicy retryPolicy;
    private RetryOptions retryOptions;
    private TokenCredential tokenCredential;
    private PersonalizerServiceVersion version;
    private PersonalizerAudience audience;

    PersonalizerClientV1Preview3Impl getService() {
        // Endpoint cannot be null, which is required in request authentication
        Objects.requireNonNull(endpoint, "'Endpoint' is required and can not be null.");
        if (audience == null) {
            audience = PersonalizerAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD;
        }
        // Global Env configuration store
        final Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration().clone() : configuration;

        // Service Version
        final PersonalizerServiceVersion serviceVersion =
            version != null ? version : PersonalizerServiceVersion.getLatest();

        HttpPipeline pipeline = httpPipeline;
        // Create a default Pipeline if it is not given
        if (pipeline == null) {
            pipeline = Utility.buildHttpPipeline(
                clientOptions,
                httpLogOptions,
                buildConfiguration,
                retryPolicy,
                retryOptions,
                azureKeyCredential,
                tokenCredential,
                audience,
                perCallPolicies,
                perRetryPolicies,
                httpClient);
        }

        return new PersonalizerClientV1Preview3ImplBuilder()
            .endpoint(endpoint)
            // Fix this, currently hardcoded to v1.1-preview.3
            // .apiVersion(serviceVersion.getVersion())
            .pipeline(pipeline)
            .buildClient();
    }

    @Override
    public PersonalizerAdministrationClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");

        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL.", ex));
        }

        if (endpoint.endsWith("/")) {
            this.endpoint = endpoint.substring(0, endpoint.length() - 1);
        } else {
            this.endpoint = endpoint;
        }

        return this;
    }

    @Override
    public PersonalizerAdministrationClientBuilder credential(AzureKeyCredential azureKeyCredential) {
        this.azureKeyCredential = Objects.requireNonNull(azureKeyCredential, "'azureKeyCredential' cannot be null.");
        return this;
    }

    @Override
    public PersonalizerAdministrationClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        return this;
    }

    @Override
    public PersonalizerAdministrationClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = logOptions;
        return this;
    }

    @Override
    public PersonalizerAdministrationClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    @Override
    public PersonalizerAdministrationClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy, "'policy' cannot be null.");

        if (policy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(policy);
        } else {
            perRetryPolicies.add(policy);
        }
        return this;
    }

    @Override
    public PersonalizerAdministrationClientBuilder httpClient(HttpClient client) {
        if (this.httpClient != null && client == null) {
            logger.info("HttpClient is being set to 'null' when it was previously configured.");
        }

        this.httpClient = client;
        return this;
    }

    @Override
    public PersonalizerAdministrationClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    @Override
    public PersonalizerAdministrationClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Set the retry policy to be used by the clients that are returned by this builder.
     *
     * @param retryPolicy The retry policy to be used when making network calls.
     * @return the PersonalizerAdministrationClientBuilder object itself.
     */
    public PersonalizerAdministrationClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    @Override
    public PersonalizerAdministrationClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Set the service version to be used by the clients that are returned by this builder.
     *
     * @param version The service version to be used when calling Personalizer service.
     * @return the PersonalizerAdministrationClientBuilder object itself.
     */
    public PersonalizerAdministrationClientBuilder serviceVersion(PersonalizerServiceVersion version) {
        this.version = version;
        return this;
    }

    /**
     * Set the azure cloud to be used by the clients that are returned by this builder.
     *
     * @param audience The azure cloud that the Personalizer instance belongs to.
     * @return the PersonalizerAdministrationClientBuilder object itself.
     */
    public PersonalizerAdministrationClientBuilder audience(PersonalizerAudience audience) {
        Objects.requireNonNull(audience, "'audience' is required and can not be null");
        this.audience = audience;
        return this;
    }

    /**
     * Create a {@link PersonalizerAdministrationClient} object to invoke the Personalizer service.
     *
     * @return the created object.
     */
    public PersonalizerAdministrationClient buildClient() {
        return new PersonalizerAdministrationClient(buildAsyncClient());
    }

    /**
     * Create a {@link PersonalizerAdministrationAsyncClient} object to invoke the Personalizer service in an asynchronous manner.
     *
     * @return the created object.
     */
    public PersonalizerAdministrationAsyncClient buildAsyncClient() {
        return new PersonalizerAdministrationAsyncClient(getService());
    }
}
