/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.graphrbac.implementation.ADGroupInner;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of an Azure tenant.
 */
public interface Group extends
        Wrapper<ADGroupInner> {
    /**
     * @return object Id.
     */
    String objectId();

    /**
     * @return object type.
     */
    String objectType();

    /**
     * @return group display name.
     */
    String displayName();

    /**
     * @return security enabled field.
     */
    Boolean securityEnabled();

    /**
     * @return mail field.
     */
    String mail();

    /**************************************************************
     * Fluent interfaces to provision a StorageAccount
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithDisplayName,
            DefinitionStages.WithMailNickname,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the storage account definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the storage account definition.
         */
        interface Blank extends WithDisplayName {
        }

        interface WithDisplayName {
            WithMailNickname withDisplayName(String displayName);
        }

        interface WithMailNickname {
            WithCreate withMailNickname(String mailNickname);
        }

        /**
         * A storage account definition with sufficient inputs to create a new
         * storage account in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                Creatable<Group> {
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
