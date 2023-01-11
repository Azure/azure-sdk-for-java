package com.azure.core.implementation.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListByteBufferInputStream extends InputStream {
    private class BufferOffsetPair {
        public final long offset;
        public final ByteBuffer byteBuffer;

        public BufferOffsetPair(long offset, ByteBuffer byteBuffer) {
            this.offset = offset;
            this.byteBuffer = byteBuffer;
        }
    }

    private final List<BufferOffsetPair> content;

    private long position = 0;
    private Long markPosition = null;

    public ListByteBufferInputStream(List<ByteBuffer> content) {
        Objects.requireNonNull(content, "'content' cannot be null");
        this.content = new ArrayList<>(content.size());
        long offset = 0;
        for (ByteBuffer bb : content) {
            this.content.add(new BufferOffsetPair(offset, bb.asReadOnlyBuffer()));
            offset += bb.remaining();
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        BufferOffsetPair buf = findCurrentBuffer();
        int toRead = Math.min(len, buf.byteBuffer.remaining());
        buf.byteBuffer.get((int)(position - buf.offset) + buf.byteBuffer.position(), b, off, toRead);
        position += toRead;
        return toRead;
    }

    @Override
    public int read() throws IOException {
        BufferOffsetPair buf = findCurrentBuffer();
        byte result = buf.byteBuffer.get((int)(position - buf.offset) + buf.byteBuffer.position());
        position += 1;
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        long skipAmount = Math.min(remainingInStream(), n);
        position += skipAmount;
        return skipAmount;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(int readlimit) {
        markPosition = position;
    }

    @Override
    public void reset() {
        Objects.requireNonNull(markPosition, "Stream has not been marked. Cannot reset.");
        position = markPosition;
    }

    @Override
    public int available() {
        return remainingInBuffer();
    }

    @Override
    public void close() {
    }

    private long remainingInStream() {
        BufferOffsetPair last = content.get(content.size() - 1);
        return last.offset + last.byteBuffer.remaining() - position;
    }

    private int remainingInBuffer() {
        BufferOffsetPair buf = findCurrentBuffer();
        return (int)(buf.offset + buf.byteBuffer.remaining() - position);
    }

    private BufferOffsetPair findCurrentBuffer() {
        for (BufferOffsetPair pair : content) {
            if (position >= pair.offset) {
                return pair;
            }
        }
        throw new IllegalStateException("Invalid stream position");
    }
}
