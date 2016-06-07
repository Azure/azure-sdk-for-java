/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;


/**
 * A Data Lake Analytics catalog U-SQL dependency information item.
 */
public class USqlAssemblyDependencyInfo {
    /**
     * Gets or sets the EntityId of the dependency.
     */
    private EntityId entityId;

    /**
     * Get the entityId value.
     *
     * @return the entityId value
     */
    public EntityId entityId() {
        return this.entityId;
    }

    /**
     * Set the entityId value.
     *
     * @param entityId the entityId value to set
     * @return the USqlAssemblyDependencyInfo object itself.
     */
    public USqlAssemblyDependencyInfo withEntityId(EntityId entityId) {
        this.entityId = entityId;
        return this;
    }

}
