package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.models.DigitalTwinsEventRoute;
import com.azure.digitaltwins.core.models.ListDigitalTwinsEventRoutesOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.digitaltwins.core.TestHelper.assertRestException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the async client's event route APIs
 */
public class EventRoutesAsyncTest extends EventRoutesTestBase {

    private final ClientLogger logger = new ClientLogger(EventRoutesAsyncTest.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void eventRouteLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws InterruptedException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);
        String eventRouteId = testResourceNamer.randomUuid();

        // CREATE
        DigitalTwinsEventRoute eventRouteToCreate = new DigitalTwinsEventRoute(EVENT_ROUTE_ENDPOINT_NAME);
        eventRouteToCreate.setFilter(FILTER);
        StepVerifier.create(asyncClient.createOrReplaceEventRoute(eventRouteId, eventRouteToCreate))
            .verifyComplete();

        waitIfLive();

        try {
            // GET
            StepVerifier.create(asyncClient.getEventRoute(eventRouteId))
                .assertNext((retrievedEventRoute) -> assertEventRoutesEqual(eventRouteToCreate, eventRouteId, retrievedEventRoute))
                .verifyComplete();

            // LIST
            StepVerifier.create(asyncClient.listEventRoutes())
                .thenConsumeWhile(
                    (eventRoute) -> eventRoute != null,
                    (retrievedEventRoute) -> {
                        // There may be other event routes in place, so ignore them if they aren't the event route
                        // that was just created. We only need to see that the newly created event route is present in the
                        // list of all event routes.
                        if (retrievedEventRoute.getEventRouteId().equals(eventRouteToCreate.getEventRouteId())) {
                            assertEventRoutesEqual(eventRouteToCreate, eventRouteId, retrievedEventRoute);
                        }
                    })
                .verifyComplete();
        } finally {
            // DELETE
            StepVerifier.create(asyncClient.deleteEventRoute(eventRouteId))
                .verifyComplete();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void getEventRouteThrowsIfEventRouteDoesNotExist(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);
        String eventRouteId = testResourceNamer.randomUuid();

        StepVerifier.create(asyncClient.getEventRoute(eventRouteId))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_NOT_FOUND));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void createEventRouteThrowsIfFilterIsMalformed(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);
        String eventRouteId = testResourceNamer.randomUuid();
        DigitalTwinsEventRoute eventRouteToCreate = new DigitalTwinsEventRoute(EVENT_ROUTE_ENDPOINT_NAME);
        eventRouteToCreate.setFilter("this is not a valid filter");

        StepVerifier.create(asyncClient.createOrReplaceEventRoute(eventRouteId, eventRouteToCreate))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_BAD_REQUEST));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void listEventRoutesPaginationWorks(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);
        final int eventRouteCountToCreate = 5;
        final int expectedPageSize = 2;

        // create enough event routes so that the list API can have multiple pages
        for (int i = 0; i < eventRouteCountToCreate; i++) {
            String eventRouteId = testResourceNamer.randomUuid();
            DigitalTwinsEventRoute eventRouteToCreate = new DigitalTwinsEventRoute(EVENT_ROUTE_ENDPOINT_NAME);
            eventRouteToCreate.setFilter(FILTER);
            StepVerifier.create(asyncClient.createOrReplaceEventRoute(eventRouteId, eventRouteToCreate))
                .verifyComplete();
        }

        // list event routes by page, make sure that all non-final pages have the expected page size
        AtomicInteger pageCount = new AtomicInteger(0);
        ListDigitalTwinsEventRoutesOptions listEventRoutesOptions = (new ListDigitalTwinsEventRoutesOptions()).setMaxItemsPerPage(expectedPageSize);
        StepVerifier.create(asyncClient.listEventRoutes(listEventRoutesOptions).byPage())
            .thenConsumeWhile(
                (pagedResponseOfEventRoute) -> pagedResponseOfEventRoute != null,
                (pagedResponseOfEventRoute) -> {
                    pageCount.incrementAndGet();

                    // Any page of results with a continuation token should be a non-final page, and should have the exact page size that we specified above
                    if (pagedResponseOfEventRoute.getContinuationToken() != null) {
                        assertEquals(expectedPageSize, pagedResponseOfEventRoute.getValue().size(), "Unexpected page size for a non-terminal page");
                    }
                })
            .verifyComplete();

        assertTrue(pageCount.get() >= 3, "At least three pages should have been returned.");
    }
}
