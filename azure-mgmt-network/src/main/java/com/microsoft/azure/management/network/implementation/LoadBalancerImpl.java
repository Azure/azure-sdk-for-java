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

    private final LoadBalancersInner innerCollection;
    private final HashMap<String, String> nicsInBackends = new HashMap<>();
    private List<String> creatablePIPKeys = new ArrayList<>();
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
    public LoadBalancerImpl apply() throws Exception {
        return this.create();
    }

    @Override
    public ServiceCall applyAsync(ServiceCallback<LoadBalancer> callback) {
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

    @Override
    public ServiceCall createResourceAsync(final ServiceCallback<Resource> callback)  {
        final LoadBalancerImpl self = this;
        beforeCreating();
        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner(),
                new ServiceCallback<LoadBalancerInner>() {
                    @Override
                    public void failure(Throwable t) {
                        callback.failure(t);
                    }

                    @Override
                    public void success(ServiceResponse<LoadBalancerInner> response) {
                        self.setInner(response.getBody());
                        callback.success(new ServiceResponse<Resource>(self, response.getResponse()));
                        try {
                            afterCreating();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void beforeCreating()  {
        // Ensure existence of backends for the VMs to be associated with
        for (String backendName : this.nicsInBackends.values()) {
            if (!this.backends().containsKey(backendName)) {
                this.withBackend(backendName);
            }
        }

        // Account for the newly created public IPs
        for (String pipKey : this.creatablePIPKeys) {
            PublicIpAddress pip = (PublicIpAddress) this.createdResource(pipKey);
            if (pip != null) {
                withExistingPublicIpAddress(pip);
            }
        }
        this.creatablePIPKeys.clear();

        // Connect the load balancing rules to the defaults
        if (this.inner().loadBalancingRules() != null) {
            for (LoadBalancingRuleInner lbRule : this.inner().loadBalancingRules()) {
                if (lbRule.frontendIPConfiguration() == null) {
                    // If no reference to frontend IP config yet, add reference to the first frontend IP config
                    String frontendIpConfigName = this.inner().frontendIPConfigurations().get(0).name();
                    SubResource frontendIpConfigReference = new SubResource()
                            .withId(this.futureResourceId() + "/frontendIPConfigurations/" + frontendIpConfigName);
                    lbRule.withFrontendIPConfiguration(frontendIpConfigReference);
                }

                if (lbRule.backendAddressPool() == null) {
                    // If no reference to a back end pool, then add reference to the first back end address pool
                    String backendPoolName = this.inner().backendAddressPools().get(0).name();
                    SubResource backendPoolReference = new SubResource()
                            .withId(this.futureResourceId() + "/backendAddressPools/" + backendPoolName);
                    lbRule.withBackendAddressPool(backendPoolReference);
                }

                if (lbRule.probe() == null) {
                    // If no probe assigned, add a reference to the first one
                    String probeName = this.inner().probes().get(0).name();
                    SubResource probeReference = new SubResource()
                            .withId(this.futureResourceId() + "/probes/" + probeName);
                    lbRule.withProbe(probeReference);
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
        if (this.inner().frontendIPConfigurations() != null) {
            for (FrontendIPConfigurationInner frontendInner : this.inner().frontendIPConfigurations()) {
                FrontendImpl frontend = new FrontendImpl(frontendInner, this);
                this.frontends.put(frontendInner.name(), frontend);
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

    private void initializeBackendsFromInner() {
        this.backends.clear();
        List<BackendAddressPoolInner> backendsInner = this.inner().backendAddressPools();
        if (backendsInner != null) {
            for (BackendAddressPoolInner backendInner : backendsInner) {
                BackendImpl backend = new BackendImpl(backendInner.name(), backendInner, this);
                this.backends.put(backendInner.name(), backend);
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

    private List<FrontendIPConfigurationInner> ensureFrontends() {
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

    private String futureResourceId() {
        return new StringBuilder()
                .append(super.resourceIdBase())
                .append("/providers/Microsoft.Network/loadBalancers/")
                .append(this.name()).toString();
    }

    LoadBalancerImpl withFrontend(FrontendImpl frontend) {
        ensureFrontends().add(frontend.inner());
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
        ensureInnerLoadBalancingRules().add(loadBalancingRule.inner());
        this.loadBalancingRules.put(loadBalancingRule.name(), loadBalancingRule);
        return this;
    }

    LoadBalancerImpl withBackend(BackendImpl backend) {
        ensureInnerBackends().add(backend.inner());
        return this;
    }

    // Withers (fluent)

    @Override
    public LoadBalancerImpl withNewFrontendPublicIpAddressAsFrontend() {
        // Autogenerated DNS leaf label for the PIP
        String dnsLeafLabel = this.name().toLowerCase().replace("\\s", "");
        return withNewFrontendPublicIpAddressAsFrontend(dnsLeafLabel);
    }

    @Override
    public LoadBalancerImpl withNewFrontendPublicIpAddressAsFrontend(String dnsLeafLabel) {
        WithGroup precreatablePIP = myManager().publicIpAddresses().define(dnsLeafLabel)
                .withRegion(this.regionName());
        Creatable<PublicIpAddress> creatablePip;
        if (super.creatableGroup == null) {
            creatablePip = precreatablePIP.withExistingResourceGroup(this.resourceGroupName());
        } else {
            creatablePip = precreatablePIP.withNewResourceGroup(super.creatableGroup);
        }

        return withNewFrontendPublicIpAddressAsFrontend(creatablePip);
    }

    @Override
    public final LoadBalancerImpl withNewFrontendPublicIpAddressAsFrontend(Creatable<PublicIpAddress> creatablePIP) {
        this.creatablePIPKeys.add(creatablePIP.key());
        this.addCreatableDependency(creatablePIP);
        return this;
    }

    @Override
    public LoadBalancerImpl withExistingPublicIpAddressesAsFrontend(PublicIpAddress... publicIpAddresses) {
        return this.withExistingPublicIpAddressesAsFrontend(null, publicIpAddresses);
    }

    @Override
    public LoadBalancerImpl withExistingPublicIpAddressesAsFrontend(
            String frontendName,
            PublicIpAddress... publicIpAddresses) {

        if (frontendName == null) {
            frontendName = "frontend" + (this.frontends.size() + 1);
        }

        for (PublicIpAddress pip : publicIpAddresses) {
            withExistingPublicIpAddress(pip.id(), frontendName);
        }

        return this;
    }

    private LoadBalancerImpl withExistingPublicIpAddress(PublicIpAddress publicIpAddress) {
        return this.withExistingPublicIpAddress(publicIpAddress.id());
    }

    private LoadBalancerImpl withExistingPublicIpAddress(String resourceId) {
        return this.withExistingPublicIpAddress(resourceId, null);
    }

    private LoadBalancerImpl withExistingPublicIpAddress(String resourceId, String frontendName) {
        if (frontendName == null) {
            frontendName = ResourceUtils.nameFromResourceId(resourceId);
        }

        return this.defineInternetFrontend(frontendName)
                .withExistingPublicIpAddress(resourceId)
                .attach();
    }

    private LoadBalancerImpl withExistingVirtualMachine(SupportsNetworkInterfaces vm, String backendName) {
        if (vm.primaryNetworkInterfaceId() != null) {
            this.nicsInBackends.put(vm.primaryNetworkInterfaceId(), backendName.toLowerCase());
        }
        return this;
    }

    @Override
    public LoadBalancerImpl withExistingVirtualMachinesAsBackend(SupportsNetworkInterfaces... vms) {
        return this.withExistingVirtualMachinesAsBackend(null, vms);
    }

    @Override public LoadBalancerImpl withExistingVirtualMachinesAsBackend(String backendName, SupportsNetworkInterfaces... vms) {
        if (backendName == null) {
            backendName = "backend" + (this.backends().size() + 1);
        }

        if (vms != null) {
            for (SupportsNetworkInterfaces vm : vms) {
                withExistingVirtualMachine(vm, backendName);
            }
        }
        return this;
    }

    @Override
    public LoadBalancerImpl withLoadBalancingRule(int port, TransportProtocol protocol, String name) {
        return this.withLoadBalancingRule(port, protocol, port, name);
    }

    @Override
    public LoadBalancerImpl withLoadBalancingRule(
            int frontendPort,
            TransportProtocol protocol,
            int backendPort,
            String name) {

        if (name == null) {
            // Auto-generate a name
            name = "rule" + (this.loadBalancingRules.size() + 1);
        }

        this.defineLoadBalancingRule(name)
            .withFrontendPort(frontendPort)
            .withBackendPort(backendPort)
            .withProtocol(protocol)
            .attach();
        return this;
    }

    @Override
    public LoadBalancerImpl withLoadBalancingRule(int frontendPort, TransportProtocol protocol, int backendPort) {
        return withLoadBalancingRule(frontendPort, protocol, backendPort, null);
    }

    @Override
    public LoadBalancerImpl withLoadBalancingRule(int port, TransportProtocol protocol) {
        return withLoadBalancingRule(port, protocol, port);
    }

    @Override
    public LoadBalancerImpl withTcpProbe(int port) {
        return withTcpProbe(port, null);
    }

    @Override
    public LoadBalancerImpl withTcpProbe(int port, String name) {
        if (name == null) {
            name = "probe" + (this.tcpProbes.size() + 1);
        }

        return this.defineTcpProbe(name)
                .withPort(port)
                .attach();
    }

    @Override
    public LoadBalancerImpl withHttpProbe(String path) {
        return withHttpProbe(path, null);
    }

    @Override
    public LoadBalancerImpl withHttpProbe(String path, String name) {
        if (name == null) {
            name = "probe" + (this.httpProbes.size() + 1);
        }

        return this.defineHttpProbe(name)
            .withRequestPath(path)
            .withPort(80)
            .attach();
    }

    @Override
    public ProbeImpl defineTcpProbe(String name) {
        ProbeInner inner = new ProbeInner()
                .withName(name)
                .withProtocol(ProbeProtocol.TCP);
        return new ProbeImpl(inner, this);
    }

    @Override
    public ProbeImpl defineHttpProbe(String name) {
        ProbeInner inner = new ProbeInner()
                .withName(name)
                .withProtocol(ProbeProtocol.HTTP)
                .withPort(80);
        return new ProbeImpl(inner, this);
    }

    @Override
    public LoadBalancingRuleImpl defineLoadBalancingRule(String name) {
        LoadBalancingRuleInner inner = new LoadBalancingRuleInner()
                .withName(name);
        return new LoadBalancingRuleImpl(inner, this);
    }

    @Override
    public FrontendImpl defineInternetFrontend(String name) {
        FrontendIPConfigurationInner inner = new FrontendIPConfigurationInner()
                .withName(name);
        return new FrontendImpl(inner, this);
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

        List<FrontendIPConfigurationInner> inners = this.inner().frontendIPConfigurations();
        if (inners != null) {
            for (int i = 0; i < inners.size(); i++) {
                if (inners.get(i).name().equalsIgnoreCase(name)) {
                    inners.remove(i);
                    break;
                }
            }
        }

        // Remove references from LB rules
        List<LoadBalancingRuleInner> lbRulesInner = this.inner().loadBalancingRules();
        if (lbRulesInner != null && frontendId != null) {
            for (LoadBalancingRuleInner lbRuleInner : lbRulesInner) {
                final SubResource frontendRef = lbRuleInner.frontendIPConfiguration();
                if (frontendRef != null && frontendRef.id().equalsIgnoreCase(frontendId)) {
                    lbRuleInner.withFrontendIPConfiguration(null);
                }
            }
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
        Probe probe = null;
        if (this.httpProbes.containsKey(name)) {
            probe = this.httpProbes.get(name);
            this.httpProbes.remove(name);
        } else if (this.tcpProbes.containsKey(name)) {
            probe = this.tcpProbes.get(name);
            this.tcpProbes.remove(name);
        }

        final String probeId;
        if (probe != null) {
            probeId = probe.inner().id();
        } else {
            probeId = null;
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

        // Remove probe referenced from load balancing rules, if any
        List<LoadBalancingRuleInner> lbRulesInner = this.inner().loadBalancingRules();
        if (lbRulesInner != null && probeId != null) {
            for (LoadBalancingRuleInner lbRuleInner : lbRulesInner) {
                SubResource probeRef = lbRuleInner.probe();
                if (probeRef != null && probeRef.id().equalsIgnoreCase(probeId)) {
                    lbRuleInner.withProbe(null);
                }
            }
        }

        return this;
    }

    @Override
    public LoadBalancerImpl withoutProbe(Probe probe) {
        return this.withoutProbe(probe.name());
    }

    @Override
    public ProbeImpl updateTcpProbe(String name) {
        return (ProbeImpl) this.tcpProbes.get(name);
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
    public LoadBalancerImpl withoutLoadBalancingRule(LoadBalancingRule rule) {
        return this.withoutLoadBalancingRule(rule.name());
    }

    @Override
    public LoadBalancerImpl withBackend(String name) {
        BackendAddressPoolInner inner = new BackendAddressPoolInner()
                .withName(name);
        BackendImpl backend = new BackendImpl(inner.name(), inner, this);
        ensureInnerBackends().add(inner);
        this.backends.put(inner.name(), backend);
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

        // Remove from inner
        List<BackendAddressPoolInner> inners = this.inner().backendAddressPools();
        if (inners != null) {
            for (int i = 0; i < inners.size(); i++) {
                if (inners.get(i).name().equalsIgnoreCase(name)) {
                    inners.remove(i);
                    break;
                }
            }
        }

        // Remove any LB rule references to it
        // TODO Revisit when full LB rule CRUD is done
        List<LoadBalancingRuleInner> rulesInner = this.inner().loadBalancingRules();
        if (rulesInner != null && backendId != null) {
            for (LoadBalancingRuleInner ruleInner : rulesInner) {
                SubResource backendRef = ruleInner.backendAddressPool();
                if (backendRef != null && backendRef.id().equalsIgnoreCase(backendId)) {
                    ruleInner.withBackendAddressPool(null);
                }
            }
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
}