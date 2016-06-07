/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;


/**
 * A Data Lake Analytics catalog U-SQL table column item.
 */
public class USqlTableColumn {
    /**
     * Gets or sets the name of the column in the table.
     */
    private String name;

    /**
     * Gets or sets the object type of the specified column (such as
     * System.String).
     */
    private String type;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the USqlTableColumn object itself.
     */
    public USqlTableColumn withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public String type() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     * @return the USqlTableColumn object itself.
     */
    public USqlTableColumn withType(String type) {
        this.type = type;
        return this;
    }

}
