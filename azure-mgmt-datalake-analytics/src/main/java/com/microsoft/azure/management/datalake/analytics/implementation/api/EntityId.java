/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import java.util.UUID;

/**
 * A Data Lake Analytics catalog entity identifier object.
 */
public class EntityId {
    /**
     * Gets or sets the name of the external table associated with this
     * database, schema and table.
     */
    private DdlName name;

    /**
     * Gets or sets the version of the external data source.
     */
    private UUID version;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public DdlName name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the EntityId object itself.
     */
    public EntityId withName(DdlName name) {
        this.name = name;
        return this;
    }

    /**
     * Get the version value.
     *
     * @return the version value
     */
    public UUID version() {
        return this.version;
    }

    /**
     * Set the version value.
     *
     * @param version the version value to set
     * @return the EntityId object itself.
     */
    public EntityId withVersion(UUID version) {
        this.version = version;
        return this;
    }

}
