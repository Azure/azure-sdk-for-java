// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import java.util.Objects;

/**
 * Used internally to encapsulate a physical address information in the Azure Cosmos DB database service.
 */
public class AddressInformation {
    private Protocol protocol;
    private boolean isPublic;
    private boolean isPrimary;
    private String physicalUri;

    public AddressInformation(boolean isPublic, boolean isPrimary, String physicalUri, Protocol protocol) {
        Objects.requireNonNull(protocol);
        this.protocol = protocol;
        this.isPublic = isPublic;
        this.isPrimary = isPrimary;
        this.physicalUri = physicalUri;
    }

    public AddressInformation(boolean isPublic, boolean isPrimary, String physicalUri, String protocolScheme) {
        this(isPublic, isPrimary, physicalUri, scheme2protocol(protocolScheme));
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public String getPhysicalUri() {
        return physicalUri;
    }

    public Protocol getProtocol() {
        return this.protocol;
    }

    public String getProtocolName() {
        return this.protocol.toString();
    }

    public String getProtocolScheme() {
        return this.protocol.scheme();
    }

    @Override
    public String toString() {
        return "AddressInformation{" +
                "protocol='" + protocol + '\'' +
                ", isPublic=" + isPublic +
                ", isPrimary=" + isPrimary +
                ", physicalUri='" + physicalUri + '\'' +
                '}';
    }

    private static Protocol scheme2protocol(String scheme) {

        Objects.requireNonNull(scheme, "scheme");

        switch (scheme.toLowerCase()) {
            case "https":
                return Protocol.HTTPS;
            case "rntbd":
                return Protocol.TCP;
            default:
                throw new IllegalArgumentException(String.format("scheme: %s", scheme));
        }
    }
}
