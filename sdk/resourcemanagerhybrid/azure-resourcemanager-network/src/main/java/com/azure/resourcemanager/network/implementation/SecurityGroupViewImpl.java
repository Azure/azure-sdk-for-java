// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.NetworkWatcher;
import com.azure.resourcemanager.network.models.SecurityGroupNetworkInterface;
import com.azure.resourcemanager.network.models.SecurityGroupView;
import com.azure.resourcemanager.network.fluent.models.SecurityGroupViewResultInner;
import com.azure.resourcemanager.network.models.SecurityGroupViewParameters;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import reactor.core.publisher.Mono;

/** The implementation of SecurityGroupView. */
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
        List<SecurityGroupNetworkInterface> securityGroupNetworkInterfaces = this.innerModel().networkInterfaces();
        if (securityGroupNetworkInterfaces != null) {
            for (SecurityGroupNetworkInterface networkInterface : securityGroupNetworkInterfaces) {
                this.networkInterfaces.put(networkInterface.id(), networkInterface);
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
        return super
            .refreshAsync()
            .map(
                securityGroupView -> {
                    SecurityGroupViewImpl impl = (SecurityGroupViewImpl) securityGroupView;
                    impl.initializeFromInner();
                    return impl;
                });
    }

    @Override
    protected Mono<SecurityGroupViewResultInner> getInnerAsync() {
        return this
            .parent()
            .manager()
            .serviceClient()
            .getNetworkWatchers()
            .getVMSecurityRulesAsync(parent.resourceGroupName(), parent.name(),
                new SecurityGroupViewParameters().withTargetResourceId(vmId));
    }
}
