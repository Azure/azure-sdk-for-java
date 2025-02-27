// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.containerorchestratorruntime.implementation;

import com.azure.core.management.SystemData;
import com.azure.core.util.Context;
import com.azure.resourcemanager.containerorchestratorruntime.fluent.models.BgpPeerInner;
import com.azure.resourcemanager.containerorchestratorruntime.models.BgpPeer;
import com.azure.resourcemanager.containerorchestratorruntime.models.BgpPeerProperties;

public final class BgpPeerImpl implements BgpPeer, BgpPeer.Definition, BgpPeer.Update {
    private BgpPeerInner innerObject;

    private final com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager serviceManager;

    public String id() {
        return this.innerModel().id();
    }

    public String name() {
        return this.innerModel().name();
    }

    public String type() {
        return this.innerModel().type();
    }

    public BgpPeerProperties properties() {
        return this.innerModel().properties();
    }

    public SystemData systemData() {
        return this.innerModel().systemData();
    }

    public BgpPeerInner innerModel() {
        return this.innerObject;
    }

    private com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager manager() {
        return this.serviceManager;
    }

    private String resourceUri;

    private String bgpPeerName;

    public BgpPeerImpl withExistingResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
        return this;
    }

    public BgpPeer create() {
        this.innerObject = serviceManager.serviceClient()
            .getBgpPeers()
            .createOrUpdate(resourceUri, bgpPeerName, this.innerModel(), Context.NONE);
        return this;
    }

    public BgpPeer create(Context context) {
        this.innerObject = serviceManager.serviceClient()
            .getBgpPeers()
            .createOrUpdate(resourceUri, bgpPeerName, this.innerModel(), context);
        return this;
    }

    BgpPeerImpl(String name,
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager serviceManager) {
        this.innerObject = new BgpPeerInner();
        this.serviceManager = serviceManager;
        this.bgpPeerName = name;
    }

    public BgpPeerImpl update() {
        return this;
    }

    public BgpPeer apply() {
        this.innerObject = serviceManager.serviceClient()
            .getBgpPeers()
            .createOrUpdate(resourceUri, bgpPeerName, this.innerModel(), Context.NONE);
        return this;
    }

    public BgpPeer apply(Context context) {
        this.innerObject = serviceManager.serviceClient()
            .getBgpPeers()
            .createOrUpdate(resourceUri, bgpPeerName, this.innerModel(), context);
        return this;
    }

    BgpPeerImpl(BgpPeerInner innerObject,
        com.azure.resourcemanager.containerorchestratorruntime.ContainerOrchestratorRuntimeManager serviceManager) {
        this.innerObject = innerObject;
        this.serviceManager = serviceManager;
        this.resourceUri = ResourceManagerUtils.getValueFromIdByParameterName(innerObject.id(),
            "/{resourceUri}/providers/Microsoft.KubernetesRuntime/bgpPeers/{bgpPeerName}", "resourceUri");
        this.bgpPeerName = ResourceManagerUtils.getValueFromIdByParameterName(innerObject.id(),
            "/{resourceUri}/providers/Microsoft.KubernetesRuntime/bgpPeers/{bgpPeerName}", "bgpPeerName");
    }

    public BgpPeer refresh() {
        this.innerObject = serviceManager.serviceClient()
            .getBgpPeers()
            .getWithResponse(resourceUri, bgpPeerName, Context.NONE)
            .getValue();
        return this;
    }

    public BgpPeer refresh(Context context) {
        this.innerObject = serviceManager.serviceClient()
            .getBgpPeers()
            .getWithResponse(resourceUri, bgpPeerName, context)
            .getValue();
        return this;
    }

    public BgpPeerImpl withProperties(BgpPeerProperties properties) {
        this.innerModel().withProperties(properties);
        return this;
    }
}
