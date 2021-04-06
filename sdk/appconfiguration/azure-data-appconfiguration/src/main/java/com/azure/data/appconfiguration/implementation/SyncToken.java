// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

/**
 * Due to the nature of some distributed systems real-time consistency between requests can't (or it's very hard) to be
 * enforced implicitly. A solution is to allow protocol support in the form of multiple Synchronization Tokens.
 * Synchronization tokens are optional.
 * <p>
 * Uses optional Sync-Token request/response headers will guarantee real-time consistency between different client
 * instances and requests.
 *
 * @see <a href="https://github.com/Azure/AppConfiguration/blob/master/docs/REST/consistency.md">Real-time Consistency</a>
 */
public final class SyncToken {
    private static final String CANNOT_EMPTY_OR_NULL = "sync-token cannot be null or empty.";
    private static final String EQUAL = "=";
    private static final String SEMICOLON = ";";
    private static final String SEQUENCE_NUMBER_CANNOT_PARSED = "Sequence number cannot be parsed to long.";
    private static final String VALID_FORMAT_ERROR_MESSAGE =
        "Expected sync-token valid format should be <id>=<value>;sn=<sn>. For multiple sync tokens, "
            + "<id>=<value>;sn=<sn>,<id>=<value>;sn=<sn>.";

    private static final ClientLogger LOGGER = new ClientLogger(SyncToken.class);

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
    public static SyncToken createSyncToken(String syncToken) {
        final SyncToken token = new SyncToken();
        if (CoreUtils.isNullOrEmpty(syncToken)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(CANNOT_EMPTY_OR_NULL));
        }

        final String[] syncTokenParts = syncToken.split(SEMICOLON, 2);
        if (syncTokenParts.length != 2) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(VALID_FORMAT_ERROR_MESSAGE));
        }

        final String[] idParts = syncTokenParts[0].split(EQUAL, 2);
        final String[] snParts = syncTokenParts[1].split(EQUAL, 2);
        if (idParts.length != 2 || snParts.length != 2
                || idParts[0].isEmpty() || idParts[1].isEmpty()
                || snParts[0].isEmpty() || snParts[1].isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(VALID_FORMAT_ERROR_MESSAGE));
        }

        try {
            token.sequenceNumber = Long.parseLong(snParts[1]);
        } catch (NumberFormatException ex) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(SEQUENCE_NUMBER_CANNOT_PARSED));
        }
        token.id = idParts[0];
        token.value = idParts[1];
        return token;
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
     * Get Token value (opaque). Allows base64 encoded string.
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
