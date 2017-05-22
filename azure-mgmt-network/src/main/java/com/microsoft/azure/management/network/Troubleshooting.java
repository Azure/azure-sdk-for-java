/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.TroubleshootingResultInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasParent;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * Client-side representation of troubleshooting configuration, associated with network watcher and an Azure resource.
 */
@Fluent
@Beta
public interface Troubleshooting extends
        HasParent<NetworkWatcher>,
        HasInner<TroubleshootingResultInner> {
}
