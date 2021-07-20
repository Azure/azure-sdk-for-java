package com.azure.analytics.purview.scanning;

import com.azure.analytics.purview.scanning.implementation.MicrosoftScanningClientImpl;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A builder for creating a new instance of the MicrosoftScanningClient type. */
@ServiceClientBuilder(
        serviceClients = {
            KeyVaultConnectionsClient.class,
            ClassificationRulesClient.class,
            DataSourcesClient.class,
            FiltersClient.class,
            ScansClient.class,
            ScanResultClient.class,
            ScanRulesetsClient.class,
            SystemScanRulesetsClient.class,
            TriggersClient.class,
            KeyVaultConnectionsAsyncClient.class,
            ClassificationRulesAsyncClient.class,
            DataSourcesAsyncClient.class,
            FiltersAsyncClient.class,
            ScansAsyncClient.class,
            ScanResultAsyncClient.class,
            ScanRulesetsAsyncClient.class,
            SystemScanRulesetsAsyncClient.class,
            TriggersAsyncClient.class
        })
public final class MicrosoftScanningClientBuilder {
    private static final String SDK_NAME = "name";

    private static final String SDK_VERSION = "version";

    private final Map<String, String> properties = new HashMap<>();

    /** Create an instance of the MicrosoftScanningClientBuilder. */
    public MicrosoftScanningClientBuilder() {
        this.pipelinePolicies = new ArrayList<>();
    }

    /*
     * The scanning endpoint of your purview account. Example:
     * https://{accountName}.scan.purview.azure.com
     */
    private String endpoint;

    /**
     * Sets The scanning endpoint of your purview account. Example: https://{accountName}.scan.purview.azure.com.
     *
     * @param endpoint the endpoint value.
     * @return the MicrosoftScanningClientBuilder.
     */
    public MicrosoftScanningClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /*
     * Api Version
     */
    private String apiVersion;

    /**
     * Sets Api Version.
     *
     * @param apiVersion the apiVersion value.
     * @return the MicrosoftScanningClientBuilder.
     */
    public MicrosoftScanningClientBuilder apiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    /*
     * The HTTP pipeline to send requests through
     */
    private HttpPipeline pipeline;

    /**
     * Sets The HTTP pipeline to send requests through.
     *
     * @param pipeline the pipeline value.
     * @return the MicrosoftScanningClientBuilder.
     */
    public MicrosoftScanningClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    /*
     * The serializer to serialize an object into a string
     */
    private SerializerAdapter serializerAdapter;

    /**
     * Sets The serializer to serialize an object into a string.
     *
     * @param serializerAdapter the serializerAdapter value.
     * @return the MicrosoftScanningClientBuilder.
     */
    public MicrosoftScanningClientBuilder serializerAdapter(SerializerAdapter serializerAdapter) {
        this.serializerAdapter = serializerAdapter;
        return this;
    }

    /*
     * The HTTP client used to send the request.
     */
    private HttpClient httpClient;

    /**
     * Sets The HTTP client used to send the request.
     *
     * @param httpClient the httpClient value.
     * @return the MicrosoftScanningClientBuilder.
     */
    public MicrosoftScanningClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /*
     * The configuration store that is used during construction of the service
     * client.
     */
    private Configuration configuration;

    /**
     * Sets The configuration store that is used during construction of the service client.
     *
     * @param configuration the configuration value.
     * @return the MicrosoftScanningClientBuilder.
     */
    public MicrosoftScanningClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /*
     * The logging configuration for HTTP requests and responses.
     */
    private HttpLogOptions httpLogOptions;

    /**
     * Sets The logging configuration for HTTP requests and responses.
     *
     * @param httpLogOptions the httpLogOptions value.
     * @return the MicrosoftScanningClientBuilder.
     */
    public MicrosoftScanningClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = httpLogOptions;
        return this;
    }

    /*
     * The retry policy that will attempt to retry failed requests, if
     * applicable.
     */
    private RetryPolicy retryPolicy;

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     *
     * @param retryPolicy the retryPolicy value.
     * @return the MicrosoftScanningClientBuilder.
     */
    public MicrosoftScanningClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /*
     * The list of Http pipeline policies to add.
     */
    private final List<HttpPipelinePolicy> pipelinePolicies;

    /**
     * Adds a custom Http pipeline policy.
     *
     * @param customPolicy The custom Http pipeline policy to add.
     * @return the MicrosoftScanningClientBuilder.
     */
    public MicrosoftScanningClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        pipelinePolicies.add(customPolicy);
        return this;
    }

    /**
     * Builds an instance of MicrosoftScanningClientImpl with the provided parameters.
     *
     * @return an instance of MicrosoftScanningClientImpl.
     */
    private MicrosoftScanningClientImpl buildInnerClient() {
        if (apiVersion == null) {
            this.apiVersion = "2018-12-01-preview";
        }
        if (pipeline == null) {
            this.pipeline = createHttpPipeline();
        }
        if (serializerAdapter == null) {
            this.serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        }
        MicrosoftScanningClientImpl client =
                new MicrosoftScanningClientImpl(pipeline, serializerAdapter, endpoint, apiVersion);
        return client;
    }

    private HttpPipeline createHttpPipeline() {
        Configuration buildConfiguration =
                (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;
        if (httpLogOptions == null) {
            httpLogOptions = new HttpLogOptions();
        }
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");
        policies.add(
                new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion, buildConfiguration));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(retryPolicy == null ? new RetryPolicy() : retryPolicy);
        policies.add(new CookiePolicy());
        policies.addAll(this.pipelinePolicies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));
        HttpPipeline httpPipeline =
                new HttpPipelineBuilder()
                        .policies(policies.toArray(new HttpPipelinePolicy[0]))
                        .httpClient(httpClient)
                        .build();
        return httpPipeline;
    }

    /**
     * Builds an instance of KeyVaultConnectionsAsyncClient async client.
     *
     * @return an instance of KeyVaultConnectionsAsyncClient.
     */
    public KeyVaultConnectionsAsyncClient buildKeyVaultConnectionsAsyncClient() {
        return new KeyVaultConnectionsAsyncClient(buildInnerClient().getKeyVaultConnections());
    }

    /**
     * Builds an instance of ClassificationRulesAsyncClient async client.
     *
     * @return an instance of ClassificationRulesAsyncClient.
     */
    public ClassificationRulesAsyncClient buildClassificationRulesAsyncClient() {
        return new ClassificationRulesAsyncClient(buildInnerClient().getClassificationRules());
    }

    /**
     * Builds an instance of DataSourcesAsyncClient async client.
     *
     * @return an instance of DataSourcesAsyncClient.
     */
    public DataSourcesAsyncClient buildDataSourcesAsyncClient() {
        return new DataSourcesAsyncClient(buildInnerClient().getDataSources());
    }

    /**
     * Builds an instance of FiltersAsyncClient async client.
     *
     * @return an instance of FiltersAsyncClient.
     */
    public FiltersAsyncClient buildFiltersAsyncClient() {
        return new FiltersAsyncClient(buildInnerClient().getFilters());
    }

    /**
     * Builds an instance of ScansAsyncClient async client.
     *
     * @return an instance of ScansAsyncClient.
     */
    public ScansAsyncClient buildScansAsyncClient() {
        return new ScansAsyncClient(buildInnerClient().getScans());
    }

    /**
     * Builds an instance of ScanResultAsyncClient async client.
     *
     * @return an instance of ScanResultAsyncClient.
     */
    public ScanResultAsyncClient buildScanResultAsyncClient() {
        return new ScanResultAsyncClient(buildInnerClient().getScanResults());
    }

    /**
     * Builds an instance of ScanRulesetsAsyncClient async client.
     *
     * @return an instance of ScanRulesetsAsyncClient.
     */
    public ScanRulesetsAsyncClient buildScanRulesetsAsyncClient() {
        return new ScanRulesetsAsyncClient(buildInnerClient().getScanRulesets());
    }

    /**
     * Builds an instance of SystemScanRulesetsAsyncClient async client.
     *
     * @return an instance of SystemScanRulesetsAsyncClient.
     */
    public SystemScanRulesetsAsyncClient buildSystemScanRulesetsAsyncClient() {
        return new SystemScanRulesetsAsyncClient(buildInnerClient().getSystemScanRulesets());
    }

    /**
     * Builds an instance of TriggersAsyncClient async client.
     *
     * @return an instance of TriggersAsyncClient.
     */
    public TriggersAsyncClient buildTriggersAsyncClient() {
        return new TriggersAsyncClient(buildInnerClient().getTriggers());
    }

    /**
     * Builds an instance of KeyVaultConnectionsClient sync client.
     *
     * @return an instance of KeyVaultConnectionsClient.
     */
    public KeyVaultConnectionsClient buildKeyVaultConnectionsClient() {
        return new KeyVaultConnectionsClient(buildInnerClient().getKeyVaultConnections());
    }

    /**
     * Builds an instance of ClassificationRulesClient sync client.
     *
     * @return an instance of ClassificationRulesClient.
     */
    public ClassificationRulesClient buildClassificationRulesClient() {
        return new ClassificationRulesClient(buildInnerClient().getClassificationRules());
    }

    /**
     * Builds an instance of DataSourcesClient sync client.
     *
     * @return an instance of DataSourcesClient.
     */
    public DataSourcesClient buildDataSourcesClient() {
        return new DataSourcesClient(buildInnerClient().getDataSources());
    }

    /**
     * Builds an instance of FiltersClient sync client.
     *
     * @return an instance of FiltersClient.
     */
    public FiltersClient buildFiltersClient() {
        return new FiltersClient(buildInnerClient().getFilters());
    }

    /**
     * Builds an instance of ScansClient sync client.
     *
     * @return an instance of ScansClient.
     */
    public ScansClient buildScansClient() {
        return new ScansClient(buildInnerClient().getScans());
    }

    /**
     * Builds an instance of ScanResultClient sync client.
     *
     * @return an instance of ScanResultClient.
     */
    public ScanResultClient buildScanResultClient() {
        return new ScanResultClient(buildInnerClient().getScanResults());
    }

    /**
     * Builds an instance of ScanRulesetsClient sync client.
     *
     * @return an instance of ScanRulesetsClient.
     */
    public ScanRulesetsClient buildScanRulesetsClient() {
        return new ScanRulesetsClient(buildInnerClient().getScanRulesets());
    }

    /**
     * Builds an instance of SystemScanRulesetsClient sync client.
     *
     * @return an instance of SystemScanRulesetsClient.
     */
    public SystemScanRulesetsClient buildSystemScanRulesetsClient() {
        return new SystemScanRulesetsClient(buildInnerClient().getSystemScanRulesets());
    }

    /**
     * Builds an instance of TriggersClient sync client.
     *
     * @return an instance of TriggersClient.
     */
    public TriggersClient buildTriggersClient() {
        return new TriggersClient(buildInnerClient().getTriggers());
    }
}
