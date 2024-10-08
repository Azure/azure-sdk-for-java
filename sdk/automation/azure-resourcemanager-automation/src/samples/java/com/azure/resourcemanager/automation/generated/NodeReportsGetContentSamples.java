// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.automation.generated;

/**
 * Samples for NodeReports GetContent.
 */
public final class NodeReportsGetContentSamples {
    /*
     * x-ms-original-file:
     * specification/automation/resource-manager/Microsoft.Automation/preview/2020-01-13-preview/examples/
     * getDscNodeReportContent.json
     */
    /**
     * Sample code: Get content of node.
     * 
     * @param manager Entry point to AutomationManager.
     */
    public static void getContentOfNode(com.azure.resourcemanager.automation.AutomationManager manager) {
        manager.nodeReports()
            .getContentWithResponse("rg", "myAutomationAccount33", "nodeId", "reportId",
                com.azure.core.util.Context.NONE);
    }
}
