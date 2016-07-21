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
import com.microsoft.azure.management.network.LoadBalancer.DefinitionStages.WithCreate;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NicIpConfiguration;
import com.microsoft.azure.management.network.Protocol;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddress.DefinitionStages.WithGroup;
import com.microsoft.azure.management.network.SupportsNetworkInterfaces;
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

    private List<LoadBalancingRuleInner> ensureLoadBalancingRules() {
        List<LoadBalancingRuleInner> rules = this.inner().loadBalancingRules();
        if (rules == null) {
            rules = new ArrayList<>();
            this.inner().withLoadBalancingRules(rules);
        }
        return rules;
    }

    private void ensureCreationPrerequisites()  {
        // Ensure backend pools list
        List<BackendAddressPoolInner> backendPools = this.inner().backendAddressPools();
        if (backendPools == null) {
            backendPools = new ArrayList<>();
            this.inner().withBackendAddressPools(backendPools);
        }

        // Ensure first backend pool
        BackendAddressPoolInner backendPool;
        if (backendPools.size() == 0) {
            backendPool = new BackendAddressPoolInner();
            backendPools.add(backendPool);
            backendPool.withName("backendpool" + backendPools.size());
        }

        // Account for the newly created public IPs
        for (String pipKey : this.creatablePIPKeys) {
            PublicIpAddress pip = (PublicIpAddress) this.createdResource(pipKey);
            if (pip != null) {
                withExistingPublicIpAddress(pip);
            }
        }
        this.creatablePIPKeys.clear();

        // TODO Connect the load balancing rules to the first front end IP config (for now)
        for (LoadBalancingRuleInner lbRule : this.inner().loadBalancingRules()) {
            if (lbRule.frontendIPConfiguration() == null) {
                // TODO Add a reference to the first frontend IP config (TODO for now...)
                SubResource frontendIpConfigReference = new SubResource();
                String frontendIpConfigName = this.inner().frontendIPConfigurations().get(0).name();
                frontendIpConfigReference.withId(this.futureResourceId() + "/frontendIPConfigurations/" + frontendIpConfigName);
                lbRule.withFrontendIPConfiguration(frontendIpConfigReference);
            }
        }

        // Connect the load balancing rules to the back end pools.
        // TODO For now, we just take the first back end pool. More logic needs to be implemented for handling more complex rule <-> backend pool.
        for (LoadBalancingRuleInner lbRule : this.inner().loadBalancingRules()) {
            if (lbRule.backendAddressPool() == null) {
                // TODO Add a reference to the first back end address pool [In progress]
                SubResource backendPoolReference = new SubResource();
                String backendPoolName = this.inner().backendAddressPools().get(0).name();
                backendPoolReference.withId(this.futureResourceId() + "/backendPools/" + backendPoolName);
            }
        }
    }

    private void runPostCreationTasks() throws Exception {
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

        FrontendIPConfigurationInner frontendIpConfig = new FrontendIPConfigurationInner();
        frontendIpConfigs.add(frontendIpConfig);
        if (name == null) {
            name = "frontend" + (frontendIpConfigs.size());
        }
        frontendIpConfig.withName(name);

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
    public WithCreate withLoadBalancingRule(String name, Protocol protocol, int frontendPort, int backendPort) {
        LoadBalancingRuleInner ruleInner = new LoadBalancingRuleInner();
        ensureLoadBalancingRules().add(ruleInner);

        ruleInner.withName(name);
        ruleInner.withProtocol(protocol.toString());
        ruleInner.withFrontendPort(frontendPort);
        ruleInner.withBackendPort(backendPort);

        return this;
    }

    // Getters

    @Override
    protected void createResource() throws Exception {
        ensureCreationPrerequisites();

        ServiceResponse<LoadBalancerInner> response =
                this.innerCollection.createOrUpdate(this.resourceGroupName(), this.name(), this.inner());
        this.setInner(response.getBody());

        runPostCreationTasks();
    }

    @Override
    protected ServiceCall createResourceAsync(final ServiceCallback<Void> callback)  {
        ensureCreationPrerequisites();
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
                            runPostCreationTasks();
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