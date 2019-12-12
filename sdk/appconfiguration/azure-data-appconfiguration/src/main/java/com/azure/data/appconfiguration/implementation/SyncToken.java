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
    private final String id;
    private final String value;
    private final long sequenceNumber;
    private final String syncTokenString;

    /**
     * Create an instance of SyncToken class.
     *
     * @param id Token ID (opaque)
     * @param value Token value (opaque). Allows base64 encoded string
     * @param sequenceNumber Token sequence number (version). Higher means newer version of the same token.
     * Allows for better concurrency and client cache ability. The client may choose to use only token's last version,
     * since token versions are inclusive. Not required for requests.
     */
    SyncToken(String id, String value, long sequenceNumber, String syncTokenString) {
        this.id = id;
        this.value = value;
        this.sequenceNumber = sequenceNumber;
        this.syncTokenString = syncTokenString;
    }

    /**
     * Create one instance of {@code SyncToken} from given sync-token string with only one sync-token.
     *
     * @param syncToken only one raw sync-token string from HTTP response header, ex.,
     * <p>Sync-Token: <id>=<value>;sn=<sn></p>. But not
     * <p>Sync-Token: <id>=<value>;sn=<sn>,<id>=<value>;sn=<sn></p>
     * @return {@code SyncToken} instance
     */
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

    /**
     * Get sync-token header in response.
     *
     * Response Sync-Token Header Syntax:
     * <p>Sync-Token: <id>=<value>;sn=<sn></p>
     *
     * @return sync-token response header
     */
    public String getSyncTokenString() {
        return syncTokenString;
    }

    /**
     * Get sync-token header in request.
     *
     * Response Sync-Token Header Syntax:
     * <p></>Sync-Token: <id>=<value></p>
     *
     * @return sync-token request header
     */
    public String getSyncTokenStringInRequest() {
        return id + "=" + value;
    }
}

