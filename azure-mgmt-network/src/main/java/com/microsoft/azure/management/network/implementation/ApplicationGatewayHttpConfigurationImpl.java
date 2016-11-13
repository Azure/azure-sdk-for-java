/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayHttpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayCookieBasedAffinity;
import com.microsoft.azure.management.network.ApplicationGatewayProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for ApplicationGatewayBackendHttpConfiguration.
 */
@LangDefinition
class ApplicationGatewayHttpConfigurationImpl
    extends ChildResourceImpl<ApplicationGatewayBackendHttpSettingsInner, ApplicationGatewayImpl, ApplicationGateway>
    implements
        ApplicationGatewayHttpConfiguration,
        ApplicationGatewayHttpConfiguration.Definition<ApplicationGateway.DefinitionStages.WithHttpConfigOrListener>,
        ApplicationGatewayHttpConfiguration.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayHttpConfiguration.Update {

    ApplicationGatewayHttpConfigurationImpl(ApplicationGatewayBackendHttpSettingsInner inner, ApplicationGatewayImpl parent) {
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
        this.parent().withBackendHttpConfiguration(this);
        return this.parent();
    }

    @Override
    public ApplicationGatewayHttpConfigurationImpl withBackendPort(int port) {
        this.inner().withPort(port);
        return this;
    }

    @Override
    public int backendPort() {
        return this.inner().port() != null ? this.inner().port().intValue() : 0;
    }

    @Override
    public ApplicationGatewayProtocol protocol() {
        return this.inner().protocol();
    }

    @Override
    public boolean cookieBasedAffinity() {
        return this.inner().cookieBasedAffinity().equals(ApplicationGatewayCookieBasedAffinity.ENABLED);
    }

    @Override
    public int requestTimeout() {
        return this.inner().requestTimeout() != null ? this.inner().requestTimeout().intValue() : 0;
    }

    // Withers

    @Override
    public ApplicationGatewayHttpConfigurationImpl withCookieBasedAffinity() {
        this.inner().withCookieBasedAffinity(ApplicationGatewayCookieBasedAffinity.ENABLED);
        return this;
    }

    @Override
    public ApplicationGatewayHttpConfigurationImpl withoutCookieBasedAffinity() {
        this.inner().withCookieBasedAffinity(ApplicationGatewayCookieBasedAffinity.DISABLED);
        return this;
    }

    @Override
    public ApplicationGatewayHttpConfigurationImpl withProtocol(ApplicationGatewayProtocol protocol) {
        this.inner().withProtocol(protocol);
        return this;
    }

    @Override
    public ApplicationGatewayHttpConfigurationImpl withRequestTimeout(int seconds) {
        this.inner().withRequestTimeout(seconds);
        return this;
    }
}
