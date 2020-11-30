// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation;

import com.azure.storage.internal.avro.implementation.schema.AvroSchema;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Represents the current state of the AvroParser
 *
 * This class internally holds state to keep track of
 *
 * <li> the data available to the schemas. </li>
 * <li> a stack of schemas that need to be parsed. </li>
 * <li> the current size of the cached buffers. </li>
 */
public class AvroParserState {

    private Stack<AvroSchema> stack;
    private List<ByteBuffer> cache;
    private long size;
    private long sourceOffset; /* Keeps track of number of bytes read from the source. */

    /**
     * Creates a new instance of an AvroParserState.
     */
    AvroParserState() {
        this(0);
    }

    /**
     * Creates a new instance of an AvroParserState with a given source offset.
     */
    AvroParserState(long sourceOffset) {
        this.stack = new Stack<>();
        this.cache = new LinkedList<>();
        this.size = 0;
        this.sourceOffset = sourceOffset;
    }

    /**
     * Writes to the state's internal cache of buffers.
     *
     * @param buffer The buffer to add to the cache.
     */
    void write(ByteBuffer buffer) {
        this.cache.add(buffer);
        this.size += buffer.remaining();
    }

    /**
     * Pushes to the state's stack of schemas to process.
     *
     * @param schema The schema to push to the stack.
     */
    public void pushToStack(AvroSchema schema) {
        this.stack.push(schema);
    }

    /**
     * Peeks the state's stack of schemas to process.
     *
     * @return The Schema at the top of the stack.
     */
    AvroSchema peekFromStack() {
        return this.stack.peek();
    }

    /**
     * Determines if the stack is empty or not.
     *
     * @return Whether or not the stack is empty.
     */
    boolean isStackEmpty() {
        return this.stack.isEmpty();
    }

    /**
     * Pops off the Schema at the top of the stack. the state's stack of schemas to process.
     *
     */
    void popOffStack() {
        this.stack.pop();
    }

    /**
     * Whether or not the state is ready to emit sizeRequired bytes.
     *
     * @param sizeRequired The number of bytes required.
     * @return Whether or not the state is ready to emit sizeRequired bytes
     */
    public boolean sizeGreaterThan(long sizeRequired) {
        return this.size >= sizeRequired;
    }

    /**
     * Gets the source offset.
     *
     * @return the source offset.
     */
    public long getSourceOffset() {
        return this.sourceOffset;
    }

    /**
     * Consumes bytes from the state's internal cache of buffers.
     * Meant for use by AvroSchema objects (specifically ones that represent primitive types, since complex types
     * are just a combination of primitive types)
     *
     * @param size The number of bytes to consume.
     * @return A List of ByteBuffers with the number of bytes requested.
     */
    public List<ByteBuffer> read(long size) {
        List<ByteBuffer> result = new LinkedList<>();
        Iterator<ByteBuffer> bufferIterator = this.cache.iterator();
        long needed = size;
        while (needed > 0) {
            ByteBuffer current = bufferIterator.next();
            /* Buffer can entirely be used to satisfy at least part of this request. */
            if (current.remaining() <= needed) {
                result.add(current);
                bufferIterator.remove();
                needed -= current.remaining();
                this.size -= current.remaining();
                this.sourceOffset += current.remaining();
            } else {
                ByteBuffer dup = current.duplicate();
                this.size -= (int) needed;
                this.sourceOffset += (int) needed;
                dup.limit(dup.position() + (int) needed);
                current.position(dup.position() + (int) needed);
                needed = 0;
                result.add(dup);
            }
        }
        return result;
    }

    /**
     * Consumes a single byte from the state's internal cache of buffers.
     * Meant for use by AvroSchema objects (specifically ones that represent primitive types, since complex types
     * are just a combination of primitive types)
     *
     * @return The byte requested.
     */
    public byte read() {
        Iterator<ByteBuffer> iterator = this.cache.iterator();
        ByteBuffer buffer = iterator.next();
        byte b = buffer.get();
        if (buffer.remaining() == 0) {
            iterator.remove();
        }
        this.size--;
        this.sourceOffset++;
        return b;
    }
}
