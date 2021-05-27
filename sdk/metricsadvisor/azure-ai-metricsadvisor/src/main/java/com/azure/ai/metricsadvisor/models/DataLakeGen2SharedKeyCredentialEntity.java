// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.ai.metricsadvisor.implementation.util.DataLakeGen2SharedKeyCredentialEntityAccessor;

/**
 * The shared key credential entity for DataLakeGen2.
 */
public final class DataLakeGen2SharedKeyCredentialEntity extends DataSourceCredentialEntity {
    private String id;
    private String name;
    private String description;
    private String sharedKey;

    static {
        DataLakeGen2SharedKeyCredentialEntityAccessor.setAccessor(
            new DataLakeGen2SharedKeyCredentialEntityAccessor.Accessor() {
                @Override
                public void setId(DataLakeGen2SharedKeyCredentialEntity entity, String id) {
                    entity.setId(id);
                }

                @Override
                public String getSharedKey(DataLakeGen2SharedKeyCredentialEntity entity) {
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
     * Creates DataLakeGen2SharedKeyCredentialEntity.
     *
     * @param name The name
     * @param sharedKey The shared key
     */
    public DataLakeGen2SharedKeyCredentialEntity(String name, String sharedKey) {
        this.name = name;
        this.sharedKey = sharedKey;
    }

    /**
     * Sets the name.
     *
     * @param name The name
     * @return an updated object with name set
     */
    public DataLakeGen2SharedKeyCredentialEntity setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the shared key.
     *
     * @param sharedKey The shared key
     * @return an updated object with shared key set
     */
    public DataLakeGen2SharedKeyCredentialEntity setSharedKey(String sharedKey) {
        this.sharedKey = sharedKey;
        return this;
    }

    /**
     * Sets the description.
     *
     * @param description The description.
     * @return an updated object with description set
     */
    public DataLakeGen2SharedKeyCredentialEntity setDescription(String description) {
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
