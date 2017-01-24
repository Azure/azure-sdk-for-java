/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.network.LoadBalancerBackend;
import com.microsoft.azure.management.network.LoadBalancerFrontend;
import com.microsoft.azure.management.network.LoadBalancerHttpProbe;
import com.microsoft.azure.management.network.LoadBalancerInboundNatPool;
import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancingRule;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NicIpConfiguration;
import com.microsoft.azure.management.network.LoadBalancerProbe;
import com.microsoft.azure.management.network.ProbeProtocol;
import com.microsoft.azure.management.network.LoadBalancerPublicFrontend;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.LoadBalancerTcpProbe;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.network.model.HasNetworkInterfaces;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

import rx.Observable;
import rx.exceptions.CompositeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Implementation of the LoadBalancer interface.
 */
@LangDefinition
class LoadBalancerImpl
    extends GroupableParentResourceImpl<
        LoadBalancer,
        LoadBalancerInner,
        LoadBalancerImpl,
        NetworkManager>
    implements
        LoadBalancer,
        LoadBalancer.Definition,
        LoadBalancer.Update {

    private final Map<String, String> nicsInBackends = new HashMap<>();
    protected final Map<String, String> creatablePIPKeys = new HashMap<>();
    protected final LoadBalancersInner innerCollection;

    private Map<String, LoadBalancerBackend> backends;
    private Map<String, LoadBalancerTcpProbe> tcpProbes;
    private Map<String, LoadBalancerHttpProbe> httpProbes;
    private Map<String, LoadBalancingRule> loadBalancingRules;
    private Map<String, LoadBalancerFrontend> frontends;
    private Map<String, LoadBalancerInboundNatRule> inboundNatRules;
    private Map<String, LoadBalancerInboundNatPool> inboundNatPools;

    protected static final String DEFAULT = "default";

    LoadBalancerImpl(String name,
            final LoadBalancerInner innerModel,
            final LoadBalancersInner innerCollection,
            final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.innerCollection = innerCollection;
    }

    // Verbs

    @Override
    public LoadBalancerImpl refresh() {
        LoadBalancerInner inner = this.innerCollection.get(this.resourceGroupName(), this.name());
        this.setInner(inner);
        initializeChildrenFromInner();
        return this;
    }

    // Helpers

    @Override
    protected void initializeChildrenFromInner() {
        initializeFrontendsFromInner();
        initializeProbesFromInner();
        initializeBackendsFromInner();
        initializeLoadBalancingRulesFromInner();
        initializeInboundNatRulesFromInner();
        initializeInboundNatPoolsFromInner();
    }

    @Override
    protected void beforeCreating()  {
        // Account for the newly created public IPs
        for (Entry<String, String> pipFrontendAssociation : this.creatablePIPKeys.entrySet()) {
            PublicIpAddress pip = (PublicIpAddress) this.createdResource(pipFrontendAssociation.getKey());
            if (pip != null) {
                withExistingPublicIpAddress(pip.id(), pipFrontendAssociation.getValue());
            }
        }
        this.creatablePIPKeys.clear();

        // Reset and update probes
        this.inner().withProbes(innersFromWrappers(this.httpProbes.values()));
        this.inner().withProbes(innersFromWrappers(this.tcpProbes.values(), this.inner().probes()));

        // Reset and update backends
        this.inner().withBackendAddressPools(innersFromWrappers(this.backends.values()));

        // Reset and update frontends
        this.inner().withFrontendIPConfigurations(innersFromWrappers(this.frontends.values()));

        // Reset and update inbound NAT rules
        List<InboundNatRuleInner> innerNatRules = innersFromWrappers(this.inboundNatRules.values());
        if (null == innerNatRules) {
            innerNatRules = new ArrayList<>();
        }
        this.inner().withInboundNatRules(innerNatRules);
        for (LoadBalancerInboundNatRule natRule : this.inboundNatRules.values()) {
            // Clear deleted frontend references
            SubResource ref = natRule.inner().frontendIPConfiguration();
            if (ref != null
                    && !this.frontends().containsKey(ResourceUtils.nameFromResourceId(ref.id()))) {
                natRule.inner().withFrontendIPConfiguration(null);
            }
        }

        // Reset and update inbound NAT pools
        List<InboundNatPoolInner> innerNatPools = innersFromWrappers(this.inboundNatPools.values());
        if (null == innerNatPools) {
            innerNatPools = new ArrayList<>();
        }
        this.inner().withInboundNatPools(innerNatPools);
        for (LoadBalancerInboundNatPool natPool : this.inboundNatPools.values()) {
            // Clear deleted frontend references
            SubResource ref = natPool.inner().frontendIPConfiguration();
            if (ref != null
                    && !this.frontends().containsKey(ResourceUtils.nameFromResourceId(ref.id()))) {
                natPool.inner().withFrontendIPConfiguration(null);
            }
        }

        // Reset and update load balancing rules
        List<LoadBalancingRuleInner> innerRules = innersFromWrappers(this.loadBalancingRules.values());
        if (innerRules == null) {
            innerRules = new ArrayList<>();
        }
        this.inner().withLoadBalancingRules(innerRules);
        for (LoadBalancingRule lbRule : this.loadBalancingRules.values()) {
            SubResource ref;

            // Clear deleted frontend references
            ref = lbRule.inner().frontendIPConfiguration();
            if (ref != null
                    && !this.frontends().containsKey(ResourceUtils.nameFromResourceId(ref.id()))) {
                lbRule.inner().withFrontendIPConfiguration(null);
            }

            // Clear deleted backend references
            ref = lbRule.inner().backendAddressPool();
            if (ref != null
                    && !this.backends().containsKey(ResourceUtils.nameFromResourceId(ref.id()))) {
                lbRule.inner().withBackendAddressPool(null);
            }

            // Clear deleted probe references
            ref = lbRule.inner().probe();
            if (ref != null
                    && !this.httpProbes().containsKey(ResourceUtils.nameFromResourceId(ref.id()))
                    && !this.tcpProbes().containsKey(ResourceUtils.nameFromResourceId(ref.id()))) {
                lbRule.inner().withProbe(null);
            }
        }
    }

    @Override
    protected void afterCreating() {
        List<Exception> nicExceptions = new ArrayList<>();

        // Update the NICs to point to the backend pool
        for (Entry<String, String> nicInBackend : this.nicsInBackends.entrySet()) {
            String nicId = nicInBackend.getKey();
            String backendName = nicInBackend.getValue();
            try {
                NetworkInterface nic = this.manager().networkInterfaces().getById(nicId);
                NicIpConfiguration nicIp = nic.primaryIpConfiguration();
                nic.update()
                    .updateIpConfiguration(nicIp.name())
                    .withExistingLoadBalancerBackend(this, backendName)
                    .parent()
                    .apply();
            } catch (Exception e) {
                nicExceptions.add(e);
            }
        }

        if (!nicExceptions.isEmpty()) {
            throw new CompositeException(nicExceptions);
        }

        this.nicsInBackends.clear();
        this.refresh();
    }

    @Override
    protected Observable<LoadBalancerInner> createInner() {
        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner());
    }

    private void initializeFrontendsFromInner() {
        this.frontends = new TreeMap<>();
        List<FrontendIPConfigurationInner> frontendsInner = this.inner().frontendIPConfigurations();
        if (frontendsInner != null) {
            for (FrontendIPConfigurationInner frontendInner : frontendsInner) {
                LoadBalancerFrontendImpl frontend = new LoadBalancerFrontendImpl(frontendInner, this);
                this.frontends.put(frontendInner.name(), frontend);
            }
        }
    }

    private void initializeBackendsFromInner() {
        this.backends = new TreeMap<>();
        List<BackendAddressPoolInner> backendsInner = this.inner().backendAddressPools();
        if (backendsInner != null) {
            for (BackendAddressPoolInner backendInner : backendsInner) {
                LoadBalancerBackendImpl backend = new LoadBalancerBackendImpl(backendInner, this);
                this.backends.put(backendInner.name(), backend);
            }
        }
    }

    private void initializeProbesFromInner() {
        this.httpProbes = new TreeMap<>();
        this.tcpProbes = new TreeMap<>();
        if (this.inner().probes() != null) {
            for (ProbeInner probeInner : this.inner().probes()) {
                LoadBalancerProbeImpl probe = new LoadBalancerProbeImpl(probeInner, this);
                if (probeInner.protocol().equals(ProbeProtocol.TCP)) {
                    this.tcpProbes.put(probeInner.name(), probe);
                } else if (probeInner.protocol().equals(ProbeProtocol.HTTP)) {
                    this.httpProbes.put(probeInner.name(), probe);
                }
            }
        }
    }

    private void initializeLoadBalancingRulesFromInner() {
        this.loadBalancingRules = new TreeMap<>();
        List<LoadBalancingRuleInner> rulesInner = this.inner().loadBalancingRules();
        if (rulesInner != null) {
            for (LoadBalancingRuleInner ruleInner : rulesInner) {
                LoadBalancingRuleImpl rule = new LoadBalancingRuleImpl(ruleInner, this);
                this.loadBalancingRules.put(ruleInner.name(), rule);
            }
        }
    }

    private void initializeInboundNatPoolsFromInner() {
        this.inboundNatPools = new TreeMap<>();
        List<InboundNatPoolInner> inners = this.inner().inboundNatPools();
        if (inners != null) {
            for (InboundNatPoolInner inner : inners) {
                LoadBalancerInboundNatPoolImpl wrapper = new LoadBalancerInboundNatPoolImpl(inner, this);
                this.inboundNatPools.put(wrapper.name(), wrapper);
            }
        }
    }

    private void initializeInboundNatRulesFromInner() {
        this.inboundNatRules = new TreeMap<>();
        List<InboundNatRuleInner> rulesInner = this.inner().inboundNatRules();
        if (rulesInner != null) {
            for (InboundNatRuleInner ruleInner : rulesInner) {
                LoadBalancerInboundNatRuleImpl rule = new LoadBalancerInboundNatRuleImpl(ruleInner, this);
                this.inboundNatRules.put(ruleInner.name(), rule);
            }
        }
    }

    String futureResourceId() {
        return new StringBuilder()
                .append(super.resourceIdBase())
                .append("/providers/Microsoft.Network/loadBalancers/")
                .append(this.name()).toString();
    }

    LoadBalancerImpl withFrontend(LoadBalancerFrontendImpl frontend) {
        if (frontend == null) {
            return null;
        } else {
            this.frontends.put(frontend.name(), frontend);
            return this;
        }
    }

    LoadBalancerImpl withProbe(LoadBalancerProbeImpl probe) {
        if (probe == null) {
            return null;
        } else if (probe.protocol() == ProbeProtocol.HTTP) {
            httpProbes.put(probe.name(), probe);
        } else if (probe.protocol() == ProbeProtocol.TCP) {
            tcpProbes.put(probe.name(), probe);
        }
        return this;
    }

    LoadBalancerImpl withLoadBalancingRule(LoadBalancingRuleImpl loadBalancingRule) {
        if (loadBalancingRule == null) {
            return null;
        } else {
            this.loadBalancingRules.put(loadBalancingRule.name(), loadBalancingRule);
            return this;
        }
    }

    LoadBalancerImpl withInboundNatRule(LoadBalancerInboundNatRuleImpl inboundNatRule) {
        if (inboundNatRule == null) {
            return null;
        } else {
            this.inboundNatRules.put(inboundNatRule.name(), inboundNatRule);
            return this;
        }
    }

    LoadBalancerImpl withInboundNatPool(LoadBalancerInboundNatPoolImpl inboundNatPool) {
        if (inboundNatPool == null) {
            return null;
        } else {
            this.inboundNatPools.put(inboundNatPool.name(), inboundNatPool);
            return this;
        }
    }

    LoadBalancerImpl withBackend(LoadBalancerBackendImpl backend) {
        if (backend == null) {
            return null;
        } else {
            this.backends.put(backend.name(), backend);
            return this;
        }
    }

    // Withers (fluent)

    @Override
    public LoadBalancerImpl withExistingPublicIpAddress(String resourceId) {
        return withExistingPublicIpAddress(resourceId, DEFAULT);
    }

    @Override
    public LoadBalancerImpl withExistingPublicIpAddress(PublicIpAddress pip) {
        return withExistingPublicIpAddress(pip.id(), DEFAULT);
    }

    @Override
    public LoadBalancerImpl withNewPublicIpAddress() {
        // Autogenerated DNS leaf label for the PIP
        String dnsLeafLabel = this.name().toLowerCase().replace("\\s", "");
        return withNewPublicIpAddress(dnsLeafLabel);
    }

    @Override
    public LoadBalancerImpl withNewPublicIpAddress(String dnsLeafLabel) {
        PublicIpAddress.DefinitionStages.WithGroup precreatablePIP = manager().publicIpAddresses().define(dnsLeafLabel)
                .withRegion(this.regionName());
        Creatable<PublicIpAddress> creatablePip;
        if (super.creatableGroup == null) {
            creatablePip = precreatablePIP.withExistingResourceGroup(this.resourceGroupName()).withLeafDomainLabel(dnsLeafLabel);
        } else {
            creatablePip = precreatablePIP.withNewResourceGroup(super.creatableGroup).withLeafDomainLabel(dnsLeafLabel);
        }
        return withNewPublicIpAddress(creatablePip);
    }

    @Override
    public LoadBalancerImpl withNewPublicIpAddress(Creatable<PublicIpAddress> creatablePIP) {
        return withNewPublicIpAddress(creatablePIP, DEFAULT);
    }

    private LoadBalancerImpl withNewPublicIpAddress(Creatable<PublicIpAddress> creatablePip, String configName) {
        this.creatablePIPKeys.put(creatablePip.key(), configName);
        this.addCreatableDependency(creatablePip);
        return this;
    }

    protected LoadBalancerImpl withExistingPublicIpAddress(String resourceId, String frontendName) {
        if (frontendName == null) {
            frontendName = DEFAULT;
        }

        return this.definePublicFrontend(frontendName)
                .withExistingPublicIpAddress(resourceId)
                .attach();
    }

    @Override
    public LoadBalancerImpl withFrontendSubnet(Network network, String subnetName) {
        return this.definePrivateFrontend(DEFAULT)
                .withExistingSubnet(network, subnetName)
                .attach();
    }

    private LoadBalancerImpl withExistingVirtualMachine(HasNetworkInterfaces vm, String backendName) {
        if (backendName == null) {
            backendName = DEFAULT;
        }

        this.defineBackend(backendName).attach();
        if (vm.primaryNetworkInterfaceId() != null) {
            this.nicsInBackends.put(vm.primaryNetworkInterfaceId(), backendName.toLowerCase());
        }

        return this;
    }

    @Override
    public LoadBalancerImpl withExistingVirtualMachines(HasNetworkInterfaces... vms) {
        if (vms != null) {
            for (HasNetworkInterfaces vm : vms) {
                withExistingVirtualMachine(vm, null);
            }
        }
        return this;
    }

    @Override
    public LoadBalancerImpl withLoadBalancingRule(int frontendPort, TransportProtocol protocol, int backendPort) {
        this.defineLoadBalancingRule(DEFAULT)
            .withFrontendPort(frontendPort)
            .withFrontend(DEFAULT)
            .withBackendPort(backendPort)
            .withBackend(DEFAULT)
            .withProtocol(protocol)
            .withProbe(DEFAULT)
            .attach();
        return this;
    }

    @Override
    public LoadBalancerImpl withLoadBalancingRule(int port, TransportProtocol protocol) {
        return withLoadBalancingRule(port, protocol, port);
    }

    @Override
    public LoadBalancerImpl withTcpProbe(int port) {
        return this.defineTcpProbe(DEFAULT)
                .withPort(port)
                .attach();
    }

    @Override
    public LoadBalancerImpl withHttpProbe(String path) {
        return this.defineHttpProbe(DEFAULT)
                .withRequestPath(path)
                .withPort(80)
                .attach();
    }

    @Override
    public LoadBalancerProbeImpl defineTcpProbe(String name) {
        LoadBalancerProbe probe = this.tcpProbes.get(name);
        if (probe == null) {
            ProbeInner inner = new ProbeInner()
                .withName(name)
                .withProtocol(ProbeProtocol.TCP);
            return new LoadBalancerProbeImpl(inner, this);
        } else {
            return (LoadBalancerProbeImpl) probe;
        }
    }

    @Override
    public LoadBalancerProbeImpl defineHttpProbe(String name) {
        LoadBalancerProbe probe = this.httpProbes.get(name);
        if (probe == null) {
            ProbeInner inner = new ProbeInner()
                .withName(name)
                .withProtocol(ProbeProtocol.HTTP)
                .withPort(80);
            return new LoadBalancerProbeImpl(inner, this);
        } else {
            return (LoadBalancerProbeImpl) probe;
        }
    }

    @Override
    public LoadBalancingRuleImpl defineLoadBalancingRule(String name) {
        LoadBalancingRule lbRule = this.loadBalancingRules.get(name);
        if (lbRule == null) {
            LoadBalancingRuleInner inner = new LoadBalancingRuleInner()
                .withName(name);
            return new LoadBalancingRuleImpl(inner, this);
        } else {
            return (LoadBalancingRuleImpl) lbRule;
        }
    }

    @Override
    public LoadBalancerInboundNatRuleImpl defineInboundNatRule(String name) {
        LoadBalancerInboundNatRule natRule = this.inboundNatRules.get(name);
        if (natRule == null) {
            InboundNatRuleInner inner = new InboundNatRuleInner()
                    .withName(name);
            return new LoadBalancerInboundNatRuleImpl(inner, this);
        } else {
            return (LoadBalancerInboundNatRuleImpl) natRule;
        }
    }

    @Override
    public LoadBalancerInboundNatPoolImpl defineInboundNatPool(String name) {
        LoadBalancerInboundNatPool natPool = this.inboundNatPools.get(name);
        if (natPool == null) {
            InboundNatPoolInner inner = new InboundNatPoolInner()
                    .withName(name);
            return new LoadBalancerInboundNatPoolImpl(inner, this);
        } else {
            return (LoadBalancerInboundNatPoolImpl) natPool;
        }
    }

    @Override
    public LoadBalancerFrontendImpl definePrivateFrontend(String name) {
        return defineFrontend(name);
    }

    @Override
    public LoadBalancerFrontendImpl definePublicFrontend(String name) {
        return defineFrontend(name);
    }

    private LoadBalancerFrontendImpl defineFrontend(String name) {
        LoadBalancerFrontend frontend = this.frontends.get(name);
        if (frontend == null) {
            FrontendIPConfigurationInner inner = new FrontendIPConfigurationInner()
                    .withName(name);
            return new LoadBalancerFrontendImpl(inner, this);
        } else {
            return (LoadBalancerFrontendImpl) frontend;
        }
    }

    @Override
    public LoadBalancerBackendImpl defineBackend(String name) {
        LoadBalancerBackend backend = this.backends.get(name);
        if (backend == null) {
            BackendAddressPoolInner inner = new BackendAddressPoolInner()
                    .withName(name);
            return new LoadBalancerBackendImpl(inner, this);
        } else {
            return (LoadBalancerBackendImpl) backend;
        }
    }

    @Override
    public LoadBalancerImpl withoutProbe(String name) {
        if (this.httpProbes.containsKey(name)) {
            this.httpProbes.remove(name);
        } else if (this.tcpProbes.containsKey(name)) {
            this.tcpProbes.remove(name);
        }
        return this;
    }

    @Override
    public LoadBalancerProbeImpl updateTcpProbe(String name) {
        return (LoadBalancerProbeImpl) this.tcpProbes.get(name);
    }

    @Override
    public LoadBalancerBackendImpl updateBackend(String name) {
        return (LoadBalancerBackendImpl) this.backends.get(name);
    }

    @Override
    public LoadBalancerFrontendImpl updateInternetFrontend(String name) {
        return (LoadBalancerFrontendImpl) this.frontends.get(name);
    }

    @Override
    public LoadBalancerFrontendImpl updateInternalFrontend(String name) {
        return (LoadBalancerFrontendImpl) this.frontends.get(name);
    }

    @Override
    public LoadBalancerInboundNatRuleImpl updateInboundNatRule(String name) {
        return (LoadBalancerInboundNatRuleImpl) this.inboundNatRules.get(name);
    }

    @Override
    public LoadBalancerInboundNatPoolImpl updateInboundNatPool(String name) {
        return (LoadBalancerInboundNatPoolImpl) this.inboundNatPools.get(name);
    }

    @Override
    public LoadBalancerProbeImpl updateHttpProbe(String name) {
        return (LoadBalancerProbeImpl) this.httpProbes.get(name);
    }

    @Override
    public LoadBalancingRuleImpl updateLoadBalancingRule(String name) {
        return (LoadBalancingRuleImpl) this.loadBalancingRules.get(name);
    }

    @Override
    public LoadBalancerImpl withoutLoadBalancingRule(String name) {
        this.loadBalancingRules.remove(name);
        return this;
    }

    @Override
    public LoadBalancerImpl withoutInboundNatRule(String name) {
        this.inboundNatRules.remove(name);
        return this;
    }

    @Override
    public LoadBalancerImpl withoutBackend(String name) {
        this.backends.remove(name);
        return this;
    }

    @Override
    public Update withoutInboundNatPool(String name) {
        this.inboundNatPools.remove(name);
        return this;
    }

    @Override
    public LoadBalancerImpl withoutFrontend(String name) {
        this.frontends.remove(name);
        return this;
    }

    // Getters

    @Override
    public Map<String, LoadBalancerBackend> backends() {
        return Collections.unmodifiableMap(this.backends);
    }

    @Override
    public Map<String, LoadBalancerInboundNatPool> inboundNatPools() {
        return Collections.unmodifiableMap(this.inboundNatPools);
    }

    @Override
    public Map<String, LoadBalancerTcpProbe> tcpProbes() {
        return Collections.unmodifiableMap(this.tcpProbes);
    }

    @Override
    public Map<String, LoadBalancerFrontend> frontends() {
        return Collections.unmodifiableMap(this.frontends);
    }

    @Override
    public Map<String, LoadBalancerInboundNatRule> inboundNatRules() {
        return Collections.unmodifiableMap(this.inboundNatRules);
    }

    @Override
    public Map<String, LoadBalancerHttpProbe> httpProbes() {
        return Collections.unmodifiableMap(this.httpProbes);
    }

    @Override
    public Map<String, LoadBalancingRule> loadBalancingRules() {
        return Collections.unmodifiableMap(this.loadBalancingRules);
    }

    @Override
    public List<String> publicIpAddressIds() {
        List<String> publicIpAddressIds = new ArrayList<>();
        for (LoadBalancerFrontend frontend : this.frontends().values()) {
            if (frontend.isPublic()) {
                String pipId = ((LoadBalancerPublicFrontend) frontend).publicIpAddressId();
                publicIpAddressIds.add(pipId);
            }
        }
        return Collections.unmodifiableList(publicIpAddressIds);
    }
}