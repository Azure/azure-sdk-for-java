// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import com.azure.core.util.CoreUtils;

/**
 * Communication identifier for Communication Services Applications
 */
public class CallingApplicationIdentifier extends CommunicationIdentifier {

    private final String id;

    /**
     * Creates a CallingApplicationIdentifier object
     *
     * @param id the string identifier representing the identity
     * @throws IllegalArgumentException thrown if id parameter fail the validation.
     */
    public CallingApplicationIdentifier(String id) {
        if (CoreUtils.isNullOrEmpty(id)) {
            throw new IllegalArgumentException("The initialization parameter [id] cannot be null or empty.");
        }
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof CallingApplicationIdentifier)) {
            return false;
        }

        return ((CallingApplicationIdentifier) that).getId().equals(id);
    }


    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
