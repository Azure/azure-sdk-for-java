/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;


/**
 * A Data Lake Analytics DDL name item.
 */
public class DdlName {
    /**
     * Gets or sets the name of the table associated with this database and
     * schema.
     */
    private String firstPart;

    /**
     * Gets or sets the name of the table associated with this database and
     * schema.
     */
    private String secondPart;

    /**
     * Gets or sets the name of the table associated with this database and
     * schema.
     */
    private String thirdPart;

    /**
     * Gets or sets the name of the table associated with this database and
     * schema.
     */
    private String server;

    /**
     * Get the firstPart value.
     *
     * @return the firstPart value
     */
    public String firstPart() {
        return this.firstPart;
    }

    /**
     * Set the firstPart value.
     *
     * @param firstPart the firstPart value to set
     * @return the DdlName object itself.
     */
    public DdlName withFirstPart(String firstPart) {
        this.firstPart = firstPart;
        return this;
    }

    /**
     * Get the secondPart value.
     *
     * @return the secondPart value
     */
    public String secondPart() {
        return this.secondPart;
    }

    /**
     * Set the secondPart value.
     *
     * @param secondPart the secondPart value to set
     * @return the DdlName object itself.
     */
    public DdlName withSecondPart(String secondPart) {
        this.secondPart = secondPart;
        return this;
    }

    /**
     * Get the thirdPart value.
     *
     * @return the thirdPart value
     */
    public String thirdPart() {
        return this.thirdPart;
    }

    /**
     * Set the thirdPart value.
     *
     * @param thirdPart the thirdPart value to set
     * @return the DdlName object itself.
     */
    public DdlName withThirdPart(String thirdPart) {
        this.thirdPart = thirdPart;
        return this;
    }

    /**
     * Get the server value.
     *
     * @return the server value
     */
    public String server() {
        return this.server;
    }

    /**
     * Set the server value.
     *
     * @param server the server value to set
     * @return the DdlName object itself.
     */
    public DdlName withServer(String server) {
        this.server = server;
        return this;
    }

}
