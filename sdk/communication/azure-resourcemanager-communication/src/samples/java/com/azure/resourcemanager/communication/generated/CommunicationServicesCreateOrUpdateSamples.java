// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.communication.generated;

import com.azure.resourcemanager.communication.models.ManagedServiceIdentity;
import com.azure.resourcemanager.communication.models.ManagedServiceIdentityType;

/**
 * Samples for CommunicationServices CreateOrUpdate.
 */
public final class CommunicationServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2023-04-01/examples/
     * communicationServices/createOrUpdate.json
     */
    /**
     * Sample code: Create or update resource.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void createOrUpdateResource(com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.communicationServices()
            .define("MyCommunicationResource")
            .withRegion("Global")
            .withExistingResourceGroup("MyResourceGroup")
            .withDataLocation("United States")
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/communication/resource-manager/Microsoft.Communication/stable/2023-04-01/examples/
     * communicationServices/createOrUpdateWithSystemAssignedIdentity.json
     */
    /**
     * Sample code: Create or update resource with managed identity.
     * 
     * @param manager Entry point to CommunicationManager.
     */
    public static void createOrUpdateResourceWithManagedIdentity(
        com.azure.resourcemanager.communication.CommunicationManager manager) {
        manager.communicationServices()
            .define("MyCommunicationResource")
            .withRegion("Global")
            .withExistingResourceGroup("MyResourceGroup")
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
            .withDataLocation("United States")
            .create();
    }
}
