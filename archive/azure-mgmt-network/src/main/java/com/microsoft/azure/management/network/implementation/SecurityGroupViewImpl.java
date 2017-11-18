/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.NetworkWatcher;
import com.microsoft.azure.management.network.SecurityGroupNetworkInterface;
import com.microsoft.azure.management.network.SecurityGroupView;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The implementation of SecurityGroupView.
 */
@LangDefinition
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
    public Observable<SecurityGroupView> refreshAsync() {
        return super.refreshAsync().map(new Func1<SecurityGroupView, SecurityGroupView>() {
            @Override
            public SecurityGroupView call(SecurityGroupView securityGroupView) {
                SecurityGroupViewImpl impl = (SecurityGroupViewImpl) securityGroupView;
                impl.initializeFromInner();
                return impl;
            }
        });
    }

    @Override
    protected Observable<SecurityGroupViewResultInner> getInnerAsync() {
        return this.parent().manager().inner().networkWatchers()
                .getVMSecurityRulesAsync(parent.resourceGroupName(), parent.name(), vmId);
    }
}