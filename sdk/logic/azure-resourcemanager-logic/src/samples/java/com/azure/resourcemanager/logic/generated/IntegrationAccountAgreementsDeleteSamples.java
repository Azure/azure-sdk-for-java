// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.logic.generated;

/**
 * Samples for IntegrationAccountAgreements Delete.
 */
public final class IntegrationAccountAgreementsDeleteSamples {
    /*
     * x-ms-original-file: specification/logic/resource-manager/Microsoft.Logic/stable/2019-05-01/examples/
     * IntegrationAccountAgreements_Delete.json
     */
    /**
     * Sample code: Delete an agreement.
     * 
     * @param manager Entry point to LogicManager.
     */
    public static void deleteAnAgreement(com.azure.resourcemanager.logic.LogicManager manager) {
        manager.integrationAccountAgreements()
            .deleteWithResponse("testResourceGroup", "testIntegrationAccount", "testAgreement",
                com.azure.core.util.Context.NONE);
    }
}
