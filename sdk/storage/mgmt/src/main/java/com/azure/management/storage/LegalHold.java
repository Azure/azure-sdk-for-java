/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.storage.implementation.StorageManager;
import com.azure.management.storage.models.LegalHoldInner;

import java.util.List;

/**
 * Type representing LegalHold.
 */
@Fluent
public interface LegalHold extends HasInner<LegalHoldInner>, HasManager<StorageManager> {
    /**
     * @return the hasLegalHold value.
     */
    Boolean hasLegalHold();

    /**
     * @return the tags value.
     */
    List<String> tags();

}