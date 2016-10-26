/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayBackend;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for ApplicationGatewayBackend.
 */
@LangDefinition
class ApplicationGatewayBackendImpl
    extends ChildResourceImpl<ApplicationGatewayBackendAddressPoolInner, ApplicationGatewayImpl, ApplicationGateway>
    implements
        ApplicationGatewayBackend,
        ApplicationGatewayBackend.Definition<ApplicationGateway.DefinitionStages.WithBackendOrHttpConfig>,
        ApplicationGatewayBackend.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayBackend.Update {

    ApplicationGatewayBackendImpl(ApplicationGatewayBackendAddressPoolInner inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
    }

    // Getters

    @Override
    public String name() {
        return this.inner().name();
    }

    // Verbs

    @Override
    public ApplicationGatewayImpl attach() {
        this.parent().withBackend(this);
        return this.parent();
    }
}
