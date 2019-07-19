// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import com.azure.data.cosmos.internal.UserAgentContainer;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslContext;

import java.net.URI;
import java.util.stream.Stream;

import static com.azure.data.cosmos.internal.directconnectivity.RntbdTransportClient.Options;
import static com.google.common.base.Preconditions.checkNotNull;

public interface RntbdEndpoint extends AutoCloseable {

    String getName();

    @Override
    void close() throws RuntimeException;

    RntbdRequestRecord request(RntbdRequestArgs requestArgs);

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
