// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.models.DigitalTwinsEventRoute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This abstract test class defines all the tests that both the sync and async event route test classes need to implement. It also
 * houses some event route test specific helper functions.
 */
public abstract class EventRoutesTestBase extends DigitalTwinsTestBase {

    private final ClientLogger logger = new ClientLogger(EventRoutesTestBase.class);

    static final String EVENT_ROUTE_ENDPOINT_NAME = "someEventHubEndpoint";
    static final String FILTER = "$eventType = 'DigitalTwinTelemetryMessages' or $eventType = 'DigitalTwinLifecycleNotification'";

    @Test
    public abstract void eventRouteLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws InterruptedException;

    @Test
    public abstract void getEventRouteThrowsIfEventRouteDoesNotExist(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion);

    @Test
    public abstract void createEventRouteThrowsIfFilterIsMalformed(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion);

    @Test
    public abstract void listEventRoutesPaginationWorks(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion);

    // Azure Digital Twins instances have a low cap on the number of event routes allowed, so we need to delete the existing
    // event routes before each test to make sure that we can add an event route in each test.
    @BeforeEach
    public void removeAllEventRoutes() {
        // Using sync client for simplicity. This function isn't testing the clients, so no need to use both sync and async clients for cleanup
        DigitalTwinsClient client = getDigitalTwinsClientBuilder(null, DigitalTwinsServiceVersion.getLatest()).buildClient();
        PagedIterable<DigitalTwinsEventRoute> listedEventRoutes = client.listEventRoutes();
        List<String> currentEventRouteIds = new ArrayList<>();
        for (DigitalTwinsEventRoute listedEventRoute : listedEventRoutes) {
            currentEventRouteIds.add(listedEventRoute.getEventRouteId());
        }

        for (String eventRouteId : currentEventRouteIds) {
            logger.info("Deleting event route " + eventRouteId + " before running the next test");
            client.deleteEventRoute(eventRouteId);
        }
    }

    // Note that only service returned eventRoute instances have their Id field set. When a user builds an instance locally,
    // there is no way to assign an Id to it.
    protected static void assertEventRoutesEqual(DigitalTwinsEventRoute expected, String expectedId, DigitalTwinsEventRoute actual) {
        assertEquals(expectedId, actual.getEventRouteId());
        assertEquals(expected.getEndpointName(), actual.getEndpointName());
        assertEquals(expected.getFilter(), actual.getFilter());
    }
}
