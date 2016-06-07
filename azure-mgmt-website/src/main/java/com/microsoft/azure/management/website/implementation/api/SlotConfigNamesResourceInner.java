/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Slot Config names azure resource.
 */
@JsonFlatten
public class SlotConfigNamesResourceInner extends Resource {
    /**
     * List of connection string names.
     */
    @JsonProperty(value = "properties.connectionStringNames")
    private List<String> connectionStringNames;

    /**
     * List of application settings names.
     */
    @JsonProperty(value = "properties.appSettingNames")
    private List<String> appSettingNames;

    /**
     * Get the connectionStringNames value.
     *
     * @return the connectionStringNames value
     */
    public List<String> connectionStringNames() {
        return this.connectionStringNames;
    }

    /**
     * Set the connectionStringNames value.
     *
     * @param connectionStringNames the connectionStringNames value to set
     * @return the SlotConfigNamesResourceInner object itself.
     */
    public SlotConfigNamesResourceInner withConnectionStringNames(List<String> connectionStringNames) {
        this.connectionStringNames = connectionStringNames;
        return this;
    }

    /**
     * Get the appSettingNames value.
     *
     * @return the appSettingNames value
     */
    public List<String> appSettingNames() {
        return this.appSettingNames;
    }

    /**
     * Set the appSettingNames value.
     *
     * @param appSettingNames the appSettingNames value to set
     * @return the SlotConfigNamesResourceInner object itself.
     */
    public SlotConfigNamesResourceInner withAppSettingNames(List<String> appSettingNames) {
        this.appSettingNames = appSettingNames;
        return this;
    }

}
