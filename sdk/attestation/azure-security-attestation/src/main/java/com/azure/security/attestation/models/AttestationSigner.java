// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.models;
import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * Represents an attestation signing certificate returned by the attestation service.
 */
@Fluent
public class AttestationSigner {

     /**
     * Sets the signing certificate.
     * @param certificates Array of X509Certificate objects.
     * @return AttestationSigner
     */
    public AttestationSigner certificates(final java.security.cert.X509Certificate[] certificates) {
        this.certificates = cloneX509CertificateChain(certificates);
        return this;
    }

    /**
     * Clone an X.509 certificate chain. Used to ensure that the `certificates` property remains immutable.
     *
     * @param certificates X.509 certificate chain to clone.
     * @return Cloned X.509 certificate chain. Each element is a newly certificate deep copied from an
     *  existing certificate.
     */
    private X509Certificate[] cloneX509CertificateChain(X509Certificate[] certificates) {
        ClientLogger logger = new ClientLogger(AttestationSigner.class);
        return Arrays.stream(certificates).map(certificate -> {
            X509Certificate newCert;
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                newCert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificate.getEncoded()));
            } catch (CertificateException e) {
                throw logger.logExceptionAsError(new RuntimeException(e));
            }
            return newCert;
        }).toArray(X509Certificate[]::new);
    }

    /**
     * Sets the KeyId.
     *
     * The KeyId is matched with the "kid" property in a JsonWebSignature object. It corresponds
     * to the kid property defined in <a href="https://datatracker.ietf.org/doc/html/rfc7517#section-4.5">JsonWebKey RFC section 4.5</a>
     *
     * @param keyId Key ID associated with this signer
     * @return AttestationSigner
     */
    public AttestationSigner keyId(String keyId) {
        this.keyId = keyId;
        return this;
    }

    /**
     * Gets the Certificates associated with this signer.
     *
     * The Certificates is an X.509 certificate chain associated with a particular attestation signer.
     *
     * It corresponds to the `x5c` property on a JSON Web Key. See <a href="https://datatracker.ietf.org/doc/html/rfc7517#section-4.7">JsonWebKey RFC Section 4.7</a>
     * for more details.
     *
     * @return Certificate chain used to sign an attestation token.
     */
    public final X509Certificate[] getCertificates() {
        return cloneX509CertificateChain(this.certificates);
    }

    /**
     * Gets the KeyId.
     *
     * The KeyId is matched with the "kid" property in a JsonWebSignature object. It corresponds
     * to the kid property defined in <a href="https://datatracker.ietf.org/doc/html/rfc7517#section-4.5">JsonWebKey RFC section 4.5</a>
     *
     * @return KeyId.
     */
    public String getKeyId() {
        return keyId;
    }

    private java.security.cert.X509Certificate[] certificates;
    private String keyId;
}
