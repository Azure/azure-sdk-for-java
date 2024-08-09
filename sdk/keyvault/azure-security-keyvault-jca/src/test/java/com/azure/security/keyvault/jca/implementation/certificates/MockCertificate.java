// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.certificates;

import java.security.PublicKey;
import java.security.cert.Certificate;

/**
 * Mock of {@link Certificate}.
 */
public final class MockCertificate extends Certificate {
    /**
     * Creates a certificate of the specified type.
     *
     * @param type the standard name of the certificate type.
     * See the CertificateFactory section in the <a href=
     * "{@docRoot}/../specs/security/standard-names.html#certificatefactory-types">
     * Java Security Standard Algorithm Names Specification</a>
     * for information about standard certificate types.
     */
    protected MockCertificate(String type) {
        super(type);
    }

    @Override
    public byte[] getEncoded() {
        return new byte[0];
    }

    @Override
    public void verify(PublicKey key) {

    }

    @Override
    public void verify(PublicKey key, String sigProvider) {

    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public PublicKey getPublicKey() {
        return null;
    }
}
