package com.azure.analytics.purview.catalog;

import com.azure.analytics.purview.catalog.implementation.PurviewCatalogServiceRestAPIDocumentImpl;
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

/** A builder for creating a new instance of the PurviewCatalogServiceRestAPIDocument type. */
@ServiceClientBuilder(
        serviceClients = {
            EntityClient.class,
            GlossaryClient.class,
            DiscoveryClient.class,
            LineageClient.class,
            RelationshipClient.class,
            TypesClient.class,
            EntityAsyncClient.class,
            GlossaryAsyncClient.class,
            DiscoveryAsyncClient.class,
            LineageAsyncClient.class,
            RelationshipAsyncClient.class,
            TypesAsyncClient.class
        })
public final class PurviewCatalogServiceRestAPIDocumentBuilder {
    private static final String SDK_NAME = "name";

    private static final String SDK_VERSION = "version";

    private final Map<String, String> properties = new HashMap<>();

    /** Create an instance of the PurviewCatalogServiceRestAPIDocumentBuilder. */
    public PurviewCatalogServiceRestAPIDocumentBuilder() {
        this.pipelinePolicies = new ArrayList<>();
    }

    /*
     * The catalog endpoint of your Purview account. Example:
     * https://{accountName}.catalog.purview.azure.com
     */
    private String endpoint;

    /**
     * Sets The catalog endpoint of your Purview account. Example: https://{accountName}.catalog.purview.azure.com.
     *
     * @param endpoint the endpoint value.
     * @return the PurviewCatalogServiceRestAPIDocumentBuilder.
     */
    public PurviewCatalogServiceRestAPIDocumentBuilder endpoint(String endpoint) {
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
     * @return the PurviewCatalogServiceRestAPIDocumentBuilder.
     */
    public PurviewCatalogServiceRestAPIDocumentBuilder apiVersion(String apiVersion) {
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
     * @return the PurviewCatalogServiceRestAPIDocumentBuilder.
     */
    public PurviewCatalogServiceRestAPIDocumentBuilder pipeline(HttpPipeline pipeline) {
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
     * @return the PurviewCatalogServiceRestAPIDocumentBuilder.
     */
    public PurviewCatalogServiceRestAPIDocumentBuilder serializerAdapter(SerializerAdapter serializerAdapter) {
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
     * @return the PurviewCatalogServiceRestAPIDocumentBuilder.
     */
    public PurviewCatalogServiceRestAPIDocumentBuilder httpClient(HttpClient httpClient) {
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
     * @return the PurviewCatalogServiceRestAPIDocumentBuilder.
     */
    public PurviewCatalogServiceRestAPIDocumentBuilder configuration(Configuration configuration) {
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
     * @return the PurviewCatalogServiceRestAPIDocumentBuilder.
     */
    public PurviewCatalogServiceRestAPIDocumentBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
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
     * @return the PurviewCatalogServiceRestAPIDocumentBuilder.
     */
    public PurviewCatalogServiceRestAPIDocumentBuilder retryPolicy(RetryPolicy retryPolicy) {
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
     * @return the PurviewCatalogServiceRestAPIDocumentBuilder.
     */
    public PurviewCatalogServiceRestAPIDocumentBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        pipelinePolicies.add(customPolicy);
        return this;
    }

    /**
     * Builds an instance of PurviewCatalogServiceRestAPIDocumentImpl with the provided parameters.
     *
     * @return an instance of PurviewCatalogServiceRestAPIDocumentImpl.
     */
    private PurviewCatalogServiceRestAPIDocumentImpl buildInnerClient() {
        if (apiVersion == null) {
            this.apiVersion = "2021-05-01-preview";
        }
        if (pipeline == null) {
            this.pipeline = createHttpPipeline();
        }
        if (serializerAdapter == null) {
            this.serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        }
        PurviewCatalogServiceRestAPIDocumentImpl client =
                new PurviewCatalogServiceRestAPIDocumentImpl(pipeline, serializerAdapter, endpoint, apiVersion);
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
     * Builds an instance of EntityAsyncClient async client.
     *
     * @return an instance of EntityAsyncClient.
     */
    public EntityAsyncClient buildEntityAsyncClient() {
        return new EntityAsyncClient(buildInnerClient().getEntities());
    }

    /**
     * Builds an instance of GlossaryAsyncClient async client.
     *
     * @return an instance of GlossaryAsyncClient.
     */
    public GlossaryAsyncClient buildGlossaryAsyncClient() {
        return new GlossaryAsyncClient(buildInnerClient().getGlossaries());
    }

    /**
     * Builds an instance of DiscoveryAsyncClient async client.
     *
     * @return an instance of DiscoveryAsyncClient.
     */
    public DiscoveryAsyncClient buildDiscoveryAsyncClient() {
        return new DiscoveryAsyncClient(buildInnerClient().getDiscoveries());
    }

    /**
     * Builds an instance of LineageAsyncClient async client.
     *
     * @return an instance of LineageAsyncClient.
     */
    public LineageAsyncClient buildLineageAsyncClient() {
        return new LineageAsyncClient(buildInnerClient().getLineages());
    }

    /**
     * Builds an instance of RelationshipAsyncClient async client.
     *
     * @return an instance of RelationshipAsyncClient.
     */
    public RelationshipAsyncClient buildRelationshipAsyncClient() {
        return new RelationshipAsyncClient(buildInnerClient().getRelationships());
    }

    /**
     * Builds an instance of TypesAsyncClient async client.
     *
     * @return an instance of TypesAsyncClient.
     */
    public TypesAsyncClient buildTypesAsyncClient() {
        return new TypesAsyncClient(buildInnerClient().getTypes());
    }

    /**
     * Builds an instance of EntityClient sync client.
     *
     * @return an instance of EntityClient.
     */
    public EntityClient buildEntityClient() {
        return new EntityClient(buildInnerClient().getEntities());
    }

    /**
     * Builds an instance of GlossaryClient sync client.
     *
     * @return an instance of GlossaryClient.
     */
    public GlossaryClient buildGlossaryClient() {
        return new GlossaryClient(buildInnerClient().getGlossaries());
    }

    /**
     * Builds an instance of DiscoveryClient sync client.
     *
     * @return an instance of DiscoveryClient.
     */
    public DiscoveryClient buildDiscoveryClient() {
        return new DiscoveryClient(buildInnerClient().getDiscoveries());
    }

    /**
     * Builds an instance of LineageClient sync client.
     *
     * @return an instance of LineageClient.
     */
    public LineageClient buildLineageClient() {
        return new LineageClient(buildInnerClient().getLineages());
    }

    /**
     * Builds an instance of RelationshipClient sync client.
     *
     * @return an instance of RelationshipClient.
     */
    public RelationshipClient buildRelationshipClient() {
        return new RelationshipClient(buildInnerClient().getRelationships());
    }

    /**
     * Builds an instance of TypesClient sync client.
     *
     * @return an instance of TypesClient.
     */
    public TypesClient buildTypesClient() {
        return new TypesClient(buildInnerClient().getTypes());
    }
}
