/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.implementation.ServicePrincipalInner;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.List;

/**
 * An immutable client-side representation of an Azure AD service principal.
 */
@LangDefinition
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
    @LangDefinition(ContainerName = "~/ServicePrincipal.Definition", ContainerFileName = "IDefinition")
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the service principal definition stages.
     */
    @LangDefinition(ContainerName = "~/ServicePrincipal.Definition", ContainerFileName = "IDefinition", IsContainerOnly = true)
    interface DefinitionStages {
        /**
         * The first stage of the service principal definition.
         */
        interface Blank extends WithCreate {
        }

        /**
         * The stage of service principal definition allowing specifying if the service principal account is enabled.
         */
        interface WithAccountEnabled {
            /**
             * Specifies whether the service principal account is enabled upon creation.
             *
             * @param enabled if set to true, the service principal account is enabled.
             * @return the next stage in service principal definition
             */
            WithCreate withAccountEnabled(boolean enabled);
        }

        /**
         * A service principal definition with sufficient inputs to create a new
         * service principal in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                Creatable<ServicePrincipal>,
                WithAccountEnabled {
        }
    }

    /**
     * Grouping of all the service principal update stages.
     */
    @LangDefinition(ContainerName = "~/ServicePrincipal.Update", ContainerFileName = "IUpdate", IsContainerOnly = true)
    interface UpdateStages {
    }

    /**
     * The template for a service principal update operation, containing all the settings that can be modified.
     */
    @LangDefinition(ContainerName = "~/ServicePrincipal.Update", ContainerFileName = "IUpdate")
    interface Update {
    }
}
