/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

/**
 * Type representing authorization rule defined for namespace.
 */
@Fluent
@Beta
public interface NamespaceAuthorizationRule extends
        AuthorizationRule<NamespaceAuthorizationRule>,
        Updatable<NamespaceAuthorizationRule.Update> {
    /**
     * @return the name of the parent namespace name
     */
    String namespaceName();

    /**
     * Grouping of Service Bus namespace authorization rule definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of namespace authorization rule definition.
         */
        interface Blank extends AuthorizationRule.DefinitionStages.WithListenOrSendOrManage<WithCreate> {
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends Creatable<NamespaceAuthorizationRule> {
        }
    }

    /**
     * The entirety of the namespace authorization rule definition.
     */
    interface Definition extends
            NamespaceAuthorizationRule.DefinitionStages.Blank,
            NamespaceAuthorizationRule.DefinitionStages.WithCreate {
    }

    /**
     * The entirety of the namespace authorization rule update.
     */
    interface Update extends
            Appliable<NamespaceAuthorizationRule>,
            AuthorizationRule.UpdateStages.WithListenOrSendOrManage<Update> {
    }
}
