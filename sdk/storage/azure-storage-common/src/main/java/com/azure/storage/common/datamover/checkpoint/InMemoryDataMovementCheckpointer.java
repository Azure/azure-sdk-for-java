// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.datamover.checkpoint;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * In memory DataMovementCheckpointer
 */
public class InMemoryDataMovementCheckpointer implements DataMovementCheckpointer {

    private final ConcurrentHashMap<String, DataTransferState> map = new ConcurrentHashMap<>();

    @Override
    public Stream<DataTransferState> listTransfers() {
        return map.values().stream();
    }

    @Override
    public void addTransfer(DataTransferState transfer) {
        map.put(transfer.getIdentifier(), transfer);
    }

    @Override
    public void removeTransfer(DataTransferState transferState) {
        map.remove(transferState.getIdentifier());
    }
}
