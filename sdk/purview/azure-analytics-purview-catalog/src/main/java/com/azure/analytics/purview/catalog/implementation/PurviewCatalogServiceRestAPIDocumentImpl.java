package com.azure.analytics.purview.catalog.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;

/** Initializes a new instance of the PurviewCatalogServiceRestAPIDocument type. */
public final class PurviewCatalogServiceRestAPIDocumentImpl {
    /** The catalog endpoint of your Purview account. Example: https://{accountName}.catalog.purview.azure.com. */
    private final String endpoint;

    /**
     * Gets The catalog endpoint of your Purview account. Example: https://{accountName}.catalog.purview.azure.com.
     *
     * @return the endpoint value.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /** Api Version. */
    private final String apiVersion;

    /**
     * Gets Api Version.
     *
     * @return the apiVersion value.
     */
    public String getApiVersion() {
        return this.apiVersion;
    }

    /** The HTTP pipeline to send requests through. */
    private final HttpPipeline httpPipeline;

    /**
     * Gets The HTTP pipeline to send requests through.
     *
     * @return the httpPipeline value.
     */
    public HttpPipeline getHttpPipeline() {
        return this.httpPipeline;
    }

    /** The serializer to serialize an object into a string. */
    private final SerializerAdapter serializerAdapter;

    /**
     * Gets The serializer to serialize an object into a string.
     *
     * @return the serializerAdapter value.
     */
    public SerializerAdapter getSerializerAdapter() {
        return this.serializerAdapter;
    }

    /** The EntitiesImpl object to access its operations. */
    private final EntitiesImpl entities;

    /**
     * Gets the EntitiesImpl object to access its operations.
     *
     * @return the EntitiesImpl object.
     */
    public EntitiesImpl getEntities() {
        return this.entities;
    }

    /** The GlossariesImpl object to access its operations. */
    private final GlossariesImpl glossaries;

    /**
     * Gets the GlossariesImpl object to access its operations.
     *
     * @return the GlossariesImpl object.
     */
    public GlossariesImpl getGlossaries() {
        return this.glossaries;
    }

    /** The DiscoveriesImpl object to access its operations. */
    private final DiscoveriesImpl discoveries;

    /**
     * Gets the DiscoveriesImpl object to access its operations.
     *
     * @return the DiscoveriesImpl object.
     */
    public DiscoveriesImpl getDiscoveries() {
        return this.discoveries;
    }

    /** The LineagesImpl object to access its operations. */
    private final LineagesImpl lineages;

    /**
     * Gets the LineagesImpl object to access its operations.
     *
     * @return the LineagesImpl object.
     */
    public LineagesImpl getLineages() {
        return this.lineages;
    }

    /** The RelationshipsImpl object to access its operations. */
    private final RelationshipsImpl relationships;

    /**
     * Gets the RelationshipsImpl object to access its operations.
     *
     * @return the RelationshipsImpl object.
     */
    public RelationshipsImpl getRelationships() {
        return this.relationships;
    }

    /** The TypesImpl object to access its operations. */
    private final TypesImpl types;

    /**
     * Gets the TypesImpl object to access its operations.
     *
     * @return the TypesImpl object.
     */
    public TypesImpl getTypes() {
        return this.types;
    }

    /**
     * Initializes an instance of PurviewCatalogServiceRestAPIDocument client.
     *
     * @param endpoint The catalog endpoint of your Purview account. Example:
     *     https://{accountName}.catalog.purview.azure.com.
     * @param apiVersion Api Version.
     */
    public PurviewCatalogServiceRestAPIDocumentImpl(String endpoint, String apiVersion) {
        this(
                new HttpPipelineBuilder()
                        .policies(new UserAgentPolicy(), new RetryPolicy(), new CookiePolicy())
                        .build(),
                JacksonAdapter.createDefaultSerializerAdapter(),
                endpoint,
                apiVersion);
    }

    /**
     * Initializes an instance of PurviewCatalogServiceRestAPIDocument client.
     *
     * @param httpPipeline The HTTP pipeline to send requests through.
     * @param endpoint The catalog endpoint of your Purview account. Example:
     *     https://{accountName}.catalog.purview.azure.com.
     * @param apiVersion Api Version.
     */
    public PurviewCatalogServiceRestAPIDocumentImpl(HttpPipeline httpPipeline, String endpoint, String apiVersion) {
        this(httpPipeline, JacksonAdapter.createDefaultSerializerAdapter(), endpoint, apiVersion);
    }

    /**
     * Initializes an instance of PurviewCatalogServiceRestAPIDocument client.
     *
     * @param httpPipeline The HTTP pipeline to send requests through.
     * @param serializerAdapter The serializer to serialize an object into a string.
     * @param endpoint The catalog endpoint of your Purview account. Example:
     *     https://{accountName}.catalog.purview.azure.com.
     * @param apiVersion Api Version.
     */
    public PurviewCatalogServiceRestAPIDocumentImpl(
            HttpPipeline httpPipeline, SerializerAdapter serializerAdapter, String endpoint, String apiVersion) {
        this.httpPipeline = httpPipeline;
        this.serializerAdapter = serializerAdapter;
        this.endpoint = endpoint;
        this.apiVersion = apiVersion;
        this.entities = new EntitiesImpl(this);
        this.glossaries = new GlossariesImpl(this);
        this.discoveries = new DiscoveriesImpl(this);
        this.lineages = new LineagesImpl(this);
        this.relationships = new RelationshipsImpl(this);
        this.types = new TypesImpl(this);
    }
}
