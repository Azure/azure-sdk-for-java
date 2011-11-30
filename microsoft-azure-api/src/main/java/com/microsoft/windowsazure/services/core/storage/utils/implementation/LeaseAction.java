package com.microsoft.windowsazure.services.core.storage.utils.implementation;

import com.microsoft.windowsazure.services.core.storage.Constants;

/**
 * RESERVED FOR INTERNAL USE. Describes actions that can be performed on a lease.
 */
public enum LeaseAction {

    /**
     * Acquire the lease.
     */
    ACQUIRE,

    /**
     * Renew the lease.
     */
    RENEW,

    /**
     * Release the lease.
     */
    RELEASE,

    /**
     * Break the lease.
     */
    BREAK;

    @Override
    public String toString() {
        switch (this) {
            case ACQUIRE:
                return "Acquire";
            case RENEW:
                return "Renew";
            case RELEASE:
                return "Release";
            case BREAK:
                return "Break";
            default:
                // Wont Happen, all possible values covered above.
                return Constants.EMPTY_STRING;
        }
    }
}
