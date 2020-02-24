// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import com.azure.data.cosmos.internal.directconnectivity.ServerProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdConstants.CurrentProtocolVersion;
import static com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdConstants.RntbdContextHeader;
import static com.google.common.base.Preconditions.checkState;

public final class RntbdContext {

    private final UUID activityId;
    private final HttpResponseStatus status;
    private final String clientVersion;
    private final long idleTimeoutInSeconds;
    private final int protocolVersion;
    private final ServerProperties serverProperties;
    private final long unauthenticatedTimeoutInSeconds;

    private RntbdContext(final RntbdResponseStatus responseStatus, final Headers headers) {

        this.activityId = responseStatus.getActivityId();
        this.status = responseStatus.getStatus();

        this.clientVersion = headers.clientVersion.getValue(String.class);
        this.idleTimeoutInSeconds = headers.idleTimeoutInSeconds.getValue(Long.class);
        this.protocolVersion = headers.protocolVersion.getValue(Long.class).intValue();
        this.unauthenticatedTimeoutInSeconds = headers.unauthenticatedTimeoutInSeconds.getValue(Long.class);

        this.serverProperties = new ServerProperties(
            headers.serverAgent.getValue(String.class), headers.serverVersion.getValue(String.class)
        );
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

    public static RntbdContext decode(final ByteBuf in) {

        in.markReaderIndex();

        final RntbdResponseStatus responseStatus = RntbdResponseStatus.decode(in);
        final int statusCode = responseStatus.getStatusCode();
        final int headersLength = responseStatus.getHeadersLength();

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

            headers.releaseBuffers();
            throw new RntbdContextException(responseStatus.getStatus(), details, Collections.unmodifiableMap(map));

        } else {
            RntbdContext context = new RntbdContext(responseStatus, headers);
            headers.releaseBuffers();
            return context;
        }
    }

    public void encode(final ByteBuf out) {

        final Headers headers = new Headers(this);
        final int length = RntbdResponseStatus.LENGTH + headers.computeLength();
        final RntbdResponseStatus responseStatus = new RntbdResponseStatus(length, this.status(), this.activityId());

        final int start = out.writerIndex();

        responseStatus.encode(out);
        headers.encode(out);
        headers.releaseBuffers();

        final int end = out.writerIndex();

        checkState(end - start == responseStatus.getLength());
    }

    public static RntbdContext from(final RntbdContextRequest request, final ServerProperties properties, final HttpResponseStatus status) {

        // NOTE TO CODE REVIEWERS
        // ----------------------
        // In its current form this method is meant to enable a limited set of test scenarios. It will be revised as
        // required to support test scenarios as they are developed.

        final Headers headers = new Headers(Unpooled.EMPTY_BUFFER);

        headers.clientVersion.setValue(request.getClientVersion());
        headers.idleTimeoutInSeconds.setValue(0);
        headers.protocolVersion.setValue(CurrentProtocolVersion);
        headers.serverAgent.setValue(properties.getAgent());
        headers.serverVersion.setValue(properties.getVersion());
        headers.unauthenticatedTimeoutInSeconds.setValue(0);

        final int length = RntbdResponseStatus.LENGTH + headers.computeLength();
        final UUID activityId = request.getActivityId();

        final RntbdResponseStatus responseStatus = new RntbdResponseStatus(length, status, activityId);

        return new RntbdContext(responseStatus, headers);
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this);
    }

    private static final class Headers extends RntbdTokenStream<RntbdContextHeader> {

        final RntbdToken clientVersion;
        final RntbdToken idleTimeoutInSeconds;
        final RntbdToken protocolVersion;
        final RntbdToken serverAgent;
        final RntbdToken serverVersion;
        final RntbdToken unauthenticatedTimeoutInSeconds;

        private Headers(final RntbdContext context) {
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
}
