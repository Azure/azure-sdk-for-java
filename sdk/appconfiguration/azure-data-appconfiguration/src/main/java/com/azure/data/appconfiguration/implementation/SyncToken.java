// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

/**
 * SyncToken class requires id, value and sequence number
 */
public final class SyncToken {
    private String id, value;
    private long sequenceNumber;
    private String syncTokenString;

    /**
     * Create an instance of SyncToken class
     *
     * @param id ID of token
     * @param value Value of Token
     * @param sequenceNumber Token sequence number (version). Higher value means newer version of the same token.
     */
    private SyncToken(String id, String value, long sequenceNumber, String syncTokenString) {
        this.id = id;
        this.value = value;
        this.sequenceNumber = sequenceNumber;
        this.syncTokenString = syncTokenString;
    }

    static SyncToken fromSyncTokenString(String syncToken) {
        if (CoreUtils.isNullOrEmpty(syncToken)) {
            return null;
        }
        try {
            final String[] syncTokenParts = syncToken.split(";", 2);
            // Not a fully formed Sync-Token
            if (syncTokenParts.length != 2) {
                return null;
            }

            final String[] idParts = syncTokenParts[0].split("=", 2);
            // Identifier is missing a section.
            if (idParts.length != 2) {
                return null;
            }

            final String[] snParts = syncTokenParts[1].split("=", 2);
            if (snParts.length != 2) {
                return null;
            }

            final long sequenceNumber = Long.parseLong(snParts[1]);
            return new SyncToken(idParts[0], idParts[1], sequenceNumber, syncToken);
        } catch (NumberFormatException e) {
            new ClientLogger(SyncToken.class).logExceptionAsWarning(
                new RuntimeException("Cannot parse sequence number for invalid number format."));
            return null;
        }
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public String getSyncTokenStringInRequest() {
        return new StringBuilder().append(id).append("=").append(value).toString();
    }
}
