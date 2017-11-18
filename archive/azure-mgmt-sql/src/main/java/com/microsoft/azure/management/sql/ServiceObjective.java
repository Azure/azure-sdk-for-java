/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasId;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.sql.implementation.ServiceObjectiveInner;


/**
 * An immutable client-side representation of an Azure SQL Service Objective.
 */
@Fluent
public interface ServiceObjective extends
        HasInner<ServiceObjectiveInner>,
        Refreshable<ServiceObjective>,
        HasResourceGroup,
        HasName,
        HasId {
    /**
     * @return name of the SQL Server to which this replication belongs
     */
    String sqlServerName();

    /**
     * @return the name for the service objective.
     */
    String serviceObjectiveName();

    /**
     * @return whether the service level objective is the default service
     * objective.
     */
    boolean isDefault();

    /**
     * @return whether the service level objective is a system service objective.
     */
    boolean isSystem();

    /**
     * @return the description for the service level objective.
     */
    String description();

    /**
     * @return whether the service level objective is enabled.
     */
    boolean enabled();
}

