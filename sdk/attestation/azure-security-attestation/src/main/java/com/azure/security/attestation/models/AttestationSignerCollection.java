// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * An AttestationSignerCollection represents a collection of {@link AttestationSigner} objects.
 */
@Immutable
public interface AttestationSignerCollection {
    /**
     * Retrieve the list of attestation signers in the collection.
     * @return a {@link List} of {@link AttestationSigner} objects.
     */
    List<AttestationSigner> getAttestationSigners();
}
