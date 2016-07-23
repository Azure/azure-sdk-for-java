/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NicIpConfiguration;
import com.microsoft.azure.management.network.ProbeProtocol;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddress.DefinitionStages.WithGroup;
import com.microsoft.azure.management.network.SupportsNetworkInterfaces;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
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
    private final List<SupportsNetworkInterfaces> vms = new ArrayList<>();
    private List<String> creatablePIPKeys = new ArrayList<>();

    LoadBalancerImpl(String name,
            final LoadBalancerInner innerModel,
            final LoadBalancersInner innerCollection,
            final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.innerCollection = innerCollection;
    }

    // Verbs

    @Override
    public LoadBalancerImpl refresh() throws Exception {
        ServiceResponse<LoadBalancerInner> response =
            this.innerCollection.get(this.resourceGroupName(), this.name());
        this.setInner(response.getBody());
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
    private String futureResourceId() {
        return new StringBuilder()
                .append(super.resourceIdBase())
                .append("/providers/Microsoft.Network/loadBalancers/")
                .append(this.name()).toString();
    }

    private LoadBalancingRuleInner createLoadBalancingRuleInner(String name) {
        List<LoadBalancingRuleInner> rules = this.inner().loadBalancingRules();
        if (rules == null) {
            rules = new ArrayList<>();
            this.inner().withLoadBalancingRules(rules);
        }

        if (name == null) {
           name = "lbrule" + (rules.size() + 1);
        }

        LoadBalancingRuleInner ruleInner = new LoadBalancingRuleInner().withName(name);
        rules.add(ruleInner);
        return ruleInner;
    }


    private ProbeInner createProbeInner(String name) {
        List<ProbeInner> probes = this.inner().probes();
        if (probes == null) {
            probes = new ArrayList<>();
            this.inner().withProbes(probes);
        }

        if (name == null) {
            name = "probe" + (probes.size() + 1);
        }

        ProbeInner probeInner = new ProbeInner().withName(name);
        probes.add(probeInner);
        return probeInner;
    }

    private void beforeCreating()  {
        // Ensure backend pools list
        List<BackendAddressPoolInner> backendPools = this.inner().backendAddressPools();
        if (backendPools == null) {
            backendPools = new ArrayList<>();
            this.inner().withBackendAddressPools(backendPools);
        }

        // Ensure first backend pool
        BackendAddressPoolInner backendPool;
        if (backendPools.size() == 0) {
            backendPool = new BackendAddressPoolInner()
                    .withName("backendpool" + (backendPools.size() + 1));
            backendPools.add(backendPool);
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
        for (SupportsNetworkInterfaces vm : this.vms) {
            NetworkInterface primaryNIC = vm.primaryNetworkInterface();
            NicIpConfiguration nicIp = primaryNIC.primaryIpConfiguration();
            primaryNIC.update()
                .updateIpConfiguration(nicIp.name())
                    .withExistingLoadBalancer(this)
                    .withBackendAddressPool(this.inner().backendAddressPools().get(0).name())
                    .parent()
                .apply();
        }

        this.vms.clear();
        this.refresh();
    }

    NetworkManager myManager() {
        return this.myManager;
    }

    private FrontendIPConfigurationInner createFrontendIPConfig(String name) {
        List<FrontendIPConfigurationInner> frontendIpConfigs = this.inner().frontendIPConfigurations();
        if (frontendIpConfigs == null) {
            frontendIpConfigs = new ArrayList<FrontendIPConfigurationInner>();
            this.inner().withFrontendIPConfigurations(frontendIpConfigs);
        }

        if (name == null) {
            name = "frontend" + (frontendIpConfigs.size() + 1);
        }

        FrontendIPConfigurationInner frontendIpConfig = new FrontendIPConfigurationInner()
                .withName(name);
        frontendIpConfigs.add(frontendIpConfig);
        return frontendIpConfig;
    }

    // Withers (fluent)

    private LoadBalancerImpl withExistingPublicIpAddress(PublicIpAddress publicIpAddress) {
        return this.withExistingPublicIpAddress(publicIpAddress.id());
    }

    private LoadBalancerImpl withExistingPublicIpAddress(String resourceId) {
        FrontendIPConfigurationInner frontendIpConfig = createFrontendIPConfig(null);
        SubResource pip = new SubResource();
        pip.withId(resourceId);
        frontendIpConfig.withPublicIPAddress(pip);
        return this;
    }

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
        this.creatablePIPKeys.add(creatablePIP.key());
        this.addCreatableDependency(creatablePIP);
        return this;
    }

    @Override
    public LoadBalancerImpl withExistingPublicIpAddresses(PublicIpAddress... publicIpAddresses) {
        for (PublicIpAddress pip : publicIpAddresses) {
            withExistingPublicIpAddress(pip);
        }
        return this;
    }

    private LoadBalancerImpl withExistingVirtualMachine(SupportsNetworkInterfaces vm) {
      this.vms.add(vm);
      return this;
    }

    @Override
    public LoadBalancerImpl withExistingVirtualMachines(SupportsNetworkInterfaces... vms) {
        for (SupportsNetworkInterfaces vm : vms) {
            withExistingVirtualMachine(vm);
        }
        return this;
    }

    @Override
    public LoadBalancerImpl withLoadBalancedPort(int frontendPort, TransportProtocol protocol, int backendPort, String name) {
        createLoadBalancingRuleInner(name)
            .withProtocol(protocol)
            .withFrontendPort(frontendPort)
            .withBackendPort(backendPort);

        return this;
    }

    @Override
    public LoadBalancerImpl withLoadBalancedPort(int frontendPort, TransportProtocol protocol, int backendPort) {
        return withLoadBalancedPort(frontendPort, protocol, backendPort, null);
    }

    @Override
    public LoadBalancerImpl withLoadBalancedPort(int port, TransportProtocol protocol) {
        return withLoadBalancedPort(port, protocol, port);
    }

    @Override
    public LoadBalancerImpl withTcpProbe(int port) {
        return withTcpProbe(port, null);
    }

    @Override
    public LoadBalancerImpl withTcpProbe(int port, String name) {
        createProbeInner(name)
            .withPort(port)
            .withProtocol(ProbeProtocol.TCP);

        return this;
    }

    @Override
    public LoadBalancerImpl withHttpProbe(String path) {
        return withHttpProbe(path, 80, null);
    }

    @Override
    public LoadBalancerImpl withHttpProbe(String path, int port) {
        return withHttpProbe(path, port, null);
    }

    @Override
    public LoadBalancerImpl withHttpProbe(String path, String name) {
        return withHttpProbe(path, 80, name);
    }

    @Override
    public LoadBalancerImpl withHttpProbe(String path, int port, String name) {
        createProbeInner(name)
            .withRequestPath(path)
            .withPort(port)
            .withProtocol(ProbeProtocol.HTTP);
        return this;
    }

    // Getters

    @Override
    protected void createResource() throws Exception {
        beforeCreating();

        ServiceResponse<LoadBalancerInner> response =
                this.innerCollection.createOrUpdate(this.resourceGroupName(), this.name(), this.inner());
        this.setInner(response.getBody());

        afterCreating();
    }

    @Override
    protected ServiceCall createResourceAsync(final ServiceCallback<Void> callback)  {
        beforeCreating();
        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner(),
                Utils.fromVoidCallback(this, new ServiceCallback<Void>() {
                    @Override
                    public void failure(Throwable t) {
                        callback.failure(t);
                    }

                    @Override
                    public void success(ServiceResponse<Void> result) {
                        callback.success(result);
                        try {
                            afterCreating();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }));
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