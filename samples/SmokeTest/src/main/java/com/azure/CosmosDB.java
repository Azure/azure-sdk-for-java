package com.azure;

import com.azure.data.cosmos.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CosmosDB {
    private static final String dbName = "JavaSolarSystem-" + UUID.randomUUID();
    private static final String collectionName = "Planets";

    private static Mono<Void> createDatabase(CosmosClient client) {
        System.out.println("Creating database... ");
        return client.createDatabaseIfNotExists(dbName).then();
    }

    private static Mono<CosmosContainer> createCollection(CosmosClient client) {
        System.out.println("Creating collection... ");
        return client.getDatabase(dbName).createContainer(collectionName, "/id")
            .map(response -> response.container());
    }

    private static Mono<Void> createDocuments(CosmosContainer container) {
        System.out.println("Inserting Items... ");
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

        return Flux.fromIterable(planets).flatMap(planet -> container.createItem(planet)).then();
    }

    private static Mono<Void> simpleQuery(CosmosContainer container) {
        System.out.println("Querying collection...");
        FeedOptions options = new FeedOptions().enableCrossPartitionQuery(true);
        Flux<FeedResponse<CosmosItemProperties>> queryResults = container.queryItems("SELECT c.id FROM c", options);

        return queryResults.map(cosmosItemPropertiesFeedResponse -> {
            System.out.println('\t' + cosmosItemPropertiesFeedResponse.results().toString());
            return cosmosItemPropertiesFeedResponse;
        }).then();
    }

    private static Mono<Void> deleteDatabase(CosmosClient client) {
        System.out.print("Cleaning up the resource...");
        return client.getDatabase(dbName).delete().then();
    }

    public static void main(String[] args) {
        System.out.println("\n---------------------");
        System.out.println("COSMOS DB");
        System.out.println("---------------------\n");

        CosmosClient client = CosmosClient
            .builder().endpoint(System.getenv("COSMOS_ENDPOINT"))
            .key(System.getenv("COSMOS_KEY"))
            .build();

        try {
            //if the database already exists, it is going to be deleted with all its content.
            deleteDatabase(client).block();
        } catch (Exception e) {
            //This means that the database does not exists already, it's fine
        }

        try {
            createDatabase(client)
                .then(createCollection(client))
                .flatMap(collection -> createDocuments(collection)
                    .then(simpleQuery(collection))
                )
                .block();
        } finally {
           deleteDatabase(client).block();
           client.close();
        }
    }
}

// Classes for this sample
class Planet {
    public String id;
    public boolean hasRings;
    public int radious;
    public Moon[] moons;

    public Planet(String id, boolean hasRings, int radius, Moon[] moons) {
        this.id = id;
        this.hasRings = hasRings;
        this.radious = radius;
        this.moons = moons;
    }
}

class Moon {
    public String name;

    public Moon(String name) {
        this.name = name;
    }

}
