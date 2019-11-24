// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

/**
 * SyncToken class requires id, value and sequence number
 */
public final class SyncToken {
    private final ClientLogger logger = new ClientLogger(SyncToken.class);

    private String id, value;
    private long sequenceNumber;


    SyncToken() {
    }

    /**
     * Create an instance of SyncToken class
     *
     * @param id ID of token
     * @param value Value of Token
     * @param sequenceNumber Token sequence number (version). Higher value means newer version of the same token.
     */
    SyncToken(String id, String value, long sequenceNumber) {
        this.id = id;
        this.value = value;
        this.sequenceNumber = sequenceNumber;
    }

    public SyncToken fromSyncTokenString(String syncToken) {
        if (CoreUtils.isNullOrEmpty(syncToken)) {
            return null;
        }

        try {
            final int position = syncToken.indexOf(";sn=");
            sequenceNumber = Long.parseLong(syncToken.substring(position + 4));
            final String idValue = syncToken.substring(0, position);
            final int jointPosition = idValue.indexOf("=");
            id = idValue.substring(0, jointPosition);
            value = idValue.substring(jointPosition + 1);
            if (CoreUtils.isNullOrEmpty(id) || CoreUtils.isNullOrEmpty(value) || sequenceNumber == 0) {
                return null;
            }

        } catch (NumberFormatException e) {
            logger.logExceptionAsError(
                new RuntimeException("Cannot parse sequence number for invalid number format."));
            return null;
        }

        return new SyncToken(id, value, sequenceNumber);

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


    @Override
    public String toString() {
        return String.format(id + "=" + value);
    }
}
