/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for ApplicationGatewayBackendHttpConfiguration.
 */
@LangDefinition
class ApplicationGatewayBackendHttpConfigurationImpl
    extends ChildResourceImpl<ApplicationGatewayBackendHttpSettingsInner, ApplicationGatewayImpl, ApplicationGateway>
    implements
        ApplicationGatewayBackendHttpConfiguration,
        ApplicationGatewayBackendHttpConfiguration.Definition<ApplicationGateway.DefinitionStages.WithBackendHttpConfigOrListener>,
        ApplicationGatewayBackendHttpConfiguration.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayBackendHttpConfiguration.Update {

    ApplicationGatewayBackendHttpConfigurationImpl(ApplicationGatewayBackendHttpSettingsInner inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
        this.inner().withPort(80);
    }

    // Getters

    @Override
    public String name() {
        return this.inner().name();
    }

    // Verbs

    @Override
    public ApplicationGatewayImpl attach() {
        this.parent().withBackendHttpConfiguration(this);
        return this.parent();
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withBackendPort(int port) {
        this.inner().withPort(port);
        return this;
    }
}
