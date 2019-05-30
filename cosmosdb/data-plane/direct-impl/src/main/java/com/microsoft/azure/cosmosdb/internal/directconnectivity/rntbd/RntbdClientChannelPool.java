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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

@JsonSerialize(using = RntbdClientChannelPool.JsonSerializer.class)
public final class RntbdClientChannelPool extends FixedChannelPool {

    private static final Logger logger = LoggerFactory.getLogger(RntbdClientChannelPool.class);
    private static final AtomicReference<Field> pendingAcquireCount = new AtomicReference<>();

    private final ConcurrentHashMap<ChannelId, Channel> atCapacity;
    private final AtomicInteger availableChannelCount;
    private final AtomicBoolean closed;
    private final int maxRequestsPerChannel;

    /**
     * Creates a new instance using the {@link ChannelHealthChecker#ACTIVE}
     *
     * @param bootstrap the {@link Bootstrap} that is used for connections
     * @param config    the {@link RntbdEndpoint.Config} that is used for the channel pool instance created
     */
    RntbdClientChannelPool(final Bootstrap bootstrap, final RntbdEndpoint.Config config) {

        super(bootstrap, new RntbdClientChannelHandler(config), ChannelHealthChecker.ACTIVE, null,
            -1L, config.getMaxChannelsPerEndpoint(), Integer.MAX_VALUE, true
        );

        this.maxRequestsPerChannel = config.getMaxRequestsPerChannel();
        this.availableChannelCount = new AtomicInteger();
        this.atCapacity = new ConcurrentHashMap<>();
        this.closed = new AtomicBoolean();
    }

    @Override
    public Future<Channel> acquire(Promise<Channel> promise) {
        this.throwIfClosed();
        return super.acquire(promise);
    }

    @Override
    public Future<Void> release(Channel channel, Promise<Void> promise) {
        this.throwIfClosed();
        return super.release(channel, promise);
    }

    @Override
    public void close() {

        if (this.closed.compareAndSet(false, true)) {

            if (!this.atCapacity.isEmpty()) {

                for (Channel channel : this.atCapacity.values()) {
                    super.offerChannel(channel);
                }

                this.atCapacity.clear();
            }

            this.availableChannelCount.set(0);
            super.close();
        }
    }

    public int availableChannelCount() {
        return this.availableChannelCount.get();
    }

    public int pendingAcquireCount() {

        Field field = pendingAcquireCount.get();

        if (field == null) {

            synchronized (pendingAcquireCount) {

                field = pendingAcquireCount.get();

                if (field == null) {
                    field = FieldUtils.getDeclaredField(FixedChannelPool.class, "pendingAcquireCount", true);
                    pendingAcquireCount.set(field);
                }
            }

        }
        try {
            return (int)FieldUtils.readField(field, this);
        } catch (IllegalAccessException error) {
            logger.error("could not access field due to ", error);
        }

        return -1;
    }

    /**
     * Poll a {@link Channel} out of internal storage to reuse it
     * <p>
     * Maintainers: Implementations of this method must be thread-safe.
     *
     * @return a value of {@code null}, if no {@link Channel} is ready to be reused
     */
    @Override
    protected synchronized Channel pollChannel() {

        final Channel channel = super.pollChannel();

        if (channel != null) {
            this.availableChannelCount.decrementAndGet();
            return channel;
        }

        if (this.atCapacity.isEmpty()) {
            return null;
        }

        return this.atCapacity.search(Long.MAX_VALUE, (id, value) -> {
            if (pendingRequestCount(value) < this.maxRequestsPerChannel) {
                this.availableChannelCount.decrementAndGet();
                this.atCapacity.remove(id);
                return value;
            }
            return null;
        });
    }

    /**
     * Offer a {@link Channel} back to the internal storage
     * <p>
     * Maintainers: Implementations of this method must be thread-safe.
     *
     * @param channel the {@link Channel} to return to internal storage
     * @return {@code true}, if the {@link Channel} could be added to internal storage; otherwise {@code false}
     */
    @Override
    protected synchronized boolean offerChannel(final Channel channel) {

        checkArgument(channel.isActive(), "%s inactive", channel);
        final boolean offered;

        if (pendingRequestCount(channel) >= this.maxRequestsPerChannel) {
            this.atCapacity.put(channel.id(), channel);
            offered = true;
        } else {
            offered = super.offerChannel(channel);
        }

        if (offered) {
            this.availableChannelCount.incrementAndGet();
        }

        return offered;
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toJson(this);
    }

    private static int pendingRequestCount(final Channel channel) {
        return channel.pipeline().get(RntbdRequestManager.class).getPendingRequestCount();
    }

    private void throwIfClosed() {
        checkState(!this.closed.get(), "%s is closed", this);
    }

    // region Types

    static final class JsonSerializer extends StdSerializer<RntbdClientChannelPool> {

        public JsonSerializer() {
            this(null);
        }

        public JsonSerializer(Class<RntbdClientChannelPool> type) {
            super(type);
        }

        @Override
        public void serialize(RntbdClientChannelPool value, JsonGenerator generator, SerializerProvider provider) throws IOException {

            generator.writeStartObject();
            generator.writeNumberField("acquiredChannelCount", value.acquiredChannelCount());
            generator.writeNumberField("availableChannelCount", value.availableChannelCount());
            generator.writeNumberField("maxRequestsPerChannel", value.maxRequestsPerChannel);
            generator.writeNumberField("pendingAcquisitionCount", value.pendingAcquireCount());
            generator.writeBooleanField("isClosed", value.closed.get());
            generator.writeEndObject();
        }
    }
}
