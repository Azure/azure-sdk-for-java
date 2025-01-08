// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.models.LegalHoldInner;
import java.util.List;

/** Type representing LegalHold. */
@Fluent
public interface LegalHold extends HasInnerModel<LegalHoldInner>, HasManager<StorageManager> {
    /**
     * Checks whether legal hold id true.
     *
     * @return the hasLegalHold value.
     */
    Boolean hasLegalHold();

    /**
     * Gets the tags.
     *
     * @return the tags value.
     */
    List<String> tags();
}
