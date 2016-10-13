package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 *  Entry point to SQL Server management API.
 */
@Fluent
public interface SqlServers extends
        SupportsCreating<SqlServer.DefinitionStages.Blank>,
        SupportsListing<SqlServer>,
        SupportsListingByGroup<SqlServer>,
        SupportsGettingByGroup<SqlServer>,
        SupportsGettingById<SqlServer>,
        SupportsDeleting,
        SupportsDeletingByGroup,
        SupportsBatchCreation<SqlServer> {
}
