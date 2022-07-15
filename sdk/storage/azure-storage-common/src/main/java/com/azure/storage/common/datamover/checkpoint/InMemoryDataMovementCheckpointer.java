
package com.azure.storage.common.datamover.checkpoint;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In memory DataMovementCheckpointer
 */
public class InMemoryDataMovementCheckpointer implements DataMovementCheckpointer {

    private final ConcurrentHashMap<String, DataTransferState> map = new ConcurrentHashMap<>();

    @Override
    public Iterable<DataTransferState> listTransfers() {
        return map.values();
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
