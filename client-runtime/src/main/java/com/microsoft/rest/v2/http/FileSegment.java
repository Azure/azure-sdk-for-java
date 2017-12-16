/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import java.nio.channels.FileChannel;

/**
 * A HTTP request body that contains a chunk of a file.
 */
public class FileSegment {
    private final FileChannel fileChannel;
    private final long offset;
    private final long length;

    /**
     * Create a new FileSegment with the provided file.
     *
     * @param fileChannel the file to send in the request
     * @param offset the starting byte index in the file
     * @param length the length of the bytes to send
     */
    public FileSegment(FileChannel fileChannel, long offset, long length) {
        if (fileChannel == null) {
            throw new IllegalArgumentException("file cannot be null");
        }

        this.fileChannel = fileChannel;
        this.offset = offset;
        this.length = length;
    }

    /**
     * @return the length of the data to read from the file.
     */
    public long length() {
        return length;
    }

    /**
     * @return the offset from the beginning of the file.
     */
    public long offset() {
        return offset;
    }

    /**
     * @return the file channel.
     */
    public FileChannel fileChannel() {
        return fileChannel;
    }
}
