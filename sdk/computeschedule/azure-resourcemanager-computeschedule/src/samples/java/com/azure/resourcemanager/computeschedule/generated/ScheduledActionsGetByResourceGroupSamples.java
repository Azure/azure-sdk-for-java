// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.computeschedule.generated;

/**
 * Samples for ScheduledActions GetByResourceGroup.
 */
public final class ScheduledActionsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_Get_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        scheduledActionsGetMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .getByResourceGroupWithResponse("rgcomputeschedule", "myScheduledAction", com.azure.core.util.Context.NONE);
    }
}
