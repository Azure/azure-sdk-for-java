/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Lake Store filesystem file status information response.
 */
public class FileStatusResult {
    /**
     * the file status object associated with the specified path.
     */
    @JsonProperty(value = "FileStatus", access = JsonProperty.Access.WRITE_ONLY)
    private FileStatusProperties fileStatus;

    /**
     * Get the fileStatus value.
     *
     * @return the fileStatus value
     */
    public FileStatusProperties fileStatus() {
        return this.fileStatus;
    }

}
