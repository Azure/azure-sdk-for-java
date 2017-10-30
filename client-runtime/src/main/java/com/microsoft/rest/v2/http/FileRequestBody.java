/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * A HTTP request body that contains a chunk of a file.
 */
public class FileRequestBody implements HttpRequestBody {
    private final FileSegment fileSegment;

    /**
     * Create a new FileHttpRequestBody with the provided file.
     *
     * @param fileSegment the segment of file as the request body
     */
    public FileRequestBody(FileSegment fileSegment) {
        this.fileSegment = fileSegment;
    }

    @Override
    public int contentLength() {
        return fileSegment.length();
    }

    @Override
    public String contentType() {
        return "application/octet-stream";
    }

    @Override
    public InputStream createInputStream() {
        ByteBuf content = ByteBufAllocator.DEFAULT.buffer(fileSegment.length());
        try {
            content.writeBytes(fileSegment.fileChannel(), fileSegment.offset(), fileSegment.length());
        } catch (IOException e) {
            throw new RuntimeException("Unable to read file");
        }
        return new ByteBufInputStream(content);
    }

    /**
     * @return the lazy loaded content of the request, in the format of a file segment.
     */
    public FileSegment content() {
        return fileSegment;
    }
}
