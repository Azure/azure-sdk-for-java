// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosContinuablePagedFlux;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CosmosDB {
    private static final String DB_NAME = "JavaSolarSystem-" + UUID.randomUUID();
    private static final String COLLECTION_NAME = "Planets";

    private static final String AZURE_COSMOS_ENDPOINT = System.getenv("AZURE_COSMOS_ENDPOINT");
    private static final String AZURE_COSMOS_KEY= System.getenv("AZURE_COSMOS_KEY");

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosDB.class);

    private static Mono<Void> createDatabase(CosmosAsyncClient client) {
        LOGGER.info("Creating database '{}'... ", DB_NAME);
        return client.createDatabaseIfNotExists(DB_NAME).then();
    }

    private static Mono<CosmosAsyncContainer> createCollection(CosmosAsyncClient client) {
        LOGGER.info("Creating collection '{}'... ", COLLECTION_NAME);
        return client
            .getDatabase(DB_NAME)
            .createContainer(COLLECTION_NAME, "/id")
            .map(response -> response.getContainer());
    }

    private static Mono<Void> createDocuments(CosmosAsyncContainer container) {
        LOGGER.info("Inserting Items... ");
        List<Planet> planets = Arrays.asList(
            new Planet(
                "Earth",
                false,
                3959,
                new Moon[]{
                    new Moon("Moon")
                }),
            new Planet(
                "Mars",
                false,
                2106,
                new Moon[]{
                    new Moon("Phobos"),
                    new Moon("Deimos")
                })
        );

        return Flux.fromIterable(planets)
            .flatMap(planet -> container.createItem(planet))
            .then();
    }

    private static Mono<Void> simpleQuery(CosmosAsyncContainer container) {
        LOGGER.info("Querying collection...");
        CosmosContinuablePagedFlux<Planet> queryResults = container
            .queryItems("SELECT c.id FROM c", Planet.class);

        return queryResults.byPage()
            .doOnEach(pageSignal -> {
                LOGGER.info("\t{}", pageSignal.get().getResults().toString());
            }).then();
    }

    private static Mono<Void> deleteDatabase(CosmosAsyncClient client) {
        LOGGER.info("Cleaning up the resource...");
        return client.getDatabase(DB_NAME).delete().then();
    }

    public static void main(String[] args) {
        LOGGER.info("---------------------");
        LOGGER.info("COSMOS DB");
        LOGGER.info("---------------------");

        CosmosAsyncClient client = CosmosAsyncClient
            .cosmosClientBuilder()
            .setEndpoint(AZURE_COSMOS_ENDPOINT)
            .setKey(AZURE_COSMOS_KEY)
            .buildAsyncClient();

        try {
            //if the database already exists, it is going to be deleted with all its content.
            deleteDatabase(client).block();
        } catch (Exception e) {
            //This means that the database does not exists already, it's fine
            LOGGER.info("\tDatabase does not presently exist");
        }

        try {
            createDatabase(client)
                .then(createCollection(client))
                .flatMap(collection -> createDocuments(collection)
                    .then(simpleQuery(collection))
                ).block();
        } catch (Exception e) {
            LOGGER.error("ERROR");
        } finally {
           deleteDatabase(client).block();
           LOGGER.info("Closing client...");
           client.close();
           LOGGER.info("Cosmos client closed");
        }

        return;
    }
}

// Classes for this sample
class Planet {
    public String id;
    public boolean hasRings;
    public int radius;
    public Moon[] moons;

    public Planet(String id, boolean hasRings, int radius, Moon[] moons) {
        this.id = id;
        this.hasRings = hasRings;
        this.radius = radius;
        this.moons = moons;
    }
}

class Moon {
    public String name;

    public Moon(String name) {
        this.name = name;
    }

}
