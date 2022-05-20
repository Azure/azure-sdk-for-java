// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

/**
 * Common communication identifier for Communication Services
 */
public abstract class CommunicationIdentifier {
    protected String rawId;

    /**
     * Returns the rawId for a given CommunicationIdentifier.
     * You can use the rawId for encoding the identifier and then use it as a key in a database.
     * @return raw id
     */
    public String getRawId() {
        return rawId;
    }

    public int hashCode() {
        return getRawId().hashCode();
    }
}
