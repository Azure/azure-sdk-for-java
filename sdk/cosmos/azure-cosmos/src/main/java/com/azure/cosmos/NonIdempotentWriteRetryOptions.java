// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

/**
 * Class to define the options for non-idempotent write operations
 */
public final class NonIdempotentWriteRetryOptions {
    private boolean writeRetriesEnabled = false;
    private boolean useTrackingId = false;

    /**
     * Creates an instance of the NonIdempotentWriteRetryOptions class.
     */
    public NonIdempotentWriteRetryOptions() {

    }

    /**
     * Returns a flag indicating whether automatic retries for write operations even when they are not guaranteed to be
     * idempotent are enabled or not. The default value is false.
     * @return a flag indicating whether automatic retries for non-idempotent write operations are enabled or not.
     */
    public boolean isEnabled() {
        return this.writeRetriesEnabled;
    }

    /**
     * Specifies whether automatic retries for write operations even when they are not guaranteed
     * to be idempotent are enabled.
     * @param isEnabled - a flag indicating whether to enable automatic retries even for non-idempotent write operations
     * @return current options
     */
    public NonIdempotentWriteRetryOptions setEnabled(boolean isEnabled) {
        this.writeRetriesEnabled = isEnabled;

        return this;
    }

    /**
     * Returns a flag indicating whether write operations can use the
     * trackingId system property '/_trackingId' to allow identification of conflicts and pre-condition failures due
     * to retries. If enabled, each document being created or replaced will have an additional '/_trackingId' property
     * for which the value will be updated by the SDK. If it is not desired to add this new json property (for example
     * due to the RU-increase based on the payload size or because it causes documents to exceed the max payload size
     * upper limit), the usage of this system property can be disabled by setting this parameter to false. This means
     * there could be a higher level of 409/312 due to retries - and applications would need to handle them gracefully
     * on their own.
     * @return a flag indicating whether write operations can use the
     * trackingId system property '/_trackingId' to allow identification of conflicts and pre-condition failures due
     * to retries.
     */
    public boolean isTrackingIdUsed() {
        return this.useTrackingId;
    }

    /**
     * Specifies whether write operations can use the
     * trackingId system property '/_trackingId' to allow identification of conflicts and pre-condition failures due
     * to retries. If enabled, each document being created or replaced will have an additional '/_trackingId' property
     * for which the value will be updated by the SDK. If it is not desired to add this new json property (for example
     * due to the RU-increase based on the payload size or because it causes documents to exceed the max payload size
     * upper limit), the usage of this system property can be disabled by setting this parameter to false. This means
     * there could be a higher level of 409/312 due to retries - and applications would need to handle them gracefully
     * on their own.
     * @param useTrackingIdForCreateAndReplace indicates whether a system property '/_trackingId' should be used to
     * allow identification of conflicts and pre-condition failures due to retries.
     * @return current options
     */
    public NonIdempotentWriteRetryOptions setTrackingIdUsed(boolean useTrackingIdForCreateAndReplace) {
        this.useTrackingId = useTrackingIdForCreateAndReplace;

        return this;
    }
}
