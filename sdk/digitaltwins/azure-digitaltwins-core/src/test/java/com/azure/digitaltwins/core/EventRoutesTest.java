package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.models.EventRoute;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.HttpURLConnection;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.digitaltwins.core.TestHelper.assertRestException;

/**
 * Tests for the sync client's event route APIs
 */
public class EventRoutesTest extends EventRoutesTestBase {

    private final ClientLogger logger = new ClientLogger(EventRoutesTest.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void eventRouteLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String eventRouteId = testResourceNamer.randomUuid();

        // CREATE
        EventRoute eventRouteToCreate = new EventRoute(EVENT_ROUTE_ENDPOINT_NAME);
        eventRouteToCreate.setFilter(FILTER);
        client.createEventRoute(eventRouteId, eventRouteToCreate);

        try {
            // GET
            EventRoute retrievedEventRoute = client.getEventRoute(eventRouteId);
            assertEventRoutesEqual(eventRouteToCreate, eventRouteId, retrievedEventRoute);

            // LIST
            PagedIterable<EventRoute> listedEventRoutes = client.listEventRoutes();
            for (EventRoute listedEventRoute : listedEventRoutes) {
                // There may be other event routes in place, so ignore them if they aren't the event route
                // that was just created. We only need to see that the newly created event route is present in the
                // list of all event routes.
                if (listedEventRoute.getId().equals(retrievedEventRoute)) {
                    assertEventRoutesEqual(retrievedEventRoute, eventRouteId, listedEventRoute);
                    break;
                }
            }
        } finally {
            // DELETE
            client.deleteEventRoute(eventRouteId);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void getEventRouteThrowsIfEventRouteDoesNotExist(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);
        String eventRouteId = testResourceNamer.randomUuid();

        assertRestException(() -> client.getEventRoute(eventRouteId), HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void createEventRouteThrowsIfFilterIsMalformed(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);
        String eventRouteId = testResourceNamer.randomUuid();
        EventRoute eventRouteToCreate = new EventRoute(EVENT_ROUTE_ENDPOINT_NAME);
        eventRouteToCreate.setFilter("this is not a valid filter");

        assertRestException(() -> client.createEventRoute(eventRouteId, eventRouteToCreate), HttpURLConnection.HTTP_BAD_REQUEST);
    }
}
