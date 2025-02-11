// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.implementation.models;

import com.azure.core.annotation.Immutable;
import com.azure.security.attestation.models.AttestationSigner;
import com.azure.security.attestation.models.AttestationSignerCollection;

import java.util.List;

/**
 * An AttestationSignerCollection represents a collection of {@link AttestationSigner} objects.
 */
@Immutable
public class AttestationSignerCollectionImpl implements AttestationSignerCollection {
    private List<AttestationSigner> signers;

    public AttestationSignerCollectionImpl(List<AttestationSigner> newSigners) {
        signers = newSigners;
    }

    /**
     * Retrieve the list of attestation signers in the collection.
     * @return a {@link List} of {@link AttestationSigner} objects.
     */
    public List<AttestationSigner> getAttestationSigners() {
        return signers;
    }
}
