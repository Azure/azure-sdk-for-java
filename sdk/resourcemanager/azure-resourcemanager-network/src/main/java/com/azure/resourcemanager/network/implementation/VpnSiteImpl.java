// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.VpnSiteInner;
import com.azure.resourcemanager.network.fluent.models.VpnSiteLinkInner;
import com.azure.resourcemanager.network.models.AddressSpace;
import com.azure.resourcemanager.network.models.DeviceProperties;
import com.azure.resourcemanager.network.models.O365BreakOutCategoryPolicies;
import com.azure.resourcemanager.network.models.O365PolicyProperties;
import com.azure.resourcemanager.network.models.TagsObject;
import com.azure.resourcemanager.network.models.VirtualWan;
import com.azure.resourcemanager.network.models.VpnSite;
import com.azure.resourcemanager.network.models.VpnSiteLink;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.AcceptedImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Implementation for VPN site. */
public class VpnSiteImpl
    extends GroupableParentResourceWithTagsImpl<VpnSite, VpnSiteInner, VpnSiteImpl, NetworkManager>
    implements VpnSite, VpnSite.Definition, VpnSite.Update {

    private final ClientLogger logger = new ClientLogger(this.getClass());

    /**
     * unique key of a creatable public IP to be associated with the ip configuration.
     */
    private String creatableVirtualWanKey;

    private Creatable<VirtualWan> virtualWanCreatable;

    private Map<String, VpnSiteLink> vpnSiteLinkMap;

    VpnSiteImpl(String name, VpnSiteInner innerModel, final NetworkManager manager) {
        super(name, innerModel, manager);
        initializeChildrenFromInner();
    }

    @Override
    protected Mono<VpnSiteInner> createInner() {
        return this
            .manager()
            .serviceClient()
            .getVpnSites()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel());
    }

    @Override
    protected void initializeChildrenFromInner() {
        this.vpnSiteLinkMap = new TreeMap<>();
        List<VpnSiteLinkInner> inners = this.innerModel().vpnSiteLinks();
        if (inners != null) {
            inners.forEach(inner -> this.vpnSiteLinkMap.put(inner.name(), new VpnSiteLinkImpl(inner, this)));
        }
    }

    @Override
    protected void afterCreating() {
        this.creatableVirtualWanKey = null;
        this.virtualWanCreatable = null;

    }

    @Override
    public VpnSiteLinkImpl defineVpnSiteLink(String name) {
        VpnSiteLinkInner inner = new VpnSiteLinkInner();
        inner.withName(name);
        return new VpnSiteLinkImpl(inner, this);
    }

    @Override
    public VpnSiteLinkImpl updateVpnSiteLink(String name) {
        return (VpnSiteLinkImpl) this.vpnSiteLinkMap.get(name);
    }

    @Override
    protected Mono<VpnSiteInner> applyTagsToInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getVpnSites()
            .updateTagsAsync(resourceGroupName(), name(), new TagsObject().withTags(innerModel().tags()));
    }

    void addToCreatableDependencies(Creatable<? extends Resource> creatableResource) {
        this.addDependency(creatableResource);
    }

    Resource createdDependencyResource(String key) {
        return this.<Resource>taskResult(key);
    }

    @Override
    public boolean isSecuritySiteEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().isSecuritySite());
    }

    @Override
    public List<String> addressPrefixes() {
        return this.innerModel().addressSpace().addressPrefixes();
    }

    @Override
    public VirtualWan virtualWan() {
        return this.manager().virtualWans().getById(this.innerModel().virtualWan().id());
    }

    @Override
    public List<VpnSiteLink> vpnSiteLinks() {
        List<VpnSiteLink> vpnSiteLinkList = new ArrayList<>();
        this.innerModel().vpnSiteLinks()
            .forEach(vpnSiteLinkInner -> vpnSiteLinkList.add(new VpnSiteLinkImpl(vpnSiteLinkInner, this)));
        return vpnSiteLinkList;
    }

    @Override
    public O365PolicyProperties o365Policy() {
        return this.innerModel().o365Policy();
    }

    @Override
    public DeviceProperties device() {
        return this.innerModel().deviceProperties();
    }

    @Override
    public VpnSiteImpl withAddressSpace(String cidr) {
        if (this.innerModel().addressSpace() == null) {
            this.innerModel().withAddressSpace(new AddressSpace());
        }
        if (this.innerModel().addressSpace().addressPrefixes() == null) {
            this.innerModel().addressSpace().withAddressPrefixes(new ArrayList<String>());
        }
        this.innerModel().addressSpace().withAddressPrefixes(Arrays.asList(cidr));
        return this;
    }

    @Override
    public VpnSiteImpl enableSecuritySite() {
        this.innerModel().withIsSecuritySite(true);
        return this;
    }

    @Override
    public O365PolicyImpl defineO365Policy() {
        if (this.innerModel().o365Policy() == null) {
            this.innerModel().withO365Policy(new O365PolicyProperties());
        }
        if (this.innerModel().o365Policy().breakOutCategories() == null) {
            this.innerModel().o365Policy().withBreakOutCategories(new O365BreakOutCategoryPolicies());
        }
        return new O365PolicyImpl(this.innerModel().o365Policy(), this);
    }

    @Override
    public O365PolicyImpl updateO365Policy() {
        if (this.innerModel().o365Policy() == null) {
            this.innerModel().withO365Policy(new O365PolicyProperties());
        }
        if (this.innerModel().o365Policy().breakOutCategories() == null) {
            this.innerModel().o365Policy().withBreakOutCategories(new O365BreakOutCategoryPolicies());
        }
        return new O365PolicyImpl(this.innerModel().o365Policy(), this);
    }

    @Override
    public DeviceImpl defineDevice() {
        if (this.innerModel().deviceProperties() == null) {
            this.innerModel().withDeviceProperties(new DeviceProperties());
        }
        return new DeviceImpl(this.innerModel().deviceProperties(), this);
    }

    @Override
    public DeviceImpl updateDevice() {
        if (this.innerModel().deviceProperties() == null) {
            this.innerModel().withDeviceProperties(new DeviceProperties());
        }
        return new DeviceImpl(this.innerModel().deviceProperties(), this);
    }

    @Override
    public VpnSiteImpl withVirtualWan(String virtualWanId) {
        if (this.innerModel().virtualWan() == null) {
            this.innerModel().withVirtualWan(new SubResource());
        }
        this.innerModel().virtualWan().withId(virtualWanId);
        return this;
    }

    @Override
    public VpnSiteImpl withVirtualWan(VirtualWan virtualWan) {
        if (this.innerModel().virtualWan() == null) {
            this.innerModel().withVirtualWan(new SubResource());
        }
        this.innerModel().virtualWan().withId(virtualWan.id());
        return this;
    }

    @Override
    public VpnSiteImpl withVirtualWan(Creatable<VirtualWan> creatable) {
        if (isInCreateMode()) {
            if (this.creatableVirtualWanKey == null) {
                this.creatableVirtualWanKey = this.addDependency(creatable);
            }
        } else {
            if (this.virtualWanCreatable == null) {
                this.virtualWanCreatable = creatable;
            }
        }
        return this;
    }

    @Override
    public Accepted<VpnSite> beginCreate() {
        return AcceptedImpl
            .newAccepted(
                logger,
                this.manager().serviceClient().getHttpPipeline(),
                this.manager().serviceClient().getDefaultPollInterval(),
                () ->
                    this
                        .manager()
                        .serviceClient()
                        .getVpnSites()
                        .createOrUpdateWithResponseAsync(resourceGroupName(), name(), this.innerModel())
                        .block(),
                inner -> new VpnSiteImpl(inner.name(), inner, this.manager()),
                VpnSiteInner.class,
                () -> {
                    taskGroup().invokeDependencyAsync(taskGroup().newInvocationContext()).blockLast();
                    beforeCreating();
                },
                inner -> {
                    innerToFluentMap(this);
                    initializeChildrenFromInner();
                    afterCreating();
                },
                Context.NONE);

    }

    @Override
    protected void beforeCreating() {
        if (creatableVirtualWanKey != null) {
            VirtualWan virtualWan = this.<VirtualWan>taskResult(creatableVirtualWanKey);
            if (innerModel().virtualWan() == null) {
                this.innerModel().withVirtualWan(new SubResource());
            }
            innerModel().virtualWan().withId(virtualWan.id());
        }
    }

    @Override
    protected Mono<VpnSiteInner> getInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getVpnSites()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public VpnSite apply() {
        if (!isInCreateMode() && virtualWanCreatable != null) {
            VirtualWan virtualWan = virtualWanCreatable.create();
            if (this.innerModel().virtualWan() == null) {
                this.innerModel().withVirtualWan(new SubResource());
            }
            this.innerModel().virtualWan().withId(virtualWan.id());
        }
        VpnSiteInner inner = this.manager().serviceClient().getVpnSites().createOrUpdate(resourceGroupName(), name(), this.innerModel());
        return new VpnSiteImpl(name(), inner, this.manager());
    }
}
