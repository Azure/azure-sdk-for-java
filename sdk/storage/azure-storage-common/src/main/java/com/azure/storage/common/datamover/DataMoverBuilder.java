// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.datamover;

/**
 * Builds the DataMover
 */
public class DataMoverBuilder {

    /**
     * Builds the data mover.
     * @return The Data Mover.
     */
    public DataMover build() {
        return new DataMover();
    }
}
