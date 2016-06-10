/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Lake Store file status list information.
 */
public class FileStatuses {
    /**
     * the object containing the list of properties of the files.
     */
    @JsonProperty(value = "FileStatus", access = JsonProperty.Access.WRITE_ONLY)
    private List<FileStatusProperties> fileStatus;

    /**
     * Get the fileStatus value.
     *
     * @return the fileStatus value
     */
    public List<FileStatusProperties> fileStatus() {
        return this.fileStatus;
    }

}
