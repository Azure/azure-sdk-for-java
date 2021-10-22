// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

/**
 * The AttestationData class represents data sent to the Attestation service as either InitTimeData or
 * RunTimeData.
 *
 */
@Immutable
public final class AttestationData implements Cloneable {
    private final BinaryData data;
    private final AttestationDataInterpretation interpretation;

    /**
     * Creates a new AttestationData object for the data and how it should be interpreted by the attestation
     * service.
     * @param data - Data to be set.
     * @param interpretation - How the data should be interpreted.
     */
    public AttestationData(BinaryData data, AttestationDataInterpretation interpretation) {
        this.data = data;
        this.interpretation = interpretation;
    }

    /**
     * Returns the data with which this {@link AttestationData} was created.
     * @return The data configured for this object.
     */
    public BinaryData getData() {
        return data;
    }

    /**
     * Returns the interpretation of this attestation data.
     * @return The expected in interpretation for the data.
     */
    public AttestationDataInterpretation getInterpretation() {
        return interpretation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttestationData clone() {
        return new AttestationData(this.data, this.interpretation);
    }
}

