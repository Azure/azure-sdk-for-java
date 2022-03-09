// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.ReferenceCounted;
import io.netty.util.ResourceLeakDetector;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdResponseHeader;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkState;

@JsonPropertyOrder({ "messageLength", "referenceCount", "frame", "headers", "content" })
public final class RntbdResponse implements ReferenceCounted {

    // region Fields

    private static final AtomicIntegerFieldUpdater<RntbdResponse> REFERENCE_COUNT =
        AtomicIntegerFieldUpdater.newUpdater(RntbdResponse.class, "referenceCount");

    @JsonSerialize(using = PayloadSerializer.class)
    private final ByteBuf content;

    @JsonProperty
    private final RntbdResponseStatus frame;

    @JsonProperty
    private final RntbdResponseHeaders headers;

    private final ByteBuf message;

    @JsonProperty
    private final int messageLength;

    @JsonProperty
    private volatile int referenceCount;

    // endregion

    // region Constructors

    /**
     * Initializes a new {@link RntbdResponse} instance.
     * <p>
     * This method is provided for testing purposes only. It should not be used in product code.
     *
     * @param activityId an activity ID
     * @param statusCode a response status code.
     * @param map a collection of response headers.
     * @param content a body to be copied to the response.
     */
    public RntbdResponse(
        final UUID activityId,
        final int statusCode,
        final Map<String, String> map,
        final ByteBuf content) {

        this.headers = RntbdResponseHeaders.fromMap(map, content.readableBytes() > 0);
        this.message = Unpooled.EMPTY_BUFFER;
        this.content = content.copy();

        final HttpResponseStatus status = HttpResponseStatus.valueOf(statusCode);
        final int length = RntbdResponseStatus.LENGTH + this.headers.computeLength();

        this.frame = new RntbdResponseStatus(length, status, activityId);
        this.messageLength = length + this.content.writerIndex();
        this.referenceCount = 0;
    }

    private RntbdResponse(
        final ByteBuf message,
        final RntbdResponseStatus frame,
        final RntbdResponseHeaders headers,
        final ByteBuf content) {

        this.message = message;
        this.referenceCount = 0;
        this.frame = frame;
        this.headers = headers;
        this.content = content;
        this.messageLength = message.writerIndex();;
    }

    // endregion

    // region Accessors

    @JsonIgnore
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
    public int getMessageLength() {
        return this.messageLength;
    }

    @JsonIgnore
    public HttpResponseStatus getStatus() {
        return this.frame.getStatus();
    }

    @JsonIgnore
    public Long getTransportRequestId() {
        return this.getHeader(RntbdResponseHeader.TransportRequestID);
    }

    // endregion

    // region Methods

    /**
     * Serializes the current {@link RntbdResponse response} to the given {@link ByteBuf byte buffer}.
     *
     * @param out the output {@link ByteBuf byte buffer}.
     */
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

    /**
     * Returns the value of the given {@link RntbdResponse response} {@link RntbdResponseHeader header}.
     *
     * @param header the {@link RntbdResponse response} {@link RntbdResponseHeader header}.
     * @param <T> the {@link RntbdResponse response} {@link RntbdResponseHeader header} value type.
     *
     * @return the value of the given {@code header}.
     */
    @SuppressWarnings("unchecked")
    public <T> T getHeader(final RntbdResponseHeader header) {
        return (T) this.headers.get(header).getValue();
    }

    /**
     * Returns {@code true} if this {@link RntbdResponse response} has a payload.
     *
     * @return {@code true} if this {@link RntbdResponse response} has a payload; {@code false} otherwise.
     */
    public boolean hasPayload() {
        return this.headers.isPayloadPresent();
    }

    /**
     * Returns the reference count of this object.  If {@code 0}, it means this object has been deallocated.
     */
    @Override
    public int refCnt() {
        return this.referenceCount;
    }

    /**
     * Decreases the reference count by {@code 1}.
     * <p>
     * The current {@link RntbdResponse response} is deallocated if the count reaches {@code 0}.
     *
     * @return {@code true} if and only if the reference count became {@code 0} and this response is deallocated.
     */
    @Override
    public boolean release() {
        return this.release(1);
    }

    /**
     * Decreases the reference count by {@code decrement} and deallocates this response if the count reaches {@code 0}.
     *
     * @param decrement amount of the decrease.
     *
     * @return {@code true} if and only if the reference count became {@code 0} and this response has been deallocated.
     */
    @Override
    public boolean release(final int decrement) {

        checkArgument(decrement > 0, "expected decrement, not %s", decrement);

        return REFERENCE_COUNT.accumulateAndGet(this, decrement, (referenceCount, decrease) -> {

            if (referenceCount < decrement) {
                throw new IllegalReferenceCountException(referenceCount, -decrease);
            };

            referenceCount = referenceCount - decrease;

            if (referenceCount == 0) {
                this.content.release();
                this.headers.release();
                    this.message.release();
                }

            return referenceCount;

        }) == 0;
    }

    /**
     * Increases the reference count by {@code 1}.
     */
    @Override
    public RntbdResponse retain() {
        return this.retain(1);
    }

    /**
     * Increases the reference count by the specified {@code increment}.
     *
     * @param increment amount of the increase
     */
    @Override
    public RntbdResponse retain(final int increment) {

        checkArgument(increment > 0, "expected positive increment, not %s", increment);

        REFERENCE_COUNT.accumulateAndGet(this, increment, (referenceCount, increase) -> {
            if (referenceCount == 0) {
                this.content.retain();
                this.headers.retain();
                this.message.retain();
            }
            return referenceCount + increase;
        });

        return this;
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this);
    }

    /**
     * Records the current access location of this object for debugging purposes
     * <p>
     * If this object is determined to be leaked, the information recorded by this operation will be provided to you via
     * {@link ResourceLeakDetector}.  This method is a shortcut to {@link #touch(Object) touch(null)}.
     */
    @Override
    public RntbdResponse touch() {
        return this;
    }

    /**
     * Records the current access location of this object with additional arbitrary information for debugging purposes
     * <p>
     * If this object is determined to be leaked, the information recorded by this operation will be provided to you via
     * {@link ResourceLeakDetector}.
     *
     * @param hint information useful for debugging (unused)
     */
    @Override
    public RntbdResponse touch(final Object hint) {
        return this;
    }

    static RntbdResponse decode(final ByteBuf in) {

        final int start = in.markReaderIndex().readerIndex();

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

            content = Unpooled.EMPTY_BUFFER;
        }

        final int end = in.readerIndex();
        in.resetReaderIndex();

        return new RntbdResponse(in.readSlice(end - start), frame, headers, content);
    }

    StoreResponse toStoreResponse(final RntbdContext context) {

        checkNotNull(context, "expected non-null context");

        final int length = this.content.writerIndex();
        final byte[] content;

        if (length == 0) {
            content = null;
        } else {
            content = new byte[length];
            this.content.getBytes(0, content);
        }

        return new StoreResponse(this.getStatus().code(), this.headers.asList(context, this.getActivityId()), content);
    }

    // endregion

    // region Types

    private static class PayloadSerializer extends StdSerializer<ByteBuf> {

        private static final long serialVersionUID = 1717212953958644366L;

        PayloadSerializer() {
            super(ByteBuf.class, true);
        }

        @Override
        public void serialize(
            final ByteBuf value,
            final JsonGenerator generator,
            final SerializerProvider provider) throws IOException {

            final int length = value.readableBytes();

            generator.writeStartObject();
            generator.writeObjectField("lengthInBytes", length);
            generator.writeObjectField("hexDump", ByteBufUtil.hexDump(value, 0, length));
            generator.writeObjectField("string", value.getCharSequence(0, length, StandardCharsets.UTF_8));
            generator.writeEndObject();
        }
    }

    // endregion
}
