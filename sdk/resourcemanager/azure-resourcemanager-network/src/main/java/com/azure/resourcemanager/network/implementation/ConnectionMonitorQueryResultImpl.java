// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.ConnectionMonitorQueryResult;
import com.azure.resourcemanager.network.models.ConnectionMonitorSourceStatus;
import com.azure.resourcemanager.network.models.ConnectionStateSnapshot;
import com.azure.resourcemanager.network.fluent.models.ConnectionMonitorQueryResultInner;
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
        return innerModel().sourceStatus();
    }

    @Override
    public List<ConnectionStateSnapshot> states() {
        return Collections.unmodifiableList(innerModel().states());
    }
}
