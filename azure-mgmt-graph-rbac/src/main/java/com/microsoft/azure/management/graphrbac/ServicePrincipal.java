/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.graphrbac.implementation.ServicePrincipalInner;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.List;

/**
 * An immutable client-side representation of an Azure tenant.
 */
public interface ServicePrincipal extends
        Wrapper<ServicePrincipalInner> {

    /**
     * @return object Id.
     */
    String objectId();

    /**
     * @return object type.
     */
    String objectType();

    /**
     * @return service principal display name.
     */
    String displayName();

    /**
     * @return app id.
     */
    String appId();

    /**
     * @return the list of names.
     */
    List<String> servicePrincipalNames();

    /**************************************************************
     * Fluent interfaces to provision a service principal
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the storage account definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the storage account definition.
         */
        interface Blank extends WithCreate {
        }

        interface WithAccountEnabled {
            WithCreate withAccountEnabled(boolean enabled);
        }

        /**
         * A storage account definition with sufficient inputs to create a new
         * storage account in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                Creatable<ServicePrincipal>,
                WithAccountEnabled {
        }
    }

    /**
     * Grouping of all the storage account update stages.
     */
    interface UpdateStages {
    }

    /**
     * The template for a storage account update operation, containing all the settings that can be modified.
     */
    interface Update {
    }
}
