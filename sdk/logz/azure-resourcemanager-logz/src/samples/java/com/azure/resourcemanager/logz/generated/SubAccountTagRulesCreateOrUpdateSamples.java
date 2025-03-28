// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.logz.generated;

/**
 * Samples for SubAccountTagRules CreateOrUpdate.
 */
public final class SubAccountTagRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/logz/resource-manager/Microsoft.Logz/stable/2020-10-01/examples/SubAccountTagRules_CreateOrUpdate.
     * json
     */
    /**
     * Sample code: SubAccountTagRules_CreateOrUpdate.
     * 
     * @param manager Entry point to LogzManager.
     */
    public static void subAccountTagRulesCreateOrUpdate(com.azure.resourcemanager.logz.LogzManager manager) {
        manager.subAccountTagRules()
            .createOrUpdateWithResponse("myResourceGroup", "myMonitor", "SubAccount1", "default", null,
                com.azure.core.util.Context.NONE);
    }
}
