/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayHttpListener;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for ApplicationGatewayHttpListener.
 */
@LangDefinition
class ApplicationGatewayHttpListenerImpl
    extends ChildResourceImpl<ApplicationGatewayHttpListenerInner, ApplicationGatewayImpl, ApplicationGateway>
    implements
        ApplicationGatewayHttpListener,
        ApplicationGatewayHttpListener.Definition<ApplicationGateway.DefinitionStages.WithHttpListenerOrCreate>,
        ApplicationGatewayHttpListener.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayHttpListener.Update {

    ApplicationGatewayHttpListenerImpl(ApplicationGatewayHttpListenerInner inner, ApplicationGatewayImpl parent) {
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
        this.parent().withHttpListener(this);
        return this.parent();
    }
}
