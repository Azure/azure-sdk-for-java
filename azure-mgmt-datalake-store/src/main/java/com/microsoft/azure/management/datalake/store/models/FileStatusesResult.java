/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Lake Store filesystem file status list information response.
 */
public class FileStatusesResult {
    /**
     * the object representing the list of file statuses.
     */
    @JsonProperty(value = "FileStatuses", access = JsonProperty.Access.WRITE_ONLY)
    private FileStatuses fileStatuses;

    /**
     * Get the fileStatuses value.
     *
     * @return the fileStatuses value
     */
    public FileStatuses fileStatuses() {
        return this.fileStatuses;
    }

}
