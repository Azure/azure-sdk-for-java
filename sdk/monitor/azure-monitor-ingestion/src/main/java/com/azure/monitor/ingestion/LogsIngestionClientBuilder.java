// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.ingestion.implementation.IngestionUsingDataCollectionRulesClientBuilder;
import com.azure.monitor.ingestion.implementation.IngestionUsingDataCollectionRulesServiceVersion;
import com.azure.monitor.ingestion.models.LogsIngestionAudience;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Fluent builder for creating instances of {@link LogsIngestionClient} and {@link LogsIngestionAsyncClient}. The
 * builder provides various options to customize the client as per your requirements.
 *
 * <p>There are two required properties that should be set to build a client:
 * <ol>
 * <li>{@code endpoint} - The <a href="https://learn.microsoft.com/azure/azure-monitor/essentials/data-collection-endpoint-overview?tabs=portal#create-a-data-collection-endpoint">data collection endpoint</a>.
 * See {@link LogsIngestionClientBuilder#endpoint(String) endpoint} method for more details.</li>
 * <li>{@code credential} - The AAD authentication credential that has the "Monitoring Metrics Publisher" role assigned to it.
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">Azure Identity</a>
 * provides a variety of AAD credential types that can be used. See {@link LogsIngestionClientBuilder#credential(TokenCredential) credential } method for more details.</li>
 * </ol>
 *
 * <p><strong>Instantiating an asynchronous Logs ingestion client</strong></p>
 * <!-- src_embed com.azure.monitor.ingestion.LogsIngestionAsyncClient.instantiation -->
 * <pre>
 * LogsIngestionAsyncClient logsIngestionAsyncClient = new LogsIngestionClientBuilder&#40;&#41;
 *         .credential&#40;tokenCredential&#41;
 *         .endpoint&#40;&quot;&lt;data-collection-endpoint&gt;&quot;&#41;
 *         .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.monitor.ingestion.LogsIngestionAsyncClient.instantiation -->
 *
 * <p><strong>Instantiating a synchronous Logs ingestion client</strong></p>
 * <!-- src_embed com.azure.monitor.ingestion.LogsIngestionClient.instantiation -->
 * <pre>
 * LogsIngestionClient logsIngestionClient = new LogsIngestionClientBuilder&#40;&#41;
 *         .credential&#40;tokenCredential&#41;
 *         .endpoint&#40;&quot;&lt;data-collection-endpoint&gt;&quot;&#41;
 *         .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.monitor.ingestion.LogsIngestionClient.instantiation -->
 */
@ServiceClientBuilder(serviceClients = {LogsIngestionClient.class, LogsIngestionAsyncClient.class})
public final class LogsIngestionClientBuilder implements ConfigurationTrait<LogsIngestionClientBuilder>,
        HttpTrait<LogsIngestionClientBuilder>, EndpointTrait<LogsIngestionClientBuilder>, TokenCredentialTrait<LogsIngestionClientBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(LogsIngestionClientBuilder.class);
    private final IngestionUsingDataCollectionRulesClientBuilder innerLogBuilder =
            new IngestionUsingDataCollectionRulesClientBuilder();
    private String endpoint;
    private TokenCredential tokenCredential;


    /**
     * Creates a new instance of {@link LogsIngestionClientBuilder}.
     */
    public LogsIngestionClientBuilder() {

    }


    /**
     * Sets the <a href="https://learn.microsoft.com/azure/azure-monitor/essentials/data-collection-endpoint-overview?tabs=portal#create-a-data-collection-endpoint">data collection endpoint</a>.
     * @param endpoint the data collection endpoint.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    @Override
    public LogsIngestionClientBuilder endpoint(String endpoint) {
        try {
            new URL(endpoint);
            innerLogBuilder.endpoint(endpoint);
            this.endpoint = endpoint;
            return this;
        } catch (MalformedURLException exception) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'endpoint' must be a valid URL.", exception));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogsIngestionClientBuilder pipeline(HttpPipeline pipeline) {
        innerLogBuilder.pipeline(pipeline);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogsIngestionClientBuilder httpClient(HttpClient httpClient) {
        innerLogBuilder.httpClient(httpClient);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogsIngestionClientBuilder configuration(Configuration configuration) {
        innerLogBuilder.configuration(configuration);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogsIngestionClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        innerLogBuilder.httpLogOptions(httpLogOptions);
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     * @param retryPolicy the retryPolicy value.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    public LogsIngestionClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        innerLogBuilder.retryPolicy(retryPolicy);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogsIngestionClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        innerLogBuilder.addPolicy(customPolicy);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogsIngestionClientBuilder retryOptions(RetryOptions retryOptions) {
        innerLogBuilder.retryOptions(retryOptions);
        return this;
    }

    /**
     * Sets the AAD authentication credential that has the "Monitoring Metrics Publisher" role assigned to it.
     * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">Azure Identity</a>
     * provides a variety of AAD credential types that can be used.
     *
     * @param tokenCredential the tokenCredential value.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    @Override
    public LogsIngestionClientBuilder credential(TokenCredential tokenCredential) {
        innerLogBuilder.credential(tokenCredential);
        this.tokenCredential = tokenCredential;
        return this;
    }


    /**
     * Sets the audience for the authorization scope of log ingestion clients. If this value is not set, the default
     * audience will be the azure public cloud.
     *
     * @param audience the audience value.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    public LogsIngestionClientBuilder audience(LogsIngestionAudience audience) {
        innerLogBuilder.audience(audience);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogsIngestionClientBuilder clientOptions(ClientOptions clientOptions) {
        innerLogBuilder.clientOptions(clientOptions);
        return this;
    }

    /**
     * The service version to use when creating the client. By default, the latest service version is used.
     * This is the value returned by the {@link LogsIngestionServiceVersion#getLatest() getLatest} method.
     *
     * @param serviceVersion The {@link LogsIngestionServiceVersion}.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    public LogsIngestionClientBuilder serviceVersion(LogsIngestionServiceVersion serviceVersion) {
        innerLogBuilder.serviceVersion(IngestionUsingDataCollectionRulesServiceVersion.valueOf(serviceVersion.getVersion()));
        return this;
    }

    /**
     * Creates a synchronous client with the configured options in this builder.
     * @return A synchronous {@link LogsIngestionClient}.
     */
    public LogsIngestionClient buildClient() {
        if (endpoint == null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("endpoint is required to build the client."));
        }
        if (tokenCredential == null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("credential is required to build the client."));
        }
        return new LogsIngestionClient(innerLogBuilder.buildClient());
    }

    /**
     * Creates an asynchronous client with the configured options in this builder.
     * @return An asynchronous {@link LogsIngestionAsyncClient}.
     */
    public LogsIngestionAsyncClient buildAsyncClient() {
        if (endpoint == null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("endpoint is required to build the client."));
        }
        if (tokenCredential == null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("credential is required to build the client."));
        }
        return new LogsIngestionAsyncClient(innerLogBuilder.buildAsyncClient());
    }

}
