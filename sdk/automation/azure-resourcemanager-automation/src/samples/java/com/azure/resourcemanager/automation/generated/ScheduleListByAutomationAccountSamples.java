// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.automation.generated;

/**
 * Samples for Schedule ListByAutomationAccount.
 */
public final class ScheduleListByAutomationAccountSamples {
    /*
     * x-ms-original-file:
     * specification/automation/resource-manager/Microsoft.Automation/preview/2020-01-13-preview/examples/
     * listSchedulesByAutomationAccount_First100.json
     */
    /**
     * Sample code: List schedules by automation account, first 100.
     * 
     * @param manager Entry point to AutomationManager.
     */
    public static void
        listSchedulesByAutomationAccountFirst100(com.azure.resourcemanager.automation.AutomationManager manager) {
        manager.schedules().listByAutomationAccount("rg", "myAutomationAccount33", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/automation/resource-manager/Microsoft.Automation/preview/2020-01-13-preview/examples/
     * listSchedulesByAutomationAccount_Next100.json
     */
    /**
     * Sample code: List schedules by automation account, next 100.
     * 
     * @param manager Entry point to AutomationManager.
     */
    public static void
        listSchedulesByAutomationAccountNext100(com.azure.resourcemanager.automation.AutomationManager manager) {
        manager.schedules().listByAutomationAccount("rg", "myAutomationAccount33", com.azure.core.util.Context.NONE);
    }
}
