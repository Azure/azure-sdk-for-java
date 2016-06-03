/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;


/**
 * The Data Lake Analytics job resources.
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
     * 'VertexResource', 'JobManagerResource', 'StatisticsResource',
     * 'VertexResourceInUserFolder', 'JobManagerResourceInUserFolder',
     * 'StatisticsResourceInUserFolder'.
     */
    private JobResourceType type;

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
     * @return the JobResource object itself.
     */
    public JobResource withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the resourcePath value.
     *
     * @return the resourcePath value
     */
    public String resourcePath() {
        return this.resourcePath;
    }

    /**
     * Set the resourcePath value.
     *
     * @param resourcePath the resourcePath value to set
     * @return the JobResource object itself.
     */
    public JobResource withResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
        return this;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public JobResourceType type() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     * @return the JobResource object itself.
     */
    public JobResource withType(JobResourceType type) {
        this.type = type;
        return this;
    }

}
