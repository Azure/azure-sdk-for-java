// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.models;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * An AttestationSigner object represents an entity which might sign a certificate from the attestation
 * service.
 */
public interface AttestationSigner {
    /**
     * Gets the Certificates associated with this signer.
     * <p>
     * The Certificates is an X.509 certificate chain associated with a particular attestation signer.
     * <p>
     * It corresponds to the `x5c` property on a JSON Web Key. See <a href="https://datatracker.ietf.org/doc/html/rfc7517#section-4.7">JsonWebKey RFC Section 4.7</a>
     * for more details.
     *
     * @return Certificate chain used to sign an attestation token.
     */
    List<X509Certificate> getCertificates();

    /**
     * Gets the KeyId.
     * <p>
     * The KeyId is matched with the "kid" property in a JsonWebSignature object. It corresponds
     * to the kid property defined in <a href="https://datatracker.ietf.org/doc/html/rfc7517#section-4.5">JsonWebKey RFC section 4.5</a>
     *
     * @return KeyId.
     */
    String getKeyId();

    /**
     * Validate that the attestation signer is valid.
     */
    void validate();
}
