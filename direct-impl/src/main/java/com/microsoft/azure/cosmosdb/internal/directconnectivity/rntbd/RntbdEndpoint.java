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

import com.microsoft.azure.cosmosdb.internal.UserAgentContainer;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.StoreResponse;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslContext;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.microsoft.azure.cosmosdb.internal.directconnectivity.RntbdTransportClient.Options;

public interface RntbdEndpoint extends AutoCloseable {

    String getName();

    @Override
    void close() throws RuntimeException;

    CompletableFuture<StoreResponse> request(RntbdRequestArgs requestArgs);

    interface Provider extends AutoCloseable {

        @Override
        void close() throws RuntimeException;

        Config config();

        int count();

        RntbdEndpoint get(URI physicalAddress);

        Stream<RntbdEndpoint> list();
    }

    final class Config {

        private final Options options;
        private final SslContext sslContext;
        private final LogLevel wireLogLevel;

        public Config(final Options options, final SslContext sslContext, final LogLevel wireLogLevel) {

            checkNotNull(options, "options");
            checkNotNull(sslContext, "sslContext");

            this.options = options;
            this.sslContext = sslContext;
            this.wireLogLevel = wireLogLevel;
        }

        public int getConnectionTimeout() {
            final long value = this.options.getConnectionTimeout().toMillis();
            assert value <= Integer.MAX_VALUE;
            return (int)value;
        }

        public int getMaxChannelsPerEndpoint() {
            return this.options.getMaxChannelsPerEndpoint();
        }

        public int getMaxRequestsPerChannel() {
            return this.options.getMaxRequestsPerChannel();
        }

        public long getReceiveHangDetectionTime() {
            return this.options.getReceiveHangDetectionTime().toNanos();
        }

        public long getRequestTimeout() {
            return this.options.getRequestTimeout().toNanos();
        }

        public long getSendHangDetectionTime() {
            return this.options.getSendHangDetectionTime().toNanos();
        }

        public SslContext getSslContext() {
            return this.sslContext;
        }

        public UserAgentContainer getUserAgent() {
            return this.options.getUserAgent();
        }

        public LogLevel getWireLogLevel() {
            return this.wireLogLevel;
        }

        @Override
        public String toString() {
            return RntbdObjectMapper.toJson(this);
        }
    }
}
