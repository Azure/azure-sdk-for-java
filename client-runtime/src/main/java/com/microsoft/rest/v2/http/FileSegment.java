/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * A HTTP request body that contains a chunk of a file.
 */
public class FileSegment {
    private final FileChannel fileChannel;
    private final long offset;
    private final int length;

    /**
     * Create a new FileSegment with the provided file.
     *
     * @param fileChannel the file to send in the request
     * @param offset the starting byte index in the file
     * @param length the length of the bytes to send
     */
    public FileSegment(FileChannel fileChannel, long offset, int length) {
        if (fileChannel == null || !fileChannel.isOpen()) {
            throw new IllegalArgumentException("File channel is null or closed.");
        }
        try {
            if (offset + length > fileChannel.size()) {
                throw new IndexOutOfBoundsException("Position " + offset + " + length " + length + " but file size " + fileChannel.size());
            }
            this.fileChannel = fileChannel;
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read from file.", e);
        }
        this.offset = offset;
        this.length = length;
    }

    /**
     * @return the length of the data to read from the file.
     */
    public int length() {
        return length;
    }

    /**
     * @return the offset from the beginning of the file.
     */
    public long offset() {
        return offset;
    }

    /**
     * @return the channel to the file.
     */
    public FileChannel fileChannel() {
        return fileChannel;
    }
}
