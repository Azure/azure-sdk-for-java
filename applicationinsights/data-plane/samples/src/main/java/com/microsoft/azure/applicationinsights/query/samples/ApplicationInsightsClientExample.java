/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 */
package com.microsoft.azure.applicationinsights.query.samples;

import java.util.ArrayList;
import java.util.List;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.applicationinsights.query.implementation.ApplicationInsightsDataClientImpl;
import com.microsoft.azure.applicationinsights.query.models.QueryBody;
import com.microsoft.azure.applicationinsights.query.models.MetricId;
import com.microsoft.azure.applicationinsights.query.models.MetricsResult;
import com.microsoft.azure.applicationinsights.query.models.MetricsResultsItem;
import com.microsoft.azure.applicationinsights.query.models.MetricsPostBodySchema;
import com.microsoft.azure.applicationinsights.query.models.MetricsPostBodySchemaParameters;
import com.microsoft.azure.applicationinsights.query.models.EventType;
import com.microsoft.azure.applicationinsights.query.models.EventsResult;
import com.microsoft.azure.applicationinsights.query.models.EventsResults;
import com.microsoft.azure.applicationinsights.query.models.QueryResults;
import com.microsoft.azure.applicationinsights.query.models.Column;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;

/**
 * Basic query example
 *
 */
public class ApplicationInsightsClientExample 
{
    public static void main( String[] args )
    {
        // ApplicationTokenCredentials work well for service principal authentication
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
            "<clientId>",
            "<tenantId>",
            "<clientSecret>",
            AzureEnvironment.AZURE
        );
        
        // New up client. Accepts credentials, or a pre-authenticated restClient
        ApplicationInsightsDataClientImpl client = new ApplicationInsightsDataClientImpl(credentials);
        
        // Prepare information for query
        String query = "availabilityResults | take 1";
        String appId = "<appInsightsAppGUID>";
        String eventId = "<eventGuid>";

        // POST parameters for multiple metrics
        List<MetricsPostBodySchema> parameters = new ArrayList<MetricsPostBodySchema>();
        parameters.add(new MetricsPostBodySchema().withId("1").withParameters(new MetricsPostBodySchemaParameters().withMetricId(MetricId.AVAILABILITY_RESULTSAVAILABILITY_PERCENTAGE)));
        parameters.add(new MetricsPostBodySchema().withId("2").withParameters(new MetricsPostBodySchemaParameters().withMetricId(MetricId.AVAILABILITY_RESULTSDURATION)));

        // Execute log query
        QueryResults queryResults = client.querys().execute(appId, new QueryBody().withQuery(query));
        
        // Metrics
        MetricsResult metricResultSingle = client.metrics().get(appId, MetricId.AVAILABILITY_RESULTSAVAILABILITY_PERCENTAGE);
        List<MetricsResultsItem> metricResultMultiple = client.metrics().getMultiple(appId, parameters);
        Object metadata = client.metrics().getMetadata(appId);

        // Events
        EventsResults eventsResultByType = client.events().getByType(appId, EventType.AVAILABILITY_RESULTS);
        EventsResults eventsResult = client.events().get(appId, EventType.AVAILABILITY_RESULTS, eventId);

        // Process and print results
        List<Object> row = queryResults.tables().get(0).rows().get(0);
        List<Column> columnNames = queryResults
            .tables()
            .get(0)
            .columns();

        for (int i = 0; i < row.size(); i++){        
            System.out.println("The value of " + columnNames.get(i).name() + " is " + row.get(i));
        }
        
        System.out.println(metricResultMultiple.get(0).body().value().additionalProperties());
        
    }
}