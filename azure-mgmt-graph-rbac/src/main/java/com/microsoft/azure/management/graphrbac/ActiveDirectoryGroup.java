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
 * An immutable client-side representation of an Azure AD group.
 */
public interface ActiveDirectoryGroup extends
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
     * Fluent interfaces to provision a Group
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
     * Grouping of all the group definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the group definition.
         */
        interface Blank extends WithDisplayName {
        }

        /**
         * The stage of group definition allowing display name to be specified.
         */
        interface WithDisplayName {
            /**
             * Specifies the display name of the group.
             *
             * @param displayName the human readable display name
             * @return the next stage of group definition
             */
            WithMailNickname withDisplayName(String displayName);
        }

        /**
         * The stage of group definition allowing mail nickname to be specified.
         */
        interface WithMailNickname {
            /**
             * Specifies the mail nickname of the group.
             *
             * @param mailNickname the mail nickname for the group
             * @return the next stage of group definition
             */
            WithCreate withMailNickname(String mailNickname);
        }

        /**
         * An AD group definition with sufficient inputs to create a new
         * group in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                Creatable<ActiveDirectoryGroup> {
        }
    }

    /**
     * Grouping of all the group update stages.
     */
    interface UpdateStages {
    }

    /**
     * The template for a group update operation, containing all the settings that can be modified.
     */
    interface Update {
    }
}
