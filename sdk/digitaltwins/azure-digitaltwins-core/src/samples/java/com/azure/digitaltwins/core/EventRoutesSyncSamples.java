// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.digitaltwins.core.helpers.ConsoleLogger;
import com.azure.digitaltwins.core.helpers.SamplesArguments;
import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;
import com.azure.digitaltwins.core.models.DigitalTwinsEventRoute;
import com.azure.identity.ClientSecretCredentialBuilder;

import java.util.UUID;

/**
 * This sample will list all the current event routes in your Azure Digital Twins instance,
 * then it will retrieve a particular event route. If the parameter for an existing event
 * route endpoint name is provided then it will also create a new event route, and it will
 * delete that newly created event route.
 */
public class EventRoutesSyncSamples {
    private static DigitalTwinsClient client;

    public static void main(String[] args) {
        SamplesArguments parsedArguments = new SamplesArguments(args);

        client = new DigitalTwinsClientBuilder()
            .credential(
                new ClientSecretCredentialBuilder()
                    .tenantId(parsedArguments.getTenantId())
                    .clientId(parsedArguments.getClientId())
                    .clientSecret(parsedArguments.getClientSecret())
                    .build()
            )
            .endpoint(parsedArguments.getDigitalTwinEndpoint())
            .httpLogOptions(
                new HttpLogOptions()
                    .setLogLevel(parsedArguments.getHttpLogDetailLevel()))
            .buildClient();

        ConsoleLogger.printHeader("List event routes");
        ConsoleLogger.print("Listing all current event routes in your Azure Digital Twins instance");
        PagedIterable<DigitalTwinsEventRoute> eventRoutes = null;
        try {
            eventRoutes = client.listEventRoutes();
        } catch (ErrorResponseException ex) {
            ConsoleLogger.printFatal("Failed to list event routes");
            ex.printStackTrace();
            System.exit(0);
        }

        String existingEventRouteId = null;
        for (DigitalTwinsEventRoute eventRoute : eventRoutes) {
            existingEventRouteId = eventRoute.getEventRouteId();
            ConsoleLogger.print(String.format("\tEventRouteId: %s", eventRoute.getEventRouteId()));
            ConsoleLogger.print(String.format("\tEventRouteEndpointName: %s", eventRoute.getEndpointName()));
            if (eventRoute.getFilter() != null) {
                ConsoleLogger.print(String.format("\tFilter: %s", eventRoute.getFilter()));
            }
            ConsoleLogger.print("");
        }

        if (existingEventRouteId != null) {
            ConsoleLogger.printHeader("Get event route");
            ConsoleLogger.print(String.format("Getting a single event route with Id %s", existingEventRouteId));
            try {
                DigitalTwinsEventRoute existingEventRoute = client.getEventRoute(existingEventRouteId);
                ConsoleLogger.print(String.format("Successfully retrieved event route with Id %s", existingEventRouteId));
                ConsoleLogger.print(String.format("\tEventRouteId: %s", existingEventRoute.getEventRouteId()));
                ConsoleLogger.print(String.format("\tEventRouteEndpointName: %s", existingEventRoute.getEndpointName()));
                if (existingEventRoute.getFilter() != null) {
                    ConsoleLogger.print(String.format("\tFilter: %s", existingEventRoute.getFilter()));
                }
            } catch (ErrorResponseException ex) {
                ConsoleLogger.printFatal(String.format("Failed to get event route with Id %s", existingEventRouteId));
                ex.printStackTrace();
                System.exit(0);
            }
        } else {
            ConsoleLogger.print("No event routes exist on your Azure Digital Twins instance yet.");
        }

        if (parsedArguments.getEventRouteEndpointName() != null) {
            ConsoleLogger.printHeader("Create an event route");
            ConsoleLogger.print("An event route endpoint name was provided as an input parameter, so this sample will create a new event route");

            String eventRouteId = "SomeEventRoute-" + UUID.randomUUID();
            String eventRouteEndpointName = parsedArguments.getEventRouteEndpointName();
            String filter = "$eventType = 'DigitalTwinTelemetryMessages' or $eventType = 'DigitalTwinLifecycleNotification'";
            DigitalTwinsEventRoute eventRoute = new DigitalTwinsEventRoute(eventRouteEndpointName);
            eventRoute.setFilter(filter);

            try {
                ConsoleLogger.print(String.format("Creating new event route with Id %s and endpoint name %s", eventRouteId, eventRouteEndpointName));
                client.createOrReplaceEventRoute(eventRouteId, eventRoute);
                ConsoleLogger.print(String.format("Successfully created event route with Id %s", eventRouteId));
            } catch (ErrorResponseException ex) {
                ConsoleLogger.printFatal(String.format("Failed to create new event route with Id %s", eventRouteId));
                ex.printStackTrace();
                System.exit(0);
            }

            try {
                ConsoleLogger.printHeader("Delete an event route");
                ConsoleLogger.print(String.format("Deleting the newly created event route with Id %s", eventRouteId));
                client.deleteEventRoute(eventRouteId);
                ConsoleLogger.print(String.format("Successfully created event route with Id %s", eventRouteId));
            } catch (ErrorResponseException ex) {
                ConsoleLogger.printFatal(String.format("Failed to delete event route with Id %s", eventRouteId));
                ex.printStackTrace();
                System.exit(0);
            }
        } else {
            ConsoleLogger.printWarning("No event route endpoint name was provided as an input parameter, so this sample will not create a new event route");
            ConsoleLogger.printWarning("In order to create a new endpoint for this sample to use, use the Azure portal or the control plane client library.");
        }
    }
}
