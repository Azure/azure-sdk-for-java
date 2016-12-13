/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayCookieBasedAffinity;
import com.microsoft.azure.management.network.ApplicationGatewayProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;

/**
 *  Implementation for ApplicationGatewayBackendHttpConfiguration.
 */
@LangDefinition
class ApplicationGatewayBackendHttpConfigurationImpl
    extends ChildResourceImpl<ApplicationGatewayBackendHttpSettingsInner, ApplicationGatewayImpl, ApplicationGateway>
    implements
        ApplicationGatewayBackendHttpConfiguration,
        ApplicationGatewayBackendHttpConfiguration.Definition<ApplicationGateway.DefinitionStages.WithCreate>,
        ApplicationGatewayBackendHttpConfiguration.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayBackendHttpConfiguration.Update {

    ApplicationGatewayBackendHttpConfigurationImpl(ApplicationGatewayBackendHttpSettingsInner inner, ApplicationGatewayImpl parent) {
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
    public ApplicationGatewayBackendHttpConfigurationImpl withPort(int port) {
        this.inner().withPort(port);
        return this;
    }

    @Override
    public int port() {
        return Utils.toPrimitiveInt(this.inner().port());
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
        return Utils.toPrimitiveInt(this.inner().requestTimeout());
    }

    // Withers

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withCookieBasedAffinity() {
        this.inner().withCookieBasedAffinity(ApplicationGatewayCookieBasedAffinity.ENABLED);
        return this;
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withoutCookieBasedAffinity() {
        this.inner().withCookieBasedAffinity(ApplicationGatewayCookieBasedAffinity.DISABLED);
        return this;
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withProtocol(ApplicationGatewayProtocol protocol) {
        this.inner().withProtocol(protocol);
        return this;
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withRequestTimeout(int seconds) {
        this.inner().withRequestTimeout(seconds);
        return this;
    }
}
