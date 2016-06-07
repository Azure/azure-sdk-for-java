/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Lake Store filesystem error containing a specific WebHDFS exception.
 */
public class AdlsError {
    /**
     * Gets the object representing the actual WebHDFS exception being
     * returned.
     */
    @JsonProperty(value = "RemoteException", access = JsonProperty.Access.WRITE_ONLY)
    private AdlsRemoteException remoteException;

    /**
     * Get the remoteException value.
     *
     * @return the remoteException value
     */
    public AdlsRemoteException remoteException() {
        return this.remoteException;
    }

}
