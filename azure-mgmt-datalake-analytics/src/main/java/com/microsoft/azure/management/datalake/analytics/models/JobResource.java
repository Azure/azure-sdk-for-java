/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;


/**
 * The Data Lake Analytics U-SQL job resources.
 */
public class JobResource {
    /**
     * Gets or set the name of the resource.
     */
    private String name;

    /**
     * Gets or sets the path to the resource.
     */
    private String resourcePath;

    /**
     * Gets or sets the job resource type. Possible values include:
     * 'VertexResource', 'StatisticsResource'.
     */
    private String type;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the resourcePath value.
     *
     * @return the resourcePath value
     */
    public String getResourcePath() {
        return this.resourcePath;
    }

    /**
     * Set the resourcePath value.
     *
     * @param resourcePath the resourcePath value to set
     */
    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public String getType() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     */
    public void setType(String type) {
        this.type = type;
    }

}
