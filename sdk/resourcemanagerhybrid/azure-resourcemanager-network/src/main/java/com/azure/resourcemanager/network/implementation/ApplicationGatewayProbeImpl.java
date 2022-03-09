// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayProbe;
import com.azure.resourcemanager.network.models.ApplicationGatewayProbeHealthResponseMatch;
import com.azure.resourcemanager.network.models.ApplicationGatewayProtocol;
import com.azure.resourcemanager.network.fluent.models.ApplicationGatewayProbeInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Implementation for ApplicationGatewayProbe. */
class ApplicationGatewayProbeImpl
    extends ChildResourceImpl<ApplicationGatewayProbeInner, ApplicationGatewayImpl, ApplicationGateway>
    implements ApplicationGatewayProbe,
        ApplicationGatewayProbe.Definition<ApplicationGateway.DefinitionStages.WithCreate>,
        ApplicationGatewayProbe.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayProbe.Update {
    private final ClientLogger logger = new ClientLogger(getClass());

    ApplicationGatewayProbeImpl(ApplicationGatewayProbeInner inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
    }

    // Getters

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String healthyHttpResponseBodyContents() {
        ApplicationGatewayProbeHealthResponseMatch match = this.innerModel().match();
        if (match == null) {
            return null;
        } else {
            return match.body();
        }
    }

    @Override
    public ApplicationGatewayProtocol protocol() {
        return this.innerModel().protocol();
    }

    @Override
    public int timeBetweenProbesInSeconds() {
        return (this.innerModel().interval() != null) ? this.innerModel().interval().intValue() : 0;
    }

    @Override
    public String path() {
        return this.innerModel().path();
    }

    @Override
    public Set<String> healthyHttpResponseStatusCodeRanges() {
        Set<String> httpResponseStatusCodeRanges = new TreeSet<>();
        if (this.innerModel().match() != null && this.innerModel().match().statusCodes() != null) {
            httpResponseStatusCodeRanges.addAll(this.innerModel().match().statusCodes());
        }

        return Collections.unmodifiableSet(httpResponseStatusCodeRanges);
    }

    @Override
    public int timeoutInSeconds() {
        return (this.innerModel().timeout() != null) ? this.innerModel().timeout().intValue() : 0;
    }

    @Override
    public int retriesBeforeUnhealthy() {
        return (this.innerModel().unhealthyThreshold() != null) ? this.innerModel().unhealthyThreshold() : 0;
    }

    @Override
    public String host() {
        return this.innerModel().host();
    }

    // Fluent setters

    @Override
    public ApplicationGatewayProbeImpl withProtocol(ApplicationGatewayProtocol protocol) {
        this.innerModel().withProtocol(protocol);
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
        this.innerModel().withPath(path);
        return this;
    }

    @Override
    public ApplicationGatewayProbeImpl withHost(String host) {
        this.innerModel().withHost(host);
        return this;
    }

    @Override
    public ApplicationGatewayProbeImpl withTimeoutInSeconds(int seconds) {
        this.innerModel().withTimeout(seconds);
        return this;
    }

    @Override
    public ApplicationGatewayProbeImpl withTimeBetweenProbesInSeconds(int seconds) {
        this.innerModel().withInterval(seconds);
        return this;
    }

    @Override
    public ApplicationGatewayProbeImpl withRetriesBeforeUnhealthy(int retryCount) {
        this.innerModel().withUnhealthyThreshold(retryCount);
        return this;
    }

    @Override
    public ApplicationGatewayProbeImpl withHealthyHttpResponseStatusCodeRanges(Set<String> ranges) {
        if (ranges != null) {
            for (String range : ranges) {
                this.withHealthyHttpResponseStatusCodeRange(range);
            }
        }
        return this;
    }

    @Override
    public ApplicationGatewayProbeImpl withHealthyHttpResponseStatusCodeRange(String range) {
        if (range != null) {
            ApplicationGatewayProbeHealthResponseMatch match = this.innerModel().match();
            if (match == null) {
                match = new ApplicationGatewayProbeHealthResponseMatch();
                this.innerModel().withMatch(match);
            }

            List<String> ranges = match.statusCodes();
            if (ranges == null) {
                ranges = new ArrayList<>();
                match.withStatusCodes(ranges);
            }

            if (!ranges.contains(range)) {
                ranges.add(range);
            }
        }

        return this;
    }

    @Override
    public ApplicationGatewayProbeImpl withHealthyHttpResponseStatusCodeRange(int from, int to) {
        if (from < 0 || to < 0) {
            throw logger
                .logExceptionAsError(
                    new IllegalArgumentException("The start and end of a range cannot be negative numbers."));
        } else if (to < from) {
            throw logger
                .logExceptionAsError(
                    new IllegalArgumentException("The end of the range cannot be less than the start of the range."));
        } else {
            return this.withHealthyHttpResponseStatusCodeRange(String.valueOf(from) + "-" + String.valueOf(to));
        }
    }

    @Override
    public ApplicationGatewayProbeImpl withoutHealthyHttpResponseStatusCodeRanges() {
        ApplicationGatewayProbeHealthResponseMatch match = this.innerModel().match();
        if (match != null) {
            match.withStatusCodes(null);
            if (match.body() == null) {
                this.innerModel().withMatch(null);
            }
        }

        return this;
    }

    @Override
    public ApplicationGatewayProbeImpl withHealthyHttpResponseBodyContents(String text) {
        ApplicationGatewayProbeHealthResponseMatch match = this.innerModel().match();
        if (text != null) {
            if (match == null) {
                match = new ApplicationGatewayProbeHealthResponseMatch();
                this.innerModel().withMatch(match);
            }
            match.withBody(text);
        } else {
            if (match != null) {
                if (match.statusCodes() == null || match.statusCodes().isEmpty()) {
                    // If match is becoming empty then remove altogether
                    this.innerModel().withMatch(null);
                } else {
                    match.withBody(null);
                }
            }
        }
        return this;
    }

    // Verbs

    @Override
    public ApplicationGatewayImpl attach() {
        return this.parent().withProbe(this);
    }
}
