// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.datamover.checkpoint;

import java.util.stream.Stream;

/**
 * Can checkpoint transfers.
 */
public interface DataMovementCheckpointer {

    /**
     * List transfers.
     * @return transfers
     */
    Stream<DataTransferState> listTransfers();

    /**
     * Adds transfer.
     * @param transfer transfer.
     */
    void addTransfer(DataTransferState transfer);

    /**
     * Removes transfer.
     * @param transferState transfer.
     */
    void removeTransfer(DataTransferState transferState);
}
