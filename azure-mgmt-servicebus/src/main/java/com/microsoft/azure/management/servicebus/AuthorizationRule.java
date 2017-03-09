/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.servicebus.implementation.ServiceBusManager;
import com.microsoft.azure.management.servicebus.implementation.SharedAccessAuthorizationRuleInner;

import java.util.List;

/**
 * Type representing authorization rule defined for namespace, queue, topics and subscriptions.
 */
@Fluent
public interface AuthorizationRule extends
        IndependentChildResource<ServiceBusManager, SharedAccessAuthorizationRuleInner>,
    Refreshable<AuthorizationRule>,
    Updatable<AuthorizationRule.Update> {

    /**
     * The rights associated with the rule.
     */
    List<AccessRights> rights();

    /**
     * @return the primary and secondary keys
     */
    void listKeys();

    /**
     * Regenerates primary or secondary keys.
     */
    void regenerateKeys();


    /**
     * The entirety of the authorization rule definition.
     */
    interface Definition extends
        AuthorizationRule.DefinitionStages.Blank,
        AuthorizationRule.DefinitionStages.WithGroup,
        AuthorizationRule.DefinitionStages.WithCreate {
    }

    interface DefinitionStages {
        interface WithRights {
            WithCreate withRights(AccessRights rights);
        }

        interface WithCreate extends
            Creatable<AuthorizationRule>,
            AuthorizationRule.DefinitionStages.WithRights {
        }

        interface Blank extends
            GroupableResource.DefinitionWithRegion<WithGroup>{
        }

        interface WithGroup extends
            GroupableResource.DefinitionStages.WithGroup<WithCreate>{
        }
    }

    interface Update extends
        AuthorizationRule.UpdateStages.WithRights {
    }

    interface UpdateStages {
        interface WithRights {
            Update withRights(AccessRights rights);
        }
    }

}
