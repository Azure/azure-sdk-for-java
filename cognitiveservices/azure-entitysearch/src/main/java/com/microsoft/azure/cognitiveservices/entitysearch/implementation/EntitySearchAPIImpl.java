/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.entitysearch.implementation;

import com.microsoft.azure.cognitiveservices.entitysearch.EntitySearchAPI;
import com.microsoft.azure.cognitiveservices.entitysearch.Entities;
import com.microsoft.rest.ServiceClient;
import com.microsoft.rest.RestClient;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Initializes a new instance of the EntitySearchAPI class.
 */
public class EntitySearchAPIImpl extends ServiceClient implements EntitySearchAPI {

    /**
     * The Entities object to access its operations.
     */
    private Entities entities;

    /**
     * Gets the Entities object to access its operations.
     * @return the Entities object.
     */
    public Entities entities() {
        return this.entities;
    }

    /**
     * Initializes an instance of EntitySearchAPI client.
     */
    public EntitySearchAPIImpl() {
        this("https://api.cognitive.microsoft.com/bing/v7.0");
    }

    /**
     * Initializes an instance of EntitySearchAPI client.
     *
     * @param baseUrl the base URL of the host
     */
    public EntitySearchAPIImpl(String baseUrl) {
        super(baseUrl);
        initialize();
    }

    /**
     * Initializes an instance of EntitySearchAPI client.
     *
     * @param clientBuilder the builder for building an OkHttp client, bundled with user configurations
     * @param restBuilder the builder for building an Retrofit client, bundled with user configurations
     */
    public EntitySearchAPIImpl(OkHttpClient.Builder clientBuilder, Retrofit.Builder restBuilder) {
        this("https://api.cognitive.microsoft.com/bing/v7.0", clientBuilder, restBuilder);
        initialize();
    }

    /**
     * Initializes an instance of EntitySearchAPI client.
     *
     * @param baseUrl the base URL of the host
     * @param clientBuilder the builder for building an OkHttp client, bundled with user configurations
     * @param restBuilder the builder for building an Retrofit client, bundled with user configurations
     */
    public EntitySearchAPIImpl(String baseUrl, OkHttpClient.Builder clientBuilder, Retrofit.Builder restBuilder) {
        super(baseUrl, clientBuilder, restBuilder);
        initialize();
    }

    /**
     * Initializes an instance of EntitySearchAPI client.
     *
     * @param restClient the REST client containing pre-configured settings
     */
    public EntitySearchAPIImpl(RestClient restClient) {
        super(restClient);
        initialize();
    }

    private void initialize() {
        this.entities = new EntitiesImpl(retrofit(), this);
    }
}
