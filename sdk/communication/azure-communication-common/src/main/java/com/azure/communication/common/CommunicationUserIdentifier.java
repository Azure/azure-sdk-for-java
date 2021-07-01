// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import com.azure.core.util.CoreUtils;

/**
 * Communication identifier for Communication Services Users
 */
public final class CommunicationUserIdentifier extends CommunicationIdentifier {

    private final String id;

    /**
     * Creates a CommunicationUserIdentifier object
     *
     * @param id id of the communication user.
     * @throws IllegalArgumentException thrown if id parameter fail the validation.
     */
    public CommunicationUserIdentifier(String id) {
        if (CoreUtils.isNullOrEmpty(id)) {
            throw new IllegalArgumentException("The initialization parameter [id] cannot be null or empty.");
        }
        this.id = id;
    }

    /**
     * Get id of the communication user.
     * @return id of the communication user.
     */
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof CommunicationUserIdentifier)) {
            return false;
        }

        return ((CommunicationUserIdentifier) that).getId().equals(id);
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
