/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.management.network.NetworkWatcher;
import com.azure.management.network.SecurityGroupNetworkInterface;
import com.azure.management.network.SecurityGroupView;
import com.azure.management.network.models.SecurityGroupViewResultInner;
import com.azure.management.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The implementation of SecurityGroupView.
 */
class SecurityGroupViewImpl extends RefreshableWrapperImpl<SecurityGroupViewResultInner, SecurityGroupView>
        implements SecurityGroupView {
    private Map<String, SecurityGroupNetworkInterface> networkInterfaces;
    private final NetworkWatcherImpl parent;
    private final String vmId;

    SecurityGroupViewImpl(NetworkWatcherImpl parent, SecurityGroupViewResultInner innerObject, String vmId) {
        super(innerObject);
        this.parent = parent;
        this.vmId = vmId;
        initializeFromInner();
    }

    private void initializeFromInner() {
        this.networkInterfaces = new TreeMap<>();
        List<SecurityGroupNetworkInterface> securityGroupNetworkInterfaces = this.inner().networkInterfaces();
        if (securityGroupNetworkInterfaces != null) {
            for (SecurityGroupNetworkInterface networkInterface : securityGroupNetworkInterfaces) {
                this.networkInterfaces.put(networkInterface.getId(), networkInterface);
            }
        }
    }

    @Override
    public Map<String, SecurityGroupNetworkInterface> networkInterfaces() {
        return Collections.unmodifiableMap(this.networkInterfaces);
    }

    @Override
    public String vmId() {
        return vmId;
    }

    @Override
    public NetworkWatcher parent() {
        return parent;
    }

    @Override
    public Mono<SecurityGroupView> refreshAsync() {
        return super.refreshAsync().map(securityGroupView -> {
            SecurityGroupViewImpl impl = (SecurityGroupViewImpl) securityGroupView;
            impl.initializeFromInner();
            return impl;
        });
    }

    @Override
    protected Mono<SecurityGroupViewResultInner> getInnerAsync() {
        return this.parent().manager().inner().networkWatchers()
                .getVMSecurityRulesAsync(parent.resourceGroupName(), parent.name(), vmId);
    }
}