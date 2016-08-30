/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NicIpConfiguration;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddress.DefinitionStages.WithGroup;
import com.microsoft.azure.management.network.SupportsNetworkInterfaces;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public LoadBalancer apply() throws Exception {
        return this.create();
    }

    @Override
    public Observable<LoadBalancer> applyAsync() {
        return createAsync();
    }

    @Override
    public ServiceCall<LoadBalancer> applyAsync(ServiceCallback<LoadBalancer> callback) {
        return createAsync(callback);
    }

    // Helpers

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
        return super.myManager;
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
            name = "frontend" + frontendIpConfigs.size() + 1;
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

    // Getters

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

    // CreatorTaskGroup.ResourceCreator implementation
    @Override
    public Observable<LoadBalancer> createResourceAsync()  {
        final LoadBalancer self = this;
        ensureCreationPrerequisites();
        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner())
                .flatMap(new Func1<ServiceResponse<LoadBalancerInner>, Observable<LoadBalancer>>() {
                    @Override
                    public Observable<LoadBalancer> call(ServiceResponse<LoadBalancerInner> loadBalancerInner) {
                        setInner(loadBalancerInner.getBody());
                        try {
                            runPostCreationTasks();
                            return Observable.just(self);
                        } catch (Exception e) {
                            return Observable.error(e);
                        }
                    }
                });
    }
}