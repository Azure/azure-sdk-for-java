package com.microsoft.azure.storage;

import com.microsoft.azure.storage.blob.SubStream;

import java.io.InputStream;
import java.util.Iterator;

public final class SubStreamGenerator implements Iterable<InputStream> {

    private final Object mutex = new Object();
    private final long blockSize;
    private final InputStream wrappedStream;
    private int currentBlock = 0;
    private long lastBlockSize;
    private int blocksPending;

    public SubStreamGenerator(InputStream wrappedStream, int totalBlocks, long blockSize) {
        this.wrappedStream = wrappedStream;
        this.blocksPending = totalBlocks;
        this.blockSize = blockSize;
        this.lastBlockSize = blockSize;
    }

    public void setLastBlockSize(long blockSize) {
        this.lastBlockSize = blockSize;
    }

    @Override
    public Iterator<InputStream> iterator() {
        return new SubStreamIterator();
    }

    private class SubStreamIterator implements Iterator<InputStream> {

        @Override
        public boolean hasNext() {
            return blocksPending > 0;
        }

        @Override
        public SubStream next() {
            blocksPending--;
            return new SubStream(
                    wrappedStream,
                    currentBlock++ * blockSize,
                    blocksPending > 0 ? blockSize : lastBlockSize,
                    mutex);
        }

        @Override
        public void remove() {
            // No-op
        }
    }
}




