// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.models.ConnectionMonitorQueryResultInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.util.List;

/** List of connection states snaphots. */
@Fluent
public interface ConnectionMonitorQueryResult extends HasInnerModel<ConnectionMonitorQueryResultInner> {
    /** @return status of connection monitor source */
    ConnectionMonitorSourceStatus sourceStatus();

    /** @return information about connection states */
    List<ConnectionStateSnapshot> states();
}
