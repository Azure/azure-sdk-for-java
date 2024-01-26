// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.util.polling;

/**
 * A test certificate type for testing polling.
 */
public class CertificateOutput {
    private final String name;

    /**
     * Creates a new CertificateOutput object.
     *
     * @param certName The certificate name
     */
    public CertificateOutput(String certName) {
        name = certName;
    }

    /**
     * Gets the certificate name
     *
     * @return The certificate name.
     */
    public String getName() {
        return name;
    }
}
