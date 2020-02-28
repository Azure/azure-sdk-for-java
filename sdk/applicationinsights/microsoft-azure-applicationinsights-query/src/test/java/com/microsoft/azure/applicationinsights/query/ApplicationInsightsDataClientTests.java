// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.applicationinsights.query;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.applicationinsights.query.implementation.ApplicationInsightsDataClientImpl;
import com.microsoft.azure.applicationinsights.query.models.EventType;
import com.microsoft.azure.applicationinsights.query.models.EventsResults;
import com.microsoft.azure.applicationinsights.query.models.MetricId;
import com.microsoft.azure.applicationinsights.query.models.MetricsPostBodySchema;
import com.microsoft.azure.applicationinsights.query.models.MetricsPostBodySchemaParameters;
import com.microsoft.azure.applicationinsights.query.models.MetricsResult;
import com.microsoft.azure.applicationinsights.query.models.MetricsResultsItem;
import com.microsoft.azure.applicationinsights.query.models.QueryBody;
import com.microsoft.azure.applicationinsights.query.models.QueryResults;
import com.microsoft.rest.RestClient;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ApplicationInsightsDataClientTests extends TestBase {
    protected static ApplicationInsightsDataClientImpl applicationInsightsClient;
    private static String appId = "578f0e27-12e9-4631-bc02-50b965da2633";

    @Override
    protected String baseUri() {
        return AzureEnvironment.AZURE.applicationInsightsEndpoint() + "v1/";
    }

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        applicationInsightsClient = new ApplicationInsightsDataClientImpl(restClient);
    }

    @Override
    protected void cleanUpResources() {
    }

    @Test
    public void canQuery() {
        String query = "availabilityResults | take 1";
        QueryResults queryResults = applicationInsightsClient.querys().execute(appId, new QueryBody().withQuery(query));
        Assert.assertNotNull(queryResults);

        // Query should return a single table with one row
        Assert.assertEquals(queryResults.tables().size(), 1);
        Assert.assertEquals(queryResults.tables().get(0).rows().size(), 1);

        // Check type behavior on results
        Assert.assertTrue(queryResults.tables().get(0).rows().get(0).get(1) instanceof String);
        Assert.assertNull(queryResults.tables().get(0).rows().get(0).get(6));
    }

    @Test
    public void canGetMetric() {
        MetricsResult metricResult = applicationInsightsClient.metrics().get(appId, MetricId.AVAILABILITY_RESULTSAVAILABILITY_PERCENTAGE);
        // Validate properties
        Assert.assertNotNull(metricResult.value().additionalProperties());
        Assert.assertNotNull(metricResult.value().start());
        Assert.assertTrue(metricResult.value().start() instanceof DateTime);
    }

    @Test
    public void canGetMultipleMetrics() {
        List<MetricsPostBodySchema> parameters = new ArrayList<MetricsPostBodySchema>();
        parameters.add(new MetricsPostBodySchema().withId("1").withParameters(new MetricsPostBodySchemaParameters().withMetricId(MetricId.AVAILABILITY_RESULTSAVAILABILITY_PERCENTAGE)));
        parameters.add(new MetricsPostBodySchema().withId("2").withParameters(new MetricsPostBodySchemaParameters().withMetricId(MetricId.AVAILABILITY_RESULTSDURATION)));

        List<MetricsResultsItem> metricResult = applicationInsightsClient.metrics().getMultiple(appId, parameters);
        // Check per-item metadata
        Assert.assertNotNull(metricResult.get(0).id());
        Assert.assertEquals(metricResult.get(0).status(), 200);

        // Validate properties
        Assert.assertNotNull(metricResult.get(0).body().value().start());
        Assert.assertTrue(metricResult.get(0).body().value().start() instanceof DateTime);
    }


    @Test
    public void canGetMetricsMetadata() {
        Object metadata = applicationInsightsClient.metrics().getMetadata(appId);
        // Sanity check
        Assert.assertNotNull(metadata);
    }

    @Test
    public void canGetEventsByType() {
        EventsResults eventsResult = applicationInsightsClient.events().getByType(appId, EventType.AVAILABILITY_RESULTS);
        Assert.assertNotNull(eventsResult.value().get(0).id());
    }

    @Test
    public void canGetEvent() {
        String eventId = "e313e0a0-9c1f-11e8-9f6d-3b25765db004";
        EventsResults eventsResult = applicationInsightsClient.events().get(appId, EventType.AVAILABILITY_RESULTS, eventId);
        Assert.assertNotNull(eventsResult.value().get(0).id());
    }
}
