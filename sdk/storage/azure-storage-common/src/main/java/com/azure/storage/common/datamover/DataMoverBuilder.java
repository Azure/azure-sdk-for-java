// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.datamover;

import com.azure.storage.common.datamover.checkpoint.DataMovementCheckpointer;

/**
 * Builds the DataMover
 */
public class DataMoverBuilder {

    /**
     * Configures checkpointer.
     * @param checkpointer checkpointer
     * @return this.
     */
    public DataMoverBuilder checkpointer(DataMovementCheckpointer checkpointer) {
        return this;
    }

    /**
     * Builds the data mover.
     * @return The Data Mover.
     */
    public DataMover build() {
        return new DataMover();
    }
}
