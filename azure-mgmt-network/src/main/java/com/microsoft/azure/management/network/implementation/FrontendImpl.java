/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.network.InternetFrontend;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for {@link InternetFrontend}.
 */
class FrontendImpl
    extends ChildResourceImpl<FrontendIPConfigurationInner, LoadBalancerImpl>
    implements
        InternetFrontend,
        InternetFrontend.Definition<LoadBalancer.DefinitionStages.WithInternetFrontendOrBackend>,
        InternetFrontend.UpdateDefinition<LoadBalancer.Update>,
        InternetFrontend.Update {

    protected FrontendImpl(FrontendIPConfigurationInner inner, LoadBalancerImpl parent) {
        super(inner, parent);
    }

    // Getters

    @Override
    public String name() {
        return this.inner().name();
    }

    // Fluent setters

    @Override
    public FrontendImpl withExistingPublicIpAddress(PublicIpAddress pip) {
        return this.withExistingPublicIpAddress(pip.id());
    }

    @Override
    public FrontendImpl withExistingPublicIpAddress(String resourceId) {
        SubResource pipRef = new SubResource().withId(resourceId);
        this.inner().withPublicIPAddress(pipRef);
        return this;
    }

    // Verbs

    @Override
    public LoadBalancerImpl attach() {
        return this.parent().withFrontend(this);
    }
}
