/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.graphrbac.implementation.ADGroupInner;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasId;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;

/**
 * An immutable client-side representation of an Azure AD group.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
@Beta
public interface ActiveDirectoryGroup extends
        Indexable,
        HasId,
        HasName,
        HasInner<ADGroupInner>,
        HasManager<GraphRbacManager> {
    /**
     * @return security enabled field.
     */
    boolean securityEnabled();

    /**
     * @return mail field.
     */
    String mail();

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithEmailAlias,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the AD group definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the AD group definition.
         */
        interface Blank extends WithEmailAlias {
        }

        /**
         * An AD Group definition allowing mail nickname to be specified.
         */
        interface WithEmailAlias {
            WithCreate withEmailAlias(String mailNickname);
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
}
