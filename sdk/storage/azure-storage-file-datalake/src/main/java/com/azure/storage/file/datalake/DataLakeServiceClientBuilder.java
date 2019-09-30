// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.common.BaseClientBuilder;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;


public final class DataLakeServiceClientBuilder extends BaseClientBuilder {

    private static final String DATA_LAKE_ENDPOINT_MIDFIX = "dfs";
    private static final String BLOB_ENDPOINT_PREFIX = "blob";

    private static String blobEndpoint;
    private static String dfsEndpoint;


    private final ClientLogger logger = new ClientLogger(DataLakeServiceClientBuilder.class);

    /**
     * Creates a builder instance that is able to configure and construct {@link DataLakeServiceClientBuilder
     * DataLakeServiceClients} and {@link DataLakeServiceClientBuilder DataLakeServiceAsyncClients}.
     */
    public DataLakeServiceClientBuilder() {
    }

    /**
     * @return a {@link DataLakeServiceClient} created from the configurations in this builder.
     */
    public DataLakeServiceClient buildClient() {
        return new DataLakeServiceClient(buildAsyncClient());
    }

    /**
     * @return a {@link DataLakeServiceAsyncClient} created from the configurations in this builder.
     */
    public DataLakeServiceAsyncClient buildAsyncClient() {
        HttpPipeline pipeline = super.getPipeline();
        if (pipeline == null) {
            pipeline = super.buildPipeline();
        }

        // TODO (gapra): Get the appropriate values for methods
        return new DataLakeServiceAsyncClient(new AzureBlobStorageBuilder()
            .url(super.endpoint)
            .pipeline(pipeline)
            .build(), null /* DataLakeStorageClientBuilder */, accountName);

        //new DataLakeStorageClientBuilder()
        //            .filesystem() // file system identifier
        //            .path1() // file/directory path
        //            .pipeline(pipeline)
        //            .prefix()
        //            .url()
        //            .build()
    }

    /**
     * Sets the data lake service endpoint, additionally parses it for information (SAS token)
     *
     * @param endpoint URL of the service
     * @return the updated DataLakeServiceClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    public DataLakeServiceClientBuilder endpoint(String endpoint) {

        // TODO (gapra): endpoint stuff

        return this;
    }

    @Override
    protected Class<DataLakeServiceClientBuilder> getClazz() {
        return DataLakeServiceClientBuilder.class;
    }

    String endpoint() {
        return super.endpoint;
    }

    @Override
    protected void applyServiceSpecificValidations(ResponseValidationPolicyBuilder builder) {
    // TODO (gapra) : Does ADLS service support CPK?
    }

    @Override
    protected String getServiceUrlMidfix() {
        return DATA_LAKE_ENDPOINT_MIDFIX;
    }

    @Override
    protected UserAgentPolicy getUserAgentPolicy() {
        return new UserAgentPolicy(DataLakeConfiguration.NAME, DataLakeConfiguration.VERSION, super.getConfiguration());
    }
}
