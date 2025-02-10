// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.DataLakePathClient;

/**
 * Parameters when calling getProperties() on {@link DataLakePathClient}
 */
public class PathGetPropertiesOptions {
    private DataLakeRequestConditions requestConditions;
    private Boolean userPrincipalName;

    /**
     * Creates a new instance of {@link PathGetPropertiesOptions}.
     */
    public PathGetPropertiesOptions() {
    }

    /**
     * Gets the {@link DataLakeRequestConditions}.
     *
     * @return {@link DataLakeRequestConditions}
     */
    public DataLakeRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link DataLakeRequestConditions}.
     *
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return The updated options.
     */
    public PathGetPropertiesOptions setRequestConditions(DataLakeRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Gets the value for the x-ms-upn header.
     *
     * @return The value for the x-ms-upn header.
     */
    public Boolean isUserPrincipalName() {
        return userPrincipalName;
    }

    /**
     * Sets the value for the x-ms-upn header.
     *
     * @param userPrincipalName The value for the x-ms-upn header.
     * @return The updated options.
     */
    public PathGetPropertiesOptions setUserPrincipalName(Boolean userPrincipalName) {
        this.userPrincipalName = userPrincipalName;
        return this;
    }
}
