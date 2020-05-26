// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.storage.common.implementation.StorageImplUtils;

import java.time.Duration;
import java.time.OffsetDateTime;

public class FileScheduleDeletionOptions {
    private final Duration timeToExpire;
    private final FileExpirationOffset expiryRelativeTo;
    private final OffsetDateTime expiresOn;

    public FileScheduleDeletionOptions() {
        this.timeToExpire = null;
        this.expiryRelativeTo = null;
        this.expiresOn = null;
    }

    public FileScheduleDeletionOptions(Duration timeToExpire, FileExpirationOffset expiryRelativeTo) {
        StorageImplUtils.assertNotNull("timeToExpire", timeToExpire);
        StorageImplUtils.assertNotNull("expiryRelativeTo", expiryRelativeTo);
        this.timeToExpire = timeToExpire;
        this.expiryRelativeTo = expiryRelativeTo;
        this.expiresOn = null;
    }

    public FileScheduleDeletionOptions(OffsetDateTime expiresOn) {
        StorageImplUtils.assertNotNull("expiresOn", expiresOn);
        this.expiresOn = expiresOn;
        this.timeToExpire = null;
        this.expiryRelativeTo = null;
    }

    public Duration getTimeToExpire() {
        return timeToExpire;
    }

    public FileExpirationOffset getExpiryRelativeTo() {
        return expiryRelativeTo;
    }

    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }
}
