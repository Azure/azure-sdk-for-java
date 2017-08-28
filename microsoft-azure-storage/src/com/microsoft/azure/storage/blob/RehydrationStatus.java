package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.core.Utility;

import java.util.Locale;

/**
 * The rehydration status for the blob that is currently archived.
 * Only applicable for block blobs on standard storage accounts for this version.
 */
public enum RehydrationStatus {
    /**
     * The rehydration status is not recognized by this version of the library.
     */
    UNKNOWN,

    /**
     * The blob is being rehydrated to hot storage.
    */
    PENDING_TO_HOT,

    /**
     * The blob is being rehydrated to cool storage.
    **/
    PENDING_TO_COOL;

    /**
     * Parses a rehydration status from the given string.
     *
     * @param rehydrationStatusString
     *        A <code>String</code> which represents the rehydration status to string.
     *
     * @return A <code>RehydrationStatus</code> value that represents the rehydration status of the blob.
     */
    protected static RehydrationStatus parse(final String rehydrationStatusString) {
        if (Utility.isNullOrEmpty(rehydrationStatusString)) {
            return UNKNOWN;
        }
        else if ("rehydrate-pending-to-hot".equals(rehydrationStatusString.toLowerCase(Locale.US))) {
            return PENDING_TO_HOT;
        }
        else if ("rehydrate-pending-to-cool".equals(rehydrationStatusString.toLowerCase(Locale.US))) {
            return PENDING_TO_COOL;
        }
        else {
            return UNKNOWN;
        }
    }
}