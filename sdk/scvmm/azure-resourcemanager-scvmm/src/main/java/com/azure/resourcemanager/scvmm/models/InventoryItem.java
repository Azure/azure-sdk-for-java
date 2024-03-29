// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.scvmm.models;

import com.azure.core.management.SystemData;
import com.azure.core.util.Context;
import com.azure.resourcemanager.scvmm.fluent.models.InventoryItemInner;

/** An immutable client-side representation of InventoryItem. */
public interface InventoryItem {
    /**
     * Gets the id property: Fully qualified resource Id for the resource.
     *
     * @return the id value.
     */
    String id();

    /**
     * Gets the name property: The name of the resource.
     *
     * @return the name value.
     */
    String name();

    /**
     * Gets the type property: The type of the resource.
     *
     * @return the type value.
     */
    String type();

    /**
     * Gets the systemData property: The system data.
     *
     * @return the systemData value.
     */
    SystemData systemData();

    /**
     * Gets the kind property: Metadata used by portal/tooling/etc to render different UX experiences for resources of
     * the same type; e.g. ApiApps are a kind of Microsoft.Web/sites type. If supported, the resource provider must
     * validate and persist this value.
     *
     * @return the kind value.
     */
    String kind();

    /**
     * Gets the managedResourceId property: Gets the tracked resource id corresponding to the inventory resource.
     *
     * @return the managedResourceId value.
     */
    String managedResourceId();

    /**
     * Gets the uuid property: Gets the UUID (which is assigned by VMM) for the inventory item.
     *
     * @return the uuid value.
     */
    String uuid();

    /**
     * Gets the inventoryItemName property: Gets the Managed Object name in VMM for the inventory item.
     *
     * @return the inventoryItemName value.
     */
    String inventoryItemName();

    /**
     * Gets the provisioningState property: Gets the provisioning state.
     *
     * @return the provisioningState value.
     */
    String provisioningState();

    /**
     * Gets the inner com.azure.resourcemanager.scvmm.fluent.models.InventoryItemInner object.
     *
     * @return the inner object.
     */
    InventoryItemInner innerModel();

    /** The entirety of the InventoryItem definition. */
    interface Definition
        extends DefinitionStages.Blank, DefinitionStages.WithParentResource, DefinitionStages.WithCreate {
    }
    /** The InventoryItem definition stages. */
    interface DefinitionStages {
        /** The first stage of the InventoryItem definition. */
        interface Blank extends WithParentResource {
        }
        /** The stage of the InventoryItem definition allowing to specify parent resource. */
        interface WithParentResource {
            /**
             * Specifies resourceGroupName, vmmServerName.
             *
             * @param resourceGroupName The name of the resource group.
             * @param vmmServerName Name of the VMMServer.
             * @return the next definition stage.
             */
            WithCreate withExistingVmmServer(String resourceGroupName, String vmmServerName);
        }
        /**
         * The stage of the InventoryItem definition which contains all the minimum required properties for the resource
         * to be created, but also allows for any other optional properties to be specified.
         */
        interface WithCreate extends DefinitionStages.WithKind {
            /**
             * Executes the create request.
             *
             * @return the created resource.
             */
            InventoryItem create();

            /**
             * Executes the create request.
             *
             * @param context The context to associate with this operation.
             * @return the created resource.
             */
            InventoryItem create(Context context);
        }
        /** The stage of the InventoryItem definition allowing to specify kind. */
        interface WithKind {
            /**
             * Specifies the kind property: Metadata used by portal/tooling/etc to render different UX experiences for
             * resources of the same type; e.g. ApiApps are a kind of Microsoft.Web/sites type. If supported, the
             * resource provider must validate and persist this value..
             *
             * @param kind Metadata used by portal/tooling/etc to render different UX experiences for resources of the
             *     same type; e.g. ApiApps are a kind of Microsoft.Web/sites type. If supported, the resource provider
             *     must validate and persist this value.
             * @return the next definition stage.
             */
            WithCreate withKind(String kind);
        }
    }
    /**
     * Refreshes the resource to sync with Azure.
     *
     * @return the refreshed resource.
     */
    InventoryItem refresh();

    /**
     * Refreshes the resource to sync with Azure.
     *
     * @param context The context to associate with this operation.
     * @return the refreshed resource.
     */
    InventoryItem refresh(Context context);
}
