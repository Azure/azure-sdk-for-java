/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayProbe;
import com.microsoft.azure.management.network.ApplicationGatewayProbeHealthResponseMatch;
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
    public String healthyHttpResponseBodyContents() {
        ApplicationGatewayProbeHealthResponseMatch match = this.inner().match();
        if (match == null) {
            return null;
        } else {
            return match.body();
        }
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
    public Set<String> healthyHttpResponseStatusCodeRanges() {
        Set<String> httpResponseStatusCodeRanges = new TreeSet<>();
        if (this.inner().match() == null) {
            // Empty
        } else if (this.inner().match().statusCodes() == null) {
            // Empty
        } else {
            httpResponseStatusCodeRanges.addAll(this.inner().match().statusCodes());
        }

        return Collections.unmodifiableSet(httpResponseStatusCodeRanges);
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
            ApplicationGatewayProbeHealthResponseMatch match = this.inner().match();
            if (match == null) {
                match = new ApplicationGatewayProbeHealthResponseMatch();
                this.inner().withMatch(match);
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
            throw new IllegalArgumentException("The start and end of a range cannot be negative numbers.");
        } else if (to < from) {
            throw new IllegalArgumentException("The end of the range cannot be less than the start of the range.");
        } else {
            return this.withHealthyHttpResponseStatusCodeRange(String.valueOf(from) + "-" + String.valueOf(to));
        }
    }

    @Override
    public ApplicationGatewayProbeImpl withoutHealthyHttpResponseStatusCodeRanges() {
        ApplicationGatewayProbeHealthResponseMatch match = this.inner().match();
        if (match != null) {
            match.withStatusCodes(null);
            if (match.body() == null) {
                this.inner().withMatch(null);
            }
        }

        return this;
    }

    @Override
    public ApplicationGatewayProbeImpl withHealthyHttpResponseBodyContents(String text) {
        ApplicationGatewayProbeHealthResponseMatch match = this.inner().match();
        if (text != null) {
            if (match == null) {
                match = new ApplicationGatewayProbeHealthResponseMatch();
                this.inner().withMatch(match);
            }
            match.withBody(text);
        } else {
            if (match == null) {
                // Nothing else to do
            } else if (match.statusCodes() == null || match.statusCodes().isEmpty()) {
                // If match is becoming empty then remove altogether
                this.inner().withMatch(null);
            } else {
                match.withBody(null);
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
