/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroupAsync;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListingAsync;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.sql.implementation.ServersInner;
import com.microsoft.azure.management.sql.implementation.SqlServerManager;

/**
 *  Entry point to SQL Server management API.
 */
@Fluent
public interface SqlServers extends
        SupportsCreating<SqlServer.DefinitionStages.Blank>,
        SupportsListingAsync<SqlServer>,
        SupportsListingByGroupAsync<SqlServer>,
        SupportsGettingByGroup<SqlServer>,
        SupportsGettingById<SqlServer>,
        SupportsDeletingById,
        SupportsDeletingByGroup,
        SupportsBatchCreation<SqlServer>,
        SupportsBatchDeletion,
        HasManager<SqlServerManager>,
        HasInner<ServersInner> {
}
