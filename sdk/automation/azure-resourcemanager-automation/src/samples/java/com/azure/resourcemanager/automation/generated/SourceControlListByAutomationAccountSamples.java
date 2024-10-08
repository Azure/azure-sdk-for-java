// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.automation.generated;

/**
 * Samples for SourceControl ListByAutomationAccount.
 */
public final class SourceControlListByAutomationAccountSamples {
    /*
     * x-ms-original-file:
     * specification/automation/resource-manager/Microsoft.Automation/preview/2020-01-13-preview/examples/sourceControl/
     * getAllSourceControls.json
     */
    /**
     * Sample code: List sourceControls.
     * 
     * @param manager Entry point to AutomationManager.
     */
    public static void listSourceControls(com.azure.resourcemanager.automation.AutomationManager manager) {
        manager.sourceControls()
            .listByAutomationAccount("rg", "sampleAccount9", null, com.azure.core.util.Context.NONE);
    }
}
