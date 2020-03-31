/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;


import com.azure.core.annotation.Fluent;
import com.azure.management.network.models.ConnectionMonitorQueryResultInner;
import com.azure.management.resources.fluentcore.model.HasInner;

import java.util.List;

/**
 * List of connection states snaphots.
 */
@Fluent
public interface ConnectionMonitorQueryResult extends HasInner<ConnectionMonitorQueryResultInner> {
    /**
     * @return status of connection monitor source
     */
    ConnectionMonitorSourceStatus sourceStatus();

    /**
     * @return information about connection states
     */
    List<ConnectionStateSnapshot> states();
}
