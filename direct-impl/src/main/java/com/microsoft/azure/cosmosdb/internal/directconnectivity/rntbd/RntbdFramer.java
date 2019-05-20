/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.microsoft.azure.cosmosdb.internal.directconnectivity.rntbd;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;

import static com.google.common.base.Preconditions.checkNotNull;

final class RntbdFramer {

    private RntbdFramer() {
    }

    static boolean canDecodeHead(final ByteBuf in) throws CorruptedFrameException {

        checkNotNull(in, "in");

        if (in.readableBytes() < RntbdResponseStatus.LENGTH) {
            return false;
        }

        final int start = in.readerIndex();
        final long length = in.getUnsignedIntLE(start);

        if (length > Integer.MAX_VALUE) {
            final String reason = String.format("Head frame length exceeds Integer.MAX_VALUE, %d: %d",
                Integer.MAX_VALUE, length
            );
            throw new CorruptedFrameException(reason);
        }

        if (length < Integer.BYTES) {
            final String reason = String.format("Head frame length is less than size of length field, %d: %d",
                Integer.BYTES, length
            );
            throw new CorruptedFrameException(reason);
        }

        return length <= in.readableBytes();
    }

    static boolean canDecodePayload(final ByteBuf in, final int start) {

        checkNotNull(in, "in");

        final int readerIndex = in.readerIndex();

        if (start < readerIndex) {
            throw new IllegalArgumentException("start < in.readerIndex()");
        }

        final int offset = start - readerIndex;

        if (in.readableBytes() - offset < Integer.BYTES) {
            return false;
        }

        final long length = in.getUnsignedIntLE(start);

        if (length > Integer.MAX_VALUE) {
            final String reason = String.format("Payload frame length exceeds Integer.MAX_VALUE, %d: %d",
                Integer.MAX_VALUE, length
            );
            throw new CorruptedFrameException(reason);
        }

        return offset + Integer.BYTES + length <= in.readableBytes();
    }

    static boolean canDecodePayload(final ByteBuf in) {
        return canDecodePayload(in, in.readerIndex());
    }
}
