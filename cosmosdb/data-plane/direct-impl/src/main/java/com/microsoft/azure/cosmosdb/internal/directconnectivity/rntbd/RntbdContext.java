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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.ServerProperties;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.TransportException;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static com.microsoft.azure.cosmosdb.internal.directconnectivity.rntbd.RntbdConstants.CurrentProtocolVersion;
import static com.microsoft.azure.cosmosdb.internal.directconnectivity.rntbd.RntbdConstants.RntbdContextHeader;

public final class RntbdContext {

    private final RntbdResponseStatus frame;
    private final Headers headers;

    private RntbdContext(final RntbdResponseStatus frame, final Headers headers) {

        this.frame = frame;
        this.headers = headers;
    }

    @JsonProperty
    UUID getActivityId() {
        return this.frame.getActivityId();
    }

    @JsonProperty
    String getClientVersion() {
        return this.headers.clientVersion.getValue(String.class);
    }

    @JsonProperty
    long getIdleTimeoutInSeconds() {
        return this.headers.idleTimeoutInSeconds.getValue(Long.class);
    }

    @JsonProperty
    int getProtocolVersion() {
        return this.headers.protocolVersion.getValue(Long.class).intValue();
    }

    @JsonProperty
    ServerProperties getServerProperties() {
        return new ServerProperties(
            this.headers.serverAgent.getValue(String.class),
            this.headers.serverVersion.getValue(String.class)
        );
    }

    String getServerVersion() {
        return this.headers.serverVersion.getValue(String.class);
    }

    @JsonProperty
    int getStatusCode() {
        return this.frame.getStatusCode();
    }

    @JsonProperty
    long getUnauthenticatedTimeoutInSeconds() {
        return this.headers.unauthenticatedTimeoutInSeconds.getValue(Long.class);
    }

    static RntbdContext decode(final ByteBuf in) throws TransportException {

        in.markReaderIndex();

        final RntbdResponseStatus frame = RntbdResponseStatus.decode(in);
        final int statusCode = frame.getStatusCode();
        final int headersLength = frame.getHeadersLength();

        if (statusCode < 200 || statusCode >= 400) {
            if (!RntbdFramer.canDecodePayload(in, in.readerIndex() + headersLength)) {
                in.resetReaderIndex();
                return null;
            }
        }

        final Headers headers = Headers.decode(in.readSlice(headersLength));

        if (statusCode < 200 || statusCode >= 400) {

            final ObjectNode details = RntbdObjectMapper.readTree(in.readSlice(in.readIntLE()));
            final HashMap<String, Object> map = new HashMap<>(4);

            if (headers.clientVersion.isPresent()) {
                map.put("requiredClientVersion", headers.clientVersion.getValue());
            }

            if (headers.protocolVersion.isPresent()) {
                map.put("requiredProtocolVersion", headers.protocolVersion.getValue());
            }

            if (headers.serverAgent.isPresent()) {
                map.put("serverAgent", headers.serverAgent.getValue());
            }

            if (headers.serverVersion.isPresent()) {
                map.put("serverVersion", headers.serverVersion.getValue());
            }

            throw new TransportException(frame.getStatus(), details, Collections.unmodifiableMap(map));
        }

        return new RntbdContext(frame, headers);
    }

    public void encode(final ByteBuf out) {

        final int start = out.writerIndex();

        this.frame.encode(out);
        this.headers.encode(out);

        final int length = out.writerIndex() - start;

        if (length != this.frame.getLength()) {
            throw new IllegalStateException();
        }
    }

    public static RntbdContext from(final RntbdContextRequest request, final ServerProperties properties, final HttpResponseStatus status) {

        // NOTE TO CODE REVIEWERS
        // ----------------------
        // In its current form this method is meant to enable a limited set of test scenarios. It will be revised as
        // required to support test scenarios as they are developed.

        final Headers headers = new Headers();

        headers.clientVersion.setValue(request.getClientVersion());
        headers.idleTimeoutInSeconds.setValue(0);
        headers.protocolVersion.setValue(CurrentProtocolVersion);
        headers.serverAgent.setValue(properties.getAgent());
        headers.serverVersion.setValue(properties.getVersion());
        headers.unauthenticatedTimeoutInSeconds.setValue(0);

        final int length = RntbdResponseStatus.LENGTH + headers.computeLength();
        final UUID activityId = request.getActivityId();

        final RntbdResponseStatus frame = new RntbdResponseStatus(length, status, activityId);

        return new RntbdContext(frame, headers);
    }

    @Override
    public String toString() {
        final ObjectWriter writer = RntbdObjectMapper.writer();
        try {
            return writer.writeValueAsString(this);
        } catch (final JsonProcessingException error) {
            throw new CorruptedFrameException(error);
        }
    }

    private static final class Headers extends RntbdTokenStream<RntbdContextHeader> {

        RntbdToken clientVersion;
        RntbdToken idleTimeoutInSeconds;
        RntbdToken protocolVersion;
        RntbdToken serverAgent;
        RntbdToken serverVersion;
        RntbdToken unauthenticatedTimeoutInSeconds;

        Headers() {

            super(RntbdContextHeader.set, RntbdContextHeader.map);

            this.clientVersion = this.get(RntbdContextHeader.ClientVersion);
            this.idleTimeoutInSeconds = this.get(RntbdContextHeader.IdleTimeoutInSeconds);
            this.protocolVersion = this.get(RntbdContextHeader.ProtocolVersion);
            this.serverAgent = this.get(RntbdContextHeader.ServerAgent);
            this.serverVersion = this.get(RntbdContextHeader.ServerVersion);
            this.unauthenticatedTimeoutInSeconds = this.get(RntbdContextHeader.UnauthenticatedTimeoutInSeconds);
        }

        static Headers decode(final ByteBuf in) {
            final Headers headers = new Headers();
            Headers.decode(in, headers);
            return headers;
        }
    }
}
