/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;


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
    public String getFirstPart() {
        return this.firstPart;
    }

    /**
     * Set the firstPart value.
     *
     * @param firstPart the firstPart value to set
     */
    public void setFirstPart(String firstPart) {
        this.firstPart = firstPart;
    }

    /**
     * Get the secondPart value.
     *
     * @return the secondPart value
     */
    public String getSecondPart() {
        return this.secondPart;
    }

    /**
     * Set the secondPart value.
     *
     * @param secondPart the secondPart value to set
     */
    public void setSecondPart(String secondPart) {
        this.secondPart = secondPart;
    }

    /**
     * Get the thirdPart value.
     *
     * @return the thirdPart value
     */
    public String getThirdPart() {
        return this.thirdPart;
    }

    /**
     * Set the thirdPart value.
     *
     * @param thirdPart the thirdPart value to set
     */
    public void setThirdPart(String thirdPart) {
        this.thirdPart = thirdPart;
    }

    /**
     * Get the server value.
     *
     * @return the server value
     */
    public String getServer() {
        return this.server;
    }

    /**
     * Set the server value.
     *
     * @param server the server value to set
     */
    public void setServer(String server) {
        this.server = server;
    }

}
