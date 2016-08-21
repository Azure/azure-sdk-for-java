/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.network.Backend;
import com.microsoft.azure.management.network.Frontend;
import com.microsoft.azure.management.network.HttpProbe;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancingRule;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NicIpConfiguration;
import com.microsoft.azure.management.network.Probe;
import com.microsoft.azure.management.network.TcpProbe;
import com.microsoft.azure.management.network.ProbeProtocol;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddress.DefinitionStages.WithGroup;
import com.microsoft.azure.management.network.SupportsNetworkInterfaces;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.ResourceServiceCall;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

/**
 * Implementation of the LoadBalancer interface.
 */
class LoadBalancerImpl
    extends GroupableResourceImpl<
        LoadBalancer,
        LoadBalancerInner,
        LoadBalancerImpl,
        NetworkManager>
    implements
        LoadBalancer,
        LoadBalancer.Definition,
        LoadBalancer.Update {

    static final String DEFAULT = "default";
    private final LoadBalancersInner innerCollection;
    private final HashMap<String, String> nicsInBackends = new HashMap<>();
    private final HashMap<String, String> creatablePIPKeys = new HashMap<>();
    private final TreeMap<String, Backend> backends = new TreeMap<>();
    private final TreeMap<String, TcpProbe> tcpProbes = new TreeMap<>();
    private final TreeMap<String, HttpProbe> httpProbes = new TreeMap<>();
    private final TreeMap<String, LoadBalancingRule> loadBalancingRules = new TreeMap<>();
    private final TreeMap<String, Frontend> frontends = new TreeMap<>();

    LoadBalancerImpl(String name,
            final LoadBalancerInner innerModel,
            final LoadBalancersInner innerCollection,
            final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.innerCollection = innerCollection;
        initializeFrontendsFromInner();
        initializeProbesFromInner();
        initializeBackendsFromInner();
        initializeLoadBalancingRulesFromInner();
    }

    // Verbs

    @Override
    public LoadBalancerImpl refresh() throws Exception {
        ServiceResponse<LoadBalancerInner> response =
            this.innerCollection.get(this.resourceGroupName(), this.name());
        this.setInner(response.getBody());
        initializeFrontendsFromInner();
        initializeProbesFromInner();
        initializeBackendsFromInner();
        initializeLoadBalancingRulesFromInner();
        return this;
    }

    @Override
    public LoadBalancer apply() throws Exception {
        return this.create();
    }

    @Override
    public ServiceCall<LoadBalancer> applyAsync(ServiceCallback<LoadBalancer> callback) {
        return createAsync(callback);
    }

    // Helpers

    // CreatorTaskGroup.ResourceCreator implementation

    @Override
    public Resource createResource() throws Exception {
        beforeCreating();
        ServiceResponse<LoadBalancerInner> response =
                this.innerCollection.createOrUpdate(this.resourceGroupName(), this.name(), this.inner());
        this.setInner(response.getBody());
        afterCreating();
        return this;
    }

    private void beforeCreating()  {
        // Account for the newly created public IPs
        for (Entry<String, String> pipFrontendAssociation : this.creatablePIPKeys.entrySet()) {
            PublicIpAddress pip = (PublicIpAddress) this.createdResource(pipFrontendAssociation.getKey());
            if (pip != null) {
                withExistingPublicIpAddress(pip.id(), pipFrontendAssociation.getValue());
            }
        }
        this.creatablePIPKeys.clear();

        // Reset and update backends
        if (this.backends.size() > 0) {
            this.inner().withBackendAddressPools(null);
            List<BackendAddressPoolInner> backendsInner = ensureInnerBackends();
            for (Backend backend : this.backends.values()) {
                backendsInner.add(backend.inner());
            }
        }

        // Reset and update frontends
        if (this.frontends.size() > 0) {
            this.inner().withFrontendIPConfigurations(null);
            List<FrontendIPConfigurationInner> frontendsInner = ensureInnerFrontends();
            for (Frontend frontend : this.frontends.values()) {
                frontendsInner.add(frontend.inner());
            }
        }

        // Reset and update load balancing rules
        if (this.loadBalancingRules.size() > 0) {
            this.inner().withLoadBalancingRules(null);
            List<LoadBalancingRuleInner> rulesInner = ensureInnerLoadBalancingRules();
            for (LoadBalancingRule lbRule : this.loadBalancingRules.values()) {
                rulesInner.add(lbRule.inner());

                // Clear deleted frontend references
                SubResource frontendRef = lbRule.inner().frontendIPConfiguration();
                if (frontendRef != null
                        && !this.frontends().containsKey(ResourceUtils.nameFromResourceId(frontendRef.id()))) {
                    lbRule.inner().withFrontendIPConfiguration(null);
                }

                // Clear deleted backend references
                SubResource backendRef = lbRule.inner().backendAddressPool();
                if (backendRef != null
                        && !this.backends().containsKey(ResourceUtils.nameFromResourceId(backendRef.id()))) {
                    lbRule.inner().withBackendAddressPool(null);
                }

                // Clear deleted probe references
                SubResource probeRef = lbRule.inner().probe();
                if (probeRef != null
                        && !this.httpProbes().containsKey(ResourceUtils.nameFromResourceId(probeRef.id()))
                        && !this.tcpProbes().containsKey(ResourceUtils.nameFromResourceId(probeRef.id()))) {
                    lbRule.inner().withProbe(null);
                }
            }
        }
    }

    private void afterCreating() throws Exception {
        // Update the NICs to point to the backend pool
        for (Entry<String, String> nicInBackend : this.nicsInBackends.entrySet()) {
            String nicId = nicInBackend.getKey();
            String backendName = nicInBackend.getValue();
            NetworkInterface nic = this.myManager().networkInterfaces().getById(nicId);
            NicIpConfiguration nicIp = nic.primaryIpConfiguration();
            nic.update()
                .updateIpConfiguration(nicIp.name())
                    .withExistingLoadBalancer(this)
                    .withBackendAddressPool(backendName)
                    .parent()
                .apply();
        }

        this.nicsInBackends.clear();
        this.refresh();
    }

    private void initializeFrontendsFromInner() {
        this.frontends.clear();
        List<FrontendIPConfigurationInner> frontendsInner = this.inner().frontendIPConfigurations();
        if (frontendsInner != null) {
            for (FrontendIPConfigurationInner frontendInner : frontendsInner) {
                FrontendImpl frontend = new FrontendImpl(frontendInner, this);
                this.frontends.put(frontendInner.name(), frontend);
            }
        }
    }

    private void initializeBackendsFromInner() {
        this.backends.clear();
        List<BackendAddressPoolInner> backendsInner = this.inner().backendAddressPools();
        if (backendsInner != null) {
            for (BackendAddressPoolInner backendInner : backendsInner) {
                BackendImpl backend = new BackendImpl(backendInner, this);
                this.backends.put(backendInner.name(), backend);
            }
        }
    }

    private void initializeProbesFromInner() {
        this.httpProbes.clear();
        this.tcpProbes.clear();
        if (this.inner().probes() != null) {
            for (ProbeInner probeInner : this.inner().probes()) {
                ProbeImpl probe = new ProbeImpl(probeInner, this);
                if (probeInner.protocol().equals(ProbeProtocol.TCP)) {
                    this.tcpProbes.put(probeInner.name(), probe);
                } else if (probeInner.protocol().equals(ProbeProtocol.HTTP)) {
                    this.httpProbes.put(probeInner.name(), probe);
                }
            }
        }
    }

    private void initializeLoadBalancingRulesFromInner() {
        this.loadBalancingRules.clear();
        List<LoadBalancingRuleInner> rulesInner = this.inner().loadBalancingRules();
        if (rulesInner != null) {
            for (LoadBalancingRuleInner ruleInner : rulesInner) {
                LoadBalancingRuleImpl rule = new LoadBalancingRuleImpl(ruleInner, this);
                this.loadBalancingRules.put(ruleInner.name(), rule);
            }
        }
    }

    NetworkManager myManager() {
        return this.myManager;
    }

    private List<FrontendIPConfigurationInner> ensureInnerFrontends() {
        List<FrontendIPConfigurationInner> frontendsInner = this.inner().frontendIPConfigurations();
        if (frontendsInner == null) {
            frontendsInner = new ArrayList<>();
            this.inner().withFrontendIPConfigurations(frontendsInner);
        }

        return frontendsInner;
    }

    private List<ProbeInner> ensureInnerProbes() {
        List<ProbeInner> probes = this.inner().probes();
        if (probes == null) {
            probes = new ArrayList<>();
            this.inner().withProbes(probes);
        }

        return probes;
    }

    private List<LoadBalancingRuleInner> ensureInnerLoadBalancingRules() {
        List<LoadBalancingRuleInner> rules = this.inner().loadBalancingRules();
        if (rules == null) {
            rules = new ArrayList<>();
            this.inner().withLoadBalancingRules(rules);
        }

        return rules;
    }

    private List<BackendAddressPoolInner> ensureInnerBackends() {
        List<BackendAddressPoolInner> backends = this.inner().backendAddressPools();
        if (backends == null) {
            backends = new ArrayList<>();
            this.inner().withBackendAddressPools(backends);
        }

        return backends;
    }

    String futureResourceId() {
        return new StringBuilder()
                .append(super.resourceIdBase())
                .append("/providers/Microsoft.Network/loadBalancers/")
                .append(this.name()).toString();
    }

    LoadBalancerImpl withFrontend(FrontendImpl frontend) {
        this.frontends.put(frontend.name(), frontend);
        return this;
    }

    LoadBalancerImpl withProbe(ProbeImpl probe) {
        ensureInnerProbes().add(probe.inner());
        if (probe.protocol() == ProbeProtocol.HTTP) {
            httpProbes.put(probe.name(), probe);
        } else if (probe.protocol() == ProbeProtocol.TCP) {
            tcpProbes.put(probe.name(), probe);
        }
        return this;
    }

    LoadBalancerImpl withLoadBalancingRule(LoadBalancingRuleImpl loadBalancingRule) {
        this.loadBalancingRules.put(loadBalancingRule.name(), loadBalancingRule);
        return this;
    }

    LoadBalancerImpl withBackend(BackendImpl backend) {
        this.backends.put(backend.name(), backend);
        return this;
    }

    // Withers (fluent)

    @Override
    public LoadBalancerImpl withNewPublicIpAddress() {
        // Autogenerated DNS leaf label for the PIP
        String dnsLeafLabel = this.name().toLowerCase().replace("\\s", "");
        return withNewPublicIpAddress(dnsLeafLabel);
    }

    @Override
    public LoadBalancerImpl withNewPublicIpAddress(String dnsLeafLabel) {
        WithGroup precreatablePIP = myManager().publicIpAddresses().define(dnsLeafLabel)
                .withRegion(this.regionName());
        Creatable<PublicIpAddress> creatablePip;
        if (super.creatableGroup == null) {
            creatablePip = precreatablePIP.withExistingResourceGroup(this.resourceGroupName());
        } else {
            creatablePip = precreatablePIP.withNewResourceGroup(super.creatableGroup);
        }

        return withNewPublicIpAddress(creatablePip);
    }

    @Override
    public final LoadBalancerImpl withNewPublicIpAddress(Creatable<PublicIpAddress> creatablePIP) {
        this.creatablePIPKeys.put(creatablePIP.key(), DEFAULT);
        this.addCreatableDependency(creatablePIP);
        return this;
    }

    @Override
    public LoadBalancerImpl withExistingPublicIpAddresses(PublicIpAddress... publicIpAddresses) {
        for (PublicIpAddress pip : publicIpAddresses) {
            withExistingPublicIpAddress(pip.id(), DEFAULT);
        }

        return this;
    }

    private LoadBalancerImpl withExistingPublicIpAddress(String resourceId, String frontendName) {
        if (frontendName == null) {
            frontendName = DEFAULT;
        }

        return this.defineInternetFrontend(frontendName)
                .withExistingPublicIpAddress(resourceId)
                .attach();
    }

    private LoadBalancerImpl withExistingVirtualMachine(SupportsNetworkInterfaces vm, String backendName) {
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
    public LoadBalancerImpl withExistingVirtualMachines(SupportsNetworkInterfaces... vms) {
        if (vms != null) {
            for (SupportsNetworkInterfaces vm : vms) {
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
    public ProbeImpl defineTcpProbe(String name) {
        Probe probe = this.tcpProbes.get(name);
        if (probe == null) {
            ProbeInner inner = new ProbeInner()
                .withName(name)
                .withProtocol(ProbeProtocol.TCP);
            return new ProbeImpl(inner, this);
        } else {
            return (ProbeImpl) probe;
        }
    }

    @Override
    public ProbeImpl defineHttpProbe(String name) {
        Probe probe = this.httpProbes.get(name);
        if (probe == null) {
            ProbeInner inner = new ProbeInner()
                .withName(name)
                .withProtocol(ProbeProtocol.HTTP)
                .withPort(80);
            return new ProbeImpl(inner, this);
        } else {
            return (ProbeImpl) probe;
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
    public FrontendImpl defineInternetFrontend(String name) {
        Frontend frontend = this.frontends.get(name);
        if (frontend == null) {
            FrontendIPConfigurationInner inner = new FrontendIPConfigurationInner()
                    .withName(name);
            return new FrontendImpl(inner, this);
        } else {
            return (FrontendImpl) frontend;
        }
    }

    @Override
    public BackendImpl defineBackend(String name) {
        Backend backend = this.backends.get(name);
        if (backend == null) {
            BackendAddressPoolInner inner = new BackendAddressPoolInner()
                    .withName(name);
            return new BackendImpl(inner, this);
        } else {
            return (BackendImpl) backend;
        }
    }

    @Override
    public LoadBalancerImpl withoutFrontend(String name) {
        Frontend frontend = this.frontends.get(name);
        this.frontends.remove(name);

        final String frontendId;
        if (frontend != null) {
            frontendId = frontend.inner().id();
        } else {
            frontendId = null;
        }

        // Remove references from inbound NAT rules
        List<InboundNatRuleInner> natRulesInner = this.inner().inboundNatRules();
        if (natRulesInner != null && frontendId != null) {
            for (InboundNatRuleInner natRuleInner : natRulesInner) {
                final SubResource frontendRef = natRuleInner.frontendIPConfiguration();
                if (frontendRef != null && frontendRef.id().equalsIgnoreCase(frontendId)) {
                    natRuleInner.withFrontendIPConfiguration(null);
                }
            }
        }

        return this;
    }

    @Override
    public LoadBalancerImpl withoutProbe(String name) {
        if (this.httpProbes.containsKey(name)) {
            this.httpProbes.remove(name);
        } else if (this.tcpProbes.containsKey(name)) {
            this.tcpProbes.remove(name);
        }

        List<ProbeInner> probes = this.inner().probes();
        if (probes != null) {
            for (int i = 0; i < probes.size(); i++) {
                if (probes.get(i).name().equalsIgnoreCase(name)) {
                    probes.remove(i);
                    break;
                }
            }
        }

        return this;
    }

    @Override
    public ProbeImpl updateTcpProbe(String name) {
        return (ProbeImpl) this.tcpProbes.get(name);
    }

    @Override
    public BackendImpl updateBackend(String name) {
        return (BackendImpl) this.backends.get(name);
    }

    @Override
    public FrontendImpl updateInternetFrontend(String name) {
        return (FrontendImpl) this.frontends.get(name);
    }

    @Override
    public ProbeImpl updateHttpProbe(String name) {
        return (ProbeImpl) this.httpProbes.get(name);
    }

    @Override
    public LoadBalancingRuleImpl updateLoadBalancingRule(String name) {
        return (LoadBalancingRuleImpl) this.loadBalancingRules.get(name);
    }

    @Override
    public LoadBalancerImpl withoutLoadBalancingRule(String name) {
        if (this.loadBalancingRules.containsKey(name)) {
           this.loadBalancingRules.remove(name);
        }

        List<LoadBalancingRuleInner> rulesInner = this.inner().loadBalancingRules();
        if (rulesInner != null) {
            for (int i = 0; i < rulesInner.size(); i++) {
                if (rulesInner.get(i).name().equalsIgnoreCase(name)) {
                    rulesInner.remove(i);
                    break;
                }
            }
        }

        return this;
    }

    @Override
    public LoadBalancerImpl withoutBackend(String name) {
        // Remove from cache
        Backend backend = this.backends().get(name);
        this.backends.remove(name);
        final String backendId;
        if (backend != null) {
            backendId = backend.inner().id();
        } else {
            backendId = null;
        }

        // Remove any outbound NAT rules to it
        // TODO Revisit when full outbound NAT rule is done
        List<OutboundNatRuleInner> outboundNatsInner = this.inner().outboundNatRules();
        if (outboundNatsInner != null && backendId != null) {
            for (OutboundNatRuleInner outboundNatInner : outboundNatsInner) {
                SubResource backendRef = outboundNatInner.backendAddressPool();
                if (backendRef != null && backendRef.id().equalsIgnoreCase(backendId)) {
                    outboundNatInner.withBackendAddressPool(null);
                }
            }
        }

        return this;
    }

    // Getters

    @Override
    public Map<String, Backend> backends() {
        return Collections.unmodifiableMap(this.backends);
    }

    @Override
    public Map<String, TcpProbe> tcpProbes() {
        return Collections.unmodifiableMap(this.tcpProbes);
    }

    @Override
    public Map<String, Frontend> frontends() {
        return Collections.unmodifiableMap(this.frontends);
    }

    @Override
    public Map<String, HttpProbe> httpProbes() {
        return Collections.unmodifiableMap(this.httpProbes);
    }

    @Override
    public Map<String, LoadBalancingRule> loadBalancingRules() {
        return Collections.unmodifiableMap(this.loadBalancingRules);
    }

    @Override
    public List<String> publicIpAddressIds() {
        List<String> publicIpAddressIds = new ArrayList<>();
        if (this.inner().frontendIPConfigurations() != null) {
            for (FrontendIPConfigurationInner frontEndIpConfig : this.inner().frontendIPConfigurations()) {
                publicIpAddressIds.add(frontEndIpConfig.publicIPAddress().id());
            }
        }
        return Collections.unmodifiableList(publicIpAddressIds);
    }

    public ServiceCall<Resource> createResourceAsync(final ServiceCallback<Resource> callback)  {
        beforeCreating();
        ResourceServiceCall<LoadBalancer, LoadBalancerInner, LoadBalancerImpl> serviceCall = new ResourceServiceCall<>(this);
        serviceCall.withSuccessHandler(new ResourceServiceCall.SuccessHandler<LoadBalancerInner>() {
            @Override
            public void success(ServiceResponse<LoadBalancerInner> response) {
                try {
                    afterCreating();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner(), serviceCall.wrapCallBack(callback));
        return serviceCall;
    }
}