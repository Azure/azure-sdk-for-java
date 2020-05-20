// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.storage.common.implementation.StorageImplUtils;

import java.time.Duration;
import java.time.OffsetDateTime;

public class BlobScheduleDeletionOptions {
    private final Duration timeToExpire;
    private final BlobExpirationOffset expiryRelativeTo;
    private final OffsetDateTime expiresOn;

    public BlobScheduleDeletionOptions() {
        this.timeToExpire = null;
        this.expiryRelativeTo = null;
        this.expiresOn = null;
    }

    public BlobScheduleDeletionOptions(Duration timeToExpire, BlobExpirationOffset expiryRelativeTo) {
        StorageImplUtils.assertNotNull("timeToExpire", timeToExpire);
        StorageImplUtils.assertNotNull("expiryRelativeTo", expiryRelativeTo);
        this.timeToExpire = timeToExpire;
        this.expiryRelativeTo = expiryRelativeTo;
        this.expiresOn = null;
    }

    public BlobScheduleDeletionOptions(OffsetDateTime expiresOn) {
        StorageImplUtils.assertNotNull("expiresOn", expiresOn);
        this.expiresOn = expiresOn;
        this.timeToExpire = null;
        this.expiryRelativeTo = null;
    }

    public Duration getTimeToExpire() {
        return timeToExpire;
    }

    public BlobExpirationOffset getExpiryRelativeTo() {
        return expiryRelativeTo;
    }

    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }
}
