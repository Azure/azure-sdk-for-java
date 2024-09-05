// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

/**
 * Entry point for Virtual Network management API in Azure.
 */
@Fluent
public interface Device extends HasInnerModel<DeviceProperties>, ChildResource<VpnSite> {

    /** @return Name of the device Vendor */
    String deviceVendor();

    /** @return Model of the device. */
    String deviceModel();

    /** @return Link speed */
    Integer linkSpeedInMbps();

    /**
     * The entirety of a vpn device definition.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of a vpn device definition stages applicable as part of a vpn site creation. */
    interface DefinitionStages {
        /**
         * The first stage of a vpn device definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }

        /**
         * The stage of the vpn devic definition allowing to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InDefinition<ParentT> {

            /**
             * Specifies the value to which this device vendor applies.
             *
             * @param deviceVendor device vendor
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withDeviceVendor(String deviceVendor);

            /**
             * Specifies the value to which this device model applies.
             *
             * @param deviceModel device model
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withDeviceModel(String deviceModel);

            /**
             * Specifies the value to which this link speed applies.
             *
             * @param linkSpeedInMbps the number of link speed
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withLinkSpeedInMbps(Integer linkSpeedInMbps);
        }
    }

    /**
     * The entirety of a vpn device definition as part of a vpn site update.
     *
     * @param <ParentT> the return type of the final {@link VpnSiteLink.UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of vpn device definition stages applicable as part of a vpn site update. */
    interface UpdateDefinitionStages {
        /** Grouping of a vpn device definition stages applicable as part of a vpn site update. */
        interface Blank<ParentT> extends UpdateDefinitionStages.WithAttach<ParentT> {
        }
        /**
         * The stage of the vpn device allowing to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this update
         */
        interface WithAttach<ParentT> extends Attachable.InUpdate<ParentT> {

            /**
             * Specifies the value to which this device vendor applies.
             *
             * @param deviceVendor device vendor
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withDeviceVendor(String deviceVendor);

            /**
             * Specifies the value to which this device model applies.
             *
             * @param deviceModel device model
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withDeviceModel(String deviceModel);

            /**
             * Specifies the value to which this link speed applies.
             *
             * @param linkSpeedInMbps the number of link speed
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withLinkSpeedInMbps(Integer linkSpeedInMbps);
        }
    }

    /** The entirety of vpn device update as part of a vpn site update. */
    interface Update
        extends Settable<VpnSite.Update> {

        /**
         * Specifies the value to which this device vendor applies.
         *
         * @param deviceVendor device vendor
         * @return the next stage of the update
         */
        Update withDeviceVendor(String deviceVendor);

        /**
         * Specifies the value to which this device model applies.
         *
         * @param deviceModel device model
         * @return the next stage of the update
         */
        Update withDeviceModel(String deviceModel);

        /**
         * Specifies the value to which this link speed applies.
         *
         * @param linkSpeedInMbps the number of link speed
         * @return the next stage of the update
         */
        Update withLinkSpeedInMbps(Integer linkSpeedInMbps);
    }

}
