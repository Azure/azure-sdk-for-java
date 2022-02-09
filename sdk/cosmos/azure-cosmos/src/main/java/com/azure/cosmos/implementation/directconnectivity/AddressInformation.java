// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdUtils;

import java.net.URI;
import java.util.Locale;
import java.util.Objects;

/**
 * Used internally to encapsulate a physical address information in the Azure Cosmos DB database service.
 */
public class AddressInformation {
    private Protocol protocol;
    private boolean isPublic;
    private boolean isPrimary;
    private Uri physicalUri;

    public AddressInformation(boolean isPublic, boolean isPrimary, String physicalUri, Protocol protocol) {
        Objects.requireNonNull(protocol);
        this.protocol = protocol;
        this.isPublic = isPublic;
        this.isPrimary = isPrimary;
        this.physicalUri = new Uri(normalizePhysicalUri(physicalUri));
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

    public Uri getPhysicalUri() {
        return physicalUri;
    }

    public Protocol getProtocol() {
        return this.protocol;
    }

    public String getProtocolName() {
        return this.protocol.scheme();
    }

    public String getProtocolScheme() {
        return this.protocol.scheme();
    }

    public URI getServerKey() {
        return RntbdUtils.getServerKey(this.physicalUri.getURI());
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

        switch (scheme.toLowerCase(Locale.ROOT)) {
            case "https":
                return Protocol.HTTPS;
            case "rntbd":
                return Protocol.TCP;
            default:
                throw new IllegalArgumentException(String.format("scheme: %s", scheme));
        }
    }

    private static String normalizePhysicalUri(String physicalUri) {
        if (StringUtils.isEmpty(physicalUri)) {
            return physicalUri;
        }

        // backend returns non normalized uri with "//" tail
        // e.g, https://cdb-ms-prod-westus2-fd2.documents.azure.com:15248/apps/4f5c042d-76fb-4ce6-bda3-517e6ef3984f/
        // services/cf4b9ab2-019c-45ca-ac88-25a92b66dddf/partitions/2078862a-d698-475b-a308-02598370d1d9/replicas/132077748219659199s//
        // we should trim the tail double "//"

        int i = physicalUri.length() -1;

        while(i >= 0 && physicalUri.charAt(i) == '/') {
            i--;
        }

        return physicalUri.substring(0, i + 1) + '/';
    }
}
