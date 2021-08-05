// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.trafficmanager.implementation;

import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.trafficmanager.fluent.EndpointsClient;
import com.azure.resourcemanager.trafficmanager.fluent.models.EndpointInner;
import com.azure.resourcemanager.trafficmanager.models.EndpointMonitorStatus;
import com.azure.resourcemanager.trafficmanager.models.EndpointPropertiesCustomHeadersItem;
import com.azure.resourcemanager.trafficmanager.models.EndpointPropertiesSubnetsItem;
import com.azure.resourcemanager.trafficmanager.models.EndpointStatus;
import com.azure.resourcemanager.trafficmanager.models.EndpointType;
import com.azure.resourcemanager.trafficmanager.models.GeographicLocation;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerEndpoint;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerProfile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import reactor.core.publisher.Mono;

/** Implementation for {@link TrafficManagerEndpoint}. */
class TrafficManagerEndpointImpl
    extends ExternalChildResourceImpl<
        TrafficManagerEndpoint, EndpointInner, TrafficManagerProfileImpl, TrafficManagerProfile>
    implements TrafficManagerEndpoint,
        TrafficManagerEndpoint.Definition<TrafficManagerProfile.DefinitionStages.WithCreate>,
        TrafficManagerEndpoint.UpdateDefinition<TrafficManagerProfile.Update>,
        TrafficManagerEndpoint.UpdateAzureEndpoint,
        TrafficManagerEndpoint.UpdateExternalEndpoint,
        TrafficManagerEndpoint.UpdateNestedProfileEndpoint {
    private final EndpointsClient client;
    private EndpointType endpointType;

    TrafficManagerEndpointImpl(
        String name, TrafficManagerProfileImpl parent, EndpointInner inner, EndpointsClient client) {
        super(name, parent, inner);
        this.client = client;
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public EndpointType endpointType() {
        if (this.innerModel().type() != null) {
            return EndpointType.fromValue(this.innerModel().type());
        } else {
            return this.endpointType;
        }
    }

    @Override
    public EndpointMonitorStatus monitorStatus() {
        return this.innerModel().endpointMonitorStatus();
    }

    @Override
    public boolean isEnabled() {
        return this.innerModel().endpointStatus().equals(EndpointStatus.ENABLED);
    }

    @Override
    public long routingWeight() {
        return ResourceManagerUtils.toPrimitiveLong(this.innerModel().weight());
    }

    @Override
    public long routingPriority() {
        return ResourceManagerUtils.toPrimitiveLong(this.innerModel().priority());
    }

    @Override
    public Set<String> geographicLocationCodes() {
        if (this.innerModel().geoMapping() == null) {
            return Collections.unmodifiableSet(new HashSet<>());
        }
        return Collections.unmodifiableSet(new HashSet<>(this.innerModel().geoMapping()));
    }

    @Override
    public Collection<EndpointPropertiesSubnetsItem> subnets() {
        if (this.innerModel().subnets() == null) {
            return Collections.unmodifiableList(new ArrayList<>());
        } else {
            return Collections.unmodifiableList(this.innerModel().subnets());
        }
    }

    @Override
    public Map<String, String> customHeaders() {
        if (this.innerModel().customHeaders() == null) {
            return Collections.unmodifiableMap(new TreeMap<>());
        } else {
            Map<String, String> headers = new TreeMap<>();
            for (EndpointPropertiesCustomHeadersItem header : this.innerModel().customHeaders()) {
                headers.put(header.name(), header.value());
            }
            return Collections.unmodifiableMap(headers);
        }
    }

    @Override
    public TrafficManagerEndpointImpl toResourceId(String resourceId) {
        this.innerModel().withTargetResourceId(resourceId);
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl toFqdn(String externalFqdn) {
        this.innerModel().withTarget(externalFqdn);
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl toProfile(TrafficManagerProfile nestedProfile) {
        this.innerModel().withTargetResourceId(nestedProfile.id());
        this.innerModel().withMinChildEndpoints(1L);
        return this;
    }

    public TrafficManagerEndpointImpl fromRegion(Region location) {
        this.innerModel().withEndpointLocation(location.toString());
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withMinimumEndpointsToEnableTraffic(int count) {
        this.innerModel().withMinChildEndpoints((long) count);
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withRoutingPriority(int priority) {
        this.innerModel().withPriority((long) priority);
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withTrafficDisabled() {
        this.innerModel().withEndpointStatus(EndpointStatus.DISABLED);
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withTrafficEnabled() {
        this.innerModel().withEndpointStatus(EndpointStatus.ENABLED);
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withRoutingWeight(int weight) {
        this.innerModel().withWeight((long) weight);
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withGeographicLocation(GeographicLocation geographicLocation) {
        return this.withGeographicLocation(geographicLocation.code());
    }

    @Override
    public TrafficManagerEndpointImpl withoutGeographicLocation(GeographicLocation geographicLocation) {
        return this.withoutGeographicLocation(geographicLocation.code());
    }

    @Override
    public TrafficManagerEndpointImpl withGeographicLocations(List<GeographicLocation> geographicLocations) {
        for (GeographicLocation location : geographicLocations) {
            this.withGeographicLocation(location);
        }
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withGeographicLocation(String geographicLocationCode) {
        if (this.innerModel().geoMapping() == null) {
            this.innerModel().withGeoMapping(new ArrayList<>());
        }
        boolean found = false;
        for (String locationCode : this.innerModel().geoMapping()) {
            if (locationCode.equalsIgnoreCase(geographicLocationCode)) {
                found = true;
                break;
            }
        }
        if (!found) {
            this.innerModel().geoMapping().add(geographicLocationCode);
        }
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withGeographicLocations(Collection<String> geographicLocationCodes) {
        for (String locationCode : geographicLocationCodes) {
            this.withGeographicLocation(locationCode);
        }
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withoutGeographicLocation(String geographicLocationCode) {
        if (this.innerModel().geoMapping() == null) {
            return this;
        }
        int itemIndex = -1;
        int i = 0;
        for (String locationCode : this.innerModel().geoMapping()) {
            if (locationCode.equalsIgnoreCase(geographicLocationCode)) {
                itemIndex = i;
                break;
            }
            i++;
        }
        if (itemIndex != -1) {
            this.innerModel().geoMapping().remove(itemIndex);
        }
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withSubnet(String subnetStartIp, int mask) {
        if (this.innerModel().subnets() == null) {
            this.innerModel().withSubnets(new ArrayList<>());
        }
        boolean found = false;
        for (EndpointPropertiesSubnetsItem subnetItem : this.innerModel().subnets()) {
            if (subnetItem.first() != null && subnetItem.scope() != null) {
                if (subnetItem.first().equalsIgnoreCase(subnetStartIp) && subnetItem.scope() == mask) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            this.innerModel().subnets().add(
                new EndpointPropertiesSubnetsItem().withFirst(subnetStartIp).withScope(mask));
        }
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withSubnet(String subnetStartIp, String subnetEndIp) {
        if (this.innerModel().subnets() == null) {
            this.innerModel().withSubnets(new ArrayList<>());
        }
        boolean found = false;
        for (EndpointPropertiesSubnetsItem subnetItem : this.innerModel().subnets()) {
            if (subnetItem.first() != null && subnetItem.last() != null) {
                if (subnetItem.first().equalsIgnoreCase(subnetStartIp)
                    && subnetItem.last().equalsIgnoreCase(subnetEndIp)) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            this
                .innerModel()
                .subnets()
                .add(new EndpointPropertiesSubnetsItem().withFirst(subnetStartIp).withLast(subnetEndIp));
        }
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withSubnets(List<EndpointPropertiesSubnetsItem> subnets) {
        this.innerModel().withSubnets(new ArrayList<>());
        for (EndpointPropertiesSubnetsItem subnet : subnets) {
            this
                .innerModel()
                .subnets()
                .add(
                    new EndpointPropertiesSubnetsItem()
                        .withFirst(subnet.first())
                        .withLast(subnet.last())
                        .withScope(subnet.scope()));
        }
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withoutSubnet(String subnetStartIp, int mask) {
        if (this.innerModel().subnets() == null) {
            return this;
        }
        int foundIndex = -1;
        int i = 0;
        for (EndpointPropertiesSubnetsItem subnetItem : this.innerModel().subnets()) {
            if (subnetItem.first() != null && subnetItem.scope() != null) {
                if (subnetItem.first().equalsIgnoreCase(subnetStartIp) && subnetItem.scope() == mask) {
                    foundIndex = i;
                    break;
                }
            }
            i++;
        }
        if (foundIndex != -1) {
            this.innerModel().subnets().remove(foundIndex);
        }
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withoutSubnet(String subnetStartIp, String subnetEndIp) {
        if (this.innerModel().subnets() == null) {
            return this;
        }
        int foundIndex = -1;
        int i = 0;
        for (EndpointPropertiesSubnetsItem subnetItem : this.innerModel().subnets()) {
            if (subnetItem.first() != null && subnetItem.last() != null) {
                if (subnetItem.first().equalsIgnoreCase(subnetStartIp)
                    && subnetItem.last().equalsIgnoreCase(subnetEndIp)) {
                    foundIndex = i;
                    break;
                }
            }
            i++;
        }
        if (foundIndex != -1) {
            this.innerModel().subnets().remove(foundIndex);
        }
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withCustomHeader(String name, String value) {
        if (this.innerModel().customHeaders() == null) {
            this.innerModel().withCustomHeaders(new ArrayList<>());
        }
        boolean found = false;
        for (EndpointPropertiesCustomHeadersItem headersItem : this.innerModel().customHeaders()) {
            if (headersItem.name() != null
                && headersItem.name().equalsIgnoreCase(name)
                && headersItem.value() != null
                && headersItem.value().equalsIgnoreCase(value)) {
                found = true;
                break;
            }
        }
        if (!found) {
            this.innerModel().customHeaders().add(
                new EndpointPropertiesCustomHeadersItem().withName(name).withValue(value));
        }
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withCustomHeaders(Map<String, String> headers) {
        this.innerModel().withCustomHeaders(new ArrayList<>());
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            this
                .innerModel()
                .customHeaders()
                .add(new EndpointPropertiesCustomHeadersItem().withName(entry.getKey()).withValue(entry.getValue()));
        }
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withoutCustomHeader(String name) {
        if (this.innerModel().customHeaders() == null) {
            return this;
        }

        int foundIndex = -1;
        int i = 0;
        for (EndpointPropertiesCustomHeadersItem headersItem : this.innerModel().customHeaders()) {
            if (headersItem.name() != null && headersItem.name().equalsIgnoreCase(name)) {
                foundIndex = i;
                break;
            }
            i++;
        }
        if (foundIndex != -1) {
            this.innerModel().customHeaders().remove(foundIndex);
        }
        return this;
    }

    @Override
    public Mono<TrafficManagerEndpoint> createResourceAsync() {
        return this
            .client
            .createOrUpdateAsync(
                this.parent().resourceGroupName(),
                this.parent().name(),
                this.endpointType().localName(),
                this.name(),
                this.innerModel())
            .map(
                inner -> {
                    setInner(inner);
                    return this;
                });
    }

    @Override
    public Mono<TrafficManagerEndpoint> updateResourceAsync() {
        return createResourceAsync();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this
            .client
            .deleteAsync(
                this.parent().resourceGroupName(), this.parent().name(), this.endpointType().localName(), this.name())
            .then();
    }

    @Override
    public TrafficManagerProfileImpl attach() {
        return this.parent().withEndpoint(this);
    }

    @Override
    protected Mono<EndpointInner> getInnerAsync() {
        return this
            .client
            .getAsync(
                this.parent().resourceGroupName(), this.parent().name(), this.endpointType().toString(), this.name());
    }

    void withEndpointType(EndpointType endpointType) {
        this.endpointType = endpointType;
    }
}
