/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayProbe;
import com.microsoft.azure.management.network.ApplicationGatewayProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for ApplicationGatewayProbe.
 */
@LangDefinition
class ApplicationGatewayProbeImpl
    extends ChildResourceImpl<ApplicationGatewayProbeInner, ApplicationGatewayImpl, ApplicationGateway>
    implements
        ApplicationGatewayProbe,
        ApplicationGatewayProbe.Definition<ApplicationGateway.DefinitionStages.WithCreate>,
        ApplicationGatewayProbe.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayProbe.Update {

    ApplicationGatewayProbeImpl(ApplicationGatewayProbeInner inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
    }

    // Getters

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public ApplicationGatewayProtocol protocol() {
        return this.inner().protocol();
    }

    @Override
    public int timeBetweenProbesInSeconds() {
        return (this.inner().interval() != null) ? this.inner().interval().intValue() : 0;
    }

    @Override
    public String path() {
        return this.inner().path();
    }

    @Override
    public int timeoutInSeconds() {
        return (this.inner().timeout() != null) ? this.inner().timeout().intValue() : 0;
    }

    @Override
    public int retriesBeforeUnhealthy() {
        return (this.inner().unhealthyThreshold() != null) ? this.inner().unhealthyThreshold() : 0;
    }

    @Override
    public String host() {
        return this.inner().host();
    }

    // Fluent setters

    @Override
    public ApplicationGatewayProbeImpl withProtocol(ApplicationGatewayProtocol protocol) {
        this.inner().withProtocol(protocol);
        return this;
    }

    @Override
    public ApplicationGatewayProbeImpl withHttp() {
        return this.withProtocol(ApplicationGatewayProtocol.HTTP);
    }

    @Override
    public ApplicationGatewayProbeImpl withHttps() {
        return this.withProtocol(ApplicationGatewayProtocol.HTTPS);
    }

    @Override
    public ApplicationGatewayProbeImpl withPath(String path) {
        if (path != null && !path.startsWith("/")) {
            path = "/" + path;
        }
        this.inner().withPath(path);
        return this;
    }

    @Override
    public ApplicationGatewayProbeImpl withHost(String host) {
        this.inner().withHost(host);
        return this;
    }

    @Override
    public ApplicationGatewayProbeImpl withTimeoutInSeconds(int seconds) {
        this.inner().withTimeout(seconds);
        return this;
    }

    @Override
    public ApplicationGatewayProbeImpl withTimeBetweenProbesInSeconds(int seconds) {
        this.inner().withInterval(seconds);
        return this;
    }

    @Override
    public ApplicationGatewayProbeImpl withRetriesBeforeUnhealthy(int retryCount) {
        this.inner().withUnhealthyThreshold(retryCount);
        return this;
    }

    // Verbs

    @Override
    public ApplicationGatewayImpl attach() {
        return this.parent().withProbe(this);
    }
}
