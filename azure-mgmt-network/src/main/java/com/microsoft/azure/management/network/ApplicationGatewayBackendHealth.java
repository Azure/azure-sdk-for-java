/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * A client-side representation of the health information of an application gateway backend.
 */
@Fluent
@Beta(SinceVersion.V1_4_0)
public interface ApplicationGatewayBackendHealth extends
    HasInner<ApplicationGatewayBackendHealthPool> {
}
