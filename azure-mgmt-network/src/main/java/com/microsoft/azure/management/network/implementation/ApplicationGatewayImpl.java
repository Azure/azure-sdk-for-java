/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayBackend;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayFrontend;
import com.microsoft.azure.management.network.ApplicationGatewayListener;
import com.microsoft.azure.management.network.ApplicationGatewayIpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayOperationalState;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRule;
import com.microsoft.azure.management.network.ApplicationGatewaySku;
import com.microsoft.azure.management.network.ApplicationGatewaySkuName;
import com.microsoft.azure.management.network.ApplicationGatewaySslCertificate;
import com.microsoft.azure.management.network.ApplicationGatewaySslPolicy;
import com.microsoft.azure.management.network.ApplicationGatewayTier;
import com.microsoft.azure.management.network.IPAllocationMethod;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;

import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation of the ApplicationGateway interface.
 */
@LangDefinition
class ApplicationGatewayImpl
    extends GroupableParentResourceImpl<
        ApplicationGateway,
        ApplicationGatewayInner,
        ApplicationGatewayImpl,
        NetworkManager>
    implements
        ApplicationGateway,
        ApplicationGateway.Definition,
        ApplicationGateway.Update {

    private Map<String, ApplicationGatewayIpConfiguration> ipConfigs;
    private Map<String, ApplicationGatewayFrontend> frontends;
    private Map<String, ApplicationGatewayBackend> backends;
    private Map<String, ApplicationGatewayBackendHttpConfiguration> backendHttpConfigs;
    private Map<String, ApplicationGatewayListener> listeners;
    private Map<String, ApplicationGatewayRequestRoutingRule> rules;
    private Map<String, ApplicationGatewaySslCertificate> sslCerts;

    private static final String DEFAULT = "default";
    private final ApplicationGatewaysInner innerCollection;
    private ApplicationGatewayFrontendImpl defaultPrivateFrontend;
    private ApplicationGatewayFrontendImpl defaultPublicFrontend;

    private Map<String, String> creatablePipsByFrontend;

    ApplicationGatewayImpl(String name,
            final ApplicationGatewayInner innerModel,
            final ApplicationGatewaysInner innerCollection,
            final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.innerCollection = innerCollection;
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
        initializeRequestRoutingRulesFromInner();
        initializeSslCertificatesFromInner();
        this.defaultPrivateFrontend = null;
        this.defaultPublicFrontend = null;
        this.creatablePipsByFrontend = new HashMap<>();
    }

    private void initializeSslCertificatesFromInner() {
        this.sslCerts = new TreeMap<>();
        List<ApplicationGatewaySslCertificateInner> inners = this.inner().sslCertificates();
        if (inners != null) {
            for (ApplicationGatewaySslCertificateInner inner : inners) {
                ApplicationGatewaySslCertificateImpl cert = new ApplicationGatewaySslCertificateImpl(inner, this);
                this.sslCerts.put(inner.name(), cert);
            }
        }
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
        this.backendHttpConfigs = new TreeMap<>();
        List<ApplicationGatewayBackendHttpSettingsInner> inners = this.inner().backendHttpSettingsCollection();
        if (inners != null) {
            for (ApplicationGatewayBackendHttpSettingsInner inner : inners) {
                ApplicationGatewayBackendHttpConfigurationImpl httpConfig = new ApplicationGatewayBackendHttpConfigurationImpl(inner, this);
                this.backendHttpConfigs.put(inner.name(), httpConfig);
            }
        }
    }

    private void initializeHttpListenersFromInner() {
        this.listeners = new TreeMap<>();
        List<ApplicationGatewayHttpListenerInner> inners = this.inner().httpListeners();
        if (inners != null) {
            for (ApplicationGatewayHttpListenerInner inner : inners) {
                ApplicationGatewayListenerImpl httpListener = new ApplicationGatewayListenerImpl(inner, this);
                this.listeners.put(inner.name(), httpListener);
            }
        }
    }

    private void initializeRequestRoutingRulesFromInner() {
        this.rules = new TreeMap<>();
        List<ApplicationGatewayRequestRoutingRuleInner> inners = this.inner().requestRoutingRules();
        if (inners != null) {
            for (ApplicationGatewayRequestRoutingRuleInner inner : inners) {
                ApplicationGatewayRequestRoutingRuleImpl rule = new ApplicationGatewayRequestRoutingRuleImpl(inner, this);
                this.rules.put(inner.name(), rule);
            }
        }
    }

    private void initializeConfigsFromInner() {
        this.ipConfigs = new TreeMap<>();
        List<ApplicationGatewayIPConfigurationInner> inners = this.inner().gatewayIPConfigurations();
        if (inners != null) {
            for (ApplicationGatewayIPConfigurationInner inner : inners) {
                ApplicationGatewayIpConfigurationImpl config = new ApplicationGatewayIpConfigurationImpl(inner, this);
                this.ipConfigs.put(inner.name(), config);
            }
        }
    }

    @Override
    protected void beforeCreating()  {
        // Process created PIPs
        for (Entry<String, String> frontendPipPair : this.creatablePipsByFrontend.entrySet()) {
            Resource createdPip = this.createdResource(frontendPipPair.getValue());
            this.updateFrontend(frontendPipPair.getKey()).withExistingPublicIpAddress(createdPip.id());
        }
        this.creatablePipsByFrontend.clear();

        // Reset and update IP configs
        ensureDefaultIpConfig();
        this.inner().withGatewayIPConfigurations(innersFromWrappers(this.ipConfigs.values()));

        this.inner().withFrontendIPConfigurations(innersFromWrappers(this.frontends.values()));

        // Reset and update backends
        this.inner().withBackendAddressPools(innersFromWrappers(this.backends.values()));

        // Reset and update backend HTTP settings configs
        this.inner().withBackendHttpSettingsCollection(innersFromWrappers(this.backendHttpConfigs.values()));

        // Reset and update HTTP listeners
        this.inner().withHttpListeners(innersFromWrappers(this.listeners.values()));
        for (ApplicationGatewayListener listener : this.listeners.values()) {
            SubResource ref;

            // Clear deleted frontend references
            ref = listener.inner().frontendIPConfiguration();
            if (ref != null
                    && !this.frontends().containsKey(ResourceUtils.nameFromResourceId(ref.id()))) {
                listener.inner().withFrontendIPConfiguration(null);
            }

            // Clear deleted frontend port references
            ref = listener.inner().frontendPort();
            if (ref != null
                    && !this.frontendPorts().containsKey(ResourceUtils.nameFromResourceId(ref.id()))) {
                listener.inner().withFrontendPort(null);
            }

            // Clear deleted SSL certificate references
            ref = listener.inner().sslCertificate();
            if (ref != null
                    && !this.sslCertificates().containsKey(ResourceUtils.nameFromResourceId(ref.id()))) {
                listener.inner().withSslCertificate(null);
            }
        }

        // Reset and update request routing rules
        this.inner().withRequestRoutingRules(innersFromWrappers(this.rules.values()));
        for (ApplicationGatewayRequestRoutingRule rule : this.rules.values()) {
            SubResource ref;

            // Clear deleted backends
            ref = rule.inner().backendAddressPool();
            if (ref != null
                    && !this.backends().containsKey(ResourceUtils.nameFromResourceId(ref.id()))) {
                rule.inner().withBackendAddressPool(null);
            }

            // Clear deleted backend HTTP configs
            ref = rule.inner().backendHttpSettings();
            if (ref != null
                    && !this.backendHttpConfigurations().containsKey(ResourceUtils.nameFromResourceId(ref.id()))) {
                rule.inner().withBackendHttpSettings(null);
            }

            // Clear deleted frontend HTTP listeners
            ref = rule.inner().httpListener();
            if (ref != null
                    && !this.listeners().containsKey(ResourceUtils.nameFromResourceId(ref.id()))) {
                rule.inner().withHttpListener(null);
            }
        }

        // Reset and update SSL certs
        this.inner().withSslCertificates(innersFromWrappers(this.sslCerts.values()));
    }

    @Override
    protected void afterCreating() {
    }

    private ApplicationGatewayIpConfigurationImpl ensureDefaultIpConfig() {
        ApplicationGatewayIpConfigurationImpl ipConfig = (ApplicationGatewayIpConfigurationImpl) defaultIpConfiguration();
        if (ipConfig == null) {
            String name = SdkContext.randomResourceName("ipcfg", 11);
            ipConfig = this.defineIpConfiguration(name);
            ipConfig.attach();
        }
        return ipConfig;
    }

    protected ApplicationGatewayFrontendImpl ensureDefaultPrivateFrontend() {
        ApplicationGatewayFrontendImpl frontend = (ApplicationGatewayFrontendImpl) defaultPrivateFrontend();
        if (frontend != null) {
            return frontend;
        } else {
            String name = SdkContext.randomResourceName("frontend", 14);
            frontend = this.defineFrontend(name);
            frontend.attach();
            this.defaultPrivateFrontend = frontend;
            return frontend;
        }
    }

    protected ApplicationGatewayFrontendImpl ensureDefaultPublicFrontend() {
        ApplicationGatewayFrontendImpl frontend = (ApplicationGatewayFrontendImpl) defaultPublicFrontend();
        if (frontend != null) {
            return frontend;
        } else {
            String name = SdkContext.randomResourceName("frontend", 14);
            frontend = this.defineFrontend(name);
            frontend.attach();
            this.defaultPublicFrontend = frontend;
            return frontend;
        }
    }

    private Creatable<Network> creatableNetwork = null;
    private Creatable<Network> ensureDefaultNetworkDefinition() {
        if (this.creatableNetwork == null) {
            final String vnetName = SdkContext.randomResourceName("vnet", 10);
            this.creatableNetwork = this.manager().networks().define(vnetName)
                    .withRegion(this.region())
                    .withExistingResourceGroup(this.resourceGroupName())
                    .withAddressSpace("10.0.0.0/24")
                    .withSubnet(DEFAULT, "10.0.0.0/25")
                    .withSubnet("apps", "10.0.0.128/25");
        }

        return this.creatableNetwork;
    }

    private Creatable<PublicIpAddress> creatablePip = null;
    private Creatable<PublicIpAddress> ensureDefaultPipDefinition() {
        if (this.creatablePip == null) {
            final String pipName = SdkContext.randomResourceName("pip", 9);
            this.creatablePip = this.manager().publicIpAddresses().define(pipName)
                    .withRegion(this.regionName())
                    .withExistingResourceGroup(this.resourceGroupName());
        }

        return this.creatablePip;
    }

    private static ApplicationGatewayFrontendImpl useSubnetFromIpConfigForFrontend(
            ApplicationGatewayIpConfigurationImpl ipConfig,
            ApplicationGatewayFrontendImpl frontend) {
        if (frontend != null) {
            frontend.withExistingSubnet(ipConfig.networkId(), ipConfig.subnetName());
            if (frontend.privateIpAddress() == null) {
                frontend.withPrivateIpAddressDynamic();
            } else if (frontend.privateIpAllocationMethod() == null) {
                frontend.withPrivateIpAddressDynamic();
            }
        }
        return frontend;
    }

    @Override
    protected Observable<ApplicationGatewayInner> createInner() {
        // Determine if a default public frontend PIP should be created
        final ApplicationGatewayFrontendImpl defaultPublicFrontend = (ApplicationGatewayFrontendImpl) defaultPublicFrontend();
        final Observable<Resource> pipObservable;
        if (defaultPublicFrontend != null && defaultPublicFrontend.publicIpAddressId() == null) {
            // If public frontend requested but no PIP specified, then create a default PIP
            pipObservable = Utils.<PublicIpAddress>rootResource(ensureDefaultPipDefinition()
                    .createAsync()).map(new Func1<PublicIpAddress, Resource>() {
                        @Override
                        public Resource call(PublicIpAddress publicIpAddress) {
                            defaultPublicFrontend.withExistingPublicIpAddress(publicIpAddress);
                            return publicIpAddress;
                        }
                    });
        } else {
            // If no public frontend requested, skip creating the PIP
            pipObservable = Observable.empty();
        }

        // Determine if default VNet should be created
        final ApplicationGatewayIpConfigurationImpl defaultIpConfig = ensureDefaultIpConfig();
        final ApplicationGatewayFrontendImpl defaultPrivateFrontend = (ApplicationGatewayFrontendImpl) defaultPrivateFrontend();
        final Observable<Resource> networkObservable;
        if (defaultIpConfig.subnetName() != null) {
            // If default IP config already has a subnet assigned to it...
            if (defaultPrivateFrontend != null) {
                // ...and a private frontend is requested, then use the same vnet for the private frontend
                useSubnetFromIpConfigForFrontend(defaultIpConfig, defaultPrivateFrontend);
            }
            // ...and no need to create a default VNet
            networkObservable = Observable.empty(); // ...and don't create another VNet
        } else {
            // But if default IP config does not have a subnet specified, then create a default VNet
            networkObservable = Utils.<Network>rootResource(ensureDefaultNetworkDefinition()
                .createAsync()).map(new Func1<Network, Resource>() {
                    @Override
                    public Resource call(Network network) {
                        //... and assign the created VNet to the default IP config
                        defaultIpConfig.withExistingSubnet(network, DEFAULT);
                        if (defaultPrivateFrontend != null) {
                            // If a private frontend is also requested, then use the same VNet for the private frontend as for the IP config
                            /* TODO: Not sure if the assumption of the same subnet for the frontend and the IP config will hold in
                             * the future, but the existing ARM template for App Gateway for some reason uses the same subnet for the
                             * IP config and the private frontend. Also, trying to use different subnets results in server error today saying they
                             * have to be the same. This may need to be revisited in the future however, as this is somewhat inconsistent
                             * with what the documentation says.
                             */
                            useSubnetFromIpConfigForFrontend(defaultIpConfig, defaultPrivateFrontend);
                        }
                        return network;
                    }
                });
        }

        return Observable.merge(networkObservable, pipObservable)
                .defaultIfEmpty(null)
                .last().flatMap(new Func1<Resource, Observable<ApplicationGatewayInner>>() {
                    @Override
                    public Observable<ApplicationGatewayInner> call(Resource resource) {
                        return innerCollection.createOrUpdateAsync(resourceGroupName(), name(), inner());
                    }
                });
    }

    /**
     * Determines whether the app gateway child that can be found using a name or a port number can be created,
     * or it already exists, or there is a clash.
     * @param byName object found by name
     * @param byPort object found by port
     * @param name the desired name of the object
     * @return true if already found, false if ok to create, null if conflict
     */
    <T> Boolean needToCreate(T byName, T byPort, String name) {
        if (byName != null && byPort != null) {
            // If objects with this name and/or port already exist...
            if (byName == byPort) {
                // ...and it is the same object, then do nothing
                return false;
            } else {
                // ...but if they are inconsistent, then fail fast
                return null;
            }
        } else if (byPort != null) {
            // If no object with the requested name, but the port number is found...
            if (name == null) {
                // ...and no name is requested, then do nothing, because the object already exists
                return false;
            } else {
                // ...but if a clashing name is requested, then fail fast
                return null;
            }
        } else {
            // Ok to create the object
            return true;
        }
    }

    String futureResourceId() {
        return new StringBuilder()
                .append(super.resourceIdBase())
                .append("/providers/Microsoft.Network/applicationGateways/")
                .append(this.name()).toString();
    }

     // Withers (fluent)

    @Override
    public ApplicationGatewayImpl withInstanceCount(int capacity) {
        if (this.inner().sku() == null) {
            this.withSize(ApplicationGatewaySkuName.STANDARD_SMALL);
        }

        this.inner().sku().withCapacity(capacity);
        return this;
    }

    @Override
    public ApplicationGatewayImpl withPrivateIpAddressDynamic() {
        ensureDefaultPrivateFrontend().withPrivateIpAddressDynamic();
        return this;
    }

    @Override
    public ApplicationGatewayImpl withPrivateIpAddressStatic(String ipAddress) {
        ensureDefaultPrivateFrontend().withPrivateIpAddressStatic(ipAddress);
        return this;
    }

    ApplicationGatewayImpl withFrontend(ApplicationGatewayFrontendImpl frontend) {
        if (frontend != null) {
            this.frontends.put(frontend.name(), frontend);
        }
        return this;
    }

    ApplicationGatewayImpl withBackend(ApplicationGatewayBackendImpl backend) {
        if (backend != null) {
            this.backends.put(backend.name(), backend);
        }
        return this;
    }

    ApplicationGatewayImpl withSslCertificate(ApplicationGatewaySslCertificateImpl cert) {
        if (cert != null) {
            this.sslCerts.put(cert.name(), cert);
        }
        return this;
    }

    ApplicationGatewayImpl withHttpListener(ApplicationGatewayListenerImpl httpListener) {
        if (httpListener != null) {
            this.listeners.put(httpListener.name(), httpListener);
        }
        return this;
    }

    ApplicationGatewayImpl withRequestRoutingRule(ApplicationGatewayRequestRoutingRuleImpl rule) {
        if (rule != null) {
            this.rules.put(rule.name(), rule);
        }
        return this;
    }

    ApplicationGatewayImpl withBackendHttpConfiguration(ApplicationGatewayBackendHttpConfigurationImpl httpConfig) {
        if (httpConfig != null) {
            this.backendHttpConfigs.put(httpConfig.name(), httpConfig);
        }
        return this;
    }

    @Override
    public ApplicationGatewayImpl withSize(ApplicationGatewaySkuName skuName) {
        final int count;
        // Preserve instance count if already set
        if (this.sku() != null) {
            count = this.sku().capacity();
        } else {
            count = 1; // Default instance count
        }

        ApplicationGatewaySku sku = new ApplicationGatewaySku()
                .withName(skuName)
                .withCapacity(count);
        this.inner().withSku(sku);
        return this;
    }

    @Override
    public ApplicationGatewayImpl withExistingSubnet(Subnet subnet) {
        ensureDefaultIpConfig().withExistingSubnet(subnet);
        return this;
    }

    @Override
    public ApplicationGatewayImpl withExistingSubnet(Network network, String subnetName) {
        ensureDefaultIpConfig().withExistingSubnet(network, subnetName);
        return this;
    }

    @Override
    public ApplicationGatewayImpl withExistingSubnet(String networkResourceId, String subnetName) {
        ensureDefaultIpConfig().withExistingSubnet(networkResourceId, subnetName);
        return this;
    }

    ApplicationGatewayImpl withConfig(ApplicationGatewayIpConfigurationImpl config) {
        if (config != null) {
            this.ipConfigs.put(config.name(), config);
        }
        return this;
    }

    @Override
    public ApplicationGatewaySslCertificateImpl defineSslCertificate(String name) {
        ApplicationGatewaySslCertificate cert = this.sslCerts.get(name);
        if (cert == null) {
            ApplicationGatewaySslCertificateInner inner = new ApplicationGatewaySslCertificateInner()
                    .withName(name);
            return new ApplicationGatewaySslCertificateImpl(inner, this);
        } else {
            return (ApplicationGatewaySslCertificateImpl) cert;
        }
    }

    //TODO @Override - since app gateways don't support more than one today, no need to expose this
    private ApplicationGatewayIpConfigurationImpl defineIpConfiguration(String name) {
        ApplicationGatewayIpConfiguration config = this.ipConfigs.get(name);
        if (config == null) {
            ApplicationGatewayIPConfigurationInner inner = new ApplicationGatewayIPConfigurationInner()
                    .withName(name);
            return new ApplicationGatewayIpConfigurationImpl(inner, this);
        } else {
            return (ApplicationGatewayIpConfigurationImpl) config;
        }
    }

    //TODO @Override - since app gateways don't support more than one today, no need to expose this
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
    public ApplicationGatewayListenerImpl defineListener(String name) {
        ApplicationGatewayListener httpListener = this.listeners.get(name);
        if (httpListener == null) {
            ApplicationGatewayHttpListenerInner inner = new ApplicationGatewayHttpListenerInner()
                    .withName(name);
            return new ApplicationGatewayListenerImpl(inner, this);
        } else {
            return (ApplicationGatewayListenerImpl) httpListener;
        }
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl defineRequestRoutingRule(String name) {
        ApplicationGatewayRequestRoutingRule rule = this.rules.get(name);
        if (rule == null) {
            ApplicationGatewayRequestRoutingRuleInner inner = new ApplicationGatewayRequestRoutingRuleInner()
                    .withName(name);
            return new ApplicationGatewayRequestRoutingRuleImpl(inner, this);
        } else {
            return (ApplicationGatewayRequestRoutingRuleImpl) rule;
        }
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl defineBackendHttpConfiguration(String name) {
        ApplicationGatewayBackendHttpConfiguration httpConfig = this.backendHttpConfigs.get(name);
        if (httpConfig == null) {
            ApplicationGatewayBackendHttpSettingsInner inner = new ApplicationGatewayBackendHttpSettingsInner()
                    .withName(name)
                    .withPort(80); // Default port
            return new ApplicationGatewayBackendHttpConfigurationImpl(inner, this);
        } else {
            return (ApplicationGatewayBackendHttpConfigurationImpl) httpConfig;
        }
    }

    @Override
    public ApplicationGatewayImpl withoutPrivateFrontend() {
        // Delete all private frontends
        List<String> toDelete = new ArrayList<>();
        for (ApplicationGatewayFrontend frontend : this.frontends.values()) {
            if (frontend.isPrivate()) {
                toDelete.add(frontend.name());
            }
        }

        for (String frontendName : toDelete) {
            this.frontends.remove(frontendName);
        }

        this.defaultPrivateFrontend = null;
        return this;
    }

    @Override
    public ApplicationGatewayImpl withoutPublicFrontend() {
        // Delete all public frontends
        List<String> toDelete = new ArrayList<>();
        for (ApplicationGatewayFrontend frontend : this.frontends.values()) {
            if (frontend.isPublic()) {
                toDelete.add(frontend.name());
            }
        }

        for (String frontendName : toDelete) {
            this.frontends.remove(frontendName);
        }

        this.defaultPublicFrontend = null;
        return this;
    }

    @Override
    public ApplicationGatewayImpl withFrontendPort(int portNumber) {
        return withFrontendPort(portNumber, null);
    }

    @Override
    public ApplicationGatewayImpl withFrontendPort(int portNumber, String name) {
        // Ensure inner ports list initialized
        List<ApplicationGatewayFrontendPortInner> frontendPorts = this.inner().frontendPorts();
        if (frontendPorts == null) {
            frontendPorts = new ArrayList<ApplicationGatewayFrontendPortInner>();
            this.inner().withFrontendPorts(frontendPorts);
        }

        // Attempt to find inner port by name if provided, or port number otherwise
        ApplicationGatewayFrontendPortInner frontendPortByName = null;
        ApplicationGatewayFrontendPortInner frontendPortByNumber = null;
        for (ApplicationGatewayFrontendPortInner inner : this.inner().frontendPorts()) {
            if (name != null && name.equalsIgnoreCase(inner.name())) {
                frontendPortByName = inner;
            }
            if (inner.port() == portNumber) {
                frontendPortByNumber = inner;
            }
        }

        Boolean needToCreate = this.needToCreate(frontendPortByName, frontendPortByNumber, name);
        if (Boolean.TRUE.equals(needToCreate)) {
            // If no conflict, create a new port
            if (name == null) {
                // No name specified, so auto-name it
                name = SdkContext.randomResourceName("port", 9);
            }

            frontendPortByName = new ApplicationGatewayFrontendPortInner()
                    .withName(name)
                    .withPort(portNumber);
            frontendPorts.add(frontendPortByName);
            return this;
        } else if (Boolean.FALSE.equals(needToCreate)) {
            // If found matching port, then nothing needs to happen
            return this;
        } else {
            // If name conflict for the same port number, then fail
            return null;
        }
    }

    @Override
    public ApplicationGatewayImpl withPrivateFrontend() {
        /* NOTE: This logic is a workaround for the unusual Azure API logic:
         * - although app gateway API definition allows multiple IP configs, only one is allowed by the service currently;
         * - although app gateway frontend API definition allows for multiple frontends, only one is allowed by the service today;
         * - and although app gateway API definition allows different subnets to be specified between the IP configs and frontends, the service
         * requires the frontend and the containing subnet to be one and the same currently.
         *
         * So the logic here attempts to figure out from the API what that containing subnet for the app gateway is so that the user wouldn't
         * have to re-enter it redundantly when enabling a private frontend, since only that one subnet is supported anyway.
         *
         * TODO: When the underlying Azure API is reworked to make more sense, or the app gateway service starts supporting the functionality
         * that the underlying API implies is supported, this model and implementation should be revisited.
         */
        ensureDefaultPrivateFrontend();
        return this;
    }

    // Withers

    @Override
    public ApplicationGatewayImpl withExistingPublicIpAddress(PublicIpAddress publicIpAddress) {
        ensureDefaultPublicFrontend().withExistingPublicIpAddress(publicIpAddress);
        return this;
    }

    @Override
    public ApplicationGatewayImpl withExistingPublicIpAddress(String resourceId) {
        ensureDefaultPublicFrontend().withExistingPublicIpAddress(resourceId);
        return this;
    }

    @Override
    public ApplicationGatewayImpl withNewPublicIpAddress(Creatable<PublicIpAddress> creatable) {
        final String name = ensureDefaultPublicFrontend().name();
        this.creatablePipsByFrontend.put(name, creatable.key());
        this.addCreatableDependency(creatable);
        return this;
    }

    @Override
    public ApplicationGatewayImpl withNewPublicIpAddress() {
        ensureDefaultPublicFrontend();
        return this;
    }

    @Override
    public ApplicationGatewayImpl withoutBackendFqdn(String fqdn) {
        for (ApplicationGatewayBackend backend : this.backends.values()) {
            ((ApplicationGatewayBackendImpl) backend).withoutFqdn(fqdn);
        }
        return this;
    }

    @Override
    public ApplicationGatewayImpl withoutBackendIpAddress(String ipAddress) {
        for (ApplicationGatewayBackend backend : this.backends.values()) {
            ApplicationGatewayBackendImpl backendImpl = (ApplicationGatewayBackendImpl) backend;
            backendImpl.withoutIpAddress(ipAddress);
        }
        return this;
    }

    @Override
    public ApplicationGatewayImpl withoutIpConfiguration(String ipConfigurationName) {
        this.ipConfigs.remove(ipConfigurationName);
        return this;
    }

    @Override
    public ApplicationGatewayImpl withoutFrontend(String frontendName) {
        this.frontends.remove(frontendName);
        return this;
    }

    @Override
    public ApplicationGatewayImpl withoutFrontendPort(String name) {
        if (this.inner().frontendPorts() == null) {
            return this;
        }

        for (int i = 0; i < this.inner().frontendPorts().size(); i++) {
            ApplicationGatewayFrontendPortInner inner = this.inner().frontendPorts().get(i);
            if (inner.name().equalsIgnoreCase(name)) {
                this.inner().frontendPorts().remove(i);
                break;
            }
        }

        return this;
    }

    @Override
    public ApplicationGatewayImpl withoutFrontendPort(int portNumber) {
        for (int i = 0; i < this.inner().frontendPorts().size(); i++) {
            ApplicationGatewayFrontendPortInner inner = this.inner().frontendPorts().get(i);
            if (inner.port().equals(portNumber)) {
                this.inner().frontendPorts().remove(i);
                break;
            }
        }

        return this;
    }

    @Override
    public ApplicationGatewayImpl withoutCertificate(String name) {
        this.sslCerts.remove(name);
        return this;
    }

    @Override
    public ApplicationGatewayImpl withoutListener(String name) {
        this.listeners.remove(name);
        return this;
    }

    @Override
    public ApplicationGatewayImpl withoutRequestRoutingRule(String name) {
        this.rules.remove(name);
        return this;
    }

    @Override
    public ApplicationGatewayImpl withoutBackend(String backendName) {
        this.backends.remove(backendName);
        return this;
    }

    @Override
    public ApplicationGatewayBackendImpl updateBackend(String name) {
        return (ApplicationGatewayBackendImpl) this.backends.get(name);
    }

    @Override
    public ApplicationGatewayFrontendImpl updatePublicFrontend() {
        return (ApplicationGatewayFrontendImpl) defaultPublicFrontend();
    }

    /* TODO: Not possible to change any subnet/private IP settings today
    @Override
    public ApplicationGatewayFrontendImpl updatePrivateFrontend() {
        return (ApplicationGatewayFrontendImpl) defaultPrivateFrontend();
    }
    */

    @Override
    public ApplicationGatewayListenerImpl updateListener(String name) {
        return (ApplicationGatewayListenerImpl) this.listeners.get(name);
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl updateRequestRoutingRule(String name) {
        return (ApplicationGatewayRequestRoutingRuleImpl) this.rules.get(name);
    }

    @Override
    public ApplicationGatewayImpl withoutBackendHttpConfiguration(String name) {
        this.backendHttpConfigs.remove(name);
        return this;
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl updateBackendHttpConfiguration(String name) {
        return (ApplicationGatewayBackendHttpConfigurationImpl) this.backendHttpConfigs.get(name);
    }

    @Override
    public ApplicationGatewayIpConfigurationImpl updateIpConfiguration(String ipConfigurationName) {
        return (ApplicationGatewayIpConfigurationImpl) this.ipConfigs.get(ipConfigurationName);
    }

    @Override
    public ApplicationGatewayIpConfigurationImpl updateDefaultIpConfiguration() {
        return (ApplicationGatewayIpConfigurationImpl) this.defaultIpConfiguration();
    }

    @Override
    public ApplicationGatewayIpConfigurationImpl defineDefaultIpConfiguration() {
        return ensureDefaultIpConfig();
    }

    @Override
    public ApplicationGatewayFrontendImpl definePublicFrontend() {
        return ensureDefaultPublicFrontend();
    }

    @Override
    public ApplicationGatewayFrontendImpl definePrivateFrontend() {
        return ensureDefaultPrivateFrontend();
    }

    @Override
    public ApplicationGatewayFrontendImpl updateFrontend(String frontendName) {
        return (ApplicationGatewayFrontendImpl) this.frontends.get(frontendName);
    }

    // Getters

    @Override
    public ApplicationGatewayFrontend defaultPrivateFrontend() {
        // Default means the only private one or the one tracked as default, if more than one private present
        Map<String, ApplicationGatewayFrontend> privateFrontends = this.privateFrontends();
        if (privateFrontends.size() == 1) {
            this.defaultPrivateFrontend = (ApplicationGatewayFrontendImpl) privateFrontends.values().iterator().next();
        } else if (this.frontends().size() == 0) {
            this.defaultPrivateFrontend = null;
        }

        return this.defaultPrivateFrontend;
    }

    @Override
    public ApplicationGatewayFrontend defaultPublicFrontend() {
        // Default means the only public one or the one tracked as default, if more than one public present
        Map<String, ApplicationGatewayFrontend> publicFrontends = this.publicFrontends();
        if (publicFrontends.size() == 1) {
            this.defaultPublicFrontend = (ApplicationGatewayFrontendImpl) publicFrontends.values().iterator().next();
        } else if (this.frontends().size() == 0) {
            this.defaultPublicFrontend = null;
        }

        return this.defaultPublicFrontend;
    }

    @Override
    public ApplicationGatewayIpConfiguration defaultIpConfiguration() {
        // Default means the only one
        if (this.ipConfigs.size() == 1) {
            return this.ipConfigs.values().iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public ApplicationGatewayListener listenerByPortNumber(int portNumber) {
        ApplicationGatewayListener listener = null;
        for (ApplicationGatewayListener l : this.listeners.values()) {
            if (l.frontendPortNumber() == portNumber) {
                listener = l;
                break;
            }
        }
        return listener;
    }

    @Override
    public Map<String, ApplicationGatewayBackendHttpConfiguration> backendHttpConfigurations() {
        return Collections.unmodifiableMap(this.backendHttpConfigs);
    }

    @Override
    public Map<String, ApplicationGatewayBackend> backends() {
        return Collections.unmodifiableMap(this.backends);
    }

    @Override
    public Map<String, ApplicationGatewayRequestRoutingRule> requestRoutingRules() {
        return Collections.unmodifiableMap(this.rules);
    }

    @Override
    public Map<String, ApplicationGatewayFrontend> frontends() {
        return Collections.unmodifiableMap(this.frontends);
    }

    @Override
    public Map<String, ApplicationGatewaySslCertificate> sslCertificates() {
        return Collections.unmodifiableMap(this.sslCerts);
    }

    @Override
    public Map<String, ApplicationGatewayListener> listeners() {
        return Collections.unmodifiableMap(this.listeners);
    }

    @Override
    public Map<String, ApplicationGatewayIpConfiguration> ipConfigurations() {
        return Collections.unmodifiableMap(this.ipConfigs);
    }

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
    public Map<String, Integer> frontendPorts() {
        Map<String, Integer> ports = new TreeMap<>();
        if (this.inner().frontendPorts() != null) {
            for (ApplicationGatewayFrontendPortInner portInner : this.inner().frontendPorts()) {
                ports.put(portInner.name(), portInner.port());
            }
        }
        return Collections.unmodifiableMap(ports);
    }

    @Override
    public String frontendPortNameFromNumber(int portNumber) {
        String portName = null;
        for (Entry<String, Integer> portEntry : this.frontendPorts().entrySet()) {
            if (portNumber == portEntry.getValue()) {
                portName = portEntry.getKey();
                break;
            }
        }
        return portName;
    }

    private SubResource defaultSubnetRef() {
        ApplicationGatewayIpConfiguration ipConfig = defaultIpConfiguration();
        if (ipConfig == null) {
            return null;
        } else {
            return ipConfig.inner().subnet();
        }
    }

    @Override
    public String networkId() {
        SubResource subnetRef = defaultSubnetRef();
        if (subnetRef == null) {
            return null;
        } else {
            return ResourceUtils.parentResourceIdFromResourceId(subnetRef.id());
        }
    }

    @Override
    public String subnetName() {
        SubResource subnetRef = defaultSubnetRef();
        if (subnetRef == null) {
            return null;
        } else {
            return ResourceUtils.nameFromResourceId(subnetRef.id());
        }
    }

    @Override
    public NetworkManager manager() {
        return super.myManager;
    }

    @Override
    public String privateIpAddress() {
        ApplicationGatewayFrontend frontend = defaultPrivateFrontend();
        if (frontend == null) {
            return null;
        } else {
            return frontend.privateIpAddress();
        }
    }

    @Override
    public IPAllocationMethod privateIpAllocationMethod() {
        ApplicationGatewayFrontend frontend = defaultPrivateFrontend();
        if (frontend == null) {
            return null;
        } else {
            return frontend.privateIpAllocationMethod();
        }
    }

    @Override
    public boolean isPrivate() {
        for (ApplicationGatewayFrontend frontend : this.frontends.values()) {
            if (frontend.isPrivate()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPublic() {
        for (ApplicationGatewayFrontend frontend : this.frontends.values()) {
            if (frontend.isPublic()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<String, ApplicationGatewayFrontend> publicFrontends() {
        Map<String, ApplicationGatewayFrontend> publicFrontends = new TreeMap<>();
        for (ApplicationGatewayFrontend frontend : this.frontends().values()) {
            if (frontend.isPublic()) {
                publicFrontends.put(frontend.name(), frontend);
            }
        }

        return Collections.unmodifiableMap(publicFrontends);
    }

    @Override
    public Map<String, ApplicationGatewayFrontend> privateFrontends() {
        Map<String, ApplicationGatewayFrontend> privateFrontends = new TreeMap<>();
        for (ApplicationGatewayFrontend frontend : this.frontends.values()) {
            if (frontend.isPrivate()) {
                privateFrontends.put(frontend.name(), frontend);
            }
        }

        return Collections.unmodifiableMap(privateFrontends);
    }

    @Override
    public int instanceCount() {
        if (this.sku() != null && this.sku().capacity() != null) {
            return this.sku().capacity();
        } else {
            return 1;
        }
    }

    @Override
    public ApplicationGatewaySkuName size() {
        if (this.sku() != null && this.sku().name() != null) {
            return this.sku().name();
        } else {
            return ApplicationGatewaySkuName.STANDARD_SMALL;
        }
    }

    @Override
    public ApplicationGatewayTier tier() {
        if (this.sku() != null && this.sku().tier() != null) {
            return this.sku().tier();
        } else {
            return ApplicationGatewayTier.STANDARD;
        }
    }

    @Override
    public Update withoutPublicIpAddress() {
        return this.withoutPublicFrontend();
    }
}
