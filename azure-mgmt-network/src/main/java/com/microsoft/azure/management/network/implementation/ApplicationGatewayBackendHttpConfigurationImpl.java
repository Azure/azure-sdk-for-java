/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayConnectionDraining;
import com.microsoft.azure.management.network.ApplicationGatewayCookieBasedAffinity;
import com.microsoft.azure.management.network.ApplicationGatewayProbe;
import com.microsoft.azure.management.network.ApplicationGatewayProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
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

    @Override
    public ApplicationGatewayProbe probe() {
        if (this.parent().probes() != null && this.inner().probe() != null) {
            return this.parent().probes().get(ResourceUtils.nameFromResourceId(this.inner().probe().id()));
        } else {
            return null;
        }
    }

    @Override
    public String hostHeader() {
        return this.inner().hostName();
    }

    @Override
    public boolean isHostHeaderFromBackend() {
        return Utils.toPrimitiveBoolean(this.inner().pickHostNameFromBackendAddress());
    }

    @Override
    public boolean isProbeEnabled() {
        return Utils.toPrimitiveBoolean(this.inner().probeEnabled());
    }

    @Override
    public int connectionDrainingTimeoutInSeconds() {
        if (this.inner().connectionDraining() == null) {
            return 0;
        } else if (!this.inner().connectionDraining().enabled()) {
            return 0;
        } else {
            return this.inner().connectionDraining().drainTimeoutInSec();
        }
    }

    @Override
    public String affinityCookieName() {
        return this.inner().affinityCookieName();
    }

    @Override
    public String path() {
        return this.inner().path();
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

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withProbe(String name) {
        if (name == null) {
            return this.withoutProbe();
        } else {
            SubResource probeRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/probes/" + name);
            this.inner().withProbe(probeRef);
            return this;
        }
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withoutProbe() {
        this.inner().withProbe(null);
        return this;
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withHostHeaderFromBackend() {
        this.inner()
            .withPickHostNameFromBackendAddress(true)
            .withHostName(null);
        return this;
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withHostHeader(String hostHeader) {
        this.inner()
            .withHostName(hostHeader)
            .withPickHostNameFromBackendAddress(false);
        return this;
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withoutHostHeader() {
        this.inner()
            .withHostName(null)
            .withPickHostNameFromBackendAddress(false);
        return this;
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withConnectionDrainingTimeoutInSeconds(int seconds) {
        if (this.inner().connectionDraining() == null) {
            this.inner().withConnectionDraining(new ApplicationGatewayConnectionDraining());
        }
        if (seconds > 0) {
            this.inner().connectionDraining().withDrainTimeoutInSec(seconds).withEnabled(true);
        }
        return this;
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withoutConnectionDraining() {
        this.inner().withConnectionDraining(null);
        return this;
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withAffinityCookieName(String name) {
        this.inner().withAffinityCookieName(name);
        return this;
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl withPath(String path) {
        if (path != null) {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            if (!path.endsWith("/")) {
                path += "/";
            }
        }
        this.inner().withPath(path);
        return this;
    }
}
