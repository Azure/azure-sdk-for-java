// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.DataSourceDataLakeGen2SharedKeyAccessor;
import com.azure.core.annotation.Fluent;

/**
 * The shared key credential entity for DataLakeGen2.
 */
@Fluent
public final class DataSourceDataLakeGen2SharedKey extends DataSourceCredentialEntity {
    private String id;
    private String name;
    private String description;
    private String sharedKey;

    static {
        DataSourceDataLakeGen2SharedKeyAccessor.setAccessor(
            new DataSourceDataLakeGen2SharedKeyAccessor.Accessor() {
                @Override
                public void setId(DataSourceDataLakeGen2SharedKey entity, String id) {
                    entity.setId(id);
                }

                @Override
                public String getSharedKey(DataSourceDataLakeGen2SharedKey entity) {
                    return entity.getSharedKey();
                }
            });
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * Creates DataSourceDataLakeGen2SharedKey.
     *
     * @param name The name
     * @param sharedKey The shared key
     */
    public DataSourceDataLakeGen2SharedKey(String name, String sharedKey) {
        this.name = name;
        this.sharedKey = sharedKey;
    }

    /**
     * Sets the name.
     *
     * @param name The name
     * @return an updated object with name set
     */
    public DataSourceDataLakeGen2SharedKey setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the shared key.
     *
     * @param sharedKey The shared key
     * @return an updated object with shared key set
     */
    public DataSourceDataLakeGen2SharedKey setSharedKey(String sharedKey) {
        this.sharedKey = sharedKey;
        return this;
    }

    /**
     * Sets the description.
     *
     * @param description The description.
     * @return an updated object with description set
     */
    public DataSourceDataLakeGen2SharedKey setDescription(String description) {
        this.description = description;
        return this;
    }

    private void setId(String id) {
        this.id = id;
    }

    private String getSharedKey() {
        return this.sharedKey;
    }
}
