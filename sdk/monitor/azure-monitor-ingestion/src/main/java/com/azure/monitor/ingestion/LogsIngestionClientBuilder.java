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
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.ingestion.implementation.LogsIngestionClientImpl;
import com.azure.monitor.ingestion.models.LogsIngestionAudience;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.azure.monitor.ingestion.implementation.Utils;

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
 * provides a variety of AAD credential types that can be used. See
 * {@link LogsIngestionClientBuilder#credential(TokenCredential) credential} method for more details.</li>
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
@ServiceClientBuilder(serviceClients = { LogsIngestionClient.class, LogsIngestionAsyncClient.class })
public final class LogsIngestionClientBuilder
    implements ConfigurationTrait<LogsIngestionClientBuilder>, HttpTrait<LogsIngestionClientBuilder>,
    EndpointTrait<LogsIngestionClientBuilder>, TokenCredentialTrait<LogsIngestionClientBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(LogsIngestionClientBuilder.class);

    private static final String SDK_NAME = "name";

    private static final String SDK_VERSION = "version";

    private static final String[] DEFAULT_SCOPES = new String[] { "https://monitor.azure.com/.default" };

    private static final Map<String, String> PROPERTIES = CoreUtils.getProperties("azure-monitor-ingestion.properties");

    private final List<HttpPipelinePolicy> pipelinePolicies;

    /**
     * Creates a new instance of {@link LogsIngestionClientBuilder}.
     */
    public LogsIngestionClientBuilder() {
        this.pipelinePolicies = new ArrayList<>();
    }

    /**
     * The service endpoint.
     */
    private String endpoint;

    /**
     * Sets the <a href="https://learn.microsoft.com/azure/azure-monitor/essentials/data-collection-endpoint-overview?tabs=portal#create-a-data-collection-endpoint">data collection endpoint</a>.
     *
     * @param endpoint the data collection endpoint.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    @Override
    public LogsIngestionClientBuilder endpoint(String endpoint) {
        try {
            new URL(endpoint);
            this.endpoint = endpoint;
            return this;
        } catch (MalformedURLException exception) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("'endpoint' must be a valid URL.", exception));
        }
    }

    /*
     * The HTTP pipeline to send requests through.
     */
    private HttpPipeline pipeline;

    /**
     * {@inheritDoc}
     */
    @Override
    public LogsIngestionClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    /*
     * The HTTP client used to send the request.
     */
    private HttpClient httpClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public LogsIngestionClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /*
     * The configuration store that is used during construction of the service client.
     */
    private Configuration configuration;

    /**
     * {@inheritDoc}
     */
    @Override
    public LogsIngestionClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /*
     * The logging configuration for HTTP requests and responses.
     */
    private HttpLogOptions httpLogOptions;

    /**
     * {@inheritDoc}
     */
    @Override
    public LogsIngestionClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = httpLogOptions;
        return this;
    }

    /*
     * The retry policy that will attempt to retry failed requests, if applicable.
     */
    private RetryPolicy retryPolicy;

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     *
     * @param retryPolicy the retryPolicy value.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    public LogsIngestionClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogsIngestionClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        this.pipelinePolicies.add(customPolicy);
        return this;
    }

    /*
     * The retry options to configure retry policy for failed requests.
     */
    private RetryOptions retryOptions;

    /**
     * {@inheritDoc}
     */
    @Override
    public LogsIngestionClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /*
     * The TokenCredential used for authentication.
     */
    private TokenCredential tokenCredential;

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
        this.tokenCredential = tokenCredential;
        return this;
    }

    /**
     * The audience indicating the authorization scope of log ingestion clients.
     */
    private LogsIngestionAudience audience;

    /**
     * Sets the audience for the authorization scope of log ingestion clients. If this value is not set, the default
     * audience will be the azure public cloud.
     *
     * @param audience the audience value.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    public LogsIngestionClientBuilder audience(LogsIngestionAudience audience) {
        this.audience = audience;
        return this;
    }

    /*
     * The client options such as application ID and custom headers to set on a request.
     */
    private ClientOptions clientOptions;

    /**
     * {@inheritDoc}
     */
    @Override
    public LogsIngestionClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /*
     * Service version
     */
    private LogsIngestionServiceVersion serviceVersion;

    /**
     * The service version to use when creating the client. By default, the latest service version is used.
     * This is the value returned by the {@link LogsIngestionServiceVersion#getLatest() getLatest} method.
     *
     * @param serviceVersion The {@link LogsIngestionServiceVersion}.
     * @return the updated {@link LogsIngestionClientBuilder}.
     */
    public LogsIngestionClientBuilder serviceVersion(LogsIngestionServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * Creates a synchronous client with the configured options in this builder.
     *
     * @return A synchronous {@link LogsIngestionClient}.
     */
    public LogsIngestionClient buildClient() {
        if (endpoint == null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("endpoint is required to build the client."));
        }
        if (tokenCredential == null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("credential is required to build the client."));
        }
        return new LogsIngestionClient(buildInnerClient());
    }

    /**
     * Creates an asynchronous client with the configured options in this builder.
     *
     * @return An asynchronous {@link LogsIngestionAsyncClient}.
     */
    public LogsIngestionAsyncClient buildAsyncClient() {
        if (endpoint == null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("endpoint is required to build the client."));
        }
        if (tokenCredential == null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("credential is required to build the client."));
        }
        return new LogsIngestionAsyncClient(buildInnerClient());
    }

    /**
     * Builds an instance of LogsIngestionClientImpl with the provided parameters.
     *
     * @return an instance of LogsIngestionClientImpl.
     */
    private LogsIngestionClientImpl buildInnerClient() {
        this.validateClient();
        HttpPipeline localPipeline = (pipeline != null) ? pipeline : createHttpPipeline();
        LogsIngestionServiceVersion localServiceVersion
            = (serviceVersion != null) ? serviceVersion : LogsIngestionServiceVersion.getLatest();
        LogsIngestionClientImpl client = Utils.getLogsIngestionClientImpl(localPipeline, endpoint, localServiceVersion);
        return client;
    }

    private void validateClient() {
        // This method is invoked from 'buildInnerClient'/'buildClient' method.
        // Developer can customize this method, to validate that the necessary conditions are met for the new client.
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
    }

    private HttpPipeline createHttpPipeline() {
        Configuration buildConfiguration
            = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;
        HttpLogOptions localHttpLogOptions = this.httpLogOptions == null ? new HttpLogOptions() : this.httpLogOptions;
        ClientOptions localClientOptions = this.clientOptions == null ? new ClientOptions() : this.clientOptions;
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        String clientName = PROPERTIES.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = PROPERTIES.getOrDefault(SDK_VERSION, "UnknownVersion");
        String applicationId = CoreUtils.getApplicationId(localClientOptions, localHttpLogOptions);
        policies.add(new UserAgentPolicy(applicationId, clientName, clientVersion, buildConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddHeadersFromContextPolicy());
        HttpHeaders headers = CoreUtils.createHttpHeadersFromClientOptions(localClientOptions);
        if (headers != null) {
            policies.add(new AddHeadersPolicy(headers));
        }
        this.pipelinePolicies.stream()
            .filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_CALL)
            .forEach(p -> policies.add(p));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions, new RetryPolicy()));
        policies.add(new AddDatePolicy());
        if (tokenCredential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(tokenCredential,
                audience == null ? DEFAULT_SCOPES : new String[] { audience.toString() }));
        }
        this.pipelinePolicies.stream()
            .filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_RETRY)
            .forEach(p -> policies.add(p));
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(localHttpLogOptions));
        HttpPipeline httpPipeline = new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .clientOptions(localClientOptions)
            .build();
        return httpPipeline;
    }

}
