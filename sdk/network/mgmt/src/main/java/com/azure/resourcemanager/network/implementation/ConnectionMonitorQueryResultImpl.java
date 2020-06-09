// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.ConnectionMonitorQueryResult;
import com.azure.resourcemanager.network.ConnectionMonitorSourceStatus;
import com.azure.resourcemanager.network.ConnectionStateSnapshot;
import com.azure.resourcemanager.network.models.ConnectionMonitorQueryResultInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import java.util.Collections;
import java.util.List;

/** Implementation for {@link ConnectionMonitorQueryResult}. */
class ConnectionMonitorQueryResultImpl extends WrapperImpl<ConnectionMonitorQueryResultInner>
    implements ConnectionMonitorQueryResult {
    ConnectionMonitorQueryResultImpl(ConnectionMonitorQueryResultInner innerObject) {
        super(innerObject);
    }

    @Override
    public ConnectionMonitorSourceStatus sourceStatus() {
        return inner().sourceStatus();
    }

    @Override
    public List<ConnectionStateSnapshot> states() {
        return Collections.unmodifiableList(inner().states());
    }
}
