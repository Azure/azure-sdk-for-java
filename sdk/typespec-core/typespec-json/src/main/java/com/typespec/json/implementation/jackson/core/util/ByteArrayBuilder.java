// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.typespec.json.implementation.jackson.core.util;

import java.io.OutputStream;
import java.util.*;

/**
 * Helper class that is similar to {@link java.io.ByteArrayOutputStream}
 * in usage, but more geared to Jackson use cases internally.
 * Specific changes include segment storage (no need to have linear
 * backing buffer, can avoid reallocations, copying), as well API
 * not based on {@link java.io.OutputStream}. In short, a very much
 * specialized builder object.
 *<p>
 * Also implements {@link OutputStream} to allow
 * efficient aggregation of output content as a byte array, similar
 * to how {@link java.io.ByteArrayOutputStream} works, but somewhat more
 * efficiently for many use cases.
 *<p>
 * NOTE: maximum size limited to Java Array maximum, 2 gigabytes: this
 * because usage pattern is to collect content for a `byte[]` and so although
 * theoretically this builder can aggregate more content it will not be usable
 * as things are. Behavior may be improved if we solve the access problem.
 */
public final class ByteArrayBuilder extends OutputStream
{
    public final static byte[] NO_BYTES = new byte[0];
    
    // Size of the first block we will allocate.
    private final static int INITIAL_BLOCK_SIZE = 500;
    
    // Maximum block size we will use for individual non-aggregated blocks.
    // For 2.10, let's limit to using 128k chunks (was 256k up to 2.9)
    private final static int MAX_BLOCK_SIZE = (1 << 17);

    final static int DEFAULT_BLOCK_ARRAY_SIZE = 40;

    // Optional buffer recycler instance that we can use for allocating the first block.
    private final BufferRecycler _bufferRecycler;
    private final LinkedList<byte[]> _pastBlocks = new LinkedList<byte[]>();
    
    // Number of bytes within byte arrays in {@link _pastBlocks}.
    private int _pastLen;
    private byte[] _currBlock;
    private int _currBlockPtr;

    public ByteArrayBuilder() { this(null); }
    public ByteArrayBuilder(BufferRecycler br) { this(br, INITIAL_BLOCK_SIZE); }
    public ByteArrayBuilder(int firstBlockSize) { this(null, firstBlockSize); }

    public ByteArrayBuilder(BufferRecycler br, int firstBlockSize) {
        _bufferRecycler = br;
        // 04-Sep-2020, tatu: Let's make this bit more robust and refuse to allocate
        //    humongous blocks even if requested
        if (firstBlockSize > MAX_BLOCK_SIZE) {
            firstBlockSize = MAX_BLOCK_SIZE;
        }
        _currBlock = (br == null) ? new byte[firstBlockSize] : br.allocByteBuffer(BufferRecycler.BYTE_WRITE_CONCAT_BUFFER);
    }

    private ByteArrayBuilder(BufferRecycler br, byte[] initialBlock, int initialLen) {
        _bufferRecycler = null;
        _currBlock = initialBlock;
        _currBlockPtr = initialLen;
    }

    public static ByteArrayBuilder fromInitial(byte[] initialBlock, int length) {
        return new ByteArrayBuilder(null, initialBlock, length);
    }
    
    public void reset() {
        _pastLen = 0;
        _currBlockPtr = 0;

        if (!_pastBlocks.isEmpty()) {
            _pastBlocks.clear();
        }
    }

    /**
     * @return Number of bytes aggregated so far
     *
     * @since 2.9
     */
    public int size() {
        return _pastLen + _currBlockPtr;
    }

    /**
     * Clean up method to call to release all buffers this object may be
     * using. After calling the method, no other accessors can be used (and
     * attempt to do so may result in an exception)
     */
    public void release() {
        reset();
        if (_bufferRecycler != null && _currBlock != null) {
            _bufferRecycler.releaseByteBuffer(BufferRecycler.BYTE_WRITE_CONCAT_BUFFER, _currBlock);
            _currBlock = null;
        }
    }

    public void append(int i) {
        if (_currBlockPtr >= _currBlock.length) {
            _allocMore();
        }
        _currBlock[_currBlockPtr++] = (byte) i;
    }

    public void appendTwoBytes(int b16) {
        if ((_currBlockPtr + 1) < _currBlock.length) {
            _currBlock[_currBlockPtr++] = (byte) (b16 >> 8);
            _currBlock[_currBlockPtr++] = (byte) b16;
        } else {
            append(b16 >> 8);
            append(b16);
        }
    }

    public void appendThreeBytes(int b24) {
        if ((_currBlockPtr + 2) < _currBlock.length) {
            _currBlock[_currBlockPtr++] = (byte) (b24 >> 16);
            _currBlock[_currBlockPtr++] = (byte) (b24 >> 8);
            _currBlock[_currBlockPtr++] = (byte) b24;
        } else {
            append(b24 >> 16);
            append(b24 >> 8);
            append(b24);
        }
    }

    // @since 2.9
    public void appendFourBytes(int b32) {
        if ((_currBlockPtr + 3) < _currBlock.length) {
            _currBlock[_currBlockPtr++] = (byte) (b32 >> 24);
            _currBlock[_currBlockPtr++] = (byte) (b32 >> 16);
            _currBlock[_currBlockPtr++] = (byte) (b32 >> 8);
            _currBlock[_currBlockPtr++] = (byte) b32;
        } else {
            append(b32 >> 24);
            append(b32 >> 16);
            append(b32 >> 8);
            append(b32);
        }
    }
    
    /**
     * Method called when results are finalized and we can get the
     * full aggregated result buffer to return to the caller
     *
     * @return Aggregated contents as a {@code byte[]}
     */
    public byte[] toByteArray()
    {
        int totalLen = _pastLen + _currBlockPtr;
        
        if (totalLen == 0) { // quick check: nothing aggregated?
            return NO_BYTES;
        }
        byte[] result = new byte[totalLen];
        int offset = 0;

        for (byte[] block : _pastBlocks) {
            int len = block.length;
            System.arraycopy(block, 0, result, offset, len);
            offset += len;
        }
        System.arraycopy(_currBlock, 0, result, offset, _currBlockPtr);
        offset += _currBlockPtr;
        if (offset != totalLen) { // just a sanity check
            throw new RuntimeException("Internal error: total len assumed to be "+totalLen+", copied "+offset+" bytes");
        }
        // Let's only reset if there's sizable use, otherwise will get reset later on
        if (!_pastBlocks.isEmpty()) {
            reset();
        }
        return result;
    }

    /*
    /**********************************************************
    /* Non-stream API (similar to TextBuffer)
    /**********************************************************
     */

    /**
     * Method called when starting "manual" output: will clear out
     * current state and return the first segment buffer to fill
     *
     * @return Segment to use for writing
     */
    public byte[] resetAndGetFirstSegment() {
        reset();
        return _currBlock;
    }

    /**
     * Method called when the current segment buffer is full; will
     * append to current contents, allocate a new segment buffer
     * and return it
     *
     * @return Segment to use for writing
     */
    public byte[] finishCurrentSegment() {
        _allocMore();
        return _currBlock;
    }

    /**
     * Method that will complete "manual" output process, coalesce
     * content (if necessary) and return results as a contiguous buffer.
     * 
     * @param lastBlockLength Amount of content in the current segment
     * buffer.
     * 
     * @return Coalesced contents
     */
    public byte[] completeAndCoalesce(int lastBlockLength) {
        _currBlockPtr = lastBlockLength;
        return toByteArray();
    }

    public byte[] getCurrentSegment() { return _currBlock; }
    public void setCurrentSegmentLength(int len) { _currBlockPtr = len; }
    public int getCurrentSegmentLength() { return _currBlockPtr; }

    /*
    /**********************************************************
    /* OutputStream implementation
    /**********************************************************
     */

    @Override
    public void write(byte[] b) {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len)
    {
        while (true) {
            int max = _currBlock.length - _currBlockPtr;
            int toCopy = Math.min(max, len);
            if (toCopy > 0) {
                System.arraycopy(b, off, _currBlock, _currBlockPtr, toCopy);
                off += toCopy;
                _currBlockPtr += toCopy;
                len -= toCopy;
            }
            if (len <= 0) break;
            _allocMore();
        }
    }

    @Override
    public void write(int b) {
        append(b);
    }

    @Override public void close() { /* NOP */ }
    @Override public void flush() { /* NOP */ }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    private void _allocMore()
    {
        final int newPastLen = _pastLen + _currBlock.length;

        // 13-Feb-2016, tatu: As per [core#351] let's try to catch problem earlier;
        //     for now we are strongly limited by 2GB limit of Java arrays        
        if (newPastLen < 0) {
            throw new IllegalStateException("Maximum Java array size (2GB) exceeded by `ByteArrayBuilder`");
        }

        _pastLen = newPastLen;

        /* Let's allocate block that's half the total size, except
         * never smaller than twice the initial block size.
         * The idea is just to grow with reasonable rate, to optimize
         * between minimal number of chunks and minimal amount of
         * wasted space.
         */
        int newSize = Math.max((_pastLen >> 1), (INITIAL_BLOCK_SIZE + INITIAL_BLOCK_SIZE));
        // plus not to exceed max we define...
        if (newSize > MAX_BLOCK_SIZE) {
            newSize = MAX_BLOCK_SIZE;
        }
        _pastBlocks.add(_currBlock);
        _currBlock = new byte[newSize];
        _currBlockPtr = 0;
    }
}
