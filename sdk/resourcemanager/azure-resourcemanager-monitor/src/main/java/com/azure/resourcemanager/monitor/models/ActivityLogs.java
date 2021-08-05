// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import java.time.OffsetDateTime;

/** Entry point for Monitor Activity logs API. */
public interface ActivityLogs extends HasManager<MonitorManager> {

    /**
     * Lists available event categories supported in the Activity Logs Service.
     *
     * @return list of available event categories supported in the Activity Logs Service.
     */
    PagedIterable<LocalizableString> listEventCategories();

    /**
     * Lists available event categories supported in the Activity Logs Service.
     *
     * @return list of available event categories supported in the Activity Logs Service.
     */
    PagedFlux<LocalizableString> listEventCategoriesAsync();

    /**
     * Begins a definition for a new Activity log query.
     *
     * @return the stage of start time filter definition.
     */
    ActivityLogsQueryDefinitionStages.WithEventDataStartTimeFilter defineQuery();

    /** The entirety of a Activity Logs query definition. */
    interface ActivityLogsQueryDefinition
        extends ActivityLogsQueryDefinitionStages.WithEventDataStartTimeFilter,
            ActivityLogsQueryDefinitionStages.WithEventDataEndFilter,
            ActivityLogsQueryDefinitionStages.WithEventDataFieldFilter,
            ActivityLogsQueryDefinitionStages.WithActivityLogsSelectFilter,
            ActivityLogsQueryDefinitionStages.WithActivityLogsQueryExecute {
    }

    /** Grouping of Activity log query stages. */
    interface ActivityLogsQueryDefinitionStages {

        /** The stage of a Activity Log query allowing to specify start time filter. */
        interface WithEventDataStartTimeFilter {
            /**
             * Sets the start time for Activity Log query filter.
             *
             * @param startTime specifies start time of cut off filter.
             * @return the stage of end time filter definition.
             */
            WithEventDataEndFilter startingFrom(OffsetDateTime startTime);
        }

        /** The stage of a Activity Log query allowing to specify end time filter. */
        interface WithEventDataEndFilter {
            /**
             * Sets the end time for Activity Log query filter.
             *
             * @param endTime specifies end time of cut off filter.
             * @return the stage of optional query parameter definition and query execution.
             */
            WithEventDataFieldFilter endsBefore(OffsetDateTime endTime);
        }

        /** The stage of a Activity Log query allowing to specify data fields in the server response. */
        interface WithEventDataFieldFilter {
            /**
             * Selects data fields that will be populated in the server response.
             *
             * @param responseProperties field names in the server response.
             * @return the stage of Activity log filtering by type and query execution.
             */
            WithActivityLogsSelectFilter withResponseProperties(EventDataPropertyName... responseProperties);

            /**
             * Sets the server response to include all the available properties.
             *
             * @return the stage of Activity log filtering by type and query execution.
             */
            WithActivityLogsSelectFilter withAllPropertiesInResponse();
        }

        /** The stage of the Activity log filtering by type and query execution. */
        interface WithActivityLogsSelectFilter extends WithActivityLogsQueryExecute {

            /**
             * Filters events for a given resource group.
             *
             * @param resourceGroupName Specifies resource group name.
             * @return the stage of Activity log filtering by type and query execution.
             */
            WithActivityLogsQueryExecute filterByResourceGroup(String resourceGroupName);

            /**
             * Filters events for a given resource.
             *
             * @param resourceId Specifies resource Id.
             * @return the stage of Activity log filtering by type and query execution.
             */
            WithActivityLogsQueryExecute filterByResource(String resourceId);

            /**
             * Filters events for a given resource provider.
             *
             * @param resourceProviderName Specifies resource provider.
             * @return the stage of Activity log filtering by type and query execution.
             */
            WithActivityLogsQueryExecute filterByResourceProvider(String resourceProviderName);

            /**
             * Filters events for a given correlation id.
             *
             * @param correlationId Specifies correlation id.
             * @return the stage of Activity log filtering by type and query execution.
             */
            WithActivityLogsQueryExecute filterByCorrelationId(String correlationId);
        }

        /** The stage of the Activity log query execution. */
        interface WithActivityLogsQueryExecute {
            /**
             * Executes the query.
             *
             * @return Activity Log events received after query execution.
             */
            PagedIterable<EventData> execute();

            /**
             * Executes the query.
             *
             * @return a representation of the deferred computation of Activity Log query call.
             */
            PagedFlux<EventData> executeAsync();

            /**
             * Filters events that were generated at the Tenant level.
             *
             * @return the stage of Activity log filtering by Tenant level and query execution.
             */
            WithActivityLogsQueryExecute filterAtTenantLevel();
        }
    }
}
