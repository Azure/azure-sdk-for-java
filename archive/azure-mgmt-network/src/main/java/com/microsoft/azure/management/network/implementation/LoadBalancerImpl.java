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
import com.microsoft.azure.management.network.LoadBalancerPrivateFrontend;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancerSkuType;
import com.microsoft.azure.management.network.LoadBalancingRule;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NicIPConfiguration;
import com.microsoft.azure.management.network.LoadBalancerProbe;
import com.microsoft.azure.management.network.ProbeProtocol;
import com.microsoft.azure.management.network.LoadBalancerPublicFrontend;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.LoadBalancerTcpProbe;
import com.microsoft.azure.management.network.model.HasNetworkInterfaces;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;

import rx.Observable;
import rx.exceptions.CompositeException;
import rx.functions.Func1;

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

    private Map<String, LoadBalancerBackend> backends;
    private Map<String, LoadBalancerTcpProbe> tcpProbes;
    private Map<String, LoadBalancerHttpProbe> httpProbes;
    private Map<String, LoadBalancingRule> loadBalancingRules;
    private Map<String, LoadBalancerFrontend> frontends;
    private Map<String, LoadBalancerInboundNatRule> inboundNatRules;
    private Map<String, LoadBalancerInboundNatPool> inboundNatPools;

    LoadBalancerImpl(String name,
            final LoadBalancerInner innerModel,
            final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
    }

    // Verbs

    @Override
    public Observable<LoadBalancer> refreshAsync() {
        return super.refreshAsync().map(new Func1<LoadBalancer, LoadBalancer>() {
            @Override
            public LoadBalancer call(LoadBalancer loadBalancer) {
                LoadBalancerImpl impl = (LoadBalancerImpl) loadBalancer;
                impl.initializeChildrenFromInner();
                return impl;
            }
        });
    }

    @Override
    protected Observable<LoadBalancerInner> getInnerAsync() {
        return this.manager().inner().loadBalancers().getByResourceGroupAsync(this.resourceGroupName(), this.name());
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

    protected LoadBalancerBackendImpl ensureUniqueBackend() {
        String name = SdkContext.randomResourceName("backend", 20);
        LoadBalancerBackendImpl backend = this.defineBackend(name);
        backend.attach();
        return backend;
    }

    protected SubResource ensureFrontendRef(String name) {
        // Ensure existence of frontend, creating one if needed
        LoadBalancerFrontendImpl frontend;
        if (name == null) {
            frontend = this.ensureUniqueFrontend();
        } else {
            frontend = this.defineFrontend(name);
            frontend.attach();
        }

        // Return frontend reference
        return new SubResource()
                .withId(this.futureResourceId() + "/frontendIPConfigurations/" + frontend.name());
    }

    protected LoadBalancerFrontendImpl ensureUniqueFrontend() {
        String name = SdkContext.randomResourceName("frontend", 20);
        LoadBalancerFrontendImpl frontend = this.defineFrontend(name);
        frontend.attach();
        return frontend;
    }

    LoadBalancerPrivateFrontend findPrivateFrontendWithSubnet(String networkId, String subnetName) {
        if (null == networkId || null == subnetName) {
            return null;
        } else {
            // Use existing frontend already pointing at this PIP, if any
            for (LoadBalancerPrivateFrontend frontend : this.privateFrontends().values()) {
                if (frontend.networkId() == null || frontend.subnetName() == null) {
                    continue;
                }
                else if (networkId.equalsIgnoreCase(frontend.networkId()) && subnetName.equalsIgnoreCase(frontend.subnetName())) {
                    return frontend;
                }
            }

            return null;
        }
    }

    LoadBalancerPrivateFrontend ensurePrivateFrontendWithSubnet(String networkId, String subnetName) {
        LoadBalancerPrivateFrontend frontend = this.findPrivateFrontendWithSubnet(networkId, subnetName);
        if (networkId == null || subnetName == null) {
            return null;
        } else if (frontend != null) {
            return frontend;
        } else {
            // Create new frontend
            LoadBalancerFrontendImpl fe = this.ensureUniqueFrontend()
                    .withExistingSubnet(networkId, subnetName)
                    .withPrivateIPAddressDynamic();
            fe.attach();
            return fe;
        }
    }

    LoadBalancerPublicFrontend ensurePublicFrontendWithPip(String pipId) {
        LoadBalancerPublicFrontend frontend = this.findFrontendByPublicIPAddress(pipId);
        if (pipId == null) {
            return null;
        } else if (frontend != null) {
            return frontend;
        } else {
            // Create new frontend
            LoadBalancerFrontendImpl fe = this.ensureUniqueFrontend()
                    .withExistingPublicIPAddress(pipId);
            fe.attach();
            return fe;
        }
    }

    @Override
    protected void beforeCreating() {
        // Account for the newly created public IPs
        if (this.creatablePIPKeys != null) {
            for (Entry<String, String> pipFrontendAssociation : this.creatablePIPKeys.entrySet()) {
                PublicIPAddress pip = (PublicIPAddress) this.createdResource(pipFrontendAssociation.getKey());
                if (pip != null) {
                    withExistingPublicIPAddress(pip.id(), pipFrontendAssociation.getValue());
                }
            }
            this.creatablePIPKeys.clear();
        }

        // Reset and update probes
        List<ProbeInner> innerProbes = innersFromWrappers(this.httpProbes.values());
        innerProbes = innersFromWrappers(this.tcpProbes.values(), innerProbes);
        if (innerProbes == null) {
            innerProbes = new ArrayList<>();
        }
        this.inner().withProbes(innerProbes);

        // Reset and update backends
        List<BackendAddressPoolInner> innerBackends = innersFromWrappers(this.backends.values());
        if (null == innerBackends) {
            innerBackends = new ArrayList<>();
        }
        this.inner().withBackendAddressPools(innerBackends);

        // Reset and update frontends
        List<FrontendIPConfigurationInner> innerFrontends = innersFromWrappers(this.frontends.values());
        if (null == innerFrontends) {
            innerFrontends = new ArrayList<>();
        }
        this.inner().withFrontendIPConfigurations(innerFrontends);

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
        if (this.nicsInBackends != null) {
            List<Exception> nicExceptions = new ArrayList<>();

            // Update the NICs to point to the backend pool
            for (Entry<String, String> nicInBackend : this.nicsInBackends.entrySet()) {
                String nicId = nicInBackend.getKey();
                String backendName = nicInBackend.getValue();
                try {
                    NetworkInterface nic = this.manager().networkInterfaces().getById(nicId);
                    NicIPConfiguration nicIP = nic.primaryIPConfiguration();
                    nic.update()
                        .updateIPConfiguration(nicIP.name())
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
    }

    @Override
    protected Observable<LoadBalancerInner> createInner() {
        return this.manager().inner().loadBalancers().createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner());
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
        if (frontend != null) {
            this.frontends.put(frontend.name(), frontend);
        }
        return this;
    }

    LoadBalancerImpl withProbe(LoadBalancerProbeImpl probe) {
        if (probe == null) {
            return this;
        } else if (probe.protocol() == ProbeProtocol.HTTP) {
            httpProbes.put(probe.name(), probe);
        } else if (probe.protocol() == ProbeProtocol.TCP) {
            tcpProbes.put(probe.name(), probe);
        }
        return this;
    }

    LoadBalancerImpl withLoadBalancingRule(LoadBalancingRuleImpl loadBalancingRule) {
        if (loadBalancingRule != null) {
            this.loadBalancingRules.put(loadBalancingRule.name(), loadBalancingRule);
        }
        return this;
    }

    LoadBalancerImpl withInboundNatRule(LoadBalancerInboundNatRuleImpl inboundNatRule) {
        if (inboundNatRule != null) {
            this.inboundNatRules.put(inboundNatRule.name(), inboundNatRule);
        }
        return this;
    }

    LoadBalancerImpl withInboundNatPool(LoadBalancerInboundNatPoolImpl inboundNatPool) {
        if (inboundNatPool != null) {
            this.inboundNatPools.put(inboundNatPool.name(), inboundNatPool);
        }
        return this;
    }

    LoadBalancerImpl withBackend(LoadBalancerBackendImpl backend) {
        if (backend != null) {
            this.backends.put(backend.name(), backend);
        }
        return this;
    }

    // Withers (fluent)

    LoadBalancerImpl withNewPublicIPAddress(String dnsLeafLabel, String frontendName) {
        PublicIPAddress.DefinitionStages.WithGroup precreatablePIP = manager().publicIPAddresses().define(dnsLeafLabel)
                .withRegion(this.regionName());
        Creatable<PublicIPAddress> creatablePip;
        if (super.creatableGroup == null) {
            creatablePip = precreatablePIP.withExistingResourceGroup(this.resourceGroupName()).withLeafDomainLabel(dnsLeafLabel);
        } else {
            creatablePip = precreatablePIP.withNewResourceGroup(super.creatableGroup).withLeafDomainLabel(dnsLeafLabel);
        }
        return withNewPublicIPAddress(creatablePip, frontendName);
    }

    LoadBalancerImpl withNewPublicIPAddress(Creatable<PublicIPAddress> creatablePip, String frontendName) {
        String existingPipFrontendName = this.creatablePIPKeys.get(creatablePip.key());
        if (frontendName == null) {
            if (existingPipFrontendName != null) {
                // Reuse frontend already associated with this PIP
                frontendName = existingPipFrontendName;
            } else {
                // Auto-named unique frontend
                frontendName = ensureUniqueFrontend().name();
            }
        }

        if (existingPipFrontendName == null) {
            // No frontend associated with this PIP yet so create new association
            this.creatablePIPKeys.put(creatablePip.key(), frontendName);
            this.addCreatableDependency(creatablePip);
        } else if (!existingPipFrontendName.equalsIgnoreCase(frontendName)) {
            // Existing PIP definition already in use but under a different frontend, so error
            throw new IllegalArgumentException("This public IP address definition is already associated with a frontend under a different name.");
        }

        return this;
    }

    protected LoadBalancerImpl withExistingPublicIPAddress(String resourceId, String frontendName) {
        if (frontendName == null) {
            return ensureUniqueFrontend()
                    .withExistingPublicIPAddress(resourceId)
                    .parent();
        } else {
            return this.definePublicFrontend(frontendName)
                .withExistingPublicIPAddress(resourceId)
                .attach();
        }
    }

    LoadBalancerImpl withExistingVirtualMachine(HasNetworkInterfaces vm, String backendName) {
        if (backendName != null) {
            this.defineBackend(backendName).attach();
            if (vm.primaryNetworkInterfaceId() != null) {
                this.nicsInBackends.put(vm.primaryNetworkInterfaceId(), backendName.toLowerCase());
            }
        }
        return this;
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

    LoadBalancerFrontendImpl defineFrontend(String name) {
        LoadBalancerFrontend frontend = this.frontends.get(name);

        // Create if non-existent
        if (frontend == null) {
            FrontendIPConfigurationInner inner = new FrontendIPConfigurationInner().withName(name);
            return new LoadBalancerFrontendImpl(inner, this);
        } else {
            return (LoadBalancerFrontendImpl) frontend;
        }
    }

    @Override
    public LoadBalancerBackendImpl defineBackend(String name) {
        LoadBalancerBackend backend = this.backends.get(name);

        // Create if non-existent
        if (backend == null) {
            BackendAddressPoolInner inner = new BackendAddressPoolInner()
                    .withName(name);
            return new LoadBalancerBackendImpl(inner, this);
        } else {
            return (LoadBalancerBackendImpl) backend;
        }
    }

    @Override
    public LoadBalancerImpl withSku(LoadBalancerSkuType skuType) {
        // Note: SKU is not updatable as of now, so this is available only during definition time
        // Service return `SkuCannotBeChangedOnUpdate` upon attempt to change it.
        // Service default is LoadBalancerSkuType.BASIC
        //
        this.inner().withSku(skuType.sku());
        return this;
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
    public LoadBalancerFrontendImpl updatePublicFrontend(String name) {
        return (LoadBalancerFrontendImpl) this.frontends.get(name);
    }

    @Override
    public LoadBalancerFrontendImpl updatePrivateFrontend(String name) {
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
    public LoadBalancerSkuType sku() {
        return LoadBalancerSkuType.fromSku(this.inner().sku());
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
    public Map<String, LoadBalancerPrivateFrontend> privateFrontends() {
        Map<String, LoadBalancerPrivateFrontend> privateFrontends = new HashMap<>();
        for (LoadBalancerFrontend frontend : this.frontends().values()) {
            if (!frontend.isPublic()) {
                privateFrontends.put(frontend.name(), (LoadBalancerPrivateFrontend) frontend);
            }
        }

        return Collections.unmodifiableMap(privateFrontends);
    }

    @Override
    public Map<String, LoadBalancerPublicFrontend> publicFrontends() {
        Map<String, LoadBalancerPublicFrontend> publicFrontends = new HashMap<>();
        for (LoadBalancerFrontend frontend : this.frontends().values()) {
            if (frontend.isPublic()) {
                publicFrontends.put(frontend.name(), (LoadBalancerPublicFrontend) frontend);
            }
        }

        return Collections.unmodifiableMap(publicFrontends);
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
    public List<String> publicIPAddressIds() {
        List<String> publicIPAddressIds = new ArrayList<>();
        for (LoadBalancerFrontend frontend : this.frontends().values()) {
            if (frontend.isPublic()) {
                String pipId = ((LoadBalancerPublicFrontend) frontend).publicIPAddressId();
                publicIPAddressIds.add(pipId);
            }
        }
        return Collections.unmodifiableList(publicIPAddressIds);
    }

    @Override
    public LoadBalancerPublicFrontend findFrontendByPublicIPAddress(String pipId) {
        if (pipId == null) {
            return null;
        }

        // Use existing frontend already pointing at this PIP, if any
        for (LoadBalancerPublicFrontend frontend : this.publicFrontends().values()) {
            if (frontend.publicIPAddressId() == null) {
                continue;
            }
            else if (pipId.equalsIgnoreCase(frontend.publicIPAddressId())) {
                return frontend;
            }
        }

        return null;
    }

    @Override
    public LoadBalancerPublicFrontend findFrontendByPublicIPAddress(PublicIPAddress publicIPAddress) {
        return (publicIPAddress != null) ? this.findFrontendByPublicIPAddress(publicIPAddress.id()) : null;
    }
}