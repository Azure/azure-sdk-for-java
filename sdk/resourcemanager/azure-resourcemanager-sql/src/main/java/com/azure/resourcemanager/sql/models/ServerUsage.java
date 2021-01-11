// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.sql.fluent.models.ServerUsageInner;
import java.time.OffsetDateTime;

/** An immutable client-side representation of an Azure SQL server usage metric. */
@Fluent
public interface ServerUsage extends HasInnerModel<ServerUsageInner> {

    /** @return Name of the server usage metric */
    String name();

    /** @return the name of the resource */
    String resourceName();

    /** @return the metric display name */
    String displayName();

    /** @return the current value of the metric */
    double currentValue();

    /** @return the current limit of the metric */
    double limit();

    /** @return the units of the metric */
    String unit();

    /** @return the next reset time for the metric (ISO8601 format) */
    OffsetDateTime nextResetTime();
}
