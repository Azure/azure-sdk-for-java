// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.keyvault.fluent.models.DeletedVaultInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.time.OffsetDateTime;
import java.util.Map;

/** An immutable client-side representation of an Azure Key Vault. */
@Fluent
public interface DeletedVault extends HasInnerModel<DeletedVaultInner>, HasName, HasId {

    /**
     * Get the location value.
     *
     * @return the location value
     */
    String location();

    /**
     * Get the deletionDate value.
     *
     * @return the deletionDate value
     */
    OffsetDateTime deletionDate();

    /**
     * Get the scheduledPurgeDate value.
     *
     * @return the scheduledPurgeDate value
     */
    OffsetDateTime scheduledPurgeDate();

    /**
     * Get the tags value.
     *
     * @return the tags value
     */
    Map<String, String> tags();
}
