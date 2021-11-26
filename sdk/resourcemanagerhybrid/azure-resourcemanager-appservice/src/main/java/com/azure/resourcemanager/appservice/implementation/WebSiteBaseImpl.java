// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.fluent.models.SiteInner;
import com.azure.resourcemanager.appservice.models.CloningInfo;
import com.azure.resourcemanager.appservice.models.HostingEnvironmentProfile;
import com.azure.resourcemanager.appservice.models.HostnameSslState;
import com.azure.resourcemanager.appservice.models.ManagedServiceIdentity;
import com.azure.resourcemanager.appservice.models.OperatingSystem;
import com.azure.resourcemanager.appservice.models.RedundancyMode;
import com.azure.resourcemanager.appservice.models.SiteAvailabilityState;
import com.azure.resourcemanager.appservice.models.SlotSwapStatus;
import com.azure.resourcemanager.appservice.models.SslState;
import com.azure.resourcemanager.appservice.models.UsageState;
import com.azure.resourcemanager.appservice.models.WebSiteBase;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

class WebSiteBaseImpl implements WebSiteBase {

    private final String key;
    private SiteInner innerObject;

    private final Set<String> hostNamesSet = new HashSet<>();
    private final Set<String> enabledHostNamesSet = new HashSet<>();
    private final Set<String> trafficManagerHostNamesSet = new HashSet<>();
    private final Set<String> outboundIPAddressesSet = new HashSet<>();
    private final Set<String> possibleOutboundIPAddressesSet = new HashSet<>();
    private final Map<String, HostnameSslState> hostNameSslStateMap = new HashMap<>();
    private final Set<String> clientCertExclusionPathSet = new HashSet<>();

    WebSiteBaseImpl(SiteInner innerObject) {
        this.key = UUID.randomUUID().toString();
        this.setInner(innerObject);
    }

    @Override
    public String state() {
        return innerModel().state();
    }

    @Override
    public Set<String> hostnames() {
        return Collections.unmodifiableSet(hostNamesSet);
    }

    @Override
    public String repositorySiteName() {
        return innerModel().repositorySiteName();
    }

    @Override
    public UsageState usageState() {
        return innerModel().usageState();
    }

    @Override
    public boolean enabled() {
        return innerModel().enabled() == null || innerModel().enabled();
    }

    @Override
    public Set<String> enabledHostNames() {
        return Collections.unmodifiableSet(enabledHostNamesSet);
    }

    @Override
    public SiteAvailabilityState availabilityState() {
        return innerModel().availabilityState();
    }

    @Override
    public Map<String, HostnameSslState> hostnameSslStates() {
        return Collections.unmodifiableMap(hostNameSslStateMap);
    }

    @Override
    public OffsetDateTime lastModifiedTime() {
        return innerModel().lastModifiedTimeUtc();
    }

    @Override
    public Set<String> trafficManagerHostNames() {
        return Collections.unmodifiableSet(trafficManagerHostNamesSet);
    }

    @Override
    public boolean scmSiteAlsoStopped() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().scmSiteAlsoStopped());
    }

    @Override
    public String targetSwapSlot() {
        return innerModel().targetSwapSlot();
    }

    @Override
    public boolean clientAffinityEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().clientAffinityEnabled());
    }

    @Override
    public boolean clientCertEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().clientCertEnabled());
    }

    @Override
    public boolean hostnamesDisabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().hostNamesDisabled());
    }

    @Override
    public Set<String> outboundIPAddresses() {
        return Collections.unmodifiableSet(outboundIPAddressesSet);
    }

    @Override
    public int containerSize() {
        return ResourceManagerUtils.toPrimitiveInt(innerModel().containerSize());
    }

    @Override
    public CloningInfo cloningInfo() {
        return innerModel().cloningInfo();
    }

    @Override
    public boolean isDefaultContainer() {
        return innerModel().isDefaultContainer() == null || innerModel().isDefaultContainer();
    }

    @Override
    public String defaultHostname() {
        return innerModel().defaultHostname();
    }

    @Override
    public boolean httpsOnly() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().httpsOnly());
    }

    @Override
    public String appServicePlanId() {
        return innerModel().serverFarmId();
    }

    @Override
    public ManagedServiceIdentity identity() {
        return innerModel().identity();
    }

    private boolean reserved() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().reserved());
    }

    @Override
    public boolean hyperV() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().hyperV());
    }

    @Override
    public HostingEnvironmentProfile hostingEnvironmentProfile() {
        return innerModel().hostingEnvironmentProfile();
    }

    @Override
    public Set<String> clientCertExclusionPaths() {
        return Collections.unmodifiableSet(clientCertExclusionPathSet);
    }

    @Override
    public Set<String> possibleOutboundIpAddresses() {
        return Collections.unmodifiableSet(possibleOutboundIPAddressesSet);
    }

    @Override
    public int dailyMemoryTimeQuota() {
        return ResourceManagerUtils.toPrimitiveInt(innerModel().dailyMemoryTimeQuota());
    }

    @Override
    public OffsetDateTime suspendedTill() {
        return innerModel().suspendedTill();
    }

    @Override
    public int maxNumberOfWorkers() {
        return ResourceManagerUtils.toPrimitiveInt(innerModel().maxNumberOfWorkers());
    }

    @Override
    public SlotSwapStatus slotSwapStatus() {
        return innerModel().slotSwapStatus();
    }

    @Override
    public RedundancyMode redundancyMode() {
        return innerModel().redundancyMode();
    }

    @Override
    public OperatingSystem operatingSystem() {
        return reserved() ? OperatingSystem.LINUX : OperatingSystem.WINDOWS;
    }

    @Override
    public String resourceGroupName() {
        return innerModel().resourceGroup();
    }

    @Override
    public String type() {
        return innerModel().type();
    }

    @Override
    public String regionName() {
        return innerModel().location();
    }

    @Override
    public Region region() {
        return Region.fromName(this.regionName());
    }

    @Override
    public Map<String, String> tags() {
        return Collections.unmodifiableMap(innerModel().tags());
    }

    @Override
    public String id() {
        return innerModel().id();
    }

    @Override
    public String name() {
        return innerModel().name();
    }

    @Override
    public SiteInner innerModel() {
        return innerObject;
    }

    protected void setInner(SiteInner innerObject) {
        this.innerObject = innerObject;

        this.hostNamesSet.clear();
        if (innerModel().hostNames() != null) {
            this.hostNamesSet.addAll(innerModel().hostNames());
        }
        this.enabledHostNamesSet.clear();
        if (innerModel().enabledHostNames() != null) {
            this.enabledHostNamesSet.addAll(innerModel().enabledHostNames());
        }
        this.trafficManagerHostNamesSet.clear();
        if (innerModel().trafficManagerHostNames() != null) {
            this.trafficManagerHostNamesSet.addAll(innerModel().trafficManagerHostNames());
        }
        this.outboundIPAddressesSet.clear();
        if (innerModel().outboundIpAddresses() != null) {
            this.outboundIPAddressesSet.addAll(Arrays.asList(innerModel().outboundIpAddresses().split(",[ ]*")));
        }
        this.possibleOutboundIPAddressesSet.clear();
        if (innerModel().possibleOutboundIpAddresses() != null) {
            this.possibleOutboundIPAddressesSet.addAll(Arrays.asList(
                innerModel().possibleOutboundIpAddresses().split(",[ ]*")));
        }
        this.hostNameSslStateMap.clear();
        if (innerModel().hostnameSslStates() != null) {
            for (HostnameSslState hostNameSslState : innerModel().hostnameSslStates()) {
                // Server returns null sometimes, invalid on update, so we set default
                if (hostNameSslState.sslState() == null) {
                    hostNameSslState.withSslState(SslState.DISABLED);
                }
                hostNameSslStateMap.put(hostNameSslState.name(), hostNameSslState);
            }
        }
        this.clientCertExclusionPathSet.clear();
        if (innerModel().clientCertExclusionPaths() != null) {
            this.clientCertExclusionPathSet.addAll(Arrays.asList(
                innerModel().clientCertExclusionPaths().split(",[ ]*")));
        }
    }

    @Override
    public String key() {
        return key;
    }
}
