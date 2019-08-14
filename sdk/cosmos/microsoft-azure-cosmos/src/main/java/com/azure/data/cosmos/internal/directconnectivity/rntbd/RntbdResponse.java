// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import com.azure.data.cosmos.internal.directconnectivity.StoreResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.EmptyByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCounted;
import io.netty.util.ResourceLeakDetector;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdConstants.RntbdResponseHeader;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Math.min;

@JsonPropertyOrder({ "frame", "headers", "content" })
public final class RntbdResponse implements ReferenceCounted {

    // region Fields

    private static final String simpleClassName = RntbdResponse.class.getSimpleName();

    @JsonProperty
    @JsonSerialize(using = PayloadSerializer.class)
    private final ByteBuf content;

    @JsonProperty
    private final RntbdResponseStatus frame;

    @JsonProperty
    private final RntbdResponseHeaders headers;

    private AtomicInteger referenceCount = new AtomicInteger();

    // endregion

    public RntbdResponse(final UUID activityId, final int statusCode, final Map<String, String> map, final ByteBuf content) {

        this.headers = RntbdResponseHeaders.fromMap(map, content.readableBytes() > 0);
        this.content = content.retain();

        final HttpResponseStatus status = HttpResponseStatus.valueOf(statusCode);
        final int length = RntbdResponseStatus.LENGTH + this.headers.computeLength();

        this.frame = new RntbdResponseStatus(length, status, activityId);
    }

    private RntbdResponse(final RntbdResponseStatus frame, final RntbdResponseHeaders headers, final ByteBuf content) {

        this.frame = frame;
        this.headers = headers;
        this.content = content.retain();
    }

    public UUID getActivityId() {
        return this.frame.getActivityId();
    }

    @JsonIgnore
    public ByteBuf getContent() {
        return this.content;
    }

    @JsonIgnore
    public RntbdResponseHeaders getHeaders() {
        return this.headers;
    }

    @JsonIgnore
    public HttpResponseStatus getStatus() {
        return this.frame.getStatus();
    }

    @JsonIgnore
    public Long getTransportRequestId() {
        return this.getHeader(RntbdResponseHeader.TransportRequestID);
    }

    static RntbdResponse decode(final ByteBuf in) {

        in.markReaderIndex();

        final RntbdResponseStatus frame = RntbdResponseStatus.decode(in);
        final RntbdResponseHeaders headers = RntbdResponseHeaders.decode(in.readSlice(frame.getHeadersLength()));

        final boolean hasPayload = headers.isPayloadPresent();
        final ByteBuf content;

        if (hasPayload) {

            if (!RntbdFramer.canDecodePayload(in)) {
                in.resetReaderIndex();
                return null;
            }

            content = in.readSlice(in.readIntLE());

        } else {

            content = new EmptyByteBuf(in.alloc());
        }

        return new RntbdResponse(frame, headers, content);
    }

    public void encode(final ByteBuf out) {

        final int start = out.writerIndex();

        this.frame.encode(out);
        this.headers.encode(out);

        final int length = out.writerIndex() - start;
        checkState(length == this.frame.getLength());

        if (this.hasPayload()) {
            out.writeIntLE(this.content.readableBytes());
            out.writeBytes(this.content);
        } else if (this.content.readableBytes() > 0) {
            throw new IllegalStateException();
        }
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    public <T> T getHeader(final RntbdResponseHeader header) {
        return (T)this.headers.get(header).getValue();
    }

    public boolean hasPayload() {
        return this.headers.isPayloadPresent();
    }

    /**
     * Returns the reference count of this object.  If {@code 0}, it means this object has been deallocated.
     */
    @Override
    public int refCnt() {
        return this.referenceCount.get();
    }

    /**
     * Decreases the reference count by {@code 1} and deallocate this object if the reference count reaches {@code 0}
     *
     * @return {@code true} if and only if the reference count became {@code 0} and this object is de-allocated
     */
    @Override
    public boolean release() {
        return this.release(1);
    }

    /**
     * Decreases the reference count by {@code decrement} and de-allocates this object if the reference count reaches {@code 0}
     *
     * @param decrement amount of the decrease
     * @return {@code true} if and only if the reference count became {@code 0} and this object has been de-allocated
     */
    @Override
    public boolean release(final int decrement) {

        return this.referenceCount.getAndAccumulate(decrement, (value, n) -> {
            value = value - min(value, n);
            if (value == 0) {
                assert this.headers != null && this.content != null;
                this.headers.releaseBuffers();
                this.content.release();
            }
            return value;
        }) == 0;
    }

    /**
     * Increases the reference count by {@code 1}.
     */
    @Override
    public ReferenceCounted retain() {
        this.referenceCount.incrementAndGet();
        return this;
    }

    /**
     * Increases the reference count by the specified {@code increment}.
     *
     * @param increment amount of the increase
     */
    @Override
    public ReferenceCounted retain(final int increment) {
        this.referenceCount.addAndGet(increment);
        return this;
    }

    StoreResponse toStoreResponse(final RntbdContext context) {

        checkNotNull(context, "context");
        final int length = this.content.readableBytes();

        return new StoreResponse(
            this.getStatus().code(),
            this.headers.asList(context, this.getActivityId()),
            length == 0 ? null : this.content.readCharSequence(length, StandardCharsets.UTF_8).toString()
        );
    }

    @Override
    public String toString() {
        return simpleClassName + '(' + RntbdObjectMapper.toJson(this) + ')';
    }

    /**
     * Records the current access location of this object for debugging purposes
     * <p>
     * If this object is determined to be leaked, the information recorded by this operation will be provided to you
     * via {@link ResourceLeakDetector}.  This method is a shortcut to {@link #touch(Object) touch(null)}.
     */
    @Override
    public ReferenceCounted touch() {
        return this;
    }

    /**
     * Records the current access location of this object with additional arbitrary information for debugging purposes
     * <p>
     * If this object is determined to be leaked, the information recorded by this operation will be
     * provided to you via {@link ResourceLeakDetector}.
     *
     * @param hint information useful for debugging (unused)
     */
    @Override
    public ReferenceCounted touch(final Object hint) {
        return this;
    }

    private static class PayloadSerializer extends StdSerializer<ByteBuf> {

        public PayloadSerializer() {
            super(ByteBuf.class, true);
        }

        @Override
        public void serialize(final ByteBuf value, final JsonGenerator generator, final SerializerProvider provider) throws IOException {

            final int length = value.readableBytes();

            generator.writeStartObject();
            generator.writeObjectField("length", length);
            generator.writeObjectField("content", ByteBufUtil.hexDump(value, 0, length));
            generator.writeEndObject();
        }
    }
}
