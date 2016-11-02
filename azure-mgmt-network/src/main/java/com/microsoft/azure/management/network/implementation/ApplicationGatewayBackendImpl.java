/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayBackend;
import com.microsoft.azure.management.network.ApplicationGatewayBackendAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for ApplicationGatewayBackend.
 */
@LangDefinition
class ApplicationGatewayBackendImpl
    extends ChildResourceImpl<ApplicationGatewayBackendAddressPoolInner, ApplicationGatewayImpl, ApplicationGateway>
    implements
        ApplicationGatewayBackend,
        ApplicationGatewayBackend.Definition<ApplicationGateway.DefinitionStages.WithBackendOrHttpConfig>,
        ApplicationGatewayBackend.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayBackend.Update {

    ApplicationGatewayBackendImpl(ApplicationGatewayBackendAddressPoolInner inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
    }

    // Helpers

    private List<ApplicationGatewayBackendAddress> ensureAddresses() {
        List<ApplicationGatewayBackendAddress> addresses = this.inner().backendAddresses();
        if (addresses == null) {
            addresses = new ArrayList<ApplicationGatewayBackendAddress>();
            this.inner().withBackendAddresses(addresses);
        }
        return addresses;
    }

    // Getters

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public Map<String, String> backendNicIpConfigurationNames() {
        // This assumes a NIC can only have one IP config associated with the backend of an app gateway,
        // which is correct at the time of this implementation and seems unlikely to ever change
        final Map<String, String> ipConfigNames = new TreeMap<>();
        if (this.inner().backendIPConfigurations() != null) {
            for (NetworkInterfaceIPConfigurationInner inner : this.inner().backendIPConfigurations()) {
                String nicId = ResourceUtils.parentResourceIdFromResourceId(inner.id());
                String ipConfigName = ResourceUtils.nameFromResourceId(inner.id());
                ipConfigNames.put(nicId, ipConfigName);
            }
        }

        return Collections.unmodifiableMap(ipConfigNames);
    }

    @Override
    public List<ApplicationGatewayBackendAddress> addresses() {
        List<ApplicationGatewayBackendAddress> addresses = new ArrayList<>();
        if (this.inner().backendAddresses() != null) {
            for (ApplicationGatewayBackendAddress address : this.inner().backendAddresses()) {
                addresses.add(address);
            }
        }
        return Collections.unmodifiableList(addresses);
    }

    // Verbs

    @Override
    public ApplicationGatewayImpl attach() {
        this.parent().withBackend(this);
        return this.parent();
    }

    // Withers

    @Override
    public ApplicationGatewayBackendImpl withIpAddress(String ipAddress) {
        ApplicationGatewayBackendAddress address = new ApplicationGatewayBackendAddress()
                .withIpAddress(ipAddress);
        ensureAddresses().add(address);
        return this;
    }

    @Override
    public ApplicationGatewayBackendImpl withFqdn(String fqdn) {
        ApplicationGatewayBackendAddress address = new ApplicationGatewayBackendAddress()
                .withFqdn(fqdn);
        ensureAddresses().add(address);
        return this;
    }
}
