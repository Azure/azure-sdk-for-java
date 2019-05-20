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

import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public final class RntbdRequest {

    private static final byte[] EmptyByteArray = {};

    private final RntbdRequestFrame frame;
    private final RntbdRequestHeaders headers;
    private final byte[] payload;

    private RntbdRequest(final RntbdRequestFrame frame, final RntbdRequestHeaders headers, final byte[] payload) {

        checkNotNull(frame, "frame");
        checkNotNull(headers, "headers");

        this.frame = frame;
        this.headers = headers;
        this.payload = payload == null ? EmptyByteArray : payload;
    }

    public UUID getActivityId() {
        return this.frame.getActivityId();
    }

    static RntbdRequest decode(final ByteBuf in) {

        final int resourceOperationCode = in.getInt(in.readerIndex() + Integer.BYTES);

        if (resourceOperationCode == 0) {
            final String reason = String.format("resourceOperationCode=0x%08X", resourceOperationCode);
            throw new IllegalStateException(reason);
        }

        final int start = in.readerIndex();
        final int expectedLength = in.readIntLE();

        final RntbdRequestFrame header = RntbdRequestFrame.decode(in);
        final RntbdRequestHeaders metadata = RntbdRequestHeaders.decode(in);
        final ByteBuf payloadBuf = in.readSlice(expectedLength - (in.readerIndex() - start));

        final int observedLength = in.readerIndex() - start;

        if (observedLength != expectedLength) {
            final String reason = String.format("expectedLength=%d, observedLength=%d", expectedLength, observedLength);
            throw new IllegalStateException(reason);
        }

        final byte[] payload = new byte[payloadBuf.readableBytes()];
        payloadBuf.readBytes(payload);
        in.discardReadBytes();

        return new RntbdRequest(header, metadata, payload);
    }

    void encode(final ByteBuf out) {

        final int expectedLength = RntbdRequestFrame.LENGTH + this.headers.computeLength();
        final int start = out.readerIndex();

        out.writeIntLE(expectedLength);
        this.frame.encode(out);
        this.headers.encode(out);

        assert out.writerIndex() - start == expectedLength;

        if (this.payload.length > 0) {
            out.writeIntLE(this.payload.length);
            out.writeBytes(this.payload);
        }
    }

    public static RntbdRequest from(final RntbdRequestArgs args) {

        final RxDocumentServiceRequest serviceRequest = args.getServiceRequest();

        final RntbdRequestFrame frame = new RntbdRequestFrame(
            args.getActivityId(),
            serviceRequest.getOperationType(),
            serviceRequest.getResourceType());

        final RntbdRequestHeaders headers = new RntbdRequestHeaders(args, frame);

        return new RntbdRequest(frame, headers, serviceRequest.getContent());
    }
}
