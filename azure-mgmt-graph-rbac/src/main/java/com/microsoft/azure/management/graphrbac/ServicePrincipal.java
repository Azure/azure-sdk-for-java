/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.graphrbac.implementation.ServicePrincipalInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasId;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;

import java.util.List;

/**
 * An immutable client-side representation of an Azure AD service principal.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
@Beta
public interface ServicePrincipal extends
        Indexable,
        HasInner<ServicePrincipalInner>,
        HasId,
        HasName {
    /**
     * @return object type.
     */
    String objectType();

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
     * Grouping of all the service principal definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the service principal definition.
         */
        interface Blank extends WithApplication {
        }

        interface WithApplication {
            WithCreate withExistingApplication(String id);
            WithCreate withExistingApplication(Application application);
            WithCreate withNewApplication(Creatable<Application> applicationCreatable);
            WithCreate withNewApplication(String signOnUrl);
        }

        interface WithKey {
            Credential.DefinitionStages.Blank<WithCreate> defineKey(String name);
        }

        /**
         * A service principal definition with sufficient inputs to create a new
         * service principal in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                Creatable<ServicePrincipal>,
                WithKey {
        }
    }

    /**
     * Grouping of all the service principal update stages.
     */
    interface UpdateStages {
    }

    /**
     * The template for a service principal update operation, containing all the settings that can be modified.
     */
    interface Update {
    }
}
