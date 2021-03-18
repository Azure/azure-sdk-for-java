// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.PrivateDnsZoneGroupInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.IndependentChild;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

import java.util.List;

/** An immutable client-side representation of an Azure private DNS zone group. */
public interface PrivateDnsZoneGroup extends
    IndependentChild<NetworkManager>,
    HasInnerModel<PrivateDnsZoneGroupInner>,
    Refreshable<PrivateDnsZoneGroup>,
    Updatable<PrivateDnsZoneGroup.Update> {

    /**
     * @return the provisioning state.
     */
    ProvisioningState provisioningState();

    /**
     * @return the collection of private DNS zone configurations.
     */
    List<PrivateDnsZoneConfig> privateDnsZoneConfigures();

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithZoneConfigure,
        DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the definition.
         */
        interface Blank extends WithZoneConfigure {
        }

        /**
         * The stage of a definition allowing to specify the private DNS zone configure.
         */
        interface WithZoneConfigure {
            /**
             * Specifies the private DNS zone configure.
             *
             * @param name the name of the configure
             * @param privateDnsZoneId the ID of the private DNS zone
             * @return the next stage of the definition
             */
            WithCreate withPrivateDnsZoneConfigure(String name, String privateDnsZoneId);
        }

        /**
         * A definition with sufficient inputs to create a new private dns zone group in the cloud, but
         * exposing additional optional inputs to specify.
         */
        interface WithCreate extends
            WithZoneConfigure,
            Creatable<PrivateDnsZoneGroup> {
        }
    }

    /** The template for update operation, containing all the settings that can be modified. */
    interface Update extends
        UpdateStages.WithZoneConfigure,
        Appliable<PrivateDnsZoneGroup> {
    }

    /**
     * Grouping of all the update stages.
     */
    interface UpdateStages {
        /**
         * The stage of a update allowing to specify the private DNS zone configure.
         */
        interface WithZoneConfigure {
            /**
             * Specifies the private DNS zone configure.
             *
             * @param name the name of the configure
             * @param privateDnsZoneId the ID of the private DNS zone
             * @return the next stage of the update
             */
            Update withPrivateDnsZoneConfigure(String name, String privateDnsZoneId);

            /**
             * Removes the private DNS zone configure.
             *
             * @param name the name of the configure
             * @return the next stage of the update
             */
            Update withoutPrivateDnsZoneConfigure(String name);
        }
    }
}
