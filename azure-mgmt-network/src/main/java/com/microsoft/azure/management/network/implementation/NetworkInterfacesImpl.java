package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkInterfaces;
import com.microsoft.azure.management.network.implementation.api.NetworkInterfaceInner;
import com.microsoft.azure.management.network.implementation.api.NetworkInterfacesInner;
import com.microsoft.azure.management.network.implementation.api.NetworkInterfaceIPConfiguration;
import com.microsoft.azure.management.network.implementation.api.NetworkInterfaceDnsSettings;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.resources.implementation.ResourceManager;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The type representing Azure network interfaces.
 */
class NetworkInterfacesImpl
        extends GroupableResourcesImpl<NetworkInterface, NetworkInterfaceImpl, NetworkInterfaceInner, NetworkInterfacesInner>
        implements NetworkInterfaces {

    private final NetworkManager networkManager;

    NetworkInterfacesImpl(
            final NetworkInterfacesInner client,
            final NetworkManager networkManager,
            final ResourceManager resourceManager) {
        super(resourceManager, client);
        this.networkManager = networkManager;
    }

    @Override
    public PagedList<NetworkInterface> list() throws CloudException, IOException {
        return wrapList(innerCollection.listAll().getBody());
    }

    @Override
    public PagedList<NetworkInterface> listByGroup(String groupName) throws CloudException, IOException {
        return wrapList(innerCollection.list(groupName).getBody());
    }

    @Override
    public NetworkInterface getByGroup(String groupName, String name) throws CloudException, IOException {
        return wrapModel(this.innerCollection.get(groupName, name).getBody());
    }

    @Override
    public void delete(String id) throws Exception {
        this.delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        this.innerCollection.delete(groupName, name);
    }

    @Override
    public NetworkInterfaceImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected NetworkInterfaceImpl wrapModel(String name) {
        NetworkInterfaceInner inner = new NetworkInterfaceInner();
        inner.withIpConfigurations(new ArrayList<NetworkInterfaceIPConfiguration>());
        inner.withDnsSettings(new NetworkInterfaceDnsSettings());
        return new NetworkInterfaceImpl(name,
                inner,
                this.innerCollection,
                this.networkManager,
                this.resourceManager);
    }

    @Override
    protected NetworkInterfaceImpl wrapModel(NetworkInterfaceInner inner) {
        return new NetworkInterfaceImpl(inner.name(),
                inner,
                this.innerCollection,
                this.networkManager,
                this.resourceManager);
    }
}
