// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import java.time.OffsetDateTime;

/** An immutable client-side representation of an Azure AD credential. */
@Fluent
public interface Credential extends Indexable, HasId, HasName {
    /** @return start date. */
    OffsetDateTime startDate();

    /** @return end date. */
    OffsetDateTime endDate();

    /** @return key value. */
    String value();
}
