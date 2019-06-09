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

package com.azure.data.cosmos.directconnectivity.rntbd;

import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import io.netty.buffer.ByteBuf;

import java.util.Objects;
import java.util.UUID;

final public class RntbdRequest {

    final private static byte[] EmptyByteArray = {};

    final private RntbdRequestFrame frame;
    final private RntbdRequestHeaders headers;
    final private byte[] payload;

    private RntbdRequest(RntbdRequestFrame frame, RntbdRequestHeaders headers, byte[] payload) {

        Objects.requireNonNull(frame, "frame");
        Objects.requireNonNull(headers, "headers");

        this.frame = frame;
        this.headers = headers;
        this.payload = payload == null ? EmptyByteArray : payload;
    }

    static RntbdRequest decode(ByteBuf in) {

        int resourceOperationCode = in.getInt(in.readerIndex() + Integer.BYTES);

        if (resourceOperationCode == 0) {
            String reason = String.format("resourceOperationCode=0x%08X", resourceOperationCode);
            throw new IllegalStateException(reason);
        }

        int start = in.readerIndex();
        int expectedLength = in.readIntLE();

        RntbdRequestFrame header = RntbdRequestFrame.decode(in);
        RntbdRequestHeaders metadata = RntbdRequestHeaders.decode(in);
        ByteBuf payloadBuf = in.readSlice(expectedLength - (in.readerIndex() - start));

        int observedLength = in.readerIndex() - start;

        if (observedLength != expectedLength) {
            String reason = String.format("expectedLength=%d, observedLength=%d", expectedLength, observedLength);
            throw new IllegalStateException(reason);
        }

        byte[] payload = new byte[payloadBuf.readableBytes()];
        payloadBuf.readBytes(payload);
        in.discardReadBytes();

        return new RntbdRequest(header, metadata, payload);
    }

    public static RntbdRequest from(RntbdRequestArgs args) {

        RxDocumentServiceRequest serviceRequest = args.getServiceRequest();

        final RntbdRequestFrame frame = new RntbdRequestFrame(
            serviceRequest.getActivityId(),
            serviceRequest.getOperationType(),
            serviceRequest.getResourceType());

        final RntbdRequestHeaders headers = new RntbdRequestHeaders(args, frame);

        return new RntbdRequest(frame, headers, serviceRequest.getContent());
    }

    public UUID getActivityId() {
        return this.frame.getActivityId();
    }

    void encode(ByteBuf out) {

        int expectedLength = RntbdRequestFrame.LENGTH + headers.computeLength();
        int start = out.readerIndex();

        out.writeIntLE(expectedLength);
        this.frame.encode(out);
        this.headers.encode(out);

        assert out.writerIndex() - start == expectedLength;

        if (this.payload.length > 0) {
            out.writeIntLE(this.payload.length);
            out.writeBytes(this.payload);
        }
    }
}
