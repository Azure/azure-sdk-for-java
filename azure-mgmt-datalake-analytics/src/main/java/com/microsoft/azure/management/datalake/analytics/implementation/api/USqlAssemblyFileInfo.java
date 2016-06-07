/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;


/**
 * A Data Lake Analytics catalog U-SQL assembly file information item.
 */
public class USqlAssemblyFileInfo {
    /**
     * Gets or sets the assembly file type. Possible values include:
     * 'Assembly', 'Resource'.
     */
    private FileType type;

    /**
     * Gets or sets the the original path to the assembly file.
     */
    private String originalPath;

    /**
     * Gets or sets the the content path to the assembly file.
     */
    private String contentPath;

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public FileType type() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     * @return the USqlAssemblyFileInfo object itself.
     */
    public USqlAssemblyFileInfo withType(FileType type) {
        this.type = type;
        return this;
    }

    /**
     * Get the originalPath value.
     *
     * @return the originalPath value
     */
    public String originalPath() {
        return this.originalPath;
    }

    /**
     * Set the originalPath value.
     *
     * @param originalPath the originalPath value to set
     * @return the USqlAssemblyFileInfo object itself.
     */
    public USqlAssemblyFileInfo withOriginalPath(String originalPath) {
        this.originalPath = originalPath;
        return this;
    }

    /**
     * Get the contentPath value.
     *
     * @return the contentPath value
     */
    public String contentPath() {
        return this.contentPath;
    }

    /**
     * Set the contentPath value.
     *
     * @param contentPath the contentPath value to set
     * @return the USqlAssemblyFileInfo object itself.
     */
    public USqlAssemblyFileInfo withContentPath(String contentPath) {
        this.contentPath = contentPath;
        return this;
    }

}
