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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.azure.data.cosmos.directconnectivity.StoreResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.EmptyByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCounted;
import io.netty.util.ResourceLeakDetector;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.data.cosmos.directconnectivity.rntbd.RntbdConstants.RntbdResponseHeader;
import static java.lang.Math.min;

@JsonPropertyOrder({ "frame", "headers", "content" })
final public class RntbdResponse implements ReferenceCounted {

    // region Fields

    @JsonSerialize(using = PayloadSerializer.class)
    @JsonProperty
    final private ByteBuf content;

    @JsonProperty
    final private RntbdResponseStatus frame;

    @JsonProperty
    final private RntbdResponseHeaders headers;

    private AtomicInteger referenceCount = new AtomicInteger();

    // endregion

    public RntbdResponse(UUID activityId, int statusCode, Map<String, String> map, ByteBuf content) {

        this.headers = RntbdResponseHeaders.fromMap(map, content.readableBytes() > 0);
        this.content = content.retain();

        HttpResponseStatus status = HttpResponseStatus.valueOf(statusCode);
        int length = RntbdResponseStatus.LENGTH + headers.computeLength();

        this.frame = new RntbdResponseStatus(length, status, activityId);
    }

    private RntbdResponse(RntbdResponseStatus frame, RntbdResponseHeaders headers, ByteBuf content) {

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

    public InputStreamReader getResponseStreamReader() {
        InputStream istream = new ByteBufInputStream(this.content.retain(), true);
        return new InputStreamReader(istream);
    }

    @JsonIgnore
    public HttpResponseStatus getStatus() {
        return this.frame.getStatus();
    }

    static RntbdResponse decode(ByteBuf in) {

        in.markReaderIndex();

        final RntbdResponseStatus frame = RntbdResponseStatus.decode(in);
        final RntbdResponseHeaders headers = RntbdResponseHeaders.decode(in.readSlice(frame.getHeadersLength()));

        final boolean hasPayload = headers.isPayloadPresent();
        ByteBuf content;

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

    public void encode(ByteBuf out) {

        int start = out.writerIndex();

        this.frame.encode(out);
        this.headers.encode(out);

        int length = out.writerIndex() - start;

        if (length != this.frame.getLength()) {
            throw new IllegalStateException();
        }

        if (this.hasPayload()) {
            out.writeIntLE(this.content.readableBytes());
            out.writeBytes(this.content);
        } else if (this.content.readableBytes() > 0) {
            throw new IllegalStateException();
        }
    }

    @JsonIgnore
    public <T> T getHeader(RntbdResponseHeader header) {
        T value = (T)this.headers.get(header).getValue();
        return value;
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
    public boolean release(int decrement) {

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
    public ReferenceCounted retain(int increment) {
        this.referenceCount.addAndGet(increment);
        return this;
    }

    StoreResponse toStoreResponse(RntbdContext context) {

        Objects.requireNonNull(context);
        int length = this.content.readableBytes();

        return new StoreResponse(
            this.getStatus().code(),
            this.headers.asList(context, this.getActivityId()),
            length == 0 ? null : this.content.readCharSequence(length, StandardCharsets.UTF_8).toString()
        );
    }

    @Override
    public String toString() {
        ObjectWriter writer = RntbdObjectMapper.writer();
        try {
            return writer.writeValueAsString(this);
        } catch (JsonProcessingException error) {
            throw new CorruptedFrameException(error);
        }
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
    public ReferenceCounted touch(Object hint) {
        return this;
    }

    private static class PayloadSerializer extends StdSerializer<ByteBuf> {

        public PayloadSerializer() {
            super(ByteBuf.class, true);
        }

        @Override
        public void serialize(ByteBuf value, JsonGenerator generator, SerializerProvider provider) throws IOException {

            int length = value.readableBytes();

            generator.writeStartObject();
            generator.writeObjectField("length", length);
            generator.writeObjectField("content", ByteBufUtil.hexDump(value, 0, length));
            generator.writeEndObject();
        }
    }
}
