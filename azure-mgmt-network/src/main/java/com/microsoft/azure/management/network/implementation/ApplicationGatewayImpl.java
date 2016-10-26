/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGateway.DefinitionStages.WithHttpListenerOrCreate;
import com.microsoft.azure.management.network.ApplicationGatewayBackend;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayFrontend;
import com.microsoft.azure.management.network.ApplicationGatewayHttpListener;
import com.microsoft.azure.management.network.ApplicationGatewayHttpListener.DefinitionStages.Blank;
import com.microsoft.azure.management.network.ApplicationGatewayIpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayOperationalState;
import com.microsoft.azure.management.network.ApplicationGatewaySku;
import com.microsoft.azure.management.network.ApplicationGatewaySkuName;
import com.microsoft.azure.management.network.ApplicationGatewaySslPolicy;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.Subnet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.microsoft.azure.management.apigeneration.LangDefinition;

import rx.Observable;

/**
 * Implementation of the LoadBalancer interface.
 */
@LangDefinition
class ApplicationGatewayImpl
    extends NetworkGroupableParentResourceImpl<
        ApplicationGateway,
        ApplicationGatewayInner,
        ApplicationGatewayImpl,
        ApplicationGatewaysInner>
    implements
        ApplicationGateway,
        ApplicationGateway.Definition,
        ApplicationGateway.Update {

    private Map<String, ApplicationGatewayIpConfiguration> configs;
    private Map<String, ApplicationGatewayFrontend> frontends;
    private Map<String, ApplicationGatewayBackend> backends;
    private Map<String, ApplicationGatewayBackendHttpConfiguration> httpConfigs;
    private Map<String, ApplicationGatewayHttpListener> httpListeners;

    ApplicationGatewayImpl(String name,
            final ApplicationGatewayInner innerModel,
            final ApplicationGatewaysInner innerCollection,
            final NetworkManager networkManager) {
        super(name, innerModel, innerCollection, networkManager);
    }

    // Verbs

    @Override
    public ApplicationGatewayImpl refresh() {
        ApplicationGatewayInner inner = this.innerCollection.get(this.resourceGroupName(), this.name());
        this.setInner(inner);
        initializeChildrenFromInner();
        return this;
    }

    // Helpers

    @Override
    protected void initializeChildrenFromInner() {
        initializeConfigsFromInner();
        initializeFrontendsFromInner();
        initializeBackendsFromInner();
        initializeBackendHttpConfigsFromInner();
        initializeHttpListenersFromInner();
    }

    private void initializeFrontendsFromInner() {
        this.frontends = new TreeMap<>();
        List<ApplicationGatewayFrontendIPConfigurationInner> inners = this.inner().frontendIPConfigurations();
        if (inners != null) {
            for (ApplicationGatewayFrontendIPConfigurationInner inner : inners) {
                ApplicationGatewayFrontendImpl frontend = new ApplicationGatewayFrontendImpl(inner, this);
                this.frontends.put(inner.name(), frontend);
            }
        }
    }

    private void initializeBackendsFromInner() {
        this.backends = new TreeMap<>();
        List<ApplicationGatewayBackendAddressPoolInner> inners = this.inner().backendAddressPools();
        if (inners != null) {
            for (ApplicationGatewayBackendAddressPoolInner inner : inners) {
                ApplicationGatewayBackendImpl backend = new ApplicationGatewayBackendImpl(inner, this);
                this.backends.put(inner.name(), backend);
            }
        }
    }

    private void initializeBackendHttpConfigsFromInner() {
        this.httpConfigs = new TreeMap<>();
        List<ApplicationGatewayBackendHttpSettingsInner> inners = this.inner().backendHttpSettingsCollection();
        if (inners != null) {
            for (ApplicationGatewayBackendHttpSettingsInner inner : inners) {
                ApplicationGatewayBackendHttpConfigurationImpl httpConfig = new ApplicationGatewayBackendHttpConfigurationImpl(inner, this);
                this.httpConfigs.put(inner.name(), httpConfig);
            }
        }
    }

    private void initializeHttpListenersFromInner() {
        this.httpListeners = new TreeMap<>();
        List<ApplicationGatewayHttpListenerInner> inners = this.inner().httpListeners();
        if (inners != null) {
            for (ApplicationGatewayHttpListenerInner inner : inners) {
                ApplicationGatewayHttpListenerImpl httpListener = new ApplicationGatewayHttpListenerImpl(inner, this);
                this.httpListeners.put(inner.name(), httpListener);
            }
        }
    }

    private void initializeConfigsFromInner() {
        this.configs = new TreeMap<>();
        List<ApplicationGatewayIPConfigurationInner> inners = this.inner().gatewayIPConfigurations();
        if (inners != null) {
            for (ApplicationGatewayIPConfigurationInner inner : inners) {
                ApplicationGatewayIpConfigurationImpl config = new ApplicationGatewayIpConfigurationImpl(inner, this);
                this.configs.put(inner.name(), config);
            }
        }
    }

    @Override
    protected void beforeCreating()  {
        // Account for the newly created public IPs
        for (Entry<String, String> pipFrontendAssociation : this.creatablePIPKeys.entrySet()) {
            PublicIpAddress pip = (PublicIpAddress) this.createdResource(pipFrontendAssociation.getKey());
            if (pip != null) {
                withExistingPublicIpAddress(pip.id(), pipFrontendAssociation.getValue());
            }
        }
        this.creatablePIPKeys.clear();

        // Reset and update configs
        this.inner().withGatewayIPConfigurations(innersFromWrappers(this.configs.values()));

        // Reset and update frontends
        this.inner().withFrontendIPConfigurations(innersFromWrappers(this.frontends.values()));

        // Reset and update backends
        this.inner().withBackendAddressPools(innersFromWrappers(this.backends.values()));

        // Reset and update backend HTTP configs
        this.inner().withBackendHttpSettingsCollection(innersFromWrappers(this.httpConfigs.values()));

        // Reset and update HTTP listeners
        this.inner().withHttpListeners(innersFromWrappers(this.httpListeners.values()));
    }

    @Override
    protected void afterCreating() {
    }

    @Override
    protected Observable<ApplicationGatewayInner> createInner() {
        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner());
    }

    NetworkManager manager() {
        return this.myManager;
    }

    String futureResourceId() {
        return new StringBuilder()
                .append(super.resourceIdBase())
                .append("/providers/Microsoft.Network/applicationGateways/")
                .append(this.name()).toString();
    }

     // Withers (fluent)

    ApplicationGatewayImpl withFrontend(ApplicationGatewayFrontendImpl frontend) {
        if (frontend == null) {
            return null;
        } else {
            this.frontends.put(frontend.name(), frontend);
            return this;
        }
    }

    ApplicationGatewayImpl withBackend(ApplicationGatewayBackendImpl backend) {
        if (backend == null) {
            return null;
        } else {
            this.backends.put(backend.name(), backend);
            return this;
        }
    }

    ApplicationGatewayImpl withHttpListener(ApplicationGatewayHttpListenerImpl httpListener) {
        if (httpListener == null) {
            return null;
        } else {
            this.httpListeners.put(httpListener.name(), httpListener);
            return this;
        }
    }

    ApplicationGatewayImpl withBackendHttpConfiguration(ApplicationGatewayBackendHttpConfigurationImpl httpConfig) {
        if (httpConfig == null) {
            return null;
        } else {
            this.httpConfigs.put(httpConfig.name(), httpConfig);
            return this;
        }
    }

    @Override
    public ApplicationGatewayImpl withSku(ApplicationGatewaySkuName skuName, int capacity) {
        ApplicationGatewaySku sku = new ApplicationGatewaySku()
                .withName(skuName)
                .withCapacity(capacity);
        this.inner().withSku(sku);
        return this;
    }

    @Override
    public ApplicationGatewayImpl withContainingSubnet(Subnet subnet) {
        this.defineIpConfiguration(DEFAULT)
            .withContainingSubnet(subnet)
            .attach();
        return this;
    }

    @Override
    public ApplicationGatewayImpl withContainingSubnet(Network network, String subnetName) {
        this.defineIpConfiguration(DEFAULT)
            .withContainingSubnet(network, subnetName)
            .attach();
        return this;
    }

    @Override
    public ApplicationGatewayImpl withContainingSubnet(String networkResourceId, String subnetName) {
        this.defineIpConfiguration(DEFAULT)
            .withContainingSubnet(networkResourceId, subnetName)
            .attach();
        return this;
    }

    ApplicationGatewayImpl withConfig(ApplicationGatewayIpConfigurationImpl config) {
        if (config == null) {
            return null;
        } else {
            this.configs.put(config.name(), config);
            return this;
        }
    }

    // Getters

    @Override
    public ApplicationGatewaySku sku() {
        return this.inner().sku();
    }

    @Override
    public ApplicationGatewayOperationalState operationalState() {
        return this.inner().operationalState();
    }

    @Override
    public ApplicationGatewaySslPolicy sslPolicy() {
        return this.inner().sslPolicy();
    }

    @Override
    public ApplicationGatewayIpConfigurationImpl defineIpConfiguration(String name) {
        ApplicationGatewayIpConfiguration config = this.configs.get(name);
        if (config == null) {
            ApplicationGatewayIPConfigurationInner inner = new ApplicationGatewayIPConfigurationInner()
                    .withName(name);
            return new ApplicationGatewayIpConfigurationImpl(inner, this);
        } else {
            return (ApplicationGatewayIpConfigurationImpl) config;
        }
    }

    @Override
    public ApplicationGatewayImpl withFrontendSubnet(Network network, String subnetName) {
        return this.definePrivateFrontend(DEFAULT)
                .withExistingSubnet(network, subnetName)
                .attach();
    }

    @Override
    public ApplicationGatewayFrontendImpl definePrivateFrontend(String name) {
        return defineFrontend(name);
    }

    @Override
    public ApplicationGatewayFrontendImpl definePublicFrontend(String name) {
        return defineFrontend(name);
    }

    private ApplicationGatewayFrontendImpl defineFrontend(String name) {
        ApplicationGatewayFrontend frontend = this.frontends.get(name);
        if (frontend == null) {
            ApplicationGatewayFrontendIPConfigurationInner inner = new ApplicationGatewayFrontendIPConfigurationInner()
                    .withName(name);
            return new ApplicationGatewayFrontendImpl(inner, this);
        } else {
            return (ApplicationGatewayFrontendImpl) frontend;
        }
    }

    @Override
    public ApplicationGatewayBackendImpl defineBackend(String name) {
        ApplicationGatewayBackend backend = this.backends.get(name);
        if (backend == null) {
            ApplicationGatewayBackendAddressPoolInner inner = new ApplicationGatewayBackendAddressPoolInner()
                    .withName(name);
            return new ApplicationGatewayBackendImpl(inner, this);
        } else {
            return (ApplicationGatewayBackendImpl) backend;
        }
    }


    @Override
    public Blank<WithHttpListenerOrCreate> defineHttpListener(String name) {
        ApplicationGatewayHttpListener httpListener = this.httpListeners.get(name);
        if (httpListener == null) {
            ApplicationGatewayHttpListenerInner inner = new ApplicationGatewayHttpListenerInner()
                    .withName(name);
            return new ApplicationGatewayHttpListenerImpl(inner, this);
        } else {
            return (ApplicationGatewayHttpListenerImpl) httpListener;
        }
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl defineBackendHttpConfiguration(String name) {
        ApplicationGatewayBackendHttpConfiguration httpConfig = this.httpConfigs.get(name);
        if (httpConfig == null) {
            ApplicationGatewayBackendHttpSettingsInner inner = new ApplicationGatewayBackendHttpSettingsInner()
                    .withName(name);
            return new ApplicationGatewayBackendHttpConfigurationImpl(inner, this);
        } else {
            return (ApplicationGatewayBackendHttpConfigurationImpl) httpConfig;
        }
    }

    @Override
    protected ApplicationGatewayImpl withExistingPublicIpAddress(String resourceId, String frontendName) {
        if (frontendName == null) {
            frontendName = DEFAULT;
        }

        return this.definePublicFrontend(frontendName)
                .withExistingPublicIpAddress(resourceId)
                .attach();
    }

    @Override
    public ApplicationGatewayImpl withFrontendPort(int portNumber) {
        return withFrontendPort(portNumber, null);
    }

    @Override
    public ApplicationGatewayImpl withFrontendPort(int portNumber, String name) {
        if (name == null) {
            // Auto-name the port if no name provided
            if (this.inner().frontendPorts() != null) {
                name = "port" + (this.inner().frontendPorts().size() + 1); // TODO: better uniqueness would be nice
            } else {
                name = "port1";
            }
        }

        List<ApplicationGatewayFrontendPortInner> frontendPorts = this.inner().frontendPorts();
        if (frontendPorts == null) {
            frontendPorts = new ArrayList<ApplicationGatewayFrontendPortInner>();
            this.inner().withFrontendPorts(frontendPorts);
        }

        ApplicationGatewayFrontendPortInner frontendPort = null;
        for (ApplicationGatewayFrontendPortInner inner : this.inner().frontendPorts()) {
            if (name.equalsIgnoreCase(inner.name())) {
                frontendPort = inner;
                break;
            }
        }

        if (frontendPort == null) {
            frontendPort = new ApplicationGatewayFrontendPortInner()
                    .withName(name)
                    .withPort(portNumber);
            frontendPorts.add(frontendPort);
        } else {
            frontendPort.withPort(portNumber);
        }

        return this;
    }
}
