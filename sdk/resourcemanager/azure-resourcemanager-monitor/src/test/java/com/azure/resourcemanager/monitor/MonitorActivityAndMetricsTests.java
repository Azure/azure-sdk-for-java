// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.monitor.models.EventData;
import com.azure.resourcemanager.monitor.models.EventDataPropertyName;
import com.azure.resourcemanager.monitor.models.LocalizableString;
import com.azure.resourcemanager.monitor.models.MetricCollection;
import com.azure.resourcemanager.monitor.models.MetricDefinition;
import com.azure.resourcemanager.monitor.models.ResultType;
import com.azure.resourcemanager.resources.core.TestUtilities;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MonitorActivityAndMetricsTests extends MonitorManagementTest {
    @Test
    public void canListEventsAndMetrics() throws Exception {
        OffsetDateTime recordDateTime = sdkContext.dateTimeNow().minusDays(40);
        VirtualMachine vm = computeManager.virtualMachines().list().iterator().next();

        // Metric Definition
        PagedIterable<MetricDefinition> mt = monitorManager.metricDefinitions().listByResource(vm.id());

        Assertions.assertNotNull(mt);
        MetricDefinition mDef = mt.iterator().next();
        Assertions.assertNotNull(mDef.metricAvailabilities());
        Assertions.assertNotNull(mDef.namespace());
        Assertions.assertNotNull(mDef.supportedAggregationTypes());

        // Metric
        MetricCollection metrics =
            mDef
                .defineQuery()
                .startingFrom(recordDateTime.minusDays(30))
                .endsBefore(recordDateTime)
                .withResultType(ResultType.DATA)
                .execute();

        Assertions.assertNotNull(metrics);
        Assertions.assertNotNull(metrics.namespace());
        Assertions.assertNotNull(metrics.resourceRegion());
        Assertions.assertEquals("Microsoft.Compute/virtualMachines", metrics.namespace());

        // Activity Logs
        PagedIterable<EventData> retVal =
            monitorManager
                .activityLogs()
                .defineQuery()
                .startingFrom(recordDateTime.minusDays(30))
                .endsBefore(recordDateTime)
                .withResponseProperties(
                    EventDataPropertyName.RESOURCEID,
                    EventDataPropertyName.EVENTTIMESTAMP,
                    EventDataPropertyName.OPERATIONNAME,
                    EventDataPropertyName.EVENTNAME)
                .filterByResource(vm.id())
                .execute();

        Assertions.assertNotNull(retVal);
        for (EventData event : retVal) {
            Assertions.assertTrue(event.resourceId().toLowerCase().startsWith(vm.id().toLowerCase()));
            Assertions.assertNotNull(event.eventName().localizedValue());
            Assertions.assertNotNull(event.operationName().localizedValue());
            Assertions.assertNotNull(event.eventTimestamp());

            Assertions.assertNull(event.category());
            Assertions.assertNull(event.authorization());
            Assertions.assertNull(event.caller());
            Assertions.assertNull(event.correlationId());
            Assertions.assertNull(event.description());
            Assertions.assertNull(event.eventDataId());
            Assertions.assertNull(event.httpRequest());
            Assertions.assertNull(event.level());
        }

        // List Event Categories
        PagedIterable<LocalizableString> eventCategories = monitorManager.activityLogs().listEventCategories();
        Assertions.assertNotNull(eventCategories);
        Assertions.assertTrue(TestUtilities.getSize(eventCategories) > 0);

        // List Activity logs at tenant level is not allowed for the current tenant
        try {
            monitorManager
                .activityLogs()
                .defineQuery()
                .startingFrom(recordDateTime.minusDays(30))
                .endsBefore(recordDateTime)
                .withResponseProperties(
                    EventDataPropertyName.RESOURCEID,
                    EventDataPropertyName.EVENTTIMESTAMP,
                    EventDataPropertyName.OPERATIONNAME,
                    EventDataPropertyName.EVENTNAME)
                .filterByResource(vm.id())
                .filterAtTenantLevel()
                .execute();
        } catch (ManagementException er) {
            // should throw "The client '...' with object id '...' does not have authorization to perform action
            // 'microsoft.insights/eventtypes/values/read' over scope
            // '/providers/microsoft.insights/eventtypes/management'.
            Assertions.assertEquals(403, er.getResponse().getStatusCode());
        }
    }
}
