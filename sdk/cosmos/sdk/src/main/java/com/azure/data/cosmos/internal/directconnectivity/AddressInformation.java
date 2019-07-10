/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
