// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.fluent.models.OutboundRuleInner;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerBackend;
import com.azure.resourcemanager.network.models.LoadBalancerFrontend;
import com.azure.resourcemanager.network.models.LoadBalancerOutboundRule;
import com.azure.resourcemanager.network.models.LoadBalancerOutboundRuleProtocol;
import com.azure.resourcemanager.network.models.ProvisioningState;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LoadBalancerOutboundRuleImpl extends ChildResourceImpl<OutboundRuleInner, LoadBalancerImpl, LoadBalancer>
    implements LoadBalancerOutboundRule,
    LoadBalancerOutboundRule.Definition<LoadBalancerImpl>,
    LoadBalancerOutboundRule.Update<LoadBalancerImpl> {

    LoadBalancerOutboundRuleImpl(OutboundRuleInner inner, LoadBalancerImpl parent) {
        super(inner, parent);
    }

    // Getters
    @Override
    public LoadBalancerOutboundRuleProtocol protocol() {
        return this.innerModel().protocol();
    }


    @Override
    public List<String> frontendIpConfigurationIds() {
        List<String> frontendIpConfigurationIds = new ArrayList<>();
        if(this.innerModel().frontendIpConfigurations() != null && !this.innerModel().frontendIpConfigurations().isEmpty()) {
            for(SubResource frontendIpConfiguration : this.innerModel().frontendIpConfigurations()) {
                frontendIpConfigurationIds.add(frontendIpConfiguration.id());
            }
        }
        return frontendIpConfigurationIds;
    }

    @Override
    public Map<String, LoadBalancerFrontend> frontends() {
        Map<String, LoadBalancerFrontend> nameToFrontEndMap = new TreeMap<>();
        if(this.innerModel().frontendIpConfigurations() != null && !this.innerModel().frontendIpConfigurations().isEmpty()) {
            for(SubResource frontendIpConfiguration : this.innerModel().frontendIpConfigurations()) {
                LoadBalancerFrontend frontend = this.parent()
                    .frontends()
                    .get(ResourceUtils.nameFromResourceId(frontendIpConfiguration.id()));
                nameToFrontEndMap.put(frontend.name(), frontend);
            }
        }
        return nameToFrontEndMap;
    }

    @Override
    public String backendAddressPoolId() {
        return this.innerModel().backendAddressPool().id();
    }

    @Override
    public LoadBalancerBackend backend() {
        return this
            .parent()
            .backends()
            .get(ResourceUtils.nameFromResourceId(this.innerModel().backendAddressPool().id()));
    }

    @Override
    public int allocatedOutboundPorts() {
        return this.innerModel().allocatedOutboundPorts();
    }

    @Override
    public ProvisioningState provisioningState() {
        return this.innerModel().provisioningState();
    }

    @Override
    public int idleTimeoutInMinutes() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().idleTimeoutInMinutes());
    }

    @Override
    public boolean tcpResetEnabled() {
        return this.innerModel().enableTcpReset().booleanValue();
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    // Fluent setters

    @Override
    public LoadBalancerOutboundRuleImpl withProtocol(LoadBalancerOutboundRuleProtocol protocol) {
        this.innerModel().withProtocol(protocol);
        return this;
    }

    @Override
    public LoadBalancerOutboundRuleImpl fromBackend(String name) {
        // Ensure existence of backend, creating one if needed
        this.parent().defineBackend(name).attach();

        SubResource backendRef =
            new SubResource().withId(this.parent().futureResourceId() + "/backendAddressPools/" + name);
        this.innerModel().withBackendAddressPool(backendRef);
        return this;
    }

    @Override
    public LoadBalancerOutboundRuleImpl toFrontend(String name) {
        SubResource frontendRef = this.parent().ensureFrontendRef(name);
        if (frontendRef != null) {
            this.innerModel().withFrontendIpConfigurations(Arrays.asList(frontendRef));
        }
        return this;
    }

    @Override
    public LoadBalancerOutboundRuleImpl toFrontends(List<String> names) {
        List<SubResource> frontendRefs = new ArrayList<>();
        if(names != null && !names.isEmpty()) {
            for(String name : names) {
                SubResource frontendRef = this.parent().ensureFrontendRef(name);
                frontendRefs.add(frontendRef);
            }
        }
        this.innerModel().withFrontendIpConfigurations(frontendRefs);
        return this;
    }

    @Override
    public LoadBalancerOutboundRuleImpl withIdleTimeoutInMinutes(int minutes) {
        this.innerModel().withIdleTimeoutInMinutes(minutes);
        return this;
    }

    @Override
    public LoadBalancerOutboundRuleImpl withEnableTcpReset(boolean enableTcpReset) {
        this.innerModel().withEnableTcpReset(enableTcpReset);
        return this;
    }

    // Verbs
    @Override
    public LoadBalancerImpl attach() {
        return this.parent().withOutboundRule(this);
    }

}
