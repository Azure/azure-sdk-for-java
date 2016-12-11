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
        ApplicationGatewayBackend.Definition<ApplicationGateway.DefinitionStages.WithCreate>,
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
        if (ipAddress == null) {
            return this;
        }

        ApplicationGatewayBackendAddress address = new ApplicationGatewayBackendAddress()
                .withIpAddress(ipAddress);
        List<ApplicationGatewayBackendAddress> addresses = ensureAddresses();
        for (ApplicationGatewayBackendAddress a : addresses) {
            if (ipAddress.equalsIgnoreCase(a.ipAddress())) {
                return this; // Address already included, so skip
            }
        }
        addresses.add(address);
        return this;
    }

    @Override
    public ApplicationGatewayBackendImpl withFqdn(String fqdn) {
        if (fqdn == null) {
            return this;
        }
        ApplicationGatewayBackendAddress address = new ApplicationGatewayBackendAddress()
                .withFqdn(fqdn);
        ensureAddresses().add(address);
        return this;
    }

    @Override
    public ApplicationGatewayBackendImpl withoutIpAddress(String ipAddress) {
        if (ipAddress == null) {
            return this;
        }
        if (this.inner().backendAddresses() == null) {
            return this;
        }

        final List<ApplicationGatewayBackendAddress> addresses = ensureAddresses();
        for (int i = 0; i < addresses.size(); i++) {
            String curIpAddress = addresses.get(i).ipAddress();
            if (curIpAddress != null && curIpAddress.equalsIgnoreCase(ipAddress)) {
                addresses.remove(i);
                break;
            }
        }
        return this;
    }

    @Override
    public ApplicationGatewayBackendImpl withoutAddress(ApplicationGatewayBackendAddress address) {
        ensureAddresses().remove(address);
        return this;
    }

    @Override
    public ApplicationGatewayBackendImpl withoutFqdn(String fqdn) {
        if (fqdn == null) {
            return this;
        }
        final List<ApplicationGatewayBackendAddress> addresses = ensureAddresses();
        for (int i = 0; i < addresses.size(); i++) {
            String curFqdn = addresses.get(i).fqdn();
            if (curFqdn != null && curFqdn.equalsIgnoreCase(fqdn)) {
                addresses.remove(i);
                break;
            }
        }
        return this;
    }

    @Override
    public boolean containsIpAddress(String ipAddress) {
        if (ipAddress != null) {
            for (ApplicationGatewayBackendAddress address : this.inner().backendAddresses()) {
                if (ipAddress.equalsIgnoreCase(address.ipAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean containsFqdn(String fqdn) {
        if (fqdn != null) {
            for (ApplicationGatewayBackendAddress address : this.inner().backendAddresses()) {
                if (fqdn.equalsIgnoreCase(address.fqdn())) {
                    return true;
                }
            }
        }
        return false;
    }
}
