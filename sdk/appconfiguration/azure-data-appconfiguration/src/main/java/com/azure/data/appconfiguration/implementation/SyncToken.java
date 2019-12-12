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
    private static final ClientLogger logger = new ClientLogger(SyncToken.class);

    private final String id;
    private final String value;
    private final long sequenceNumber;

    /**
     * Create an instance of SyncToken class.
     *
     * @param id Token ID (opaque)
     * @param value Token value (opaque). Allows base64 encoded string
     * @param sequenceNumber Token sequence number (version). Higher means newer version of the same token.
     * Allows for better concurrency and client cache ability. The client may choose to use only token's last version,
     * since token versions are inclusive. Not required for requests.
     */
    private SyncToken(String id, String value, long sequenceNumber) {
        this.id = id;
        this.value = value;
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * Create an {@code SyncToken} by parsing a given sync-token string with only one sync-token.
     *
     * @param syncToken only one raw sync-token string from HTTP response header, ex.,
     * <p>Sync-Token: <id>=<value>;sn=<sn></p>. But not
     * <p>Sync-Token: <id>=<value>;sn=<sn>,<id>=<value>;sn=<sn></p>
     * @return {@code SyncToken} instance
     */
    static SyncToken parseSyncToken(String syncToken) {
        if (CoreUtils.isNullOrEmpty(syncToken)) {
            return null;
        }
        try {
            final String[] syncTokenParts = syncToken.split(";", 2);
            // Not a fully formatted sync-token
            if (syncTokenParts.length != 2) {
                logger.logExceptionAsWarning(
                    new RuntimeException("Failed to parse sync token, it cannot split to two parts by delimiter ';'."));
                return null;
            }

            final String[] idParts = syncTokenParts[0].split("=", 2);
            // Identifier is missing a section.
            if (idParts.length != 2) {
                logger.logExceptionAsWarning(
                    new RuntimeException("Failed to parse sync token, it cannot split 'id=value' into two parts."));
                return null;
            }

            final String[] snParts = syncTokenParts[1].split("=", 2);
            if (snParts.length != 2) {
                logger.logExceptionAsWarning(
                    new RuntimeException("Failed to parse sync token, it cannot split 'sn=value' into two parts."));
                return null;
            }

            final long sequenceNumber;
            try {
                sequenceNumber = Long.parseLong(snParts[1]);
            } catch (NumberFormatException ex) {
                logger.logExceptionAsWarning(
                    new RuntimeException("Cannot parse sequence number for invalid number format.", ex));
                return null;
            }

            return new SyncToken(idParts[0], idParts[1], sequenceNumber);
        } catch (IllegalArgumentException ex) {
            logger.logExceptionAsWarning(new RuntimeException("Cannot parse sync token for invalid format.", ex));
        }
        return null;
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

