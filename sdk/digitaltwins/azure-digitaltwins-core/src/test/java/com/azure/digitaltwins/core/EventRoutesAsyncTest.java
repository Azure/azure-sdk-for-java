package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.models.EventRoute;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.digitaltwins.core.TestHelper.assertRestException;

/**
 * Tests for the async client's event route APIs
 */
public class EventRoutesAsyncTest extends EventRoutesTestBase {

    private final ClientLogger logger = new ClientLogger(EventRoutesAsyncTest.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void eventRouteLifecycleTest(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);
        String eventRouteId = testResourceNamer.randomUuid();

        // CREATE
        EventRoute eventRouteToCreate = new EventRoute();
        eventRouteToCreate.setEndpointName(EVENT_ROUTE_ENDPOINT_NAME);
        eventRouteToCreate.setFilter(FILTER);
        StepVerifier.create(asyncClient.createEventRoute(eventRouteId, eventRouteToCreate))
            .verifyComplete();

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
                        if (retrievedEventRoute.getId().equals(eventRouteToCreate.getId())) {
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
        EventRoute eventRouteToCreate = new EventRoute();
        eventRouteToCreate.setEndpointName(EVENT_ROUTE_ENDPOINT_NAME);
        eventRouteToCreate.setFilter("this is not a valid filter");

        StepVerifier.create(asyncClient.createEventRoute(eventRouteId, eventRouteToCreate))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_BAD_REQUEST));
    }
}
