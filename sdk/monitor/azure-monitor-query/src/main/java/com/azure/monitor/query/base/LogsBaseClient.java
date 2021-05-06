// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.base;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.experimental.http.DynamicRequest;
import com.azure.monitor.query.LogsClientBuilder;

/**
 *
 */
@ServiceClient(builder = LogsClientBuilder.class)
public final class LogsBaseClient {

    /**
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DynamicRequest getLogsMetadata() {
        // TODO (srnagar): pending review
        return new DynamicRequest(null, null);
    }
}
