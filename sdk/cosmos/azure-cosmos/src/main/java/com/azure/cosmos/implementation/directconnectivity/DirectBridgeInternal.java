// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.RequestTimeline;

/**
 * This is meant to be used only internally as a bridge access to classes in
 * com.azure.cosmos.implementation.directconnectivity
 **/
public class DirectBridgeInternal {

    public static void setCosmosDiagnostics(StoreResponse storeResponse, CosmosDiagnostics cosmosDiagnostics) {
        storeResponse.setCosmosDiagnostics(cosmosDiagnostics);
    }

    public static int getSubStatusCode(StoreResponse storeResponse) {
        return storeResponse.getSubStatusCode();
    }

    public static RequestTimeline getRequestTimeline(StoreResponse storeResponse) {
        return storeResponse.getRequestTimeline();
    }

    public static void setRequestTimeline(StoreResponse storeResponse, RequestTimeline requestTimeline) {
        storeResponse.setRequestTimeline(requestTimeline);
    }
}
