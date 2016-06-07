/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;

/**
 * Class containing names for connection strings and application settings to
 * be marked as sticky to the slot
 * and not moved during swap operation
 * This is valid for all deployment slots under the site.
 */
public class SlotConfigNames {
    /**
     * List of connection string names.
     */
    private List<String> connectionStringNames;

    /**
     * List of application settings names.
     */
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
     * @return the SlotConfigNames object itself.
     */
    public SlotConfigNames withConnectionStringNames(List<String> connectionStringNames) {
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
     * @return the SlotConfigNames object itself.
     */
    public SlotConfigNames withAppSettingNames(List<String> appSettingNames) {
        this.appSettingNames = appSettingNames;
        return this;
    }

}
