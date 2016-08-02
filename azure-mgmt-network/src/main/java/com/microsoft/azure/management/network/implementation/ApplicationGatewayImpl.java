/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.network.*;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the ApplicationGateway interface.
 */
class ApplicationGatewayImpl
        extends GroupableResourceImpl<
        ApplicationGateway,
        ApplicationGatewayInner,
        ApplicationGatewayImpl,
        NetworkManager>
        implements
        ApplicationGateway,
        ApplicationGateway.Definition,
        ApplicationGateway.Update {

    private final ApplicationGatewaysInner innerCollection;
    private List<String> creatablePIPKeys = new ArrayList<>();

    ApplicationGatewayImpl(String name,
                           final ApplicationGatewayInner innerModel,
                           final ApplicationGatewaysInner innerCollection,
                           final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.innerCollection = innerCollection;
    }

    // Verbs

    @Override
    public ApplicationGatewayImpl refresh() throws Exception {
        ServiceResponse<ApplicationGatewayInner> response =
                this.innerCollection.get(this.resourceGroupName(), this.name());
        this.setInner(response.getBody());
        return this;
    }

    @Override
    public ApplicationGatewayImpl apply() throws Exception {
        return this.create();
    }

    @Override
    public ServiceCall applyAsync(ServiceCallback<ApplicationGateway> callback) {
        return createAsync(callback);
    }

    // Helpers

    private void ensureCreationPrerequisites() {
        // Account for the newly created public IPs
        for (String pipKey : this.creatablePIPKeys) {
            PublicIpAddress pip = (PublicIpAddress) this.createdResource(pipKey);
            if (pip != null) {
                withExistingPublicIpAddress(pip);
            }
        }
        this.creatablePIPKeys.clear();
        if (inner().requestRoutingRules() == null) {
            inner().withRequestRoutingRules(new ArrayList<ApplicationGatewayRequestRoutingRuleInner>());
        }
        ApplicationGatewayRequestRoutingRuleInner requestRoutingRule = new ApplicationGatewayRequestRoutingRuleInner();
        requestRoutingRule.withName("rrule" + inner().requestRoutingRules().size());
        inner().requestRoutingRules().add(requestRoutingRule);

        // Connect the http listener to the defaults
        for (ApplicationGatewayHttpListenerInner httpListener : this.inner().httpListeners()) {
            if (httpListener.frontendIPConfiguration() == null) {
                // If no reference to frontend IP config yet, add reference to the first frontend IP config
                String frontendIpConfigName = this.inner().frontendIPConfigurations().get(0).name();
                SubResource frontendIpConfigReference = new SubResource()
                        .withId(this.futureResourceId() + "/frontendIPConfigurations/" + frontendIpConfigName);
                httpListener.withFrontendIPConfiguration(frontendIpConfigReference);
            }

            if (httpListener.frontendPort() == null) {
                // If no reference to frontend port, then add reference to the first frontend port
                String frontendPortName = this.inner().frontendPorts().get(0).name();
                SubResource frontendPortReference = new SubResource().withId(this.futureResourceId() + "/frontendPorts/" + frontendPortName);
                httpListener.withFrontendPort(frontendPortReference);
            }
        }

        // Connect request routing rules to the defaults
        for (ApplicationGatewayRequestRoutingRuleInner routingRule : this.inner().requestRoutingRules()) {
            if (routingRule.httpListener() == null) {
                // If no reference to http listener yet, add reference to the first http listener
                String httpListenerName = this.inner().httpListeners().get(0).name();
                SubResource httpListenerReference = new SubResource().withId(this.futureResourceId() + "/httpListeners/" + httpListenerName);
                routingRule.withHttpListener(httpListenerReference);
            }
            if (routingRule.backendAddressPool() == null) {
                // If no reference to a back end pool, then add reference to the first back end address pool
                String backendPoolName = this.inner().backendAddressPools().get(0).name();
                SubResource backendPoolReference = new SubResource().withId(this.futureResourceId() + "/backendAddressPools/" + backendPoolName);
                routingRule.withBackendAddressPool(backendPoolReference);
            }
            if (routingRule.backendHttpSettings() == null) {
                String backendHttpSettingsName = this.inner().backendHttpSettingsCollection().get(0).name();
                SubResource backendHttpSettingsReference = new SubResource().withId(this.futureResourceId() + "/backendHttpSettingsCollection/" + backendHttpSettingsName);
                routingRule.withBackendHttpSettings(backendHttpSettingsReference);
            }
        }
    }

    NetworkManager myManager() {
        return super.myManager;
    }

    private ApplicationGatewayIPConfigurationInner createIPConfiguration(Network network) {
        List<ApplicationGatewayIPConfigurationInner> ipConfigurations = this.inner().gatewayIPConfigurations();
        if (ipConfigurations == null) {
            ipConfigurations = new ArrayList<ApplicationGatewayIPConfigurationInner>();
            this.inner().withGatewayIPConfigurations(ipConfigurations);
        }

        ApplicationGatewayIPConfigurationInner ipConfiguration = new ApplicationGatewayIPConfigurationInner();
        ipConfigurations.add(ipConfiguration);
        String name = "ipConfig" + ipConfigurations.size() + 1;
        ipConfiguration.withName(name);

        // by default use first subnet in virtual network
        String subnetId = network.subnets().values().iterator().next().inner().id();
        SubResource subnet = new SubResource().withId(subnetId);
        ipConfiguration.withSubnet(subnet);
        return ipConfiguration;
    }

    private ApplicationGatewayFrontendIPConfigurationInner createFrontendIPConfig(String name) {
        List<ApplicationGatewayFrontendIPConfigurationInner> frontendIpConfigs = this.inner().frontendIPConfigurations();
        if (frontendIpConfigs == null) {
            frontendIpConfigs = new ArrayList<ApplicationGatewayFrontendIPConfigurationInner>();
            this.inner().withFrontendIPConfigurations(frontendIpConfigs);
        }

        ApplicationGatewayFrontendIPConfigurationInner frontendIpConfig = new ApplicationGatewayFrontendIPConfigurationInner();
        frontendIpConfigs.add(frontendIpConfig);
        if (name == null) {
            name = "frontend" + frontendIpConfigs.size() + 1;
        }
        frontendIpConfig.withName(name);

        return frontendIpConfig;
    }

    // Setters (fluent)

    private ApplicationGatewayImpl withExistingPublicIpAddress(PublicIpAddress publicIpAddress) {
        return this.withExistingPublicIpAddress(publicIpAddress.id());
    }

    private ApplicationGatewayImpl withExistingPublicIpAddress(String resourceId) {
        ApplicationGatewayFrontendIPConfigurationInner frontendIpConfig = createFrontendIPConfig(null);
        SubResource pip = new SubResource();
        pip.withId(resourceId);
        frontendIpConfig.withPublicIPAddress(pip);
        return this;
    }

    @Override
    public ApplicationGatewayImpl withSku(ApplicationGatewaySkuName skuName) {
        this.inner().withSku(new ApplicationGatewaySku().withName(skuName));
        this.inner().sku().withCapacity(2);
        return this;
    }

    @Override
    public ApplicationGatewayImpl withCapacity(Integer capacity) {
        this.inner().sku().withCapacity(capacity);
        return this;
    }

    @Override
    public ApplicationGatewayImpl withExistingNetwork(Network network) {
        createIPConfiguration(network);
        return this;
    }

    @Override
    public ApplicationGatewayImpl withExistingPublicIpAddresses(PublicIpAddress... publicIpAddresses) {
        for (PublicIpAddress pip : publicIpAddresses) {
            withExistingPublicIpAddress(pip);
        }
        return this;
    }

    @Override
    public ApplicationGatewayImpl withNewPublicIpAddress() {
        // Autogenerated DNS leaf label for the PIP
        String dnsLeafLabel = (this.name() + "pip").toLowerCase().replace("\\s", "");
        return withNewPublicIpAddress(dnsLeafLabel);
    }

    @Override
    public ApplicationGatewayImpl withNewPublicIpAddress(String dnsLeafLabel) {
        PublicIpAddress.DefinitionStages.WithGroup precreatablePIP = myManager().publicIpAddresses().define(dnsLeafLabel)
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
    public ApplicationGatewayImpl withNewPublicIpAddress(Creatable<PublicIpAddress> creatablePIP) {
        this.creatablePIPKeys.add(creatablePIP.key());
        this.addCreatableDependency(creatablePIP);
        return this;
    }

    @Override
    public ApplicationGatewayImpl withFrontendPort(Integer port) {
        if (inner().frontendPorts() == null) {
            inner().withFrontendPorts(new ArrayList<ApplicationGatewayFrontendPortInner>());
        }
        ApplicationGatewayFrontendPortInner frontendPort = new ApplicationGatewayFrontendPortInner();
        frontendPort.withPort(port);
        frontendPort.withName("fp" + inner().frontendPorts().size());
        inner().frontendPorts().add(frontendPort);
        return this;
    }

    @Override
    public ApplicationGateway.DefinitionStages.WithBackendHttpSettings withBackendAddressPool() {
        if (inner().backendAddressPools() == null) {
            inner().withBackendAddressPools(new ArrayList<ApplicationGatewayBackendAddressPoolInner>());
        }
        ApplicationGatewayBackendAddressPoolInner addressPool = new ApplicationGatewayBackendAddressPoolInner();
        addressPool.withName("ap" + inner().backendAddressPools().size());
        inner().backendAddressPools().add(addressPool);
        return this;
    }

    @Override
    public ApplicationGateway.DefinitionStages.WithHttpListener withBackendHttpSettings() {
        if (inner().backendHttpSettingsCollection() == null) {
            inner().withBackendHttpSettingsCollection(new ArrayList<ApplicationGatewayBackendHttpSettingsInner>());
        }
        ApplicationGatewayBackendHttpSettingsInner httpSettings = new ApplicationGatewayBackendHttpSettingsInner();
        httpSettings.withName("hs" + inner().backendHttpSettingsCollection().size())
                .withPort(80);
        inner().backendHttpSettingsCollection().add(httpSettings);
        return this;
    }

    @Override
    public ApplicationGateway.DefinitionStages.WithCreate withHttpListener() {
        if (inner().httpListeners() == null) {
            inner().withHttpListeners(new ArrayList<ApplicationGatewayHttpListenerInner>());
        }
        ApplicationGatewayHttpListenerInner httpListener = new ApplicationGatewayHttpListenerInner();
        httpListener.withName("hl" + inner().httpListeners().size());
        inner().httpListeners().add(httpListener);
        return this;
    }

    private String futureResourceId() {
        return new StringBuilder()
                .append(super.resourceIdBase())
                .append("/providers/Microsoft.Network/applicationGateways/")
                .append(this.name()).toString();
    }

    @Override
    public Resource createResource() throws Exception {
        ensureCreationPrerequisites();

        ServiceResponse<ApplicationGatewayInner> response = this.innerCollection.createOrUpdate(this.resourceGroupName(), this.name(), this.inner());
        this.setInner(response.getBody());
        return this;
    }

    @Override
    public ServiceCall createResourceAsync(final ServiceCallback<Resource> callback) {
        final ApplicationGatewayImpl self = this;
        ensureCreationPrerequisites();
        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner(),
                new ServiceCallback<ApplicationGatewayInner>() {
                    @Override
                    public void failure(Throwable t) {
                        callback.failure(t);
                    }

                    @Override
                    public void success(ServiceResponse<ApplicationGatewayInner> response) {
                        self.setInner(response.getBody());
                        callback.success(new ServiceResponse<Resource>(self, response.getResponse()));
                    }
                });
    }
}

