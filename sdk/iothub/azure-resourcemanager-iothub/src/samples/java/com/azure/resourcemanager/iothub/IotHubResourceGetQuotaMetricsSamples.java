// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.iothub;

import com.azure.core.util.Context;

/** Samples for IotHubResource GetQuotaMetrics. */
public final class IotHubResourceGetQuotaMetricsSamples {
    /**
     * Sample code: IotHubResource_GetQuotaMetrics.
     *
     * @param iotHubManager Entry point to IotHubManager. Use this API to manage the IoT hubs in your Azure
     *     subscription.
     */
    public static void iotHubResourceGetQuotaMetrics(com.azure.resourcemanager.iothub.IotHubManager iotHubManager) {
        iotHubManager.iotHubResources().getQuotaMetrics("myResourceGroup", "testHub", Context.NONE);
    }
}
