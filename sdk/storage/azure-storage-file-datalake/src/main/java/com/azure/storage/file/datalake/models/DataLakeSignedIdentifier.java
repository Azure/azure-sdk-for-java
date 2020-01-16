// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

/**
 * signed identifier.
 */
public class DataLakeSignedIdentifier {
    /*
     * a unique id
     */
    private String id;

    /*
     * The accessPolicy property.
     */
    private DataLakeAccessPolicy accessPolicy;

    /**
     * Get the id property: a unique id.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id property: a unique id.
     *
     * @param id the id value to set.
     * @return the DataLakeSignedIdentifier object itself.
     */
    public DataLakeSignedIdentifier setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the accessPolicy property: The accessPolicy property.
     *
     * @return the accessPolicy value.
     */
    public DataLakeAccessPolicy getAccessPolicy() {
        return this.accessPolicy;
    }

    /**
     * Set the accessPolicy property: The accessPolicy property.
     *
     * @param accessPolicy the accessPolicy value to set.
     * @return the DataLakeSignedIdentifier object itself.
     */
    public DataLakeSignedIdentifier setAccessPolicy(DataLakeAccessPolicy accessPolicy) {
        this.accessPolicy = accessPolicy;
        return this;
    }
}
