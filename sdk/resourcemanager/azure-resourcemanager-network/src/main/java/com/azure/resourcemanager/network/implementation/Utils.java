// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.SubnetInner;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackend;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendAddressPool;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Defines a few utilities. */
public final class Utils {

    private Utils() {
    }

    // Internal utility function
    static Subnet getAssociatedSubnet(NetworkManager manager, SubResource subnetRef) {
        if (subnetRef == null) {
            return null;
        }

        String vnetId = ResourceUtils.parentResourceIdFromResourceId(subnetRef.id());
        String subnetName = ResourceUtils.nameFromResourceId(subnetRef.id());

        if (vnetId == null || subnetName == null) {
            return null;
        }

        Network network = manager.networks().getById(vnetId);
        if (network == null) {
            return null;
        }

        return network.subnets().get(subnetName);
    }

    // Internal utility function
    static List<Subnet> listAssociatedSubnets(NetworkManager manager, List<SubnetInner> subnetRefs) {
        final Map<String, Network> networks = new HashMap<>();
        final List<Subnet> subnets = new ArrayList<>();

        if (subnetRefs != null) {
            for (SubnetInner subnetRef : subnetRefs) {
                String networkId = ResourceUtils.parentResourceIdFromResourceId(subnetRef.id());
                Network network = networks.get(networkId.toLowerCase(Locale.ROOT));
                if (network == null) {
                    network = manager.networks().getById(networkId);
                    networks.put(networkId.toLowerCase(Locale.ROOT), network);
                }

                String subnetName = ResourceUtils.nameFromResourceId(subnetRef.id());
                subnets.add(network.subnets().get(subnetName));
            }
        }

        return Collections.unmodifiableList(subnets);
    }

    // Internal utility function
    static Collection<ApplicationGatewayBackend> listAssociatedApplicationGatewayBackends(
        NetworkManager manager, List<ApplicationGatewayBackendAddressPool> backendRefs) {
        final Map<String, ApplicationGateway> appGateways = new HashMap<>();
        final List<ApplicationGatewayBackend> backends = new ArrayList<>();

        if (backendRefs != null) {
            for (ApplicationGatewayBackendAddressPool backendRef : backendRefs) {
                String appGatewayId = ResourceUtils.parentResourceIdFromResourceId(backendRef.id());
                ApplicationGateway appGateway = appGateways.get(appGatewayId.toLowerCase(Locale.ROOT));
                if (appGateway == null) {
                    appGateway = manager.applicationGateways().getById(appGatewayId);
                    appGateways.put(appGatewayId.toLowerCase(Locale.ROOT), appGateway);
                }

                String backendName = ResourceUtils.nameFromResourceId(backendRef.id());
                backends.add(appGateway.backends().get(backendName));
            }
        }

        return Collections.unmodifiableCollection(backends);
    }
}
