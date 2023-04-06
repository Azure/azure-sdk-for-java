// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.TcpServerMock.rntbd;
import com.azure.cosmos.implementation.directconnectivity.ServerProperties;

import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdContext;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdObjectMapper;
import com.azure.cosmos.implementation.guava25.collect.ImmutableMap;
import com.azure.cosmos.implementation.guava25.collect.ImmutableSet;
import com.azure.cosmos.implementation.guava25.collect.Sets;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.EnumSet;
import java.util.UUID;
import java.util.stream.Collector;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkState;

/**
 * Except the constructor method, other methods are copied frm {@link RntbdContext}
 */
public final class ServerRntbdContext {
    private final UUID activityId;
    private final HttpResponseStatus status;
    private final String clientVersion;
    private final long idleTimeoutInSeconds;
    private final int protocolVersion;
    private final ServerProperties serverProperties;
    private final long unauthenticatedTimeoutInSeconds;

    ServerRntbdContext(
        final UUID activityId,
        final HttpResponseStatus status,
        final String clientVersion,
        final long idleTimeoutInSeconds,
        final int protocolVersion,
        final long unauthenticatedTimeoutInSeconds,
        final String serverAgent,
        final String serverVersion) {
        this.activityId = activityId;
        this.status = status;
        this.clientVersion = clientVersion;
        this.idleTimeoutInSeconds = idleTimeoutInSeconds;
        this.protocolVersion = protocolVersion;
        this.unauthenticatedTimeoutInSeconds = unauthenticatedTimeoutInSeconds;
        this.serverProperties = new ServerProperties(serverAgent, serverVersion);

    }

    @JsonProperty
    public UUID activityId() {
        return this.activityId;
    }

    @JsonProperty
    public String clientVersion() {
        return this.clientVersion;
    }

    @JsonProperty
    public long idleTimeoutInSeconds() {
        return this.idleTimeoutInSeconds;
    }

    @JsonProperty
    public int protocolVersion() {
        return this.protocolVersion;
    }

    @JsonProperty
    public ServerProperties serverProperties() {
        return this.serverProperties;
    }

    @JsonIgnore
    public String serverVersion() {
        return this.serverProperties.getVersion();
    }

    @JsonIgnore
    public HttpResponseStatus status() {
        return this.status;
    }

    @JsonProperty
    public int getStatusCode() {
        return this.status.code();
    }

    @JsonProperty
    public long getUnauthenticatedTimeoutInSeconds() {
        return this.unauthenticatedTimeoutInSeconds;
    }

    public void encode(final ByteBuf out) {

        final ServerRntbdContext.Headers headers = new ServerRntbdContext.Headers(this);
        final int length = ServerRntbdResponseStatus.LENGTH + headers.computeLength();
        final ServerRntbdResponseStatus responseStatus = new ServerRntbdResponseStatus(length, this.status(), this.activityId());

        final int start = out.writerIndex();

        responseStatus.encode(out);
        headers.encode(out);
        headers.release();

        final int end = out.writerIndex();

        checkState(end - start == responseStatus.getLength());
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this);
    }

    private static final class Headers extends ServerRntbdTokenStream<RntbdContextHeader> {

        final ServerRntbdToken clientVersion;
        final ServerRntbdToken idleTimeoutInSeconds;
        final ServerRntbdToken protocolVersion;
        final ServerRntbdToken serverAgent;
        final ServerRntbdToken serverVersion;
        final ServerRntbdToken unauthenticatedTimeoutInSeconds;

        private Headers(final ServerRntbdContext context) {
            this(Unpooled.EMPTY_BUFFER);
            this.clientVersion.setValue(context.clientVersion());
            this.idleTimeoutInSeconds.setValue(context.idleTimeoutInSeconds());
            this.protocolVersion.setValue(context.protocolVersion());
            this.serverAgent.setValue(context.serverProperties().getAgent());
            this.serverVersion.setValue(context.serverProperties().getVersion());
            this.unauthenticatedTimeoutInSeconds.setValue(context.unauthenticatedTimeoutInSeconds);
        }

        Headers(final ByteBuf in) {
            super(RntbdContextHeader.set, RntbdContextHeader.map, in);
            this.clientVersion = this.get(RntbdContextHeader.ClientVersion);
            this.idleTimeoutInSeconds = this.get(RntbdContextHeader.IdleTimeoutInSeconds);
            this.protocolVersion = this.get(RntbdContextHeader.ProtocolVersion);
            this.serverAgent = this.get(RntbdContextHeader.ServerAgent);
            this.serverVersion = this.get(RntbdContextHeader.ServerVersion);
            this.unauthenticatedTimeoutInSeconds = this.get(RntbdContextHeader.UnauthenticatedTimeoutInSeconds);
        }

        static Headers decode(final ByteBuf in) {
            final Headers headers = new Headers(in);
            Headers.decode(headers);
            return headers;
        }
    }

    enum RntbdContextHeader implements ServerRntbdConstants.RntbdHeader {

        ProtocolVersion((short) 0x0000, ServerRntbdTokenType.ULong, false),
        ClientVersion((short) 0x0001, ServerRntbdTokenType.SmallString, false),
        ServerAgent((short) 0x0002, ServerRntbdTokenType.SmallString, true),
        ServerVersion((short) 0x0003, ServerRntbdTokenType.SmallString, true),
        IdleTimeoutInSeconds((short) 0x0004, ServerRntbdTokenType.ULong, false),
        UnauthenticatedTimeoutInSeconds((short) 0x0005, ServerRntbdTokenType.ULong, false);

        public static final ImmutableMap<Short, RntbdContextHeader> map;
        public static final ImmutableSet<RntbdContextHeader> set = Sets.immutableEnumSet(EnumSet.allOf(RntbdContextHeader.class));

        static {
            final Collector<RntbdContextHeader, ?, ImmutableMap<Short, RntbdContextHeader>> collector = ImmutableMap.toImmutableMap(RntbdContextHeader::id, h -> h);
            map = set.stream().collect(collector);
        }

        private final short id;
        private final boolean isRequired;
        private final ServerRntbdTokenType type;

        RntbdContextHeader(final short id, final ServerRntbdTokenType type, final boolean isRequired) {
            this.id = id;
            this.type = type;
            this.isRequired = isRequired;
        }

        public boolean isRequired() {
            return this.isRequired;
        }

        public short id() {
            return this.id;
        }

        public ServerRntbdTokenType type() {
            return this.type;
        }
    }
}
