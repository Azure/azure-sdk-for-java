// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.fluent.inner.SiteInner;
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
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;

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
        return inner().state();
    }

    @Override
    public Set<String> hostnames() {
        return Collections.unmodifiableSet(hostNamesSet);
    }

    @Override
    public String repositorySiteName() {
        return inner().repositorySiteName();
    }

    @Override
    public UsageState usageState() {
        return inner().usageState();
    }

    @Override
    public boolean enabled() {
        return inner().enabled() == null || inner().enabled();
    }

    @Override
    public Set<String> enabledHostNames() {
        return Collections.unmodifiableSet(enabledHostNamesSet);
    }

    @Override
    public SiteAvailabilityState availabilityState() {
        return inner().availabilityState();
    }

    @Override
    public Map<String, HostnameSslState> hostnameSslStates() {
        return Collections.unmodifiableMap(hostNameSslStateMap);
    }

    @Override
    public OffsetDateTime lastModifiedTime() {
        return inner().lastModifiedTimeUtc();
    }

    @Override
    public Set<String> trafficManagerHostNames() {
        return Collections.unmodifiableSet(trafficManagerHostNamesSet);
    }

    @Override
    public boolean scmSiteAlsoStopped() {
        return Utils.toPrimitiveBoolean(inner().scmSiteAlsoStopped());
    }

    @Override
    public String targetSwapSlot() {
        return inner().targetSwapSlot();
    }

    @Override
    public boolean clientAffinityEnabled() {
        return Utils.toPrimitiveBoolean(inner().clientAffinityEnabled());
    }

    @Override
    public boolean clientCertEnabled() {
        return Utils.toPrimitiveBoolean(inner().clientCertEnabled());
    }

    @Override
    public boolean hostnamesDisabled() {
        return Utils.toPrimitiveBoolean(inner().hostNamesDisabled());
    }

    @Override
    public Set<String> outboundIPAddresses() {
        return Collections.unmodifiableSet(outboundIPAddressesSet);
    }

    @Override
    public int containerSize() {
        return Utils.toPrimitiveInt(inner().containerSize());
    }

    @Override
    public CloningInfo cloningInfo() {
        return inner().cloningInfo();
    }

    @Override
    public boolean isDefaultContainer() {
        return inner().isDefaultContainer() == null || inner().isDefaultContainer();
    }

    @Override
    public String defaultHostname() {
        return inner().defaultHostname();
    }

    @Override
    public boolean httpsOnly() {
        return Utils.toPrimitiveBoolean(inner().httpsOnly());
    }

    @Override
    public String appServicePlanId() {
        return inner().serverFarmId();
    }

    @Override
    public ManagedServiceIdentity identity() {
        return inner().identity();
    }

    private boolean reserved() {
        return Utils.toPrimitiveBoolean(inner().reserved());
    }

    @Override
    public boolean hyperV() {
        return Utils.toPrimitiveBoolean(inner().hyperV());
    }

    @Override
    public HostingEnvironmentProfile hostingEnvironmentProfile() {
        return inner().hostingEnvironmentProfile();
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
        return Utils.toPrimitiveInt(inner().dailyMemoryTimeQuota());
    }

    @Override
    public OffsetDateTime suspendedTill() {
        return inner().suspendedTill();
    }

    @Override
    public int maxNumberOfWorkers() {
        return Utils.toPrimitiveInt(inner().maxNumberOfWorkers());
    }

    @Override
    public SlotSwapStatus slotSwapStatus() {
        return inner().slotSwapStatus();
    }

    @Override
    public RedundancyMode redundancyMode() {
        return inner().redundancyMode();
    }

    @Override
    public OperatingSystem operatingSystem() {
        return reserved() ? OperatingSystem.LINUX : OperatingSystem.WINDOWS;
    }

    @Override
    public String resourceGroupName() {
        return inner().resourceGroup();
    }

    @Override
    public String type() {
        return inner().type();
    }

    @Override
    public String regionName() {
        return inner().location();
    }

    @Override
    public Region region() {
        return Region.fromName(this.regionName());
    }

    @Override
    public Map<String, String> tags() {
        return Collections.unmodifiableMap(inner().tags());
    }

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public String name() {
        return inner().name();
    }

    @Override
    public SiteInner inner() {
        return innerObject;
    }

    protected void setInner(SiteInner innerObject) {
        this.innerObject = innerObject;
        
        this.hostNamesSet.clear();
        if (inner().hostNames() != null) {
            this.hostNamesSet.addAll(inner().hostNames());
        }
        this.enabledHostNamesSet.clear();
        if (inner().enabledHostNames() != null) {
            this.enabledHostNamesSet.addAll(inner().enabledHostNames());
        }
        this.trafficManagerHostNamesSet.clear();
        if (inner().trafficManagerHostNames() != null) {
            this.trafficManagerHostNamesSet.addAll(inner().trafficManagerHostNames());
        }
        this.outboundIPAddressesSet.clear();
        if (inner().outboundIpAddresses() != null) {
            this.outboundIPAddressesSet.addAll(Arrays.asList(inner().outboundIpAddresses().split(",[ ]*")));
        }
        this.possibleOutboundIPAddressesSet.clear();
        if (inner().possibleOutboundIpAddresses() != null) {
            this.possibleOutboundIPAddressesSet.addAll(Arrays.asList(
                inner().possibleOutboundIpAddresses().split(",[ ]*")));
        }
        this.hostNameSslStateMap.clear();
        if (inner().hostnameSslStates() != null) {
            for (HostnameSslState hostNameSslState : inner().hostnameSslStates()) {
                // Server returns null sometimes, invalid on update, so we set default
                if (hostNameSslState.sslState() == null) {
                    hostNameSslState.withSslState(SslState.DISABLED);
                }
                hostNameSslStateMap.put(hostNameSslState.name(), hostNameSslState);
            }
        }
        this.clientCertExclusionPathSet.clear();
        if (inner().clientCertExclusionPaths() != null) {
            this.clientCertExclusionPathSet.addAll(Arrays.asList(
                inner().clientCertExclusionPaths().split(",[ ]*")));
        }
    }

    @Override
    public String key() {
        return key;
    }
}
