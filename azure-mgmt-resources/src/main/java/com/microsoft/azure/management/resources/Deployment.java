/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;

/**
 * An immutable client-side representation of an Azure deployment.
 */
@Fluent
public interface Deployment extends DeploymentBase {
    /**
     * Cancel a currently running template deployment.
     */
    void cancel();

    /**
     * Cancel a currently running template deployment.
     * @param callback the callback on success or failure
     * @return a handle to cancel the request
     */
    ServiceCall<Void> cancelAsync(ServiceCallback<Void> callback);
}
