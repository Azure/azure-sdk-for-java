// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.InboundNatPool;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerBackend;
import com.azure.resourcemanager.network.models.LoadBalancerFrontend;
import com.azure.resourcemanager.network.models.LoadBalancerHttpProbe;
import com.azure.resourcemanager.network.models.LoadBalancerInboundNatPool;
import com.azure.resourcemanager.network.models.LoadBalancerInboundNatRule;
import com.azure.resourcemanager.network.models.LoadBalancerPrivateFrontend;
import com.azure.resourcemanager.network.models.LoadBalancerProbe;
import com.azure.resourcemanager.network.models.LoadBalancerPublicFrontend;
import com.azure.resourcemanager.network.models.LoadBalancerSkuType;
import com.azure.resourcemanager.network.models.LoadBalancerTcpProbe;
import com.azure.resourcemanager.network.models.LoadBalancingRule;
import com.azure.resourcemanager.network.models.NicIpConfiguration;
import com.azure.resourcemanager.network.models.ProbeProtocol;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.fluent.models.BackendAddressPoolInner;
import com.azure.resourcemanager.network.fluent.models.FrontendIpConfigurationInner;
import com.azure.resourcemanager.network.models.HasNetworkInterfaces;
import com.azure.resourcemanager.network.fluent.models.InboundNatRuleInner;
import com.azure.resourcemanager.network.fluent.models.LoadBalancerInner;
import com.azure.resourcemanager.network.fluent.models.LoadBalancingRuleInner;
import com.azure.resourcemanager.network.fluent.models.ProbeInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Implementation of the LoadBalancer interface. */
class LoadBalancerImpl
    extends GroupableParentResourceWithTagsImpl<LoadBalancer, LoadBalancerInner, LoadBalancerImpl, NetworkManager>
    implements LoadBalancer, LoadBalancer.Definition, LoadBalancer.Update {

    private final ClientLogger logger = new ClientLogger(getClass());
    private final Map<String, String> nicsInBackends = new HashMap<>();
    protected final Map<String, String> creatablePIPKeys = new HashMap<>();

    private Map<String, LoadBalancerBackend> backends;
    private Map<String, LoadBalancerTcpProbe> tcpProbes;
    private Map<String, LoadBalancerHttpProbe> httpProbes;
    private Map<String, LoadBalancerHttpProbe> httpsProbes;
    private Map<String, LoadBalancingRule> loadBalancingRules;
    private Map<String, LoadBalancerFrontend> frontends;
    private Map<String, LoadBalancerInboundNatRule> inboundNatRules;
    private Map<String, LoadBalancerInboundNatPool> inboundNatPools;

    LoadBalancerImpl(String name, final LoadBalancerInner innerModel, final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
    }

    // Verbs

    @Override
    public Mono<LoadBalancer> refreshAsync() {
        return super
            .refreshAsync()
            .map(
                loadBalancer -> {
                    LoadBalancerImpl impl = (LoadBalancerImpl) loadBalancer;
                    impl.initializeChildrenFromInner();
                    return impl;
                });
    }

    @Override
    protected Mono<LoadBalancerInner> getInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getLoadBalancers()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    protected Mono<LoadBalancerInner> applyTagsToInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getLoadBalancers()
            .updateTagsAsync(resourceGroupName(), name(), innerModel().tags());
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
        String name = this.manager().resourceManager().internalContext().randomResourceName("backend", 20);
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
        return new SubResource().withId(this.futureResourceId() + "/frontendIpConfigurations/" + frontend.name());
    }

    protected LoadBalancerFrontendImpl ensureUniqueFrontend() {
        String name = this.manager().resourceManager().internalContext().randomResourceName("frontend", 20);
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
                } else if (networkId.equalsIgnoreCase(frontend.networkId())
                    && subnetName.equalsIgnoreCase(frontend.subnetName())) {
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
            LoadBalancerFrontendImpl fe =
                this.ensureUniqueFrontend().withExistingSubnet(networkId, subnetName).withPrivateIpAddressDynamic();
            fe.attach();
            return fe;
        }
    }

    LoadBalancerPublicFrontend ensurePublicFrontendWithPip(String pipId) {
        LoadBalancerPublicFrontend frontend = this.findFrontendByPublicIpAddress(pipId);
        if (pipId == null) {
            return null;
        } else if (frontend != null) {
            return frontend;
        } else {
            // Create new frontend
            LoadBalancerFrontendImpl fe = this.ensureUniqueFrontend().withExistingPublicIpAddress(pipId);
            fe.attach();
            return fe;
        }
    }

    @Override
    protected void beforeCreating() {
        // Account for the newly created public IPs
        if (this.creatablePIPKeys != null) {
            for (Entry<String, String> pipFrontendAssociation : this.creatablePIPKeys.entrySet()) {
                PublicIpAddress pip = this.<PublicIpAddress>taskResult(pipFrontendAssociation.getKey());
                if (pip != null) {
                    withExistingPublicIPAddress(pip.id(), pipFrontendAssociation.getValue());
                }
            }
            this.creatablePIPKeys.clear();
        }

        // Reset and update probes
        List<ProbeInner> innerProbes = innersFromWrappers(this.httpProbes.values());
        innerProbes = innersFromWrappers(this.httpsProbes.values(), innerProbes);
        innerProbes = innersFromWrappers(this.tcpProbes.values(), innerProbes);
        if (innerProbes == null) {
            innerProbes = new ArrayList<>();
        }
        this.innerModel().withProbes(innerProbes);

        // Reset and update backends
        List<BackendAddressPoolInner> innerBackends = innersFromWrappers(this.backends.values());
        if (null == innerBackends) {
            innerBackends = new ArrayList<>();
        }
        this.innerModel().withBackendAddressPools(innerBackends);

        // Reset and update frontends
        List<FrontendIpConfigurationInner> innerFrontends = innersFromWrappers(this.frontends.values());
        if (null == innerFrontends) {
            innerFrontends = new ArrayList<>();
        }
        this.innerModel().withFrontendIpConfigurations(innerFrontends);

        // Reset and update inbound NAT rules
        List<InboundNatRuleInner> innerNatRules = innersFromWrappers(this.inboundNatRules.values());
        if (null == innerNatRules) {
            innerNatRules = new ArrayList<>();
        }
        this.innerModel().withInboundNatRules(innerNatRules);

        for (LoadBalancerInboundNatRule natRule : this.inboundNatRules.values()) {
            // Clear deleted frontend references
            SubResource ref = natRule.innerModel().frontendIpConfiguration();
            if (ref != null && !this.frontends().containsKey(ResourceUtils.nameFromResourceId(ref.id()))) {
                natRule.innerModel().withFrontendIpConfiguration(null);
            }
        }

        // Reset and update inbound NAT pools
        List<InboundNatPool> innerNatPools = innersFromWrappers(this.inboundNatPools.values());
        if (null == innerNatPools) {
            innerNatPools = new ArrayList<>();
        }
        this.innerModel().withInboundNatPools(innerNatPools);
        for (LoadBalancerInboundNatPool natPool : this.inboundNatPools.values()) {
            // Clear deleted frontend references
            SubResource ref = natPool.innerModel().frontendIpConfiguration();
            if (ref != null && !this.frontends().containsKey(ResourceUtils.nameFromResourceId(ref.id()))) {
                natPool.innerModel().withFrontendIpConfiguration(null);
            }
        }

        // Reset and update load balancing rules
        List<LoadBalancingRuleInner> innerRules = innersFromWrappers(this.loadBalancingRules.values());
        if (innerRules == null) {
            innerRules = new ArrayList<>();
        }
        this.innerModel().withLoadBalancingRules(innerRules);
        for (LoadBalancingRule lbRule : this.loadBalancingRules.values()) {
            SubResource ref;

            // Clear deleted frontend references
            ref = lbRule.innerModel().frontendIpConfiguration();
            if (ref != null && !this.frontends().containsKey(ResourceUtils.nameFromResourceId(ref.id()))) {
                lbRule.innerModel().withFrontendIpConfiguration(null);
            }

            // Clear deleted backend references
            ref = lbRule.innerModel().backendAddressPool();
            if (ref != null && !this.backends().containsKey(ResourceUtils.nameFromResourceId(ref.id()))) {
                lbRule.innerModel().withBackendAddressPool(null);
            }

            // Clear deleted probe references
            ref = lbRule.innerModel().probe();
            if (ref != null
                && !this.httpProbes().containsKey(ResourceUtils.nameFromResourceId(ref.id()))
                && !this.httpsProbes().containsKey(ResourceUtils.nameFromResourceId(ref.id()))
                && !this.tcpProbes().containsKey(ResourceUtils.nameFromResourceId(ref.id()))) {
                lbRule.innerModel().withProbe(null);
            }
        }
    }

    protected Mono<Void> afterCreatingAsync() {
        if (this.nicsInBackends != null) {
            List<Throwable> nicExceptions = new ArrayList<>();

            return Flux
                .fromIterable(this.nicsInBackends.entrySet())
                .flatMap(
                    nicInBackend -> {
                        String nicId = nicInBackend.getKey();
                        String backendName = nicInBackend.getValue();
                        return this
                            .manager()
                            .networkInterfaces()
                            .getByIdAsync(nicId)
                            .flatMap(
                                nic -> {
                                    NicIpConfiguration nicIP = nic.primaryIPConfiguration();
                                    return nic
                                        .update()
                                        .updateIPConfiguration(nicIP.name())
                                        .withExistingLoadBalancerBackend(this, backendName)
                                        .parent()
                                        .applyAsync();
                                });
                    })
                .onErrorResume(
                    t -> {
                        nicExceptions.add(t);
                        return Mono.empty();
                    })
                .then(
                    Mono
                        .defer(
                            () -> {
                                if (!nicExceptions.isEmpty()) {
                                    return Mono.error(Exceptions.multiple(nicExceptions));
                                } else {
                                    this.nicsInBackends.clear();
                                    return Mono.empty();
                                }
                            }));
        }
        return Mono.empty();
    }

    @Override
    protected Mono<LoadBalancerInner> createInner() {
        return this
            .manager()
            .serviceClient()
            .getLoadBalancers()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel());
    }

    @Override
    public Mono<LoadBalancer> createResourceAsync() {
        beforeCreating();
        return createInner()
            .flatMap(
                inner -> {
                    setInner(inner);
                    initializeChildrenFromInner();
                    return afterCreatingAsync().then(this.refreshAsync());
                });
    }

    private void initializeFrontendsFromInner() {
        this.frontends = new TreeMap<>();
        List<FrontendIpConfigurationInner> frontendsInner = this.innerModel().frontendIpConfigurations();
        if (frontendsInner != null) {
            for (FrontendIpConfigurationInner frontendInner : frontendsInner) {
                LoadBalancerFrontendImpl frontend = new LoadBalancerFrontendImpl(frontendInner, this);
                this.frontends.put(frontendInner.name(), frontend);
            }
        }
    }

    private void initializeBackendsFromInner() {
        this.backends = new TreeMap<>();
        List<BackendAddressPoolInner> backendsInner = this.innerModel().backendAddressPools();
        if (backendsInner != null) {
            for (BackendAddressPoolInner backendInner : backendsInner) {
                LoadBalancerBackendImpl backend = new LoadBalancerBackendImpl(backendInner, this);
                this.backends.put(backendInner.name(), backend);
            }
        }
    }

    private void initializeProbesFromInner() {
        this.httpProbes = new TreeMap<>();
        this.httpsProbes = new TreeMap<>();
        this.tcpProbes = new TreeMap<>();
        if (this.innerModel().probes() != null) {
            for (ProbeInner probeInner : this.innerModel().probes()) {
                LoadBalancerProbeImpl probe = new LoadBalancerProbeImpl(probeInner, this);
                if (probeInner.protocol().equals(ProbeProtocol.TCP)) {
                    this.tcpProbes.put(probeInner.name(), probe);
                } else if (probeInner.protocol().equals(ProbeProtocol.HTTP)) {
                    this.httpProbes.put(probeInner.name(), probe);
                } else if (probeInner.protocol().equals(ProbeProtocol.HTTPS)) {
                    this.httpsProbes.put(probeInner.name(), probe);
                }
            }
        }
    }

    private void initializeLoadBalancingRulesFromInner() {
        this.loadBalancingRules = new TreeMap<>();
        List<LoadBalancingRuleInner> rulesInner = this.innerModel().loadBalancingRules();
        if (rulesInner != null) {
            for (LoadBalancingRuleInner ruleInner : rulesInner) {
                LoadBalancingRuleImpl rule = new LoadBalancingRuleImpl(ruleInner, this);
                this.loadBalancingRules.put(ruleInner.name(), rule);
            }
        }
    }

    private void initializeInboundNatPoolsFromInner() {
        this.inboundNatPools = new TreeMap<>();
        List<InboundNatPool> inners = this.innerModel().inboundNatPools();
        if (inners != null) {
            for (InboundNatPool inner : inners) {
                LoadBalancerInboundNatPoolImpl wrapper = new LoadBalancerInboundNatPoolImpl(inner, this);
                this.inboundNatPools.put(wrapper.name(), wrapper);
            }
        }
    }

    private void initializeInboundNatRulesFromInner() {
        this.inboundNatRules = new TreeMap<>();
        List<InboundNatRuleInner> rulesInner = this.innerModel().inboundNatRules();
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
            .append(this.name())
            .toString();
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
        } else if (probe.protocol() == ProbeProtocol.HTTPS) {
            httpsProbes.put(probe.name(), probe);
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
        PublicIpAddress.DefinitionStages.WithGroup precreatablePIP =
            manager().publicIpAddresses().define(dnsLeafLabel).withRegion(this.regionName());
        Creatable<PublicIpAddress> creatablePip;
        if (super.creatableGroup == null) {
            creatablePip =
                precreatablePIP.withExistingResourceGroup(this.resourceGroupName()).withLeafDomainLabel(dnsLeafLabel);
        } else {
            creatablePip = precreatablePIP.withNewResourceGroup(super.creatableGroup).withLeafDomainLabel(dnsLeafLabel);
        }
        return withNewPublicIPAddress(creatablePip, frontendName);
    }

    LoadBalancerImpl withNewPublicIPAddress(Creatable<PublicIpAddress> creatablePip, String frontendName) {
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
            this.creatablePIPKeys.put(this.addDependency(creatablePip), frontendName);
        } else if (!existingPipFrontendName.equalsIgnoreCase(frontendName)) {
            // Existing PIP definition already in use but under a different frontend, so error
            String exceptionMessage =
                "This public IP address definition is already associated with a frontend under a different name.";
            throw logger.logExceptionAsError(new IllegalArgumentException(exceptionMessage));
        }

        return this;
    }

    protected LoadBalancerImpl withExistingPublicIPAddress(String resourceId, String frontendName) {
        if (frontendName == null) {
            return ensureUniqueFrontend().withExistingPublicIpAddress(resourceId).parent();
        } else {
            return this.definePublicFrontend(frontendName).withExistingPublicIpAddress(resourceId).attach();
        }
    }

    LoadBalancerImpl withExistingVirtualMachine(HasNetworkInterfaces vm, String backendName) {
        if (backendName != null) {
            this.defineBackend(backendName).attach();
            if (vm.primaryNetworkInterfaceId() != null) {
                this.nicsInBackends.put(vm.primaryNetworkInterfaceId(), backendName.toLowerCase(Locale.ROOT));
            }
        }
        return this;
    }

    @Override
    public LoadBalancerProbeImpl defineTcpProbe(String name) {
        LoadBalancerProbe probe = this.tcpProbes.get(name);
        if (probe == null) {
            ProbeInner inner = new ProbeInner().withName(name).withProtocol(ProbeProtocol.TCP);
            return new LoadBalancerProbeImpl(inner, this);
        } else {
            return (LoadBalancerProbeImpl) probe;
        }
    }

    @Override
    public LoadBalancerProbeImpl defineHttpProbe(String name) {
        LoadBalancerProbe probe = this.httpProbes.get(name);
        if (probe == null) {
            ProbeInner inner = new ProbeInner().withName(name).withProtocol(ProbeProtocol.HTTP).withPort(80);
            return new LoadBalancerProbeImpl(inner, this);
        } else {
            return (LoadBalancerProbeImpl) probe;
        }
    }

    @Override
    public LoadBalancerProbeImpl defineHttpsProbe(String name) {
        LoadBalancerProbe probe = this.httpsProbes.get(name);
        if (probe == null) {
            ProbeInner inner = new ProbeInner().withName(name).withProtocol(ProbeProtocol.HTTPS).withPort(443);
            return new LoadBalancerProbeImpl(inner, this);
        } else {
            return (LoadBalancerProbeImpl) probe;
        }
    }

    @Override
    public LoadBalancingRuleImpl defineLoadBalancingRule(String name) {
        LoadBalancingRule lbRule = this.loadBalancingRules.get(name);
        if (lbRule == null) {
            LoadBalancingRuleInner inner = new LoadBalancingRuleInner().withName(name);
            return new LoadBalancingRuleImpl(inner, this);
        } else {
            return (LoadBalancingRuleImpl) lbRule;
        }
    }

    @Override
    public LoadBalancerInboundNatRuleImpl defineInboundNatRule(String name) {
        LoadBalancerInboundNatRule natRule = this.inboundNatRules.get(name);
        if (natRule == null) {
            InboundNatRuleInner inner = new InboundNatRuleInner().withName(name);
            return new LoadBalancerInboundNatRuleImpl(inner, this);
        } else {
            return (LoadBalancerInboundNatRuleImpl) natRule;
        }
    }

    @Override
    public LoadBalancerInboundNatPoolImpl defineInboundNatPool(String name) {
        LoadBalancerInboundNatPool natPool = this.inboundNatPools.get(name);
        if (natPool == null) {
            InboundNatPool inner = new InboundNatPool().withName(name);
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
            FrontendIpConfigurationInner inner = new FrontendIpConfigurationInner().withName(name);
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
            BackendAddressPoolInner inner = new BackendAddressPoolInner().withName(name);
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
        this.innerModel().withSku(skuType.sku());
        return this;
    }

    @Override
    public LoadBalancerImpl withoutProbe(String name) {
        if (this.httpProbes.containsKey(name)) {
            this.httpProbes.remove(name);
        } else if (this.httpsProbes.containsKey(name)) {
            this.httpsProbes.remove(name);
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
    public LoadBalancerProbeImpl updateHttpsProbe(String name) {
        return (LoadBalancerProbeImpl) this.httpsProbes.get(name);
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
        return LoadBalancerSkuType.fromSku(this.innerModel().sku());
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
    public Map<String, LoadBalancerHttpProbe> httpsProbes() {
        return Collections.unmodifiableMap(this.httpsProbes);
    }

    @Override
    public Map<String, LoadBalancingRule> loadBalancingRules() {
        return Collections.unmodifiableMap(this.loadBalancingRules);
    }

    @Override
    public List<String> publicIpAddressIds() {
        List<String> publicIPAddressIds = new ArrayList<>();
        for (LoadBalancerFrontend frontend : this.frontends().values()) {
            if (frontend.isPublic()) {
                String pipId = ((LoadBalancerPublicFrontend) frontend).publicIpAddressId();
                publicIPAddressIds.add(pipId);
            }
        }
        return Collections.unmodifiableList(publicIPAddressIds);
    }

    @Override
    public LoadBalancerPublicFrontend findFrontendByPublicIpAddress(String pipId) {
        if (pipId == null) {
            return null;
        }

        // Use existing frontend already pointing at this PIP, if any
        for (LoadBalancerPublicFrontend frontend : this.publicFrontends().values()) {
            if (frontend.publicIpAddressId() == null) {
                continue;
            } else if (pipId.equalsIgnoreCase(frontend.publicIpAddressId())) {
                return frontend;
            }
        }

        return null;
    }

    @Override
    public LoadBalancerPublicFrontend findFrontendByPublicIpAddress(PublicIpAddress publicIPAddress) {
        return (publicIPAddress != null) ? this.findFrontendByPublicIpAddress(publicIPAddress.id()) : null;
    }
}
