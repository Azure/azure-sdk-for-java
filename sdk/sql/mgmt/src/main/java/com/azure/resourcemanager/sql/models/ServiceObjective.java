// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.sql.fluent.inner.ServiceObjectiveInner;

/** An immutable client-side representation of an Azure SQL Service Objective. */
@Fluent
public interface ServiceObjective
    extends HasInner<ServiceObjectiveInner>, Refreshable<ServiceObjective>, HasResourceGroup, HasName, HasId {
    /** @return name of the SQL Server to which this service objective belongs */
    String sqlServerName();

    /** @return the name for the service objective. */
    String serviceObjectiveName();

    /** @return whether the service level objective is the default service objective */
    boolean isDefault();

    /** @return whether the service level objective is a system service objective */
    boolean isSystem();

    /** @return the description for the service level objective */
    String description();

    /** @return whether the service level objective is enabled */
    boolean enabled();
}
