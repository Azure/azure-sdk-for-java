// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.fluent.ConnectionMonitorsClient;
import com.azure.resourcemanager.network.fluent.models.ConnectionMonitorInner;
import com.azure.resourcemanager.network.fluent.models.ConnectionMonitorResultInner;
import com.azure.resourcemanager.network.models.ConnectionMonitor;
import com.azure.resourcemanager.network.models.ConnectionMonitorDestination;
import com.azure.resourcemanager.network.models.ConnectionMonitorQueryResult;
import com.azure.resourcemanager.network.models.ConnectionMonitorSource;
import com.azure.resourcemanager.network.models.HasNetworkInterfaces;
import com.azure.resourcemanager.network.models.NetworkWatcher;
import com.azure.resourcemanager.network.models.ProvisioningState;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/** Implementation for Connection Monitor and its create and update interfaces. */
public class ConnectionMonitorImpl
    extends CreatableUpdatableImpl<ConnectionMonitor, ConnectionMonitorResultInner, ConnectionMonitorImpl>
    implements ConnectionMonitor, ConnectionMonitor.Definition {
    private final ConnectionMonitorsClient client;
    private final ConnectionMonitorInner createParameters;
    private final NetworkWatcher parent;

    ConnectionMonitorImpl(
        String name,
        NetworkWatcherImpl parent,
        ConnectionMonitorResultInner innerObject,
        ConnectionMonitorsClient client) {
        super(name, innerObject);
        this.client = client;
        this.parent = parent;
        this.createParameters = new ConnectionMonitorInner().withLocation(parent.regionName());
    }

    @Override
    protected Mono<ConnectionMonitorResultInner> getInnerAsync() {
        return this.client.getAsync(parent.resourceGroupName(), parent.name(), name());
    }

    @Override
    public String location() {
        return innerModel().location();
    }

    @Override
    public Map<String, String> tags() {
        Map<String, String> tags = this.innerModel().tags();
        if (tags == null) {
            tags = new TreeMap<>();
        }
        return Collections.unmodifiableMap(tags);
    }

    @Override
    public ConnectionMonitorSource source() {
        return innerModel().source();
    }

    @Override
    public ConnectionMonitorDestination destination() {
        return innerModel().destination();
    }

    @Override
    public boolean autoStart() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().autoStart());
    }

    @Override
    public ProvisioningState provisioningState() {
        return innerModel().provisioningState();
    }

    @Override
    public OffsetDateTime startTime() {
        return innerModel().startTime();
    }

    @Override
    public String monitoringStatus() {
        return innerModel().monitoringStatus();
    }

    @Override
    public int monitoringIntervalInSeconds() {
        return ResourceManagerUtils.toPrimitiveInt(innerModel().monitoringIntervalInSeconds());
    }

    @Override
    public void stop() {
        stopAsync().block();
    }

    @Override
    public Mono<Void> stopAsync() {
        return this
            .client
            .stopAsync(parent.resourceGroupName(), parent.name(), name())
            .flatMap(aVoid -> refreshAsync())
            .then();
    }

    @Override
    public void start() {
        startAsync().block();
    }

    @Override
    public Mono<Void> startAsync() {
        return this
            .client
            .startAsync(parent.resourceGroupName(), parent.name(), name())
            .flatMap(aVoid -> refreshAsync())
            .then();
    }

    @Override
    public ConnectionMonitorQueryResult query() {
        return queryAsync().block();
    }

    @Override
    public Mono<ConnectionMonitorQueryResult> queryAsync() {
        return this
            .client
            .queryAsync(parent.resourceGroupName(), parent.name(), name())
            .map(inner -> new ConnectionMonitorQueryResultImpl(inner));
    }

    @Override
    public boolean isInCreateMode() {
        return this.innerModel().id() == null;
    }

    @Override
    public Mono<ConnectionMonitor> createResourceAsync() {
        return this
            .client
            .createOrUpdateAsync(parent.resourceGroupName(), parent.name(), this.name(), createParameters)
            .map(innerToFluentMap(this));
    }

    @Override
    public String id() {
        return innerModel().id();
    }

    @Override
    public ConnectionMonitorImpl withSourceId(String resourceId) {
        ensureConnectionMonitorSource().withResourceId(resourceId);
        return this;
    }

    @Override
    public ConnectionMonitorImpl withSource(HasNetworkInterfaces vm) {
        ensureConnectionMonitorSource().withResourceId(vm.id());
        return this;
    }

    @Override
    public ConnectionMonitorImpl withDestinationId(String resourceId) {
        ensureConnectionMonitorDestination().withResourceId(resourceId);
        return this;
    }

    @Override
    public ConnectionMonitorImpl withDestination(HasNetworkInterfaces vm) {
        ensureConnectionMonitorDestination().withResourceId(vm.id());
        return this;
    }

    @Override
    public DefinitionStages.WithDestinationPort withDestinationAddress(String address) {
        ensureConnectionMonitorDestination().withAddress(address);
        return this;
    }

    private ConnectionMonitorSource ensureConnectionMonitorSource() {
        if (createParameters.source() == null) {
            createParameters.withSource(new ConnectionMonitorSource());
        }
        return createParameters.source();
    }

    private ConnectionMonitorDestination ensureConnectionMonitorDestination() {
        if (createParameters.destination() == null) {
            createParameters.withDestination(new ConnectionMonitorDestination());
        }
        return createParameters.destination();
    }

    @Override
    public ConnectionMonitorImpl withDestinationPort(int port) {
        ensureConnectionMonitorDestination().withPort(port);
        return this;
    }

    @Override
    public ConnectionMonitorImpl withSourcePort(int port) {
        ensureConnectionMonitorSource().withPort(port);
        return this;
    }

    @Override
    public ConnectionMonitorImpl withoutAutoStart() {
        createParameters.withAutoStart(false);
        return this;
    }

    @Override
    public final ConnectionMonitorImpl withTags(Map<String, String> tags) {
        this.createParameters.withTags(new HashMap<>(tags));
        return this;
    }

    @Override
    public ConnectionMonitorImpl withTag(String key, String value) {
        if (this.createParameters.tags() == null) {
            this.createParameters.withTags(new HashMap<String, String>());
        }
        this.createParameters.tags().put(key, value);
        return this;
    }

    @Override
    public ConnectionMonitorImpl withoutTag(String key) {
        if (this.createParameters.tags() != null) {
            this.createParameters.tags().remove(key);
        }
        return this;
    }

    @Override
    public ConnectionMonitorImpl withMonitoringInterval(int seconds) {
        createParameters.withMonitoringIntervalInSeconds(seconds);
        return this;
    }
}
