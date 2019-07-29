// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import com.azure.data.cosmos.internal.directconnectivity.ServerProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdConstants.CurrentProtocolVersion;
import static com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdConstants.RntbdContextHeader;
import static com.google.common.base.Preconditions.checkState;

public final class RntbdContext {

    private final RntbdResponseStatus frame;
    private final Headers headers;
    private ServerProperties serverProperties;

    private RntbdContext(final RntbdResponseStatus frame, final Headers headers) {
        this.frame = frame;
        this.headers = headers;
    }

    @JsonProperty
    public UUID getActivityId() {
        return this.frame.getActivityId();
    }

    @JsonProperty
    public String getClientVersion() {
        return this.headers.clientVersion.getValue(String.class);
    }

    @JsonProperty
    public long getIdleTimeoutInSeconds() {
        return this.headers.idleTimeoutInSeconds.getValue(Long.class);
    }

    @JsonProperty
    public int getProtocolVersion() {
        return this.headers.protocolVersion.getValue(Long.class).intValue();
    }

    @JsonProperty
    public ServerProperties getServerProperties() {
        return this.serverProperties == null ? (this.serverProperties = new ServerProperties(
            this.headers.serverAgent.getValue(String.class),
            this.headers.serverVersion.getValue(String.class))
        ) : this.serverProperties;
    }

    @JsonIgnore
    public String getServerVersion() {
        return this.headers.serverVersion.getValue(String.class);
    }

    @JsonProperty
    public int getStatusCode() {
        return this.frame.getStatusCode();
    }

    @JsonProperty
    public long getUnauthenticatedTimeoutInSeconds() {
        return this.headers.unauthenticatedTimeoutInSeconds.getValue(Long.class);
    }

    public static RntbdContext decode(final ByteBuf in) {

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

            throw new RntbdContextException(frame.getStatus(), details, Collections.unmodifiableMap(map));
        }

        return new RntbdContext(frame, headers);
    }

    public void encode(final ByteBuf out) {

        final int start = out.writerIndex();

        this.frame.encode(out);
        this.headers.encode(out);

        final int length = out.writerIndex() - start;
        checkState(length == this.frame.getLength());
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
        return RntbdObjectMapper.toJson(this);
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
