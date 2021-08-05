// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.TcpServerMock.rntbd;

import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdUUID;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

/**
 * Methods included in this class are copied from com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestFrame.
 */
final class ServerRntbdRequestFrame {
    // region Fields

    private final UUID activityId;
    private final ServerRntbdConstants.RntbdOperationType operationType;
    private final ServerRntbdConstants.RntbdResourceType resourceType;

    // region Constructors

    ServerRntbdRequestFrame(final UUID activityId, final ServerRntbdConstants.RntbdOperationType operationType, final ServerRntbdConstants.RntbdResourceType resourceType) {
        this.activityId = activityId;
        this.operationType = operationType;
        this.resourceType = resourceType;
    }

    // endregion

    // region Methods

    UUID getActivityId() {
        return this.activityId;
    }

    ServerRntbdConstants.RntbdOperationType getOperationType() {
        return this.operationType;
    }

    static ServerRntbdRequestFrame decode(final ByteBuf in) {

        final ServerRntbdConstants.RntbdResourceType resourceType = ServerRntbdConstants.RntbdResourceType.fromId(in.readShortLE());
        final ServerRntbdConstants.RntbdOperationType operationType = ServerRntbdConstants.RntbdOperationType.fromId(in.readShortLE());
        final UUID activityId = RntbdUUID.decode(in);

        return new ServerRntbdRequestFrame(activityId, operationType, resourceType);
    }

    // endregion
}
