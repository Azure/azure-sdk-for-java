// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.http.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TracingInfoTest {

    @Test
    public void getValueTest() {
        TracingInfo tracingInfo = new TracingInfo(false, false, 0);
        assertEquals("RequestType=Startup", tracingInfo.getValue(false));
        assertEquals("RequestType=Watch", tracingInfo.getValue(true));

        tracingInfo = new TracingInfo(true, false, 0);
        assertEquals("RequestType=Startup,Env=Dev", tracingInfo.getValue(false));

        tracingInfo = new TracingInfo(false, true, 0);
        assertEquals("RequestType=Startup,UsesKeyVault", tracingInfo.getValue(false));

        tracingInfo = new TracingInfo(false, false, 1);
        assertEquals("RequestType=Startup,ReplicaCount=1", tracingInfo.getValue(false));

        tracingInfo = new TracingInfo(false, false, 0);
        tracingInfo.getFeatureFlagTracing().updateFeatureFilterTelemetry("Random");
        assertEquals("RequestType=Startup,Filter=CSTM", tracingInfo.getValue(false));
    }

}
