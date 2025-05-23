// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.apimanagement.generated;

import com.azure.resourcemanager.apimanagement.models.PolicyExportFormat;
import com.azure.resourcemanager.apimanagement.models.PolicyIdName;

/**
 * Samples for Policy Get.
 */
public final class PolicyGetSamples {
    /*
     * x-ms-original-file:
     * specification/apimanagement/resource-manager/Microsoft.ApiManagement/stable/2024-05-01/examples/
     * ApiManagementGetPolicyFormat.json
     */
    /**
     * Sample code: ApiManagementGetPolicyFormat.
     * 
     * @param manager Entry point to ApiManagementManager.
     */
    public static void
        apiManagementGetPolicyFormat(com.azure.resourcemanager.apimanagement.ApiManagementManager manager) {
        manager.policies()
            .getWithResponse("rg1", "apimService1", PolicyIdName.POLICY, PolicyExportFormat.RAWXML,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/apimanagement/resource-manager/Microsoft.ApiManagement/stable/2024-05-01/examples/
     * ApiManagementGetPolicy.json
     */
    /**
     * Sample code: ApiManagementGetPolicy.
     * 
     * @param manager Entry point to ApiManagementManager.
     */
    public static void apiManagementGetPolicy(com.azure.resourcemanager.apimanagement.ApiManagementManager manager) {
        manager.policies()
            .getWithResponse("rg1", "apimService1", PolicyIdName.POLICY, null, com.azure.core.util.Context.NONE);
    }
}
