// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.guava27.Strings;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdRequestHeader;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class RntbdRequest {
    private final static Logger logger = LoggerFactory.getLogger(RntbdRequest.class);
    private static final byte[] EMPTY_BYTE_ARRAY = {};

    private final RntbdRequestFrame frame;
    private final RntbdRequestHeaders headers;
    private final byte[] payload;

    private RntbdRequest(final RntbdRequestFrame frame, final RntbdRequestHeaders headers, final byte[] payload) {

        checkNotNull(frame, "frame");
        checkNotNull(headers, "headers");

        this.frame = frame;
        this.headers = headers;
        this.payload = payload == null ? EMPTY_BYTE_ARRAY : payload;
    }

    public UUID getActivityId() {
        return this.frame.getActivityId();
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    public <T> T getHeader(final RntbdRequestHeader header) {
        return (T) this.headers.get(header).getValue();
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    public boolean setHeaderValue(final RntbdRequestHeader header, Object value) {
        RntbdToken token = this.headers.get(header);
        if (token == null) {
            return false;
        }

        token.setValue(value);
        return true;
    }

    public Long getTransportRequestId() {
        return this.getHeader(RntbdRequestHeader.TransportRequestID);
    }

    public static RntbdRequest decode(final ByteBuf in) {

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
            final String reason = Strings.lenientFormat("expectedLength=%s, observedLength=%s", expectedLength, observedLength);
            throw new IllegalStateException(reason);
        }

        final byte[] payload = new byte[payloadBuf.readableBytes()];
        payloadBuf.readBytes(payload);
        in.discardReadBytes();

        return new RntbdRequest(header, metadata, payload);
    }

    public RntbdRequestHeaders getHeaders() {
        return this.headers;
    }
    public void encode(final ByteBuf out, boolean forThinClient) {

        final int effectivePayloadSize = this.payload != null && this.payload.length > 0 ? this.payload.length + 4 : 0;
        final int expectedLength = RntbdRequestFrame.LENGTH + this.headers.computeLength(forThinClient);
        final int start = out.writerIndex();

        logger.error("RntbdRequest.encode Start {}, ExpectedLength {} + payload length {}", start, expectedLength, effectivePayloadSize);
        out.writeIntLE(expectedLength);
        this.frame.encode(out);
        logger.error("After frame WriteIndex {}", out.writerIndex());
        this.headers.encode(out, forThinClient);
        logger.error("After headers WriteIndex {}", out.writerIndex());
        final int observedLength = out.writerIndex() - start;

        /*checkState(observedLength == expectedLength,
            "encoding error: {\"expectedLength\": %s, \"observedLength\": %s}",
            expectedLength,
            observedLength);*/

        if (this.payload.length > 0) {
            out.writeIntLE(this.payload.length);
            out.writeBytes(this.payload);
            logger.error("After payload of length {} WriteIndex {}", this.payload.length, out.writerIndex());
        } else {
            logger.error("NO PAYLOAD");
        }


    }

    public static RntbdRequest from(final RntbdRequestArgs args) {

        final RxDocumentServiceRequest serviceRequest = args.serviceRequest();

        final RntbdRequestFrame frame = new RntbdRequestFrame(
            args.activityId(),
            serviceRequest.getOperationType(),
            serviceRequest.getResourceType());

        final RntbdRequestHeaders headers = new RntbdRequestHeaders(args, frame);

        return new RntbdRequest(frame, headers, serviceRequest.getContentAsByteArray());
    }
}
