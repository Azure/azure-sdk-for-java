package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.models.EventRoute;
import com.azure.digitaltwins.core.models.ListEventRoutesOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.HttpURLConnection;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.digitaltwins.core.TestHelper.assertRestException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                if (listedEventRoute.getEventRouteId().equals(retrievedEventRoute)) {
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

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void listEventRoutesPaginationWorks(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);
        final int eventRouteCountToCreate = 5;
        final int expectedPageSize = 2;

        // create enough event routes so that the list API can have multiple pages
        for (int i = 0; i < eventRouteCountToCreate; i++) {
            String eventRouteId = testResourceNamer.randomUuid();
            EventRoute eventRouteToCreate = new EventRoute(EVENT_ROUTE_ENDPOINT_NAME);
            eventRouteToCreate.setFilter(FILTER);
            client.createEventRoute(eventRouteId, eventRouteToCreate);
        }

        // list event routes by page, make sure that all non-final pages have the expected page size
        ListEventRoutesOptions listEventRoutesOptions = (new ListEventRoutesOptions()).setMaxItemsPerPage(expectedPageSize);
        PagedIterable<EventRoute> eventRoutes = client.listEventRoutes(listEventRoutesOptions, Context.NONE);
        Iterable<PagedResponse<EventRoute>> eventRoutePages = eventRoutes.iterableByPage();
        int pageCount = 0;
        for (PagedResponse<EventRoute> eventRoutePagedResponse : eventRoutePages) {
            pageCount++;

            // Any page of results with a continuation token should be a non-final page, and should have the exact page size that we specified above
            if (eventRoutePagedResponse.getContinuationToken() != null) {
                assertEquals(expectedPageSize, eventRoutePagedResponse.getValue().size(), "Unexpected page size for a non-terminal page");
            }
        }

        assertTrue(pageCount >= 3, "At least three pages should have been returned.");
    }
}
