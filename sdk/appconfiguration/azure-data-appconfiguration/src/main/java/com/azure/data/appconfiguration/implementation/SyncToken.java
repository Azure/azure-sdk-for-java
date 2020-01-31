// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

/**
 * Due to the nature of some distributed systems real-time consistency between requests can't (or it's very hard) to be
 * enforced implicitly. A solution is to allow protocol support in the form of multiple Synchronization Tokens.
 * Synchronization tokens are optional.
 *
 * Uses optional Sync-Token request/response headers will guarantee real-time consistency between different client
 * instances and requests.
 *
 * @see <a href="https://github.com/Azure/AppConfiguration/blob/master/docs/REST/consistency.md"/>
 */
@Immutable
public final class SyncToken {
    private final ClientLogger logger = new ClientLogger(SyncToken.class);

    private String id;
    private String value;
    private long sequenceNumber;

    /**
     * Create an instance of SyncToken class. Skip the sync token if the parsing failed.
     *
     * @param syncToken only one raw sync-token string from HTTP response header, ex.,
     * <p>Sync-Token: {@code <id>=<value>;sn=<sn></p>}. But not
     * <p>Sync-Token: {@code <id>=<value>;sn=<sn>,<id>=<value>;sn=<sn>}</p>
     * Allows for better concurrency and client cache ability. The client may choose to use only token's last version,
     * since token versions are inclusive. Not required for requests.
     */
    SyncToken(String syncToken) {
        if (CoreUtils.isNullOrEmpty(syncToken)) {
            return;
        }

        final String[] syncTokenParts = syncToken.split(";", 2);
        // Not a fully formatted sync-token
        if (syncTokenParts.length != 2) {
            logger.warning("Failed to parse sync token, it cannot split to two parts by delimiter ';'.");
            return;
        }

        final String[] idParts = syncTokenParts[0].split("=", 2);
        // Identifier is missing a section.
        if (idParts.length != 2 || idParts[0].isEmpty() || idParts[1].isEmpty()) {
            logger.warning("Failed to parse sync token, it cannot split 'id=value' into two parts.");
            return;
        }

        final String[] snParts = syncTokenParts[1].split("=", 2);
        // Sequence number is missing a section
        if (snParts.length != 2 || snParts[0].isEmpty() || snParts[1].isEmpty()) {
            logger.warning("Failed to parse sync token, it cannot split 'sn=value' into two parts.");
            return;
        }

        // Not a valid number format
        try {
            this.sequenceNumber = Long.parseLong(snParts[1]);
        } catch (NumberFormatException ex) {
            logger.warning("Cannot parse sequence number for invalid number format.", ex);
            return;
        }

        this.id = idParts[0];
        this.value = idParts[1];
    }

    /**
     * Get Token ID (opaque).
     *
     * @return Token ID
     */
    public String getId() {
        return id;
    }

    /**
     *  Get Token value (opaque). Allows base64 encoded string.
     *
     * @return Token value
     */
    public String getValue() {
        return value;
    }

    /**
     * Get Token sequence number (version).
     *
     * Higher means newer version of the same token. Allows for better concurrency
     * and client cache ability. The client may choose to use only token's last version, since token versions are
     * inclusive. Not required for requests.
     *
     * @return Token sequence number
     */
    public long getSequenceNumber() {
        return sequenceNumber;
    }
}

