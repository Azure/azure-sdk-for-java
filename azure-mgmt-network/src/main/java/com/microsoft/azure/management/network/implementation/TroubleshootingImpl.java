/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.NetworkWatcher;
import com.microsoft.azure.management.network.Troubleshooting;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * Implementation for TroubleshootingResult associated with Network Watcher
 * for Virtual Network Gateway or Virtual Network Gateway Connection.
 */
@LangDefinition
public class TroubleshootingImpl extends WrapperImpl<TroubleshootingResultInner>
        implements Troubleshooting {
    private final NetworkWatcherImpl parent;
    private final String targetResourceId;

    TroubleshootingImpl(NetworkWatcherImpl parent, TroubleshootingResultInner inner, String targetResourceId) {
        super(inner);
        this.parent = parent;
        this.targetResourceId = targetResourceId;
    }

    @Override
    public NetworkWatcher parent() {
        return parent;
    }
}
