// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.AddressSpace;
import com.azure.resourcemanager.network.models.PointToSiteConfiguration;
import com.azure.resourcemanager.network.models.VirtualNetworkGateway;
import com.azure.resourcemanager.network.models.VpnClientConfiguration;
import com.azure.resourcemanager.network.models.VpnClientProtocol;
import com.azure.resourcemanager.network.models.VpnClientRevokedCertificate;
import com.azure.resourcemanager.network.models.VpnClientRootCertificate;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implementation for PointToSiteConfiguration and its create and update interfaces. */
class PointToSiteConfigurationImpl extends IndexableWrapperImpl<VpnClientConfiguration>
    implements PointToSiteConfiguration,
        PointToSiteConfiguration.Definition<VirtualNetworkGateway.Update>,
        PointToSiteConfiguration.Update {
    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERT = "-----END CERTIFICATE-----";

    private VirtualNetworkGatewayImpl parent;

    PointToSiteConfigurationImpl(VpnClientConfiguration inner, VirtualNetworkGatewayImpl parent) {
        super(inner);
        this.parent = parent;
    }

    @Override
    public VirtualNetworkGatewayImpl attach() {
        parent.attachPointToSiteConfiguration(this);
        return parent;
    }

    @Override
    public PointToSiteConfigurationImpl withAddressPool(String addressPool) {
        List<String> addressPrefixes = new ArrayList<>();
        addressPrefixes.add(addressPool);
        innerModel().withVpnClientAddressPool(new AddressSpace().withAddressPrefixes(addressPrefixes));
        return this;
    }

    @Override
    public PointToSiteConfigurationImpl withAzureCertificate(String name, String certificateData) {
        if (innerModel().vpnClientRootCertificates() == null) {
            innerModel().withVpnClientRootCertificates(new ArrayList<VpnClientRootCertificate>());
        }
        innerModel()
            .vpnClientRootCertificates()
            .add(new VpnClientRootCertificate().withName(name).withPublicCertData(certificateData));
        innerModel().withRadiusServerAddress(null).withRadiusServerSecret(null);
        return this;
    }

    @Override
    public PointToSiteConfigurationImpl withAzureCertificateFromFile(String name, File certificateFile)
        throws IOException {
        if (certificateFile == null) {
            return this;
        } else {
            byte[] content = Files.readAllBytes(certificateFile.toPath());
            String certificate =
                new String(content, StandardCharsets.UTF_8).replace(BEGIN_CERT, "").replace(END_CERT, "");
            return this.withAzureCertificate(name, certificate);
        }
    }

    @Override
    public Update withoutAzureCertificate(String name) {
        if (innerModel().vpnClientRootCertificates() != null) {
            for (VpnClientRootCertificate certificateInner : innerModel().vpnClientRootCertificates()) {
                if (name.equals(certificateInner.name())) {
                    innerModel().vpnClientRootCertificates().remove(certificateInner);
                    break;
                }
            }
        }
        return this;
    }

    @Override
    public PointToSiteConfigurationImpl withRadiusAuthentication(String serverIPAddress, String serverSecret) {
        innerModel().withRadiusServerAddress(serverIPAddress).withRadiusServerSecret(serverSecret);
        innerModel().withVpnClientRootCertificates(null);
        innerModel().withVpnClientRevokedCertificates(null);
        return this;
    }

    @Override
    public PointToSiteConfigurationImpl withRevokedCertificate(String name, String thumbprint) {
        if (innerModel().vpnClientRevokedCertificates() == null) {
            innerModel().withVpnClientRevokedCertificates(new ArrayList<VpnClientRevokedCertificate>());
        }
        innerModel()
            .vpnClientRevokedCertificates()
            .add(new VpnClientRevokedCertificate().withName(name).withThumbprint(thumbprint));
        innerModel().withRadiusServerAddress(null).withRadiusServerSecret(null);
        return this;
    }

    @Override
    public PointToSiteConfigurationImpl withSstpOnly() {
        innerModel().withVpnClientProtocols(Collections.singletonList(VpnClientProtocol.SSTP));
        return this;
    }

    @Override
    public PointToSiteConfigurationImpl withIkeV2Only() {
        innerModel().withVpnClientProtocols(Collections.singletonList(VpnClientProtocol.IKE_V2));
        return this;
    }

    @Override
    public VirtualNetworkGateway.Update parent() {
        return parent;
    }
}
