package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkInterfaces;
import com.microsoft.azure.management.network.NetworkInterfaceDnsSettings;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;

import java.io.IOException;
import java.util.ArrayList;

/**
 *  Implementation for {@link NetworkInterfaces}.
 */
class NetworkInterfacesImpl
        extends GroupableResourcesImpl<
            NetworkInterface,
            NetworkInterfaceImpl,
            NetworkInterfaceInner,
            NetworkInterfacesInner,
            NetworkManager>
        implements NetworkInterfaces {

    NetworkInterfacesImpl(
            final NetworkInterfacesInner client,
            final NetworkManager networkManager) {
        super(client, networkManager);
    }

    @Override
    public PagedList<NetworkInterface> list() throws CloudException, IOException {
        return wrapList(innerCollection.listAll());
    }

    @Override
    public PagedList<NetworkInterface> listByGroup(String groupName) throws CloudException, IOException {
        return wrapList(innerCollection.list(groupName));
    }

    @Override
    public NetworkInterface getByGroup(String groupName, String name) throws CloudException, IOException {
        return wrapModel(this.innerCollection.get(groupName, name));
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
        inner.withIpConfigurations(new ArrayList<NetworkInterfaceIPConfigurationInner>());
        inner.withDnsSettings(new NetworkInterfaceDnsSettings());
        return new NetworkInterfaceImpl(name,
                inner,
                this.innerCollection,
                super.myManager);
    }

    @Override
    protected NetworkInterfaceImpl wrapModel(NetworkInterfaceInner inner) {
        return new NetworkInterfaceImpl(inner.name(),
                inner,
                this.innerCollection,
                super.myManager);
    }
}
